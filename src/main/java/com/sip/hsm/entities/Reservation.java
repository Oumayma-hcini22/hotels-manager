package com.sip.hsm.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du client est obligatoire")
    private String nomClient;

    @NotBlank(message = "Le téléphone du client est obligatoire")
    @Pattern(regexp = "^[0-9]{8}$", message = "Le téléphone doit contenir 8 chiffres")
    private String telClient;

    @Email(message = "Email du client invalide")
    private String emailClient;

    @NotNull(message = "La date d'arrivée est obligatoire")
    private LocalDate dateArrivee;

    @NotNull(message = "La date de départ est obligatoire")
    private LocalDate dateDepart;

    @NotNull(message = "Le nombre de personnes est obligatoire")
    @Min(value = 1, message = "Au moins 1 personne")
    private Integer nbPersonnes;

    // calculé automatiquement par le serveur : nombre de nuits x prix (règle R2)
    private Double prixTotal;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @Column(updatable = false)
    private LocalDateTime dateCreation;

    // Many-to-One : plusieurs réservations pour une même chambre
    @ManyToOne
    @JoinColumn(name = "id_chambre", nullable = false)
    private Chambre chambre;

    // l'employé qui a créé la réservation
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User creePar;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    public Reservation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getTelClient() { return telClient; }
    public void setTelClient(String telClient) { this.telClient = telClient; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public LocalDate getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDate dateArrivee) { this.dateArrivee = dateArrivee; }

    public LocalDate getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }

    public Integer getNbPersonnes() { return nbPersonnes; }
    public void setNbPersonnes(Integer nbPersonnes) { this.nbPersonnes = nbPersonnes; }

    public Double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(Double prixTotal) { this.prixTotal = prixTotal; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Chambre getChambre() { return chambre; }
    public void setChambre(Chambre chambre) { this.chambre = chambre; }

    public User getCreePar() { return creePar; }
    public void setCreePar(User creePar) { this.creePar = creePar; }
}
