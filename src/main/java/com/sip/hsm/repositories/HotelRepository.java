package com.sip.hsm.repositories;

import com.sip.hsm.entities.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Recherche paginée par nom OU par ville (sans tenir compte des majuscules)
    // SQL généré : WHERE LOWER(nom) LIKE %?% OR LOWER(ville) LIKE %?%
    Page<Hotel> findByNomContainingIgnoreCaseOrVilleContainingIgnoreCase(
            String nom, String ville, Pageable pageable);
}