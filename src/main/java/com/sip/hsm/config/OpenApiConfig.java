package com.sip.hsm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Configuration de la documentation Swagger :
// - titre et description de l'API
// - bouton "Authorize" pour coller le token JWT et tester les routes protégées
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Hotels Manager API",
                version = "1.0",
                description = "API REST de gestion d'hôtels, de chambres et de réservations - Projet FullStack Spring Boot & Angular"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
