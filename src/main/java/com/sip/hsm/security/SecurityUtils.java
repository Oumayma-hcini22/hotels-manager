package com.sip.hsm.security;

import com.sip.hsm.entities.User;
import com.sip.hsm.repositories.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // renvoie l'utilisateur connecté (celui du token JWT), ou null si personne n'est connecté
    public User getUtilisateurConnecte() {
        // le JwtFilter a placé l'utilisateur dans le SecurityContext au début de la requête
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);   // getName() = l'email
    }
}
