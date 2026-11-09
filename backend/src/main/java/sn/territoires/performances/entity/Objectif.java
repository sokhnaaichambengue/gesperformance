package sn.territoires.performances.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Association indicateur / collectivité pour une année, avec objectif et pondération. */
@Entity
@Table(name = "objectifs")
public class Objectif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "collectivite_id")
    private Collectivite collectivite;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "indicateur_id")
    private Indicateur indicateur;
    @Column(nullable = false)
    private Integer annee;
    @Column(name = "valeur_cible", nullable = false)
    private BigDecimal valeurCible;
    @Column(nullable = false)
    private BigDecimal ponderation;

    public Long getId() { return id; }
    public Collectivite getCollectivite() { return collectivite; }
    public void setCollectivite(Collectivite v) { this.collectivite = v; }
    public Indicateur getIndicateur() { return indicateur; }
    public void setIndicateur(Indicateur v) { this.indicateur = v; }
    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer v) { this.annee = v; }
    public BigDecimal getValeurCible() { return valeurCible; }
    public void setValeurCible(BigDecimal v) { this.valeurCible = v; }
    public BigDecimal getPonderation() { return ponderation; }
    public void setPonderation(BigDecimal v) { this.ponderation = v; }
}
