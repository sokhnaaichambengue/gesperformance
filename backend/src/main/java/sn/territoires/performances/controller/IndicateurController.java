package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.service.IndicateurService;

@RestController
@RequestMapping("/api")
public class IndicateurController {
    private final IndicateurService service;

    public IndicateurController(IndicateurService service) {
        this.service = service;
    }

    // ---- Indicateurs
    @GetMapping("/indicateurs")
    public List<IndicateurView> indicateurs() {
        return service.listerIndicateurs();
    }

    @PostMapping("/indicateurs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ECRITURE)
    public IndicateurView creerIndicateur(@Valid @RequestBody IndicateurRequest req) {
        return service.creerIndicateur(req);
    }

    @PutMapping("/indicateurs/{id}")
    @PreAuthorize(Roles.ECRITURE)
    public IndicateurView modifierIndicateur(@PathVariable Long id, @Valid @RequestBody IndicateurRequest req) {
        return service.modifierIndicateur(id, req);
    }

    @DeleteMapping("/indicateurs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.ECRITURE)
    public void supprimerIndicateur(@PathVariable Long id) {
        service.supprimerIndicateur(id);
    }

    // ---- Objectifs
    @GetMapping("/objectifs")
    public List<ObjectifView> objectifs(@RequestParam(required = false) Long collectiviteId,
                                        @RequestParam(required = false) Long indicateurId,
                                        @RequestParam(required = false) Integer annee) {
        return service.listerObjectifs(collectiviteId, indicateurId, annee);
    }

    @PostMapping("/objectifs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ECRITURE)
    public ObjectifView creerObjectif(@Valid @RequestBody ObjectifRequest req) {
        return service.creerObjectif(req);
    }

    @PutMapping("/objectifs/{id}")
    @PreAuthorize(Roles.ECRITURE)
    public ObjectifView modifierObjectif(@PathVariable Long id, @Valid @RequestBody ObjectifRequest req) {
        return service.modifierObjectif(id, req);
    }

    @DeleteMapping("/objectifs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.ECRITURE)
    public void supprimerObjectif(@PathVariable Long id) {
        service.supprimerObjectif(id);
    }
}
