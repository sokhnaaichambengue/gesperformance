package sn.territoires.performances.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Trace d'un calcul de score pour une réalisation validée. */
@Entity
@Table(name = "evaluations")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "realisation_id")
    private Realisation realisation;
    private BigDecimal score;
    @Column(nullable = false)
    private String formule;
    @Column(nullable = false)
    private String detail;
    @Column(name = "calcule_le", nullable = false)
    private Instant calculeLe;

    @PrePersist
    void avantInsertion() {
        if (calculeLe == null) calculeLe = Instant.now();
    }

    public Long getId() { return id; }
    public Realisation getRealisation() { return realisation; }
    public void setRealisation(Realisation v) { this.realisation = v; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal v) { this.score = v; }
    public String getFormule() { return formule; }
    public void setFormule(String v) { this.formule = v; }
    public String getDetail() { return detail; }
    public void setDetail(String v) { this.detail = v; }
    public Instant getCalculeLe() { return calculeLe; }
}
