package com.sip.hsm.controllers;

import com.sip.hsm.dto.ChangePasswordRequest;
import com.sip.hsm.dto.MessageResponse;
import com.sip.hsm.dto.ProfilRequest;
import com.sip.hsm.entities.ERole;
import com.sip.hsm.entities.User;
import com.sip.hsm.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ============ MON PROFIL : tout utilisateur connecté ============

    // GET /api/users/me
    @GetMapping("/me")
    public User getProfil() {
        return userService.getProfil();
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public User updateProfil(@Valid @RequestBody ProfilRequest request) {
        return userService.updateProfil(request);
    }

    // PUT /api/users/me/password
    @PutMapping("/me/password")
    public MessageResponse changerMotDePasse(@Valid @RequestBody ChangePasswordRequest request) {
        return userService.changerMotDePasse(request);
    }

    // ============ GESTION DES UTILISATEURS : ADMIN ============

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody User user) {
        return userService.update(id, user);
    }

    @PatchMapping("/{id}/activer")
    public User activer(@PathVariable Long id, @RequestParam boolean actif) {
        return userService.activer(id, actif);
    }

    @PatchMapping("/{id}/roles")
    public User changerRoles(@PathVariable Long id, @RequestBody Set<ERole> roles) {
        return userService.changerRoles(id, roles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}