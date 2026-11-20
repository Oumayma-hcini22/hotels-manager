package com.sip.hsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration : classe de configuration Spring
@Configuration
public class PasswordConfig {

    // @Bean : Spring crée UN SEUL encodeur BCrypt, injectable partout dans l'application
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
