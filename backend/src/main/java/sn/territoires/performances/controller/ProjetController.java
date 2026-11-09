package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Projet;
import sn.territoires.performances.service.ProjetService;

@RestController
@RequestMapping("/api")
public class ProjetController {
    private final ProjetService service;

    public ProjetController(ProjetService service) {
        this.service = service;
    }

    @GetMapping("/projets")
    public List<ProjetView> lister(@RequestParam(required = false) Long collectiviteId,
                                   @RequestParam(required = false) Integer domaineId,
                                   @RequestParam(required = false) Projet.Statut statut) {
        return service.lister(collectiviteId, domaineId, statut);
    }

    @PostMapping("/projets")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ECRITURE)
    public ProjetView creer(@Valid @RequestBody ProjetRequest req) {
        return service.creer(req);
    }

    @PutMapping("/projets/{id}")
    @PreAuthorize(Roles.ECRITURE)
    public ProjetView modifier(@PathVariable Long id, @Valid @RequestBody ProjetRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/projets/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.ECRITURE)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    @GetMapping("/projets/{id}/investissements")
    public List<InvestissementView> investissements(@PathVariable Long id) {
        return service.listerInvestissements(id);
    }

    @PostMapping("/projets/{id}/investissements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ECRITURE)
    public InvestissementView ajouter(@PathVariable Long id, @Valid @RequestBody InvestissementRequest req) {
        return service.ajouterInvestissement(id, req);
    }

    @DeleteMapping("/investissements/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.ECRITURE)
    public void supprimerInvestissement(@PathVariable Long id) {
        service.supprimerInvestissement(id);
    }
}
