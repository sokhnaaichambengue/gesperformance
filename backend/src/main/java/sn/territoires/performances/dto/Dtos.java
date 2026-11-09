package sn.territoires.performances.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.entity.Indicateur;
import sn.territoires.performances.entity.Projet;

/** Objets échangés avec le frontend (requêtes validées + vues). */
public final class Dtos {
    private Dtos() {
    }

    // ---- Référentiels
    public record NomRequest(@NotBlank(message = "Le nom est obligatoire") @Size(max = 100, message = "Nom trop long (100 max)") String nom) {}
    public record RefView(Integer id, String nom) {}

    // ---- Utilisateurs
    public record UtilisateurRequest(
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            String motDePasse,
            @NotBlank(message = "Le rôle est obligatoire") String roleCode,
            Boolean actif) {}
    public record UtilisateurView(Long id, String nom, String email, String role, boolean actif) {}

    // ---- Collectivités
    public record CollectiviteRequest(
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 150) String nom,
            @NotNull(message = "Le type est obligatoire") Collectivite.Type type,
            @NotNull(message = "La région est obligatoire") Integer regionId,
            @Size(max = 150) String responsable,
            @Size(max = 255) String adresse,
            @Size(max = 30) String telephone,
            @Email(message = "Email invalide") @Size(max = 150) String email) {}
    public record CollectiviteView(Long id, String nom, Collectivite.Type type, Integer regionId, String regionNom,
                                   String responsable, String adresse, String telephone, String email) {}

    // ---- Indicateurs
    public record IndicateurRequest(
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 150) String nom,
            String description,
            @NotNull(message = "La catégorie est obligatoire") Integer categorieId,
            @NotBlank(message = "L'unité est obligatoire") @Size(max = 30) String unite,
            @NotNull(message = "La périodicité est obligatoire") Indicateur.Periodicite periodicite,
            @NotNull(message = "Le sens est obligatoire") Indicateur.Sens sens,
            Indicateur.RegleObjectifZero regleObjectifZero) {}
    public record IndicateurView(Long id, String nom, String description, Integer categorieId, String categorieNom,
                                 String unite, Indicateur.Periodicite periodicite, Indicateur.Sens sens,
                                 Indicateur.RegleObjectifZero regleObjectifZero) {}

    // ---- Objectifs (association indicateur / collectivité)
    public record ObjectifRequest(
            @NotNull(message = "La collectivité est obligatoire") Long collectiviteId,
            @NotNull(message = "L'indicateur est obligatoire") Long indicateurId,
            @NotNull(message = "L'année est obligatoire") @Min(value = 2000, message = "Année invalide") @Max(value = 2100, message = "Année invalide") Integer annee,
            @NotNull(message = "L'objectif est obligatoire") @DecimalMin(value = "0.0", message = "L'objectif doit être ≥ 0") BigDecimal valeurCible,
            @NotNull(message = "La pondération est obligatoire") @DecimalMin(value = "0.0", inclusive = false, message = "La pondération doit être > 0") BigDecimal ponderation) {}
    public record ObjectifView(Long id, Long collectiviteId, String collectiviteNom, Long indicateurId,
                               String indicateurNom, String unite, Indicateur.Sens sens, Integer annee,
                               BigDecimal valeurCible, BigDecimal ponderation) {}

    // ---- Réalisations
    public record RealisationRequest(
            @NotNull(message = "L'objectif est obligatoire") Long objectifId,
            @NotBlank(message = "La période est obligatoire")
            @Pattern(regexp = "^\\d{4}(-(T[1-4]|S[1-2]|0[1-9]|1[0-2]))?$", message = "Période invalide (ex. 2026, 2026-T1, 2026-S2, 2026-03)") String periode,
            @NotNull(message = "La valeur est obligatoire") BigDecimal valeur) {}
    public record RealisationView(Long id, Long objectifId, Long collectiviteId, String collectiviteNom,
                                  String indicateurNom, String unite, Integer annee, String periode,
                                  BigDecimal valeur, BigDecimal valeurCible, String statut, String saisiPar,
                                  String validePar, Instant saisiLe, Instant valideLe) {}
    public record EvaluationView(Long id, BigDecimal score, String formule, String detail, Instant calculeLe) {}

    // ---- Scores
    public record ScoreIndicateur(Long objectifId, String indicateur, String unite, BigDecimal cible,
                                  BigDecimal ponderation, String periode, BigDecimal valeur, BigDecimal score,
                                  String formule, String detail, String statut) {}
    public record ScoreCollectivite(Long collectiviteId, String nom, Integer annee, BigDecimal scoreGlobal,
                                    int nbValides, int nbManquants, int nbIgnores, List<ScoreIndicateur> indicateurs) {}
    public record ClassementLigne(Integer rang, Long collectiviteId, String nom, Collectivite.Type type,
                                  String region, BigDecimal score, int nbValides, int nbTotal) {}
    public record EvolutionPoint(Integer annee, BigDecimal score) {}
    public record ScoreRegion(String region, BigDecimal score) {}

    // ---- Projets et investissements
    public record ProjetRequest(
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 200) String nom,
            @NotNull(message = "La collectivité est obligatoire") Long collectiviteId,
            @NotNull(message = "Le domaine est obligatoire") Integer domaineId,
            @NotNull(message = "Le budget prévu est obligatoire") @DecimalMin(value = "0.0", message = "Budget prévu ≥ 0") BigDecimal budgetPrevu,
            @DecimalMin(value = "0.0", message = "Budget exécuté ≥ 0") BigDecimal budgetExecute,
            @NotNull(message = "L'avancement est obligatoire") @Min(value = 0, message = "Avancement entre 0 et 100") @Max(value = 100, message = "Avancement entre 0 et 100") Integer avancement,
            @NotNull(message = "Le statut est obligatoire") Projet.Statut statut) {}
    public record ProjetView(Long id, String nom, Long collectiviteId, String collectiviteNom, Integer domaineId,
                             String domaineNom, BigDecimal budgetPrevu, BigDecimal budgetExecute,
                             Integer avancement, Projet.Statut statut, BigDecimal tauxExecution) {}
    public record InvestissementRequest(
            @NotNull(message = "La date est obligatoire") LocalDate dateOperation,
            @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être > 0") BigDecimal montant,
            @Size(max = 120) String sourceFinancement) {}
    public record InvestissementView(Long id, Long projetId, LocalDate dateOperation, BigDecimal montant,
                                     String sourceFinancement) {}

    // ---- Tableau de bord
    public record DashboardView(Integer annee, List<Integer> annees, int nbCollectivites, long nbIndicateurs,
                                int nbProjets, BigDecimal scoreMoyen, BigDecimal tauxExecution,
                                BigDecimal budgetPrevu, BigDecimal budgetExecute,
                                List<ClassementLigne> classement, List<ScoreRegion> scoreParRegion,
                                List<EvolutionPoint> evolution) {}
}
