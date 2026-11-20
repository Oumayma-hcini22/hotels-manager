package com.sip.hsm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Exécuté UNE fois pour CHAQUE requête HTTP, avant les controllers
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1) lire l'en-tête  Authorization: Bearer <token>
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 2) token valide (signature + expiration) ?
            if (jwtUtils.estValide(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    // 3) charger l'utilisateur et ses rôles
                    UserDetails user = userDetailsService.loadUserByUsername(jwtUtils.getEmail(token));

                    if (user.isEnabled()) {
                        // 4) déclarer l'utilisateur "connecté" pour cette requête
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (UsernameNotFoundException e) {
                    // utilisateur supprimé entre-temps : on ne l'authentifie pas
                }
            }
        }
        // 5) continuer vers les règles de SecurityConfig puis le controller
        chain.doFilter(request, response);
    }
}
