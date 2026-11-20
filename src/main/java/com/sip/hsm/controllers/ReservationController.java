package com.sip.hsm.controllers;

import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.StatutReservation;
import com.sip.hsm.services.PdfService;
import com.sip.hsm.services.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final PdfService pdfService;

    public ReservationController(ReservationService reservationService, PdfService pdfService) {
        this.reservationService = reservationService;
        this.pdfService = pdfService;
    }

    // GET /api/reservations?statut=EN_ATTENTE&page=0&size=10
    @GetMapping
    public Page<Reservation> getAll(@RequestParam(required = false) StatutReservation statut,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return reservationService.findAll(statut, page, size);
    }

    @GetMapping("/{id}")
    public Reservation getById(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    // GET /api/reservations/6/facture  -> télécharge le PDF
    @GetMapping("/{id}/facture")
    public ResponseEntity<byte[]> facture(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        byte[] pdf = pdfService.genererFacture(reservation);
        String nomFichier = "facture-RES-" + String.format("%04d", id) + ".pdf";

        return ResponseEntity.ok()
                // "attachment" : le navigateur propose de télécharger le fichier
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping
    public ResponseEntity<Reservation> create(@Valid @RequestBody Reservation reservation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(reservation));
    }

    @PutMapping("/{id}")
    public Reservation update(@PathVariable Long id, @Valid @RequestBody Reservation reservation) {
        return reservationService.update(id, reservation);
    }

    @PatchMapping("/{id}/confirmer")
    public Reservation confirmer(@PathVariable Long id) {
        return reservationService.confirmer(id);
    }

    @PatchMapping("/{id}/annuler")
    public Reservation annuler(@PathVariable Long id) {
        return reservationService.annuler(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}