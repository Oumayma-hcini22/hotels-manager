package com.sip.hsm.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Le cahier des charges demande une énumération pour le rôle.
    // EnumType.STRING : on stocke "ADMIN" / "COLLABORATEUR" en texte dans la base
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private ERole libelle;

    public Role() {}

    public Role(ERole libelle) {
        this.libelle = libelle;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ERole getLibelle() { return libelle; }
    public void setLibelle(ERole libelle) { this.libelle = libelle; }
}