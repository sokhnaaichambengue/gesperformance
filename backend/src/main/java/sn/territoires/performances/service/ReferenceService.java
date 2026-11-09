package sn.territoires.performances.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.RefView;
import sn.territoires.performances.entity.*;
import sn.territoires.performances.repository.*;

/** Référentiels : régions, catégories d'indicateurs, domaines de compétence (type = regions | categories | domaines). */
@Service
public class ReferenceService {
    private final RegionRepository regions;
    private final CategorieRepository categories;
    private final DomaineRepository domaines;

    public ReferenceService(RegionRepository regions, CategorieRepository categories, DomaineRepository domaines) {
        this.regions = regions;
        this.categories = categories;
        this.domaines = domaines;
    }

    @Transactional(readOnly = true)
    public List<RefView> lister(String type) {
        List<? extends Nomme> liste = switch (type) {
            case "regions" -> regions.findAll(Sort.by("nom"));
            case "categories" -> categories.findAll(Sort.by("nom"));
            case "domaines" -> domaines.findAll(Sort.by("nom"));
            default -> throw Erreurs.introuvable("Référentiel");
        };
        List<RefView> res = new ArrayList<>();
        for (Nomme n : liste) res.add(new RefView(n.getId(), n.getNom()));
        return res;
    }

    @Transactional
    public RefView creer(String type, String nom) {
        Nomme n = switch (type) {
            case "regions" -> {
                Region r = new Region();
                r.setNom(nom.trim());
                yield regions.saveAndFlush(r);
            }
            case "categories" -> {
                CategorieIndicateur c = new CategorieIndicateur();
                c.setNom(nom.trim());
                yield categories.saveAndFlush(c);
            }
            case "domaines" -> {
                DomaineCompetence d = new DomaineCompetence();
                d.setNom(nom.trim());
                yield domaines.saveAndFlush(d);
            }
            default -> throw Erreurs.introuvable("Référentiel");
        };
        return new RefView(n.getId(), n.getNom());
    }

    @Transactional
    public void supprimer(String type, Integer id) {
        switch (type) {
            case "regions" -> {
                regions.delete(regions.findById(id).orElseThrow(() -> Erreurs.introuvable("Région")));
                regions.flush();
            }
            case "categories" -> {
                categories.delete(categories.findById(id).orElseThrow(() -> Erreurs.introuvable("Catégorie")));
                categories.flush();
            }
            case "domaines" -> {
                domaines.delete(domaines.findById(id).orElseThrow(() -> Erreurs.introuvable("Domaine")));
                domaines.flush();
            }
            default -> throw Erreurs.introuvable("Référentiel");
        }
    }
}
