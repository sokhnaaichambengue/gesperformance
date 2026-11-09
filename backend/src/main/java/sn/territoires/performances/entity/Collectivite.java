package sn.territoires.performances.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "collectivites")
public class Collectivite {
    public enum Type { COMMUNE, DEPARTEMENT, VILLE, POLE_TERRITORIAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nom;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "region_id")
    private Region region;
    private String responsable;
    private String adresse;
    private String telephone;
    private String email;

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String v) { this.nom = v; }
    public Type getType() { return type; }
    public void setType(Type v) { this.type = v; }
    public Region getRegion() { return region; }
    public void setRegion(Region v) { this.region = v; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String v) { this.responsable = v; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String v) { this.adresse = v; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String v) { this.telephone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
}
