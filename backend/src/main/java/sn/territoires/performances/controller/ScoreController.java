package sn.territoires.performances.controller;

import java.util.HashSet;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.*;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.service.ScoreService;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {
    private final ScoreService service;

    public ScoreController(ScoreService service) {
        this.service = service;
    }

    @GetMapping("/collectivite/{id}")
    public ScoreCollectivite detail(@PathVariable Long id, @RequestParam(required = false) Integer annee) {
        return service.detail(id, annee != null ? annee : service.anneeParDefaut());
    }

    @GetMapping("/classement")
    public List<ClassementLigne> classement(@RequestParam(required = false) Integer annee,
                                            @RequestParam(required = false) Integer regionId,
                                            @RequestParam(required = false) Collectivite.Type type) {
        return service.classement(annee != null ? annee : service.anneeParDefaut(), regionId, type);
    }

    @GetMapping("/comparaison")
    public List<ScoreCollectivite> comparaison(@RequestParam List<Long> ids,
                                               @RequestParam(required = false) Integer annee) {
        return service.comparer(ids, annee != null ? annee : service.anneeParDefaut());
    }

    @GetMapping("/evolution/{id}")
    public List<EvolutionPoint> evolution(@PathVariable Long id) {
        return service.evolution(new HashSet<>(List.of(id)));
    }
}
