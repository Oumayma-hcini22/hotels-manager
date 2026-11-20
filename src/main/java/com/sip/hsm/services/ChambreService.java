package com.sip.hsm.services;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.StatutChambre;
import com.sip.hsm.entities.TypeChambre;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ChambreService {

    Page<Chambre> filtrer(Long hotelId, TypeChambre type, StatutChambre statut,
                          Double prixMax, int page, int size);

    Chambre findById(Long id);

    List<Chambre> disponibles(LocalDate debut, LocalDate fin, Integer nbPersonnes, Long hotelId);

    Chambre create(Chambre chambre);

    Chambre update(Long id, Chambre chambre);

    Chambre changerStatut(Long id, StatutChambre statut);

    void delete(Long id);
}
