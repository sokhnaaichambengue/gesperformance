package sn.territoires.performances.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "indicateurs")
public class Indicateur {
    public enum Periodicite { MENSUELLE, TRIMESTRIELLE, SEMESTRIELLE, ANNUELLE }
    public enum Sens { HAUSSE_FAVORABLE, BAISSE_FAVORABLE }
    public enum RegleObjectifZero { APPLIQUER, IGNORER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nom;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categorie_id")
    private CategorieIndicateur categorie;
    @Column(nullable = false)
    private String unite;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Periodicite periodicite;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sens sens;
    @Enumerated(EnumType.STRING)
    @Column(name = "regle_objectif_zero", nullable = false)
    private RegleObjectifZero regleObjectifZero = RegleObjectifZero.APPLIQUER;

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String v) { this.nom = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public CategorieIndicateur getCategorie() { return categorie; }
    public void setCategorie(CategorieIndicateur v) { this.categorie = v; }
    public String getUnite() { return unite; }
    public void setUnite(String v) { this.unite = v; }
    public Periodicite getPeriodicite() { return periodicite; }
    public void setPeriodicite(Periodicite v) { this.periodicite = v; }
    public Sens getSens() { return sens; }
    public void setSens(Sens v) { this.sens = v; }
    public RegleObjectifZero getRegleObjectifZero() { return regleObjectifZero; }
    public void setRegleObjectifZero(RegleObjectifZero v) { this.regleObjectifZero = v; }
}
