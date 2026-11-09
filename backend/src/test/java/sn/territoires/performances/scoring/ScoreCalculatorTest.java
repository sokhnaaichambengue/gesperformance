package sn.territoires.performances.scoring;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {
    private static BigDecimal d(String s) {
        return new BigDecimal(s);
    }

    @Test
    void hausse_proportionnel() {
        assertEquals(d("50.00"), ScoreCalculator.calculer(true, d("200"), d("100"), false).score());
    }

    @Test
    void hausse_plafonne_a_100() {
        assertEquals(d("100.00"), ScoreCalculator.calculer(true, d("100"), d("250"), false).score());
    }

    @Test
    void hausse_valeur_zero_donne_zero() {
        assertEquals(d("0.00"), ScoreCalculator.calculer(true, d("100"), d("0"), false).score());
    }

    @Test
    void baisse_valeur_inferieure_ou_egale_donne_100() {
        assertEquals(d("100.00"), ScoreCalculator.calculer(false, d("10"), d("10"), false).score());
        assertEquals(d("100.00"), ScoreCalculator.calculer(false, d("10"), d("0"), false).score());
    }

    @Test
    void baisse_valeur_superieure() {
        assertEquals(d("50.00"), ScoreCalculator.calculer(false, d("10"), d("20"), false).score());
    }

    @Test
    void objectif_zero_baisse() {
        assertEquals(d("100.00"), ScoreCalculator.calculer(false, d("0"), d("0"), false).score());
        assertEquals(d("0.00"), ScoreCalculator.calculer(false, d("0"), d("5"), false).score());
    }

    @Test
    void objectif_zero_hausse_ne_divise_pas_par_zero() {
        assertEquals(d("100.00"), ScoreCalculator.calculer(true, d("0"), d("5"), false).score());
    }

    @Test
    void objectif_zero_ignore_si_regle() {
        assertNull(ScoreCalculator.calculer(true, d("0"), d("5"), true).score());
    }

    @Test
    void donnees_absentes_ou_objectif_negatif_exclus() {
        assertNull(ScoreCalculator.calculer(true, null, d("5"), false).score());
        assertNull(ScoreCalculator.calculer(true, d("10"), null, false).score());
        assertNull(ScoreCalculator.calculer(true, d("-1"), d("5"), false).score());
    }

    @Test
    void global_pondere_ignore_les_manquants() {
        List<ScoreCalculator.Item> items = List.of(
                new ScoreCalculator.Item(d("100.00"), d("3")),
                new ScoreCalculator.Item(d("50.00"), d("1")),
                new ScoreCalculator.Item(null, d("10")));
        assertEquals(d("87.50"), ScoreCalculator.global(items));
    }

    @Test
    void global_sans_indicateur_valide_est_null() {
        assertNull(ScoreCalculator.global(List.of(new ScoreCalculator.Item(null, d("2")))));
    }
}
