package com.sip.hsm.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
// règle R1 : un même numéro ne peut pas exister 2 fois dans le même hôtel
@Table(name = "chambres",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero", "id_hotel"}))
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le numéro est obligatoire")
    @Positive(message = "Le numéro doit être positif")
    @Column(nullable = false)
    private Integer numero;

    // équivalent de "libellé" du Projet : ex "Double vue mer"
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;

    @NotNull(message = "Le type est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TypeChambre type;

    // équivalent de "budget" du Projet : prix par nuit
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double prix;

    // équivalent de "durée" du Projet : nombre minimum de nuits (règle R13)
    @Min(value = 1, message = "La durée minimale de séjour est d'au moins 1 nuit")
    private Integer dureeMinSejour = 1;

    @Min(value = 0, message = "L'étage ne peut pas être négatif")
    private Integer etage;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "Capacité minimum : 1 personne")
    private Integer capacite;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutChambre statut = StatutChambre.DISPONIBLE;

    @Column(length = 1000)
    private String description;

    // Many-to-One : plusieurs chambres appartiennent à un hôtel
    @ManyToOne
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    // association "gérer" (User -> Projet) du diagramme de classes
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User gerePar;

    // One-to-Many : une chambre a plusieurs réservations dans le temps
    @OneToMany(mappedBy = "chambre", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    public Chambre() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public TypeChambre getType() { return type; }
    public void setType(TypeChambre type) { this.type = type; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public Integer getDureeMinSejour() { return dureeMinSejour; }
    public void setDureeMinSejour(Integer dureeMinSejour) { this.dureeMinSejour = dureeMinSejour; }

    public Integer getEtage() { return etage; }
    public void setEtage(Integer etage) { this.etage = etage; }

    public Integer getCapacite() { return capacite; }
    public void setCapacite(Integer capacite) { this.capacite = capacite; }

    public StatutChambre getStatut() { return statut; }
    public void setStatut(StatutChambre statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public User getGerePar() { return gerePar; }
    public void setGerePar(User gerePar) { this.gerePar = gerePar; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }
}