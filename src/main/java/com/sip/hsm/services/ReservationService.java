package com.sip.hsm.services;

import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.StatutReservation;
import org.springframework.data.domain.Page;

public interface ReservationService {

    Page<Reservation> findAll(StatutReservation statut, int page, int size);

    Reservation findById(Long id);

    Reservation create(Reservation reservation);

    Reservation update(Long id, Reservation reservation);

    Reservation confirmer(Long id);

    Reservation annuler(Long id);

    void delete(Long id);
}
