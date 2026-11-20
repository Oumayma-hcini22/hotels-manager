package com.sip.hsm.config;

import com.sip.hsm.entities.ERole;
import com.sip.hsm.entities.Role;
import com.sip.hsm.entities.User;
import com.sip.hsm.repositories.RoleRepository;
import com.sip.hsm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // valeurs lues dans application.properties
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1) les rôles
        for (ERole r : ERole.values()) {
            if (roleRepository.findByLibelle(r).isEmpty()) {
                roleRepository.save(new Role(r));
                System.out.println(">>> Rôle créé : " + r);
            }
        }

        // 2) le compte administrateur par défaut
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setNom("Hotels");
            admin.setPrenom("Admin");
            admin.setEmail(adminEmail);
            admin.setTel("20000000");
            admin.setPassword(passwordEncoder.encode(adminPassword));   // mot de passe crypté
            admin.setActif(true);
            admin.setRoles(Set.of(roleRepository.findByLibelle(ERole.ADMIN).orElseThrow()));
            userRepository.save(admin);
            System.out.println(">>> Admin créé : " + adminEmail + " / " + adminPassword);
        }
    }
}