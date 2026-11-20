package com.sip.hsm.services;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.Hotel;
import org.springframework.data.domain.Page;

import java.util.List;

// Le CONTRAT : la liste des opérations possibles sur les hôtels
public interface HotelService {

    Page<Hotel> findAll(String search, int page, int size);

    Hotel findById(Long id);

    List<Chambre> getChambres(Long hotelId);

    Hotel create(Hotel hotel);

    Hotel update(Long id, Hotel hotel);

    void delete(Long id);
}
