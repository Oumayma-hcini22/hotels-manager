package com.sip.hsm.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'hôtel est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @Pattern(regexp = "^[0-9]{8}$", message = "Le téléphone doit contenir 8 chiffres")
    private String tel;

    @Email(message = "Email invalide")
    private String email;

    // équivalent de "activité" du Client dans le cahier des charges
    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CategorieHotel categorie;

    @Min(value = 1, message = "Minimum 1 étoile")
    @Max(value = 5, message = "Maximum 5 étoiles")
    private Integer etoiles;

    @Column(length = 1000)
    private String description;

    // équivalent de "photo" du Client : URL de l'image
    private String image;

    // association "gérer" (User -> Client) du diagramme de classes
    // Many-to-One : un utilisateur gère plusieurs hôtels
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User gerePar;

    // One-to-Many : un hôtel contient plusieurs chambres
    // cascade REMOVE : supprimer l'hôtel supprime ses chambres
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Chambre> chambres = new ArrayList<>();

    public Hotel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public CategorieHotel getCategorie() { return categorie; }
    public void setCategorie(CategorieHotel categorie) { this.categorie = categorie; }

    public Integer getEtoiles() { return etoiles; }
    public void setEtoiles(Integer etoiles) { this.etoiles = etoiles; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public User getGerePar() { return gerePar; }
    public void setGerePar(User gerePar) { this.gerePar = gerePar; }

    public List<Chambre> getChambres() { return chambres; }
    public void setChambres(List<Chambre> chambres) { this.chambres = chambres; }
}