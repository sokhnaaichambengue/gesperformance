package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Realisation;
import sn.territoires.performances.service.RealisationService;

@RestController
@RequestMapping("/api/realisations")
public class RealisationController {
    private final RealisationService service;

    public RealisationController(RealisationService service) {
        this.service = service;
    }

    @GetMapping
    public List<RealisationView> lister(@RequestParam(required = false) Long collectiviteId,
                                        @RequestParam(required = false) Integer annee,
                                        @RequestParam(required = false) Realisation.Statut statut) {
        return service.lister(collectiviteId, annee, statut);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.SAISIE)
    public RealisationView creer(@Valid @RequestBody RealisationRequest req, Authentication auth) {
        return service.creer(req, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.SAISIE)
    public RealisationView modifier(@PathVariable Long id, @Valid @RequestBody RealisationRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.SAISIE)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    /** Validation des données : réservée aux responsables autorisés. */
    @PostMapping("/{id}/valider")
    @PreAuthorize(Roles.ECRITURE)
    public RealisationView valider(@PathVariable Long id, Authentication auth) {
        return service.valider(id, auth.getName());
    }

    @PostMapping("/{id}/rejeter")
    @PreAuthorize(Roles.ECRITURE)
    public RealisationView rejeter(@PathVariable Long id, Authentication auth) {
        return service.rejeter(id, auth.getName());
    }

    @GetMapping("/{id}/evaluations")
    public List<EvaluationView> evaluations(@PathVariable Long id) {
        return service.evaluations(id);
    }
}
