package com.sip.hsm.services;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.StatutChambre;
import com.sip.hsm.entities.StatutReservation;
import com.sip.hsm.exceptions.BusinessException;
import com.sip.hsm.exceptions.ResourceNotFoundException;
import com.sip.hsm.repositories.ReservationRepository;
import com.sip.hsm.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    // une réservation EN_ATTENTE ou CONFIRMEE "bloque" la chambre ; ANNULEE ne bloque rien
    private static final List<StatutReservation> STATUTS_ACTIFS =
            List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final ReservationRepository reservationRepository;
    private final ChambreService chambreService;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ChambreService chambreService,
                                  EmailService emailService,
                                  SecurityUtils securityUtils) {
        this.reservationRepository = reservationRepository;
        this.chambreService = chambreService;
        this.emailService = emailService;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findAll(StatutReservation statut, int page, int size) {
        // les plus récentes en premier
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        if (statut == null) {
            return reservationRepository.findAll(pageable);
        }
        return reservationRepository.findByStatut(statut, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'id " + id));
    }

    @Override
    public Reservation create(Reservation reservation) {
        reservation.setId(null);
        Chambre chambre = resoudreChambre(reservation);

        verifierRegles(reservation, chambre, null);              // R3, R4, R5, R6, R7, R13

        reservation.setChambre(chambre);
        reservation.setPrixTotal(calculerPrix(reservation, chambre));   // R2
        reservation.setStatut(StatutReservation.EN_ATTENTE);      // toujours EN_ATTENTE à la création
        reservation.setCreePar(securityUtils.getUtilisateurConnecte());   // qui a créé la réservation
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation update(Long id, Reservation data) {
        Reservation reservation = findById(id);

        // on ne modifie pas une réservation annulée ou terminée
        if (!STATUTS_ACTIFS.contains(reservation.getStatut())) {
            throw new BusinessException("Impossible de modifier une réservation " + reservation.getStatut());
        }
        Chambre chambre = resoudreChambre(data);

        // excludeId = id : on ignore la réservation elle-même dans la vérification du chevauchement
        verifierRegles(data, chambre, id);

        reservation.setNomClient(data.getNomClient());
        reservation.setTelClient(data.getTelClient());
        reservation.setEmailClient(data.getEmailClient());
        reservation.setDateArrivee(data.getDateArrivee());
        reservation.setDateDepart(data.getDateDepart());
        reservation.setNbPersonnes(data.getNbPersonnes());
        reservation.setChambre(chambre);
        reservation.setPrixTotal(calculerPrix(data, chambre));    // R2 : prix recalculé
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation confirmer(Long id) {
        Reservation reservation = findById(id);
        if (reservation.getStatut() != StatutReservation.EN_ATTENTE) {
            throw new BusinessException("Seule une réservation EN_ATTENTE peut être confirmée (statut actuel : "
                    + reservation.getStatut() + ")");
        }
        reservation.setStatut(StatutReservation.CONFIRMEE);
        Reservation saved = reservationRepository.save(reservation);

        // R11 : email de confirmation au client
        emailService.envoyerConfirmationReservation(saved);
        return saved;
    }

    @Override
    public Reservation annuler(Long id) {
        Reservation reservation = findById(id);
        if (!STATUTS_ACTIFS.contains(reservation.getStatut())) {
            throw new BusinessException("Cette réservation ne peut pas être annulée (statut actuel : "
                    + reservation.getStatut() + ")");
        }
        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationRepository.save(reservation);
    }

    @Override
    public void delete(Long id) {
        reservationRepository.delete(findById(id));
    }

    // ================== RÈGLES MÉTIER ==================

    private void verifierRegles(Reservation r, Chambre chambre, Long excludeId) {
        LocalDate debut = r.getDateArrivee();
        LocalDate fin = r.getDateDepart();

        // R3 : départ après arrivée
        if (!fin.isAfter(debut)) {
            throw new BusinessException("La date de départ doit être après la date d'arrivée");
        }
        // R4 : pas dans le passé
        if (debut.isBefore(LocalDate.now())) {
            throw new BusinessException("La date d'arrivée ne peut pas être dans le passé");
        }
        // R7 : chambre en maintenance
        if (chambre.getStatut() == StatutChambre.MAINTENANCE) {
            throw new BusinessException("La chambre n° " + chambre.getNumero()
                    + " est en maintenance et ne peut pas être réservée");
        }
        // R6 : capacité
        if (r.getNbPersonnes() > chambre.getCapacite()) {
            throw new BusinessException("La chambre n° " + chambre.getNumero() + " accepte au maximum "
                    + chambre.getCapacite() + " personne(s)");
        }
        // R13 : séjour minimum
        long nuits = ChronoUnit.DAYS.between(debut, fin);
        if (nuits < chambre.getDureeMinSejour()) {
            throw new BusinessException("Séjour minimum de " + chambre.getDureeMinSejour()
                    + " nuit(s) pour cette chambre (demandé : " + nuits + ")");
        }
        // R5 : chevauchement avec une réservation active
        long conflits = reservationRepository.compterChevauchements(
                chambre.getId(), debut, fin, STATUTS_ACTIFS, excludeId);
        if (conflits > 0) {
            throw new BusinessException("La chambre n° " + chambre.getNumero()
                    + " est déjà réservée sur cette période (du " + debut + " au " + fin + ")");
        }
    }

    // R2 : prix total = nombre de nuits x prix par nuit
    private Double calculerPrix(Reservation r, Chambre chambre) {
        long nuits = ChronoUnit.DAYS.between(r.getDateArrivee(), r.getDateDepart());
        return nuits * chambre.getPrix();
    }

    // le JSON envoie seulement  "chambre": { "id": 2 }  -> on charge la vraie chambre
    private Chambre resoudreChambre(Reservation r) {
        if (r.getChambre() == null || r.getChambre().getId() == null) {
            throw new BusinessException("La chambre de la réservation est obligatoire (chambre.id)");
        }
        return chambreService.findById(r.getChambre().getId());   // 404 si introuvable
    }
}
