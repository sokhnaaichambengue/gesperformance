package sn.territoires.performances.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Indicateur;
import sn.territoires.performances.entity.Objectif;
import sn.territoires.performances.repository.*;

/** Indicateurs et objectifs (association indicateur / collectivité / année avec pondération). */
@Service
public class IndicateurService {
    private final IndicateurRepository indicateurs;
    private final CategorieRepository categories;
    private final ObjectifRepository objectifs;
    private final CollectiviteRepository collectivites;
    private final RealisationRepository realisations;
    private final ScoreService scores;

    public IndicateurService(IndicateurRepository indicateurs, CategorieRepository categories,
                             ObjectifRepository objectifs, CollectiviteRepository collectivites,
                             RealisationRepository realisations, ScoreService scores) {
        this.indicateurs = indicateurs;
        this.categories = categories;
        this.objectifs = objectifs;
        this.collectivites = collectivites;
        this.realisations = realisations;
        this.scores = scores;
    }

    static IndicateurView vue(Indicateur i) {
        return new IndicateurView(i.getId(), i.getNom(), i.getDescription(), i.getCategorie().getId(),
                i.getCategorie().getNom(), i.getUnite(), i.getPeriodicite(), i.getSens(), i.getRegleObjectifZero());
    }

    static ObjectifView vue(Objectif o) {
        return new ObjectifView(o.getId(), o.getCollectivite().getId(), o.getCollectivite().getNom(),
                o.getIndicateur().getId(), o.getIndicateur().getNom(), o.getIndicateur().getUnite(),
                o.getIndicateur().getSens(), o.getAnnee(), o.getValeurCible(), o.getPonderation());
    }

    // ---- Indicateurs
    @Transactional(readOnly = true)
    public List<IndicateurView> listerIndicateurs() {
        return indicateurs.findAll(Sort.by("nom")).stream().map(IndicateurService::vue).toList();
    }

    @Transactional
    public IndicateurView creerIndicateur(IndicateurRequest req) {
        Indicateur i = new Indicateur();
        remplir(i, req);
        return vue(indicateurs.saveAndFlush(i));
    }

    @Transactional
    public IndicateurView modifierIndicateur(Long id, IndicateurRequest req) {
        Indicateur i = indicateurs.findById(id).orElseThrow(() -> Erreurs.introuvable("Indicateur"));
        remplir(i, req);
        indicateurs.saveAndFlush(i);
        // Le sens ou la règle ont pu changer : on trace de nouveau les calculs des réalisations validées
        for (Objectif o : objectifs.findByIndicateurId(id)) scores.retracerObjectif(o.getId());
        return vue(i);
    }

    @Transactional
    public void supprimerIndicateur(Long id) {
        Indicateur i = indicateurs.findById(id).orElseThrow(() -> Erreurs.introuvable("Indicateur"));
        indicateurs.delete(i);
        indicateurs.flush();
    }

    private void remplir(Indicateur i, IndicateurRequest req) {
        i.setNom(req.nom().trim());
        i.setDescription(CollectiviteService.vide(req.description()));
        i.setCategorie(categories.findById(req.categorieId()).orElseThrow(() -> Erreurs.invalide("Catégorie inconnue")));
        i.setUnite(req.unite().trim());
        i.setPeriodicite(req.periodicite());
        i.setSens(req.sens());
        i.setRegleObjectifZero(req.regleObjectifZero() == null
                ? Indicateur.RegleObjectifZero.APPLIQUER : req.regleObjectifZero());
    }

    // ---- Objectifs
    @Transactional(readOnly = true)
    public List<ObjectifView> listerObjectifs(Long collectiviteId, Long indicateurId, Integer annee) {
        return objectifs.filtrer(collectiviteId, indicateurId, annee).stream().map(IndicateurService::vue).toList();
    }

    @Transactional
    public ObjectifView creerObjectif(ObjectifRequest req) {
        Objectif o = new Objectif();
        remplir(o, req);
        return vue(objectifs.saveAndFlush(o));
    }

    @Transactional
    public ObjectifView modifierObjectif(Long id, ObjectifRequest req) {
        Objectif o = objectifs.findById(id).orElseThrow(() -> Erreurs.introuvable("Objectif"));
        if (!o.getCollectivite().getId().equals(req.collectiviteId()) || !o.getIndicateur().getId().equals(req.indicateurId())
                || !o.getAnnee().equals(req.annee())) {
            if (realisations.existsByObjectifId(id)) {
                throw Erreurs.conflit("Des réalisations existent : seuls l'objectif et la pondération sont modifiables");
            }
        }
        remplir(o, req);
        objectifs.saveAndFlush(o);
        scores.retracerObjectif(id); // l'objectif a changé : nouveaux calculs tracés pour les réalisations validées
        return vue(o);
    }

    @Transactional
    public void supprimerObjectif(Long id) {
        Objectif o = objectifs.findById(id).orElseThrow(() -> Erreurs.introuvable("Objectif"));
        if (realisations.existsByObjectifId(id)) {
            throw Erreurs.conflit("Cet objectif a des réalisations enregistrées : supprimez-les d'abord");
        }
        objectifs.delete(o);
        objectifs.flush();
    }

    private void remplir(Objectif o, ObjectifRequest req) {
        o.setCollectivite(collectivites.findById(req.collectiviteId()).orElseThrow(() -> Erreurs.invalide("Collectivité inconnue")));
        o.setIndicateur(indicateurs.findById(req.indicateurId()).orElseThrow(() -> Erreurs.invalide("Indicateur inconnu")));
        o.setAnnee(req.annee());
        o.setValeurCible(req.valeurCible());
        o.setPonderation(req.ponderation());
    }
}
