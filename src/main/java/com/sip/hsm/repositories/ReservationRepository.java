package com.sip.hsm.repositories;

import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.StatutReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // toutes les réservations d'une chambre
    List<Reservation> findByChambreId(Long chambreId);

    // liste filtrée par statut (paginée)
    Page<Reservation> findByStatut(StatutReservation statut, Pageable pageable);

    // R5 : compte les réservations actives de la chambre qui chevauchent la période.
    // excludeId : en modification, on ignore la réservation elle-même (null en création)
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.chambre.id = :chambreId "
         + "AND r.statut IN :statutsActifs "
         + "AND r.dateArrivee < :fin AND r.dateDepart > :debut "
         + "AND (:excludeId IS NULL OR r.id <> :excludeId)")
    long compterChevauchements(@Param("chambreId") Long chambreId,
                               @Param("debut") LocalDate debut,
                               @Param("fin") LocalDate fin,
                               @Param("statutsActifs") List<StatutReservation> statutsActifs,
                               @Param("excludeId") Long excludeId);

    // Dashboard : nombre de réservations par statut
    long countByStatut(StatutReservation statut);

    // Dashboard : chiffre d'affaires sur une période (réservations confirmées / terminées)
    @Query("SELECT COALESCE(SUM(r.prixTotal), 0) FROM Reservation r "
         + "WHERE r.statut IN :statuts AND r.dateArrivee BETWEEN :debut AND :fin")
    Double chiffreAffaires(@Param("debut") LocalDate debut,
                           @Param("fin") LocalDate fin,
                           @Param("statuts") List<StatutReservation> statuts);

    // Dashboard : réservations (non annulées) par mois d'une année -> [ [1, 5], [12, 8], ... ]
    @Query("SELECT MONTH(r.dateArrivee), COUNT(r) FROM Reservation r "
         + "WHERE YEAR(r.dateArrivee) = :annee AND r.statut IN :statuts "
         + "GROUP BY MONTH(r.dateArrivee)")
    List<Object[]> compterParMois(@Param("annee") int annee,
                                  @Param("statuts") List<StatutReservation> statuts);

    // Dashboard : nombre de chambres occupées un jour donné
    // (réservation confirmée qui couvre ce jour : arrivée <= jour < départ)
    @Query("SELECT COUNT(DISTINCT r.chambre.id) FROM Reservation r "
         + "WHERE r.statut = :statut AND r.dateArrivee <= :jour AND r.dateDepart > :jour")
    long compterChambresOccupees(@Param("jour") LocalDate jour,
                                 @Param("statut") StatutReservation statut);

    // Dashboard : les 5 dernières réservations créées
    List<Reservation> findTop5ByOrderByDateCreationDesc();
    // réservations d'un statut donné dont la date de départ est atteinte ou passée
    List<Reservation> findByStatutAndDateDepartLessThanEqual(StatutReservation statut, LocalDate date);
}