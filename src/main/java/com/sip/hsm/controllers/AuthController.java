package com.sip.hsm.controllers;

import com.sip.hsm.dto.AuthResponse;
import com.sip.hsm.dto.LoginRequest;
import com.sip.hsm.dto.MessageResponse;
import com.sip.hsm.dto.RegisterRequest;
import com.sip.hsm.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login   -> renvoie le token JWT
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // POST /api/auth/register   -> crée un compte COLLABORATEUR inactif
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}