package com.sip.hsm.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST avec JWT : pas de formulaire, donc pas de protection CSRF nécessaire
            .csrf(csrf -> csrf.disable())
            // autorise Angular (localhost:4200) à appeler l'API
            .cors(Customizer.withDefaults())
            // STATELESS : pas de session côté serveur, le token suffit
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // réponses JSON claires pour 401 et 403
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> ecrireErreur(res, 401, "Non authentifié",
                        "Vous devez vous connecter (token manquant, invalide ou expiré)"))
                .accessDeniedHandler((req, res, e) -> ecrireErreur(res, 403, "Accès refusé",
                        "Votre rôle ne permet pas cette opération")))

            // ============ RÈGLES D'ACCÈS (lues de haut en bas, la 1ère qui correspond gagne) ============
            .authorizeHttpRequests(auth -> auth
                // login et register : publics
                .requestMatchers("/api/auth/**").permitAll()
                // mon profil : tout utilisateur connecté
                .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                // gestion des utilisateurs : ADMIN uniquement
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                // CONSULTER (GET) : tout utilisateur connecté (ADMIN + COLLABORATEUR)
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                // AJOUTER (POST) : ADMIN + COLLABORATEUR
                .requestMatchers(HttpMethod.POST, "/api/hotels", "/api/chambres", "/api/reservations")
                    .hasAnyRole("ADMIN", "COLLABORATEUR")
                // tout le reste de l'API (PUT, PATCH, DELETE) : ADMIN uniquement
                .requestMatchers("/api/**").hasRole("ADMIN")
                // hors API (plus tard : les fichiers Angular) : public
                .anyRequest().permitAll())

            // notre filtre JWT s'exécute avant le filtre de login standard de Spring
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // utilisé par AuthService pour vérifier email + mot de passe
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS : autorise l'application Angular à appeler l'API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void ecrireErreur(HttpServletResponse res, int status, String erreur, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"erreur\":\"" + erreur
                + "\",\"message\":\"" + message + "\"}");
    }
}
