package com.sip.hsm.controllers;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.StatutChambre;
import com.sip.hsm.entities.TypeChambre;
import com.sip.hsm.services.ChambreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/chambres")
public class ChambreController {

    private final ChambreService chambreService;

    public ChambreController(ChambreService chambreService) {
        this.chambreService = chambreService;
    }

    // GET /api/chambres?hotelId=1&type=DOUBLE&statut=DISPONIBLE&prixMax=150&page=0&size=10
    // tous les filtres sont optionnels (required = false)
    @GetMapping
    public Page<Chambre> getAll(@RequestParam(required = false) Long hotelId,
                                @RequestParam(required = false) TypeChambre type,
                                @RequestParam(required = false) StatutChambre statut,
                                @RequestParam(required = false) Double prixMax,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return chambreService.filtrer(hotelId, type, statut, prixMax, page, size);
    }

    // GET /api/chambres/disponibles?debut=2026-12-10&fin=2026-12-13&nb=2&hotelId=1
    @GetMapping("/disponibles")
    public List<Chambre> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(defaultValue = "1") Integer nb,
            @RequestParam(required = false) Long hotelId) {
        return chambreService.disponibles(debut, fin, nb, hotelId);
    }

    @GetMapping("/{id}")
    public Chambre getById(@PathVariable Long id) {
        return chambreService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Chambre> create(@Valid @RequestBody Chambre chambre) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chambreService.create(chambre));
    }

    @PutMapping("/{id}")
    public Chambre update(@PathVariable Long id, @Valid @RequestBody Chambre chambre) {
        return chambreService.update(id, chambre);
    }

    // PATCH = modification PARTIELLE (ici, seulement le statut)
    // PATCH /api/chambres/3/statut?statut=MAINTENANCE
    @PatchMapping("/{id}/statut")
    public Chambre changerStatut(@PathVariable Long id, @RequestParam StatutChambre statut) {
        return chambreService.changerStatut(id, statut);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chambreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
