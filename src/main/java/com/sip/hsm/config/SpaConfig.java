package com.sip.hsm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

// Sert l'application Angular (dossier static/) depuis Spring Boot.
// - fichier existant (main.js, styles.css, favicon.ico...)  -> on le renvoie
// - adresse Angular (/hotels, /chambres/3/modifier...)       -> on renvoie index.html
//   et c'est le routeur Angular qui affiche la bonne page
// - adresse /api/... inconnue                                -> 404 normal
// Les controllers REST (/api/...) restent prioritaires sur cette configuration.
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource fichier = location.createRelative(resourcePath);
                        if (fichier.exists() && fichier.isReadable()) {
                            return fichier;
                        }
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
