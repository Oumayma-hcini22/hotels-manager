package com.sip.hsm.services;

import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.StatutReservation;
import com.sip.hsm.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Tâche planifiée : exécutée automatiquement par Spring, sans aucune action de l'utilisateur
@Component
public class ReservationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationScheduler.class);

    private final ReservationRepository reservationRepository;

    public ReservationScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // 1ère exécution 10 secondes après le démarrage, puis toutes les heures (3 600 000 ms)
    @Scheduled(initialDelay = 10_000, fixedRate = 3_600_000)
    @Transactional
    public void terminerReservationsPassees() {
        List<Reservation> passees = reservationRepository
                .findByStatutAndDateDepartLessThanEqual(StatutReservation.CONFIRMEE, LocalDate.now());

        passees.forEach(r -> r.setStatut(StatutReservation.TERMINEE));
        reservationRepository.saveAll(passees);

        log.info(">>> Tâche planifiée : {} réservation(s) passée(s) en TERMINEE", passees.size());
    }
}
