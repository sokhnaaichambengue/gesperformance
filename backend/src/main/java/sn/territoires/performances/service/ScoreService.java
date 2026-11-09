package sn.territoires.performances.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.*;
import sn.territoires.performances.repository.*;
import sn.territoires.performances.scoring.ScoreCalculator;
import sn.territoires.performances.scoring.ScoreCalculator.Resultat;

/**
 * Calcul des scores à partir des réalisations VALIDÉES. Pour chaque objectif d'une année, la réalisation validée
 * de la période la plus récente est retenue. Un objectif sans réalisation validée est « MANQUANT » : il est exclu
 * du score global (jamais compté comme zéro).
 */
@Service
public class ScoreService {
    private final ObjectifRepository objectifs;
    private final RealisationRepository realisations;
    private final CollectiviteRepository collectivites;
    private final EvaluationRepository evaluations;

    public ScoreService(ObjectifRepository objectifs, RealisationRepository realisations,
                        CollectiviteRepository collectivites, EvaluationRepository evaluations) {
        this.objectifs = objectifs;
        this.realisations = realisations;
        this.collectivites = collectivites;
        this.evaluations = evaluations;
    }

    @Transactional(readOnly = true)
    public int anneeParDefaut() {
        List<Integer> annees = objectifs.annees();
        return annees.isEmpty() ? Year.now().getValue() : annees.get(annees.size() - 1);
    }

    /** Scores par indicateur, groupés par collectivité, pour une année. */
    @Transactional(readOnly = true)
    public Map<Long, List<ScoreIndicateur>> scoresParCollectivite(int annee) {
        Map<Long, Realisation> derniere = new HashMap<>();
        for (Realisation r : realisations.parAnneeEtStatut(annee, Realisation.Statut.VALIDEE)) {
            derniere.merge(r.getObjectif().getId(), r, ScoreService::plusRecente);
        }
        Map<Long, List<ScoreIndicateur>> res = new LinkedHashMap<>();
        for (Objectif o : objectifs.parAnnee(annee)) {
            res.computeIfAbsent(o.getCollectivite().getId(), k -> new ArrayList<>())
                    .add(construire(o, derniere.get(o.getId())));
        }
        return res;
    }

    private static Realisation plusRecente(Realisation a, Realisation b) {
        int c = a.getPeriode().compareTo(b.getPeriode());
        if (c == 0) c = Long.compare(a.getId(), b.getId());
        return c >= 0 ? a : b;
    }

    static Resultat calculer(Objectif o, Realisation r) {
        Indicateur i = o.getIndicateur();
        return ScoreCalculator.calculer(i.getSens() == Indicateur.Sens.HAUSSE_FAVORABLE, o.getValeurCible(),
                r.getValeur(), i.getRegleObjectifZero() == Indicateur.RegleObjectifZero.IGNORER);
    }

    private static ScoreIndicateur construire(Objectif o, Realisation r) {
        Indicateur i = o.getIndicateur();
        if (r == null) {
            return new ScoreIndicateur(o.getId(), i.getNom(), i.getUnite(), o.getValeurCible(), o.getPonderation(),
                    null, null, null, "AUCUNE", "Aucune réalisation validée", "MANQUANT");
        }
        Resultat res = calculer(o, r);
        return new ScoreIndicateur(o.getId(), i.getNom(), i.getUnite(), o.getValeurCible(), o.getPonderation(),
                r.getPeriode(), r.getValeur(), res.score(), res.formule(), res.detail(),
                res.score() == null ? "IGNORE" : "VALIDE");
    }

    public static BigDecimal global(List<ScoreIndicateur> liste) {
        return ScoreCalculator.global(liste.stream()
                .map(s -> new ScoreCalculator.Item(s.score(), s.ponderation())).toList());
    }

    public static BigDecimal moyenne(List<BigDecimal> valeurs) {
        if (valeurs.isEmpty()) return null;
        BigDecimal somme = valeurs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return somme.divide(BigDecimal.valueOf(valeurs.size()), 2, RoundingMode.HALF_UP);
    }

