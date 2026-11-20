package com.sip.hsm.services;

import com.sip.hsm.dto.DashboardStats;
import com.sip.hsm.entities.StatutChambre;
import com.sip.hsm.entities.StatutReservation;
import com.sip.hsm.entities.TypeChambre;
import com.sip.hsm.repositories.ChambreRepository;
import com.sip.hsm.repositories.HotelRepository;
import com.sip.hsm.repositories.ReservationRepository;
import com.sip.hsm.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)   // le dashboard ne fait que lire
public class DashboardServiceImpl implements DashboardService {

    // réservations qui rapportent de l'argent
    private static final List<StatutReservation> STATUTS_PAYES =
            List.of(StatutReservation.CONFIRMEE, StatutReservation.TERMINEE);

    // réservations comptées dans le graphique (toutes sauf les annulées)
    private static final List<StatutReservation> STATUTS_NON_ANNULES =
            List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE, StatutReservation.TERMINEE);

    private final HotelRepository hotelRepository;
    private final ChambreRepository chambreRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(HotelRepository hotelRepository, ChambreRepository chambreRepository,
                                ReservationRepository reservationRepository, UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.chambreRepository = chambreRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardStats getStats() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate debutMois = aujourdhui.withDayOfMonth(1);
        LocalDate finMois = aujourdhui.withDayOfMonth(aujourdhui.lengthOfMonth());
        LocalDate debutAnnee = aujourdhui.withDayOfYear(1);
        LocalDate finAnnee = aujourdhui.withDayOfYear(aujourdhui.lengthOfYear());

        // ---- taux d'occupation du jour : chambres occupées / total des chambres ----
        long nbChambres = chambreRepository.count();
        long occupees = reservationRepository.compterChambresOccupees(aujourdhui, StatutReservation.CONFIRMEE);
        double taux = (nbChambres == 0) ? 0 : arrondir(occupees * 100.0 / nbChambres);

        // ---- graphique : chambres par type (tous les types, même à 0) ----
        Map<String, Long> parType = new LinkedHashMap<>();
        for (TypeChambre t : TypeChambre.values()) {
            parType.put(t.name(), 0L);
        }
        for (Object[] ligne : chambreRepository.compterParType()) {
            parType.put(((TypeChambre) ligne[0]).name(), ((Number) ligne[1]).longValue());
        }

        // ---- graphique : réservations par mois (12 cases, de janvier à décembre) ----
        List<Long> parMois = new ArrayList<>(Collections.nCopies(12, 0L));
        for (Object[] ligne : reservationRepository.compterParMois(aujourdhui.getYear(), STATUTS_NON_ANNULES)) {
            int mois = ((Number) ligne[0]).intValue();          // 1 = janvier ... 12 = décembre
            parMois.set(mois - 1, ((Number) ligne[1]).longValue());
        }

        return new DashboardStats(
                hotelRepository.count(),
                nbChambres,
                chambreRepository.countByStatut(StatutChambre.DISPONIBLE),
                chambreRepository.countByStatut(StatutChambre.MAINTENANCE),
                reservationRepository.count(),
                reservationRepository.countByStatut(StatutReservation.EN_ATTENTE),
                reservationRepository.countByStatut(StatutReservation.CONFIRMEE),
                userRepository.count(),
                userRepository.countByActifFalse(),
                arrondir(reservationRepository.chiffreAffaires(debutMois, finMois, STATUTS_PAYES)),
                arrondir(reservationRepository.chiffreAffaires(debutAnnee, finAnnee, STATUTS_PAYES)),
                taux,
                parType,
                parMois,
                reservationRepository.findTop5ByOrderByDateCreationDesc()
        );
    }

    // arrondi à 2 décimales
    private double arrondir(Double valeur) {
        if (valeur == null) return 0;
        return Math.round(valeur * 100.0) / 100.0;
    }
}