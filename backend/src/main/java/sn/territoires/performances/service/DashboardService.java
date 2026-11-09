package sn.territoires.performances.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.entity.Projet;
import sn.territoires.performances.repository.*;

@Service
public class DashboardService {
    private final CollectiviteRepository collectivites;
    private final IndicateurRepository indicateurs;
    private final ProjetRepository projets;
    private final ObjectifRepository objectifs;
    private final ScoreService scores;

    public DashboardService(CollectiviteRepository collectivites, IndicateurRepository indicateurs,
                            ProjetRepository projets, ObjectifRepository objectifs, ScoreService scores) {
        this.collectivites = collectivites;
        this.indicateurs = indicateurs;
        this.projets = projets;
        this.objectifs = objectifs;
        this.scores = scores;
    }

    /** Les filtres région/type s'appliquent aux collectivités, aux scores et aux projets ; le domaine s'applique aux projets. */
    @Transactional(readOnly = true)
    public DashboardView construire(Integer annee, Integer regionId, Collectivite.Type type, Integer domaineId) {
        int an = annee != null ? annee : scores.anneeParDefaut();
        List<Collectivite> cs = collectivites.filtrer(type, regionId);
        Set<Long> ids = cs.stream().map(Collectivite::getId).collect(Collectors.toSet());

        List<ClassementLigne> classement = scores.classement(an, regionId, type);
        List<BigDecimal> notes = classement.stream().map(ClassementLigne::score).filter(Objects::nonNull).toList();

        List<Projet> liste = projets.pourTableauDeBord(regionId, type, domaineId);
        BigDecimal prevu = liste.stream().map(Projet::getBudgetPrevu).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal execute = liste.stream().map(Projet::getBudgetExecute).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taux = prevu.signum() > 0
                ? execute.multiply(BigDecimal.valueOf(100)).divide(prevu, 2, RoundingMode.HALF_UP) : null;

        Map<String, List<BigDecimal>> parRegion = new TreeMap<>();
        for (ClassementLigne l : classement) {
            if (l.score() != null) parRegion.computeIfAbsent(l.region(), k -> new ArrayList<>()).add(l.score());
        }
        List<ScoreRegion> scoreParRegion = parRegion.entrySet().stream()
                .map(e -> new ScoreRegion(e.getKey(), ScoreService.moyenne(e.getValue())))
                .sorted(Comparator.comparing(ScoreRegion::score).reversed()).toList();

        List<Integer> annees = objectifs.annees();
        if (annees.isEmpty()) annees = List.of(an);

        return new DashboardView(an, annees, cs.size(), indicateurs.count(), liste.size(),
                ScoreService.moyenne(notes), taux, prevu, execute, classement, scoreParRegion,
                scores.evolution(ids));
    }
}
