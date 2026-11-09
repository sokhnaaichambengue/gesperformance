package sn.territoires.performances.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "realisations")
public class Realisation {
    public enum Statut { SAISIE, VALIDEE, REJETEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "objectif_id")
    private Objectif objectif;
    @Column(nullable = false)
    private String periode;
    @Column(nullable = false)
    private BigDecimal valeur;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.SAISIE;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "saisi_par")
    private Utilisateur saisiPar;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "valide_par")
    private Utilisateur validePar;
    @Column(name = "saisi_le", nullable = false)
    private Instant saisiLe;
    @Column(name = "valide_le")
    private Instant valideLe;

    @PrePersist
    void avantInsertion() {
        if (saisiLe == null) saisiLe = Instant.now();
    }

    public Long getId() { return id; }
    public Objectif getObjectif() { return objectif; }
    public void setObjectif(Objectif v) { this.objectif = v; }
    public String getPeriode() { return periode; }
    public void setPeriode(String v) { this.periode = v; }
    public BigDecimal getValeur() { return valeur; }
    public void setValeur(BigDecimal v) { this.valeur = v; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut v) { this.statut = v; }
    public Utilisateur getSaisiPar() { return saisiPar; }
    public void setSaisiPar(Utilisateur v) { this.saisiPar = v; }
    public Utilisateur getValidePar() { return validePar; }
    public void setValidePar(Utilisateur v) { this.validePar = v; }
    public Instant getSaisiLe() { return saisiLe; }
    public Instant getValideLe() { return valideLe; }
    public void setValideLe(Instant v) { this.valideLe = v; }
}
