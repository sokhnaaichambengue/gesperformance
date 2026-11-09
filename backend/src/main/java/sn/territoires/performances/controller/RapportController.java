package sn.territoires.performances.controller;

import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.service.*;

/**
 * Rapports téléchargeables. rapport = collectivites | collectivite | comparaison | evolution ; format = pdf | excel.
 */
@RestController
@RequestMapping("/api/rapports")
public class RapportController {
    private final RapportService rapports;
    private final ReportRenderer renderer;
    private final ScoreService scores;

    public RapportController(RapportService rapports, ReportRenderer renderer, ScoreService scores) {
        this.rapports = rapports;
        this.renderer = renderer;
        this.scores = scores;
    }

    @GetMapping("/{rapport}")
    public ResponseEntity<byte[]> generer(@PathVariable String rapport,
                                          @RequestParam(defaultValue = "pdf") String format,
                                          @RequestParam(required = false) Integer annee,
                                          @RequestParam(required = false) Integer regionId,
                                          @RequestParam(required = false) Collectivite.Type typeCollectivite,
                                          @RequestParam(required = false) Long collectiviteId,
                                          @RequestParam(required = false) List<Long> ids) {
        int an = annee != null ? annee : scores.anneeParDefaut();
        ReportData data = switch (rapport) {
            case "collectivites" -> rapports.collectivites(an, regionId, typeCollectivite);
            case "collectivite" -> {
                if (collectiviteId == null) throw Erreurs.invalide("Choisissez une collectivité");
                yield rapports.collectivite(collectiviteId, an);
            }
            case "comparaison" -> rapports.comparaison(ids, an);
            case "evolution" -> rapports.evolution(collectiviteId);
            default -> throw Erreurs.introuvable("Rapport");
        };
        boolean pdf = !"excel".equalsIgnoreCase(format);
        byte[] contenu = pdf ? renderer.pdf(data) : renderer.excel(data);
        String nom = "rapport-" + rapport + "-" + an + (pdf ? ".pdf" : ".xlsx");
        MediaType type = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nom).build().toString())
                .contentType(type)
                .body(contenu);
    }
}
