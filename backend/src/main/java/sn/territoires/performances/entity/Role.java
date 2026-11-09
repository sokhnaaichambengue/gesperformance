package sn.territoires.performances.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String libelle;

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
}
