package com.sip.hsm.dto;

import com.sip.hsm.entities.Reservation;

import java.util.List;
import java.util.Map;

// DTO (Data Transfer Object) : transporte les statistiques vers Angular.
// "record" = classe de données immuable : Java génère automatiquement
// le constructeur, les getters, equals, hashCode et toString.
public record DashboardStats(
        // compteurs
        long nbHotels,
        long nbChambres,
        long nbChambresDisponibles,
        long nbChambresMaintenance,
        long nbReservations,
        long nbReservationsEnAttente,
        long nbReservationsConfirmees,
        long nbUtilisateurs,
        long nbComptesEnAttente,
        // finances et occupation
        double chiffreAffairesMois,
        double chiffreAffairesAnnee,
        double tauxOccupation,
        // données des graphiques
        Map<String, Long> chambresParType,      // { "SINGLE": 1, "DOUBLE": 2, ... }
        List<Long> reservationsParMois,         // 12 valeurs : janvier -> décembre
        // tableau "dernières réservations"
        List<Reservation> dernieresReservations
) {}
