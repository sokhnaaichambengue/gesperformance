package sn.territoires.performances.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Moteur de calcul des scores (sans dépendance Spring, donc testable seul).
 * Règles : score entre 0 et 100, jamais de division par zéro, indicateur manquant = exclu (pas zéro).
 */
public final class ScoreCalculator {
    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private ScoreCalculator() {
    }

    /** score == null signifie « indicateur non valide » : il est exclu du score global. */
    public record Resultat(BigDecimal score, String formule, String detail) {
    }

    /** Un indicateur pour le calcul du score global. */
    public record Item(BigDecimal score, BigDecimal ponderation) {
    }

    /**
     * @param hausseFavorable      true : une valeur élevée est favorable ; false : une valeur faible est favorable
     * @param cible                objectif (>= 0)
     * @param valeur               valeur réalisée
     * @param ignorerSiCibleZero   règle configurable pour un objectif égal à 0 : true = indicateur ignoré,
     *                             false = 100 si l'objectif est respecté, sinon 0
     */
    public static Resultat calculer(boolean hausseFavorable, BigDecimal cible, BigDecimal valeur,
                                    boolean ignorerSiCibleZero) {
        if (cible == null || valeur == null) {
            return new Resultat(null, "INVALIDE", "Objectif ou valeur réalisée absent");
        }
        if (cible.signum() < 0) {
            return new Resultat(null, "INVALIDE", "Objectif négatif non géré");
        }
        if (cible.signum() == 0) {
            if (ignorerSiCibleZero) {
                return new Resultat(null, "OBJECTIF_ZERO_IGNORE",
                        "Objectif égal à 0 : indicateur ignoré (règle configurée)");
            }
            boolean atteint = hausseFavorable ? valeur.signum() >= 0 : valeur.signum() <= 0;
            return new Resultat(atteint ? CENT.setScale(2) : BigDecimal.ZERO.setScale(2), "OBJECTIF_ZERO",
                    "Objectif égal à 0 : " + (atteint ? "objectif respecté → 100" : "objectif non respecté → 0"));
        }
        if (hausseFavorable) {
            if (valeur.signum() <= 0) {
                return new Resultat(BigDecimal.ZERO.setScale(2), "HAUSSE", "Valeur ≤ 0 → 0");
            }
            BigDecimal brut = valeur.multiply(CENT).divide(cible, 6, RoundingMode.HALF_UP);
            return new Resultat(borner(brut), "HAUSSE",
                    "(" + plain(valeur) + " / " + plain(cible) + ") × 100, plafonné à 100");
        }
        // Baisse favorable
        if (valeur.compareTo(cible) <= 0) {
            return new Resultat(CENT.setScale(2), "BAISSE",
                    "Valeur " + plain(valeur) + " ≤ objectif " + plain(cible) + " → 100");
        }
        // ici valeur > cible > 0 : la division est sûre
        BigDecimal brut = cible.multiply(CENT).divide(valeur, 6, RoundingMode.HALF_UP);
        return new Resultat(borner(brut), "BAISSE", "(" + plain(cible) + " / " + plain(valeur) + ") × 100");
    }

    /** Σ(score × pondération) / Σ(pondérations) sur les indicateurs valides ; null si aucun indicateur valide. */
    public static BigDecimal global(List<Item> items) {
        BigDecimal somme = BigDecimal.ZERO;
        BigDecimal poids = BigDecimal.ZERO;
        for (Item it : items) {
            if (it.score() == null || it.ponderation() == null || it.ponderation().signum() <= 0) continue;
            somme = somme.add(it.score().multiply(it.ponderation()));
            poids = poids.add(it.ponderation());
        }
        if (poids.signum() == 0) return null;
        return borner(somme.divide(poids, 6, RoundingMode.HALF_UP));
    }

    private static BigDecimal borner(BigDecimal v) {
        if (v.signum() < 0) v = BigDecimal.ZERO;
        if (v.compareTo(CENT) > 0) v = CENT;
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static String plain(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }
}
