package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.NomRequest;
import sn.territoires.performances.dto.Dtos.RefView;
import sn.territoires.performances.service.ReferenceService;

/** type = regions | categories | domaines */
@RestController
@RequestMapping("/api/referentiels/{type}")
public class ReferentielController {
    private final ReferenceService service;

    public ReferentielController(ReferenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<RefView> lister(@PathVariable String type) {
        return service.lister(type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ECRITURE)
    public RefView creer(@PathVariable String type, @Valid @RequestBody NomRequest req) {
        return service.creer(type, req.nom());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.ECRITURE)
    public void supprimer(@PathVariable String type, @PathVariable Integer id) {
        service.supprimer(type, id);
    }
}
