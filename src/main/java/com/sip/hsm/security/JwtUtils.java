package com.sip.hsm.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

// Outil pour CRÉER et VÉRIFIER les tokens JWT
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // clé de signature construite à partir du secret (application.properties)
    private SecretKey cle() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Crée un token qui contient : l'email (subject), les rôles, la date de création et d'expiration
    public String genererToken(String email, List<String> roles) {
        Date maintenant = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(maintenant)
                .expiration(new Date(maintenant.getTime() + expirationMs))
                .signWith(cle())          // signature : impossible de modifier le token sans la clé
                .compact();
    }

    // Lit l'email contenu dans le token
    public String getEmail(String token) {
        return Jwts.parser().verifyWith(cle()).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Vérifie la signature et la date d'expiration
    public boolean estValide(String token) {
        try {
            Jwts.parser().verifyWith(cle()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;   // token modifié, expiré ou mal formé
        }
    }
}
