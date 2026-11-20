package com.sip.hsm.dto;

import java.util.List;

// ce que le serveur renvoie après un login réussi
public record AuthResponse(
        String token,
        String type,          // toujours "Bearer"
        Long id,
        String email,
        String nom,
        String prenom,
        List<String> roles
) {}
