package com.sip.hsm.services;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.Hotel;
import com.sip.hsm.exceptions.ResourceNotFoundException;
import com.sip.hsm.repositories.ChambreRepository;
import com.sip.hsm.repositories.HotelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sip.hsm.security.SecurityUtils;
import java.util.List;

@Service          // Spring crée et gère cet objet (un "bean")
@Transactional    // chaque méthode s'exécute dans une transaction : tout réussit ou tout est annulé
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ChambreRepository chambreRepository;
    private final SecurityUtils securityUtils;

    public HotelServiceImpl(HotelRepository hotelRepository, ChambreRepository chambreRepository,
                            SecurityUtils securityUtils) {
        this.hotelRepository = hotelRepository;
        this.chambreRepository = chambreRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional(readOnly = true)   // lecture seule : plus performant
    public Page<Hotel> findAll(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());

        // sans recherche : tous les hôtels ; avec recherche : filtre par nom OU ville
        if (search == null || search.isBlank()) {
            return hotelRepository.findAll(pageable);
        }
        String s = search.trim();
        return hotelRepository.findByNomContainingIgnoreCaseOrVilleContainingIgnoreCase(s, s, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Hotel findById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hôtel introuvable avec l'id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chambre> getChambres(Long hotelId) {
        findById(hotelId);   // vérifie que l'hôtel existe (sinon 404)
        return chambreRepository.findByHotelId(hotelId);
    }

    @Override
    public Hotel create(Hotel hotel) {
        hotel.setId(null);   // on force une création (jamais une modification)
        hotel.setGerePar(securityUtils.getUtilisateurConnecte());   // R14 : association "gérer"
        return hotelRepository.save(hotel);
    }

    @Override
    public Hotel update(Long id, Hotel data) {
        Hotel hotel = findById(id);   // 404 si introuvable
        hotel.setNom(data.getNom());
        hotel.setAdresse(data.getAdresse());
        hotel.setVille(data.getVille());
        hotel.setTel(data.getTel());
        hotel.setEmail(data.getEmail());
        hotel.setCategorie(data.getCategorie());
        hotel.setEtoiles(data.getEtoiles());
        hotel.setDescription(data.getDescription());
        hotel.setImage(data.getImage());
        // gerePar n'est pas modifié : le gestionnaire reste le même
        return hotelRepository.save(hotel);
    }

    @Override
    public void delete(Long id) {
        Hotel hotel = findById(id);
        // grâce à cascade = REMOVE : supprime aussi ses chambres et leurs réservations (R10)
        hotelRepository.delete(hotel);
    }
}
