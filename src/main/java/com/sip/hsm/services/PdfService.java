package com.sip.hsm.services;

import com.sip.hsm.entities.Reservation;

public interface PdfService {

    byte[] genererFacture(Reservation reservation);
}