    private ScoreCollectivite assembler(Collectivite c, int annee, List<ScoreIndicateur> l) {
        int valides = (int) l.stream().filter(s -> "VALIDE".equals(s.statut())).count();
        int manquants = (int) l.stream().filter(s -> "MANQUANT".equals(s.statut())).count();
        return new ScoreCollectivite(c.getId(), c.getNom(), annee, global(l), valides, manquants,
                l.size() - valides - manquants, l);
    }

    @Transactional(readOnly = true)
    public ScoreCollectivite detail(Long collectiviteId, int annee) {
        Collectivite c = collectivites.findById(collectiviteId).orElseThrow(() -> Erreurs.introuvable("Collectivité"));
        return assembler(c, annee, scoresParCollectivite(annee).getOrDefault(collectiviteId, List.of()));
    }

    @Transactional(readOnly = true)
    public List<ScoreCollectivite> comparer(List<Long> ids, int annee) {
        Map<Long, List<ScoreIndicateur>> map = scoresParCollectivite(annee);
        List<ScoreCollectivite> res = new ArrayList<>();
        for (Long id : ids) {
            Collectivite c = collectivites.findById(id).orElseThrow(() -> Erreurs.introuvable("Collectivité"));
            res.add(assembler(c, annee, map.getOrDefault(id, List.of())));
        }
        return res;
    }

    @Transactional(readOnly = true)
    public List<ClassementLigne> classement(int annee, Integer regionId, Collectivite.Type type) {
        Map<Long, List<ScoreIndicateur>> map = scoresParCollectivite(annee);
        List<ClassementLigne> brut = new ArrayList<>();
        for (Collectivite c : collectivites.filtrer(type, regionId)) {
            List<ScoreIndicateur> l = map.getOrDefault(c.getId(), List.of());
            int valides = (int) l.stream().filter(s -> "VALIDE".equals(s.statut())).count();
            brut.add(new ClassementLigne(null, c.getId(), c.getNom(), c.getType(), c.getRegion().getNom(),
                    global(l), valides, l.size()));
        }
        brut.sort(Comparator.comparing(ClassementLigne::score, Comparator.nullsLast(Comparator.<BigDecimal>reverseOrder()))
                .thenComparing(ClassementLigne::nom));
        List<ClassementLigne> res = new ArrayList<>();
        int rang = 0;
        for (ClassementLigne l : brut) {
            Integer r = l.score() == null ? null : ++rang;
            res.add(new ClassementLigne(r, l.collectiviteId(), l.nom(), l.type(), l.region(), l.score(),
                    l.nbValides(), l.nbTotal()));
        }
        return res;
    }

    /** Score moyen par année (ids == null : toutes les collectivités). */
    @Transactional(readOnly = true)
    public List<EvolutionPoint> evolution(Set<Long> ids) {
        List<EvolutionPoint> points = new ArrayList<>();
        for (Integer annee : objectifs.annees()) {
            List<BigDecimal> notes = new ArrayList<>();
            for (Map.Entry<Long, List<ScoreIndicateur>> e : scoresParCollectivite(annee).entrySet()) {
                if (ids != null && !ids.contains(e.getKey())) continue;
                BigDecimal g = global(e.getValue());
                if (g != null) notes.add(g);
            }
            if (!notes.isEmpty()) points.add(new EvolutionPoint(annee, moyenne(notes)));
        }
        return points;
    }

    // ---- Traçabilité
    @Transactional
    public void tracer(Realisation r) {
        Resultat res = calculer(r.getObjectif(), r);
        Evaluation e = new Evaluation();
        e.setRealisation(r);
        e.setScore(res.score());
        e.setFormule(res.formule());
        e.setDetail(res.detail().length() > 255 ? res.detail().substring(0, 255) : res.detail());
        evaluations.save(e);
    }

    @Transactional
    public void retracerObjectif(Long objectifId) {
        for (Realisation r : realisations.findByObjectifIdAndStatut(objectifId, Realisation.Statut.VALIDEE)) tracer(r);
    }

    @Transactional(readOnly = true)
    public List<EvaluationView> evaluations(Long realisationId) {
        return evaluations.findByRealisationIdOrderByCalculeLeDescIdDesc(realisationId).stream()
                .map(e -> new EvaluationView(e.getId(), e.getScore(), e.getFormule(), e.getDetail(), e.getCalculeLe()))
                .toList();
    }
}
