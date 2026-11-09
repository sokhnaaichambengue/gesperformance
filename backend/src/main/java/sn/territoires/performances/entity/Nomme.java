package sn.territoires.performances.entity;

import jakarta.persistence.*;

/** Base des référentiels simples (id + nom). */
@MappedSuperclass
public abstract class Nomme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String nom;

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
