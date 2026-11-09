package sn.territoires.performances.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.CollectiviteRequest;
import sn.territoires.performances.dto.Dtos.CollectiviteView;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.repository.CollectiviteRepository;
import sn.territoires.performances.repository.ObjectifRepository;
import sn.territoires.performances.repository.ProjetRepository;
import sn.territoires.performances.repository.RegionRepository;

@Service
public class CollectiviteService {
    private final CollectiviteRepository repo;
    private final RegionRepository regions;
    private final ObjectifRepository objectifs;
    private final ProjetRepository projets;

    public CollectiviteService(CollectiviteRepository repo, RegionRepository regions,
                               ObjectifRepository objectifs, ProjetRepository projets) {
        this.repo = repo;
        this.regions = regions;
        this.objectifs = objectifs;
        this.projets = projets;
    }

    static CollectiviteView vue(Collectivite c) {
        return new CollectiviteView(c.getId(), c.getNom(), c.getType(), c.getRegion().getId(),
                c.getRegion().getNom(), c.getResponsable(), c.getAdresse(), c.getTelephone(), c.getEmail());
    }

    @Transactional(readOnly = true)
    public List<CollectiviteView> lister(Collectivite.Type type, Integer regionId) {
        return repo.filtrer(type, regionId).stream().map(CollectiviteService::vue).toList();
    }

    @Transactional(readOnly = true)
    public CollectiviteView obtenir(Long id) {
        return vue(repo.findById(id).orElseThrow(() -> Erreurs.introuvable("Collectivité")));
    }

    @Transactional
    public CollectiviteView creer(CollectiviteRequest req) {
        Collectivite c = new Collectivite();
        remplir(c, req);
        return vue(repo.saveAndFlush(c));
    }

    @Transactional
    public CollectiviteView modifier(Long id, CollectiviteRequest req) {
        Collectivite c = repo.findById(id).orElseThrow(() -> Erreurs.introuvable("Collectivité"));
        remplir(c, req);
        return vue(repo.saveAndFlush(c));
    }

    @Transactional
    public void supprimer(Long id) {
        Collectivite c = repo.findById(id).orElseThrow(() -> Erreurs.introuvable("Collectivité"));
        if (objectifs.existsByCollectiviteId(id) || projets.existsByCollectiviteId(id)) {
            throw Erreurs.conflit("Cette collectivité a des objectifs ou des projets : supprimez-les d'abord");
        }
        repo.delete(c);
        repo.flush();
    }

    private void remplir(Collectivite c, CollectiviteRequest req) {
        c.setNom(req.nom().trim());
        c.setType(req.type());
        c.setRegion(regions.findById(req.regionId()).orElseThrow(() -> Erreurs.invalide("Région inconnue")));
        c.setResponsable(vide(req.responsable()));
        c.setAdresse(vide(req.adresse()));
        c.setTelephone(vide(req.telephone()));
        c.setEmail(vide(req.email()));
    }

    static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
