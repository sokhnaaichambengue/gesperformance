package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.UtilisateurRequest;
import sn.territoires.performances.dto.Dtos.UtilisateurView;
import sn.territoires.performances.service.UtilisateurService;

@RestController
@RequestMapping("/api/utilisateurs")
@PreAuthorize(Roles.ADMIN)
public class UtilisateurController {
    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<UtilisateurView> lister() {
        return service.lister();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurView creer(@Valid @RequestBody UtilisateurRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    public UtilisateurView modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurRequest req, Authentication auth) {
        return service.modifier(id, req, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, Authentication auth) {
        service.supprimer(id, auth.getName());
    }
}
