package sn.territoires.performances.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "investissements")
public class Investissement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "projet_id")
    private Projet projet;
    @Column(name = "date_operation", nullable = false)
    private LocalDate dateOperation;
    @Column(nullable = false)
    private BigDecimal montant;
    @Column(name = "source_financement")
    private String sourceFinancement;

    public Long getId() { return id; }
    public Projet getProjet() { return projet; }
    public void setProjet(Projet v) { this.projet = v; }
    public LocalDate getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDate v) { this.dateOperation = v; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal v) { this.montant = v; }
    public String getSourceFinancement() { return sourceFinancement; }
    public void setSourceFinancement(String v) { this.sourceFinancement = v; }
}
