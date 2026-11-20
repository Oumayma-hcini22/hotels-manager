package com.sip.hsm.repositories;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.StatutChambre;
import com.sip.hsm.entities.StatutReservation;
import com.sip.hsm.entities.TypeChambre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChambreRepository extends JpaRepository<Chambre, Long> {

    // chambres d'un hôtel
    List<Chambre> findByHotelId(Long hotelId);

    // R1 : vérifier si le numéro existe déjà dans cet hôtel (création)
    boolean existsByNumeroAndHotelId(Integer numero, Long hotelId);

    // R1 : même vérification en modification (on ignore la chambre elle-même)
    boolean existsByNumeroAndHotelIdAndIdNot(Integer numero, Long hotelId, Long id);

    // Filtres combinables : chaque filtre est ignoré s'il vaut null
    @Query("SELECT c FROM Chambre c WHERE "
         + "(:hotelId IS NULL OR c.hotel.id = :hotelId) AND "
         + "(:type IS NULL OR c.type = :type) AND "
         + "(:statut IS NULL OR c.statut = :statut) AND "
         + "(:prixMax IS NULL OR c.prix <= :prixMax)")
    Page<Chambre> filtrer(@Param("hotelId") Long hotelId,
                          @Param("type") TypeChambre type,
                          @Param("statut") StatutChambre statut,
                          @Param("prixMax") Double prixMax,
                          Pageable pageable);

    // Chambres libres pour une période donnée :
    //  - pas en maintenance (R7)
    //  - capacité suffisante (R6)
    //  - séjour minimum respecté (R13)
    //  - aucune réservation active qui chevauche les dates (R5)
    // Deux périodes se chevauchent si : arrivéeExistante < fin ET départExistant > début
    @Query("SELECT c FROM Chambre c WHERE "
         + "c.statut <> :maintenance "
         + "AND c.capacite >= :nbPersonnes "
         + "AND c.dureeMinSejour <= :nuits "
         + "AND (:hotelId IS NULL OR c.hotel.id = :hotelId) "
         + "AND NOT EXISTS (SELECT r FROM Reservation r WHERE r.chambre = c "
         + "      AND r.statut IN :statutsActifs "
         + "      AND r.dateArrivee < :fin AND r.dateDepart > :debut)")
    List<Chambre> chambresDisponibles(@Param("debut") LocalDate debut,
                                      @Param("fin") LocalDate fin,
                                      @Param("nbPersonnes") Integer nbPersonnes,
                                      @Param("nuits") Integer nuits,
                                      @Param("hotelId") Long hotelId,
                                      @Param("maintenance") StatutChambre maintenance,
                                      @Param("statutsActifs") List<StatutReservation> statutsActifs);

    // Dashboard : nombre de chambres par statut
    long countByStatut(StatutChambre statut);

    // Dashboard : nombre de chambres par type -> [ [SINGLE, 12], [DOUBLE, 30], ... ]
    @Query("SELECT c.type, COUNT(c) FROM Chambre c GROUP BY c.type")
    List<Object[]> compterParType();
}
