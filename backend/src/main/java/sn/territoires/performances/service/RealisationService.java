package sn.territoires.performances.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Objectif;
import sn.territoires.performances.entity.Realisation;
import sn.territoires.performances.entity.Utilisateur;
import sn.territoires.performances.repository.ObjectifRepository;
import sn.territoires.performances.repository.RealisationRepository;
import sn.territoires.performances.repository.UtilisateurRepository;

@Service
public class RealisationService {
    private final RealisationRepository repo;
    private final ObjectifRepository objectifs;
    private final UtilisateurRepository users;
    private final ScoreService scores;

    public RealisationService(RealisationRepository repo, ObjectifRepository objectifs,
                              UtilisateurRepository users, ScoreService scores) {
        this.repo = repo;
        this.objectifs = objectifs;
        this.users = users;
        this.scores = scores;
    }

    static RealisationView vue(Realisation r) {
        Objectif o = r.getObjectif();
        return new RealisationView(r.getId(), o.getId(), o.getCollectivite().getId(), o.getCollectivite().getNom(),
                o.getIndicateur().getNom(), o.getIndicateur().getUnite(), o.getAnnee(), r.getPeriode(), r.getValeur(),
                o.getValeurCible(), r.getStatut().name(), r.getSaisiPar().getNom(),
                r.getValidePar() == null ? null : r.getValidePar().getNom(), r.getSaisiLe(), r.getValideLe());
    }

    @Transactional(readOnly = true)
    public List<RealisationView> lister(Long collectiviteId, Integer annee, Realisation.Statut statut) {
        return repo.filtrer(collectiviteId, annee, statut).stream().map(RealisationService::vue).toList();
    }

    @Transactional
    public RealisationView creer(RealisationRequest req, String email) {
        Objectif o = objectifs.findById(req.objectifId()).orElseThrow(() -> Erreurs.invalide("Objectif inconnu"));
        verifierPeriode(o, req.periode());
        Realisation r = new Realisation();
        r.setObjectif(o);
        r.setPeriode(req.periode());
        r.setValeur(req.valeur());
        r.setStatut(Realisation.Statut.SAISIE);
        r.setSaisiPar(utilisateur(email));
        return vue(repo.saveAndFlush(r));
    }

    @Transactional
    public RealisationView modifier(Long id, RealisationRequest req) {
        Realisation r = trouver(id);
        if (r.getStatut() == Realisation.Statut.VALIDEE) {
            throw Erreurs.conflit("Une réalisation validée n'est pas modifiable : rejetez-la d'abord");
        }
        if (!r.getObjectif().getId().equals(req.objectifId())) {
            throw Erreurs.invalide("L'objectif d'une réalisation ne peut pas être changé");
        }
        verifierPeriode(r.getObjectif(), req.periode());
        r.setPeriode(req.periode());
        r.setValeur(req.valeur());
        r.setStatut(Realisation.Statut.SAISIE); // une réalisation corrigée repasse en attente de validation
        r.setValidePar(null);
        r.setValideLe(null);
        return vue(repo.saveAndFlush(r));
    }

    @Transactional
    public void supprimer(Long id) {
        Realisation r = trouver(id);
        if (r.getStatut() == Realisation.Statut.VALIDEE) {
            throw Erreurs.conflit("Une réalisation validée ne peut pas être supprimée : rejetez-la d'abord");
        }
        repo.delete(r);
        repo.flush();
    }

    @Transactional
    public RealisationView valider(Long id, String email) {
        Realisation r = trouver(id);
        if (r.getStatut() == Realisation.Statut.VALIDEE) throw Erreurs.invalide("Réalisation déjà validée");
        r.setStatut(Realisation.Statut.VALIDEE);
        r.setValidePar(utilisateur(email));
        r.setValideLe(Instant.now());
        repo.saveAndFlush(r);
        scores.tracer(r);
        return vue(r);
    }

    @Transactional
    public RealisationView rejeter(Long id, String email) {
        Realisation r = trouver(id);
        if (r.getStatut() == Realisation.Statut.REJETEE) throw Erreurs.invalide("Réalisation déjà rejetée");
        r.setStatut(Realisation.Statut.REJETEE);
        r.setValidePar(utilisateur(email));
        r.setValideLe(Instant.now());
        return vue(repo.saveAndFlush(r));
    }

    @Transactional(readOnly = true)
    public List<EvaluationView> evaluations(Long id) {
        trouver(id);
        return scores.evaluations(id);
    }

    private Realisation trouver(Long id) {
        return repo.findById(id).orElseThrow(() -> Erreurs.introuvable("Réalisation"));
    }

    private Utilisateur utilisateur(String email) {
        return users.findByEmail(email).orElseThrow(() -> Erreurs.invalide("Utilisateur inconnu"));
    }

    private void verifierPeriode(Objectif o, String periode) {
        if (!periode.startsWith(String.valueOf(o.getAnnee()))) {
            throw Erreurs.invalide("La période doit appartenir à l'année de l'objectif (" + o.getAnnee() + ")");
        }
    }
}
