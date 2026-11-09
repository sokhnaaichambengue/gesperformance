package sn.territoires.performances.controller;

import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.Dtos.DashboardView;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardView tableauDeBord(@RequestParam(required = false) Integer annee,
                                       @RequestParam(required = false) Integer regionId,
                                       @RequestParam(required = false) Collectivite.Type type,
                                       @RequestParam(required = false) Integer domaineId) {
        return service.construire(annee, regionId, type, domaineId);
    }
}
