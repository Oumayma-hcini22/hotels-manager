package com.sip.hsm.services;

import com.sip.hsm.entities.*;
import com.sip.hsm.exceptions.BusinessException;
import com.sip.hsm.exceptions.ResourceNotFoundException;
import com.sip.hsm.repositories.ChambreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.sip.hsm.security.SecurityUtils;

@Service
@Transactional
public class ChambreServiceImpl implements ChambreService {

    // statuts de réservation qui "bloquent" une chambre (une réservation annulée ne bloque rien)
    private static final List<StatutReservation> STATUTS_ACTIFS =
            List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final ChambreRepository chambreRepository;
    private final HotelService hotelService;
    private final SecurityUtils securityUtils;

    public ChambreServiceImpl(ChambreRepository chambreRepository, HotelService hotelService,
                              SecurityUtils securityUtils) {
        this.chambreRepository = chambreRepository;
        this.hotelService = hotelService;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Chambre> filtrer(Long hotelId, TypeChambre type, StatutChambre statut,
                                 Double prixMax, int page, int size) {
        return chambreRepository.filtrer(hotelId, type, statut, prixMax,
                PageRequest.of(page, size, Sort.by("numero").ascending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Chambre findById(Long id) {
        return chambreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre introuvable avec l'id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> disponibles(LocalDate debut, LocalDate fin, Integer nbPersonnes, Long hotelId) {
        // R3 : la date de départ doit être après la date d'arrivée
        if (!fin.isAfter(debut)) {
            throw new BusinessException("La date de départ doit être après la date d'arrivée");
        }
        // R4 : pas de recherche dans le passé
        if (debut.isBefore(LocalDate.now())) {
            throw new BusinessException("La date d'arrivée ne peut pas être dans le passé");
        }
        int nuits = (int) ChronoUnit.DAYS.between(debut, fin);
        int nb = (nbPersonnes == null || nbPersonnes < 1) ? 1 : nbPersonnes;

        return chambreRepository.chambresDisponibles(debut, fin, nb, nuits, hotelId,
                StatutChambre.MAINTENANCE, STATUTS_ACTIFS);
    }

    @Override
    public Chambre create(Chambre chambre) {
        chambre.setId(null);
        Hotel hotel = resoudreHotel(chambre);

        // R1 : numéro unique dans l'hôtel
        if (chambreRepository.existsByNumeroAndHotelId(chambre.getNumero(), hotel.getId())) {
            throw new BusinessException("La chambre n° " + chambre.getNumero()
                    + " existe déjà dans l'hôtel " + hotel.getNom());
        }
        chambre.setHotel(hotel);
        if (chambre.getStatut() == null) chambre.setStatut(StatutChambre.DISPONIBLE);
        if (chambre.getDureeMinSejour() == null) chambre.setDureeMinSejour(1);
        chambre.setGerePar(securityUtils.getUtilisateurConnecte());   // R14 : association "gérer"
        return chambreRepository.save(chambre);
    }

    @Override
    public Chambre update(Long id, Chambre data) {
        Chambre chambre = findById(id);
        Hotel hotel = resoudreHotel(data);

        // R1 en modification : on ignore la chambre elle-même
        if (chambreRepository.existsByNumeroAndHotelIdAndIdNot(data.getNumero(), hotel.getId(), id)) {
            throw new BusinessException("La chambre n° " + data.getNumero()
                    + " existe déjà dans l'hôtel " + hotel.getNom());
        }
        chambre.setNumero(data.getNumero());
        chambre.setLibelle(data.getLibelle());
        chambre.setType(data.getType());
        chambre.setPrix(data.getPrix());
        chambre.setDureeMinSejour(data.getDureeMinSejour() == null ? 1 : data.getDureeMinSejour());
        chambre.setEtage(data.getEtage());
        chambre.setCapacite(data.getCapacite());
        if (data.getStatut() != null) chambre.setStatut(data.getStatut());
        chambre.setDescription(data.getDescription());
        chambre.setHotel(hotel);
        return chambreRepository.save(chambre);
    }

    @Override
    public Chambre changerStatut(Long id, StatutChambre statut) {
        Chambre chambre = findById(id);
        chambre.setStatut(statut);
        return chambreRepository.save(chambre);
    }

    @Override
    public void delete(Long id) {
        // cascade REMOVE : supprime aussi les réservations de la chambre
        chambreRepository.delete(findById(id));
    }

    // Le JSON envoie seulement  "hotel": { "id": 1 }  -> on charge le vrai hôtel depuis la base
    private Hotel resoudreHotel(Chambre chambre) {
        if (chambre.getHotel() == null || chambre.getHotel().getId() == null) {
            throw new BusinessException("L'hôtel de la chambre est obligatoire (hotel.id)");
        }
        return hotelService.findById(chambre.getHotel().getId());   // 404 si introuvable
    }
}
