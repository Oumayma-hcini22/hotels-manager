package com.sip.hsm.controllers;

import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.Hotel;
import com.sip.hsm.services.HotelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController                    // les méthodes renvoient du JSON
@RequestMapping("/api/hotels")     // préfixe commun à toutes les URL de ce controller
public class HotelController {

    private final HotelService hotelService;   // on dépend de l'INTERFACE, pas de l'implémentation

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // GET /api/hotels?search=sousse&page=0&size=10
    @GetMapping
    public Page<Hotel> getAll(@RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return hotelService.findAll(search, page, size);
    }

    // GET /api/hotels/5
    @GetMapping("/{id}")
    public Hotel getById(@PathVariable Long id) {
        return hotelService.findById(id);
    }

    // GET /api/hotels/5/chambres  -> relation One-to-Many
    @GetMapping("/{id}/chambres")
    public List<Chambre> getChambres(@PathVariable Long id) {
        return hotelService.getChambres(id);
    }

    // POST /api/hotels   -> 201 CREATED
    // @Valid : déclenche la validation (@NotBlank, @Email...) -> 400 si invalide
    @PostMapping
    public ResponseEntity<Hotel> create(@Valid @RequestBody Hotel hotel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(hotel));
    }

    // PUT /api/hotels/5
    @PutMapping("/{id}")
    public Hotel update(@PathVariable Long id, @Valid @RequestBody Hotel hotel) {
        return hotelService.update(id, hotel);
    }

    // DELETE /api/hotels/5   -> 204 NO CONTENT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
