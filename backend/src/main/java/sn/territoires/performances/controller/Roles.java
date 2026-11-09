package sn.territoires.performances.controller;

/** Expressions de sécurité réutilisées par les contrôleurs. Lecture : tout utilisateur connecté. */
public final class Roles {
    private Roles() {
    }

    /** Création / modification / suppression des données de gestion. */
    public static final String ECRITURE = "hasAnyRole('ADMIN','RESPONSABLE_TERRITORIAL')";
    /** Saisie des réalisations. */
    public static final String SAISIE = "hasAnyRole('ADMIN','RESPONSABLE_TERRITORIAL','EVALUATEUR')";
    public static final String ADMIN = "hasRole('ADMIN')";
}
