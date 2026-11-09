package sn.territoires.performances.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "projets")
public class Projet {
    public enum Statut { PLANIFIE, EN_COURS, TERMINE, SUSPENDU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nom;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "collectivite_id")
    private Collectivite collectivite;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "domaine_id")
    private DomaineCompetence domaine;
    @Column(name = "budget_prevu", nullable = false)
    private BigDecimal budgetPrevu;
    @Column(name = "budget_execute", nullable = false)
    private BigDecimal budgetExecute = BigDecimal.ZERO;
    @Column(nullable = false)
    private Integer avancement = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.PLANIFIE;

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String v) { this.nom = v; }
    public Collectivite getCollectivite() { return collectivite; }
    public void setCollectivite(Collectivite v) { this.collectivite = v; }
    public DomaineCompetence getDomaine() { return domaine; }
    public void setDomaine(DomaineCompetence v) { this.domaine = v; }
    public BigDecimal getBudgetPrevu() { return budgetPrevu; }
    public void setBudgetPrevu(BigDecimal v) { this.budgetPrevu = v; }
    public BigDecimal getBudgetExecute() { return budgetExecute; }
    public void setBudgetExecute(BigDecimal v) { this.budgetExecute = v; }
    public Integer getAvancement() { return avancement; }
    public void setAvancement(Integer v) { this.avancement = v; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut v) { this.statut = v; }
}
