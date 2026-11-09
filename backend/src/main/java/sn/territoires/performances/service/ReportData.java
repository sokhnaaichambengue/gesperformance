package sn.territoires.performances.service;

import java.util.List;

/** Contenu d'un rapport, indépendant du format de sortie (PDF ou Excel). */
public record ReportData(String titre, String sousTitre, List<String> entetes, List<List<String>> lignes) {
}
