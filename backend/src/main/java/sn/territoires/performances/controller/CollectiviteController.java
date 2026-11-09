package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.CollectiviteRequest;
import sn.territoires.performances.dto.Dtos.CollectiviteView;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.service.CollectiviteService;

@RestController
@RequestMapping("/api/collectivites")
public class CollectiviteController {
    private final CollectiviteService service;

    public CollectiviteController(CollectiviteService service) {
        this.service = service;
    }

    @GetMapping
    public List<CollectiviteView> lister(@RequestParam(required = false) Collectivite.Type type,
                                         @RequestParam(required = false) Integer regionId) {
        return service.lister(type, regionId);
    }

    @GetMapping("/{id}")
    public CollectiviteView obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ECRITURE)
    public CollectiviteView creer(@Valid @RequestBody CollectiviteRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.ECRITURE)
    public CollectiviteView modifier(@PathVariable Long id, @Valid @RequestBody CollectiviteRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.ECRITURE)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
