package sn.territoires.performances.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Collectivite;

/** Construit le contenu des rapports à partir des mêmes calculs que le tableau de bord. */
@Service
public class RapportService {
    private final ScoreService scores;

    public RapportService(ScoreService scores) {
        this.scores = scores;
    }

    static String f(BigDecimal v) {
        return v == null ? "—" : v.stripTrailingZeros().toPlainString();
    }

    private static String statut(String s) {
        return switch (s) {
            case "VALIDE" -> "Calculé";
            case "MANQUANT" -> "Manquant";
            default -> "Ignoré";
        };
    }

    /** Résultats de toutes les collectivités (avec filtres) : classement et scores globaux. */
    @Transactional(readOnly = true)
    public ReportData collectivites(int annee, Integer regionId, Collectivite.Type type) {
        List<List<String>> lignes = new ArrayList<>();
        for (ClassementLigne l : scores.classement(annee, regionId, type)) {
            lignes.add(List.of(l.rang() == null ? "—" : String.valueOf(l.rang()), l.nom(), l.type().name(),
                    l.region(), f(l.score()), l.nbValides() + " / " + l.nbTotal()));
        }
        return new ReportData("Résultats par collectivité", "Année " + annee,
                List.of("Rang", "Collectivité", "Type", "Région", "Score global", "Indicateurs calculés"), lignes);
    }

    /** Scores par indicateur pour une collectivité. */
    @Transactional(readOnly = true)
    public ReportData collectivite(Long id, int annee) {
        ScoreCollectivite sc = scores.detail(id, annee);
        List<List<String>> lignes = new ArrayList<>();
        for (ScoreIndicateur s : sc.indicateurs()) {
            lignes.add(List.of(s.indicateur(), s.unite(), f(s.cible()), f(s.valeur()),
                    s.periode() == null ? "—" : s.periode(), f(s.ponderation()), f(s.score()), statut(s.statut())));
        }
        lignes.add(List.of("SCORE GLOBAL", "", "", "", "", "", f(sc.scoreGlobal()), ""));
        return new ReportData("Scores par indicateur — " + sc.nom(), "Année " + annee,
                List.of("Indicateur", "Unité", "Objectif", "Réalisé", "Période", "Pondération", "Score", "Statut"), lignes);
    }

    /** Comparaison de plusieurs collectivités : score par indicateur et score global. */
    @Transactional(readOnly = true)
    public ReportData comparaison(List<Long> ids, int annee) {
        if (ids == null || ids.size() < 2) throw Erreurs.invalide("Sélectionnez au moins deux collectivités");
        List<ScoreCollectivite> liste = scores.comparer(ids, annee);
        TreeSet<String> noms = new TreeSet<>();
        for (ScoreCollectivite sc : liste) for (ScoreIndicateur s : sc.indicateurs()) noms.add(s.indicateur());
        List<String> entetes = new ArrayList<>();
        entetes.add("Collectivité");
        entetes.addAll(noms);
        entetes.add("Score global");
        List<List<String>> lignes = new ArrayList<>();
        for (ScoreCollectivite sc : liste) {
            Map<String, BigDecimal> parNom = new HashMap<>();
            for (ScoreIndicateur s : sc.indicateurs()) parNom.put(s.indicateur(), s.score());
            List<String> ligne = new ArrayList<>();
            ligne.add(sc.nom());
            for (String n : noms) ligne.add(f(parNom.get(n)));
            ligne.add(f(sc.scoreGlobal()));
            lignes.add(ligne);
        }
        return new ReportData("Comparaison de collectivités", "Année " + annee, entetes, lignes);
    }

    /** Évolution du score moyen dans le temps (une collectivité, ou toutes si id absent). */
    @Transactional(readOnly = true)
    public ReportData evolution(Long collectiviteId) {
        Set<Long> ids = collectiviteId == null ? null : Set.of(collectiviteId);
        String titre = "Évolution des performances";
        if (collectiviteId != null) titre += " — " + scores.detail(collectiviteId, scores.anneeParDefaut()).nom();
        List<List<String>> lignes = new ArrayList<>();
        for (EvolutionPoint p : scores.evolution(ids)) lignes.add(List.of(String.valueOf(p.annee()), f(p.score())));
        return new ReportData(titre, collectiviteId == null ? "Toutes les collectivités" : "Collectivité sélectionnée",
                List.of("Année", "Score moyen"), lignes);
    }
}
