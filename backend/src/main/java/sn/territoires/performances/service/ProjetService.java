package sn.territoires.performances.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Investissement;
import sn.territoires.performances.entity.Projet;
import sn.territoires.performances.repository.*;

/** Projets et investissements. Chaque investissement ajouté (ou supprimé) met à jour le budget exécuté du projet. */
@Service
public class ProjetService {
    private final ProjetRepository projets;
    private final InvestissementRepository investissements;
    private final CollectiviteRepository collectivites;
    private final DomaineRepository domaines;

    public ProjetService(ProjetRepository projets, InvestissementRepository investissements,
                         CollectiviteRepository collectivites, DomaineRepository domaines) {
        this.projets = projets;
        this.investissements = investissements;
        this.collectivites = collectivites;
        this.domaines = domaines;
    }

    static ProjetView vue(Projet p) {
        BigDecimal taux = p.getBudgetPrevu().signum() > 0
                ? p.getBudgetExecute().multiply(BigDecimal.valueOf(100)).divide(p.getBudgetPrevu(), 2, RoundingMode.HALF_UP)
                : null;
        return new ProjetView(p.getId(), p.getNom(), p.getCollectivite().getId(), p.getCollectivite().getNom(),
                p.getDomaine().getId(), p.getDomaine().getNom(), p.getBudgetPrevu(), p.getBudgetExecute(),
                p.getAvancement(), p.getStatut(), taux);
    }

    static InvestissementView vue(Investissement i) {
        return new InvestissementView(i.getId(), i.getProjet().getId(), i.getDateOperation(), i.getMontant(),
                i.getSourceFinancement());
    }

    @Transactional(readOnly = true)
    public List<ProjetView> lister(Long collectiviteId, Integer domaineId, Projet.Statut statut) {
        return projets.filtrer(collectiviteId, domaineId, statut).stream().map(ProjetService::vue).toList();
    }

    @Transactional
    public ProjetView creer(ProjetRequest req) {
        Projet p = new Projet();
        remplir(p, req);
        return vue(projets.saveAndFlush(p));
    }

    @Transactional
    public ProjetView modifier(Long id, ProjetRequest req) {
        Projet p = projets.findById(id).orElseThrow(() -> Erreurs.introuvable("Projet"));
        remplir(p, req);
        return vue(projets.saveAndFlush(p));
    }

    @Transactional
    public void supprimer(Long id) {
        Projet p = projets.findById(id).orElseThrow(() -> Erreurs.introuvable("Projet"));
        projets.delete(p); // les investissements sont supprimés en cascade (base de données)
        projets.flush();
    }

    @Transactional(readOnly = true)
    public List<InvestissementView> listerInvestissements(Long projetId) {
        if (!projets.existsById(projetId)) throw Erreurs.introuvable("Projet");
        return investissements.findByProjetIdOrderByDateOperationDescIdDesc(projetId).stream()
                .map(ProjetService::vue).toList();
    }

    @Transactional
    public InvestissementView ajouterInvestissement(Long projetId, InvestissementRequest req) {
        Projet p = projets.findById(projetId).orElseThrow(() -> Erreurs.introuvable("Projet"));
        Investissement i = new Investissement();
        i.setProjet(p);
        i.setDateOperation(req.dateOperation());
        i.setMontant(req.montant());
        i.setSourceFinancement(CollectiviteService.vide(req.sourceFinancement()));
        investissements.saveAndFlush(i);
        p.setBudgetExecute(p.getBudgetExecute().add(req.montant()));
        projets.saveAndFlush(p);
        return vue(i);
    }

    @Transactional
    public void supprimerInvestissement(Long id) {
        Investissement i = investissements.findById(id).orElseThrow(() -> Erreurs.introuvable("Investissement"));
        Projet p = i.getProjet();
        BigDecimal nouveau = p.getBudgetExecute().subtract(i.getMontant());
        p.setBudgetExecute(nouveau.signum() < 0 ? BigDecimal.ZERO : nouveau);
        projets.saveAndFlush(p);
        investissements.delete(i);
        investissements.flush();
    }

    private void remplir(Projet p, ProjetRequest req) {
        p.setNom(req.nom().trim());
        p.setCollectivite(collectivites.findById(req.collectiviteId()).orElseThrow(() -> Erreurs.invalide("Collectivité inconnue")));
        p.setDomaine(domaines.findById(req.domaineId()).orElseThrow(() -> Erreurs.invalide("Domaine inconnu")));
        p.setBudgetPrevu(req.budgetPrevu());
        p.setBudgetExecute(req.budgetExecute() == null ? BigDecimal.ZERO : req.budgetExecute());
        p.setAvancement(req.avancement());
        p.setStatut(req.statut());
    }
}
