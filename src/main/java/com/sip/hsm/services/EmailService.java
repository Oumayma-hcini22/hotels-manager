package com.sip.hsm.services;

import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.User;

public interface EmailService {

    // point 5 du cahier des charges : prévenir l'admin d'une nouvelle inscription
    void notifierAdminInscription(User user);

    // bonus (R11) : confirmer la réservation au client
    void envoyerConfirmationReservation(Reservation reservation);
}
