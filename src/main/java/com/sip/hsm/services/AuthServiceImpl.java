package com.sip.hsm.services;

import com.sip.hsm.dto.AuthResponse;
import com.sip.hsm.dto.LoginRequest;
import com.sip.hsm.dto.MessageResponse;
import com.sip.hsm.dto.RegisterRequest;
import com.sip.hsm.entities.ERole;
import com.sip.hsm.entities.User;
import com.sip.hsm.exceptions.BusinessException;
import com.sip.hsm.repositories.RoleRepository;
import com.sip.hsm.repositories.UserRepository;
import com.sip.hsm.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Spring Security vérifie : email existe ? mot de passe correct (BCrypt) ? compte actif ?
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getLibelle().name())
                .toList();

        String token = jwtUtils.genererToken(user.getEmail(), roles);
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(),
                user.getNom(), user.getPrenom(), roles);
    }

    @Override
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Cet email est déjà utilisé : " + request.email());
        }

        User user = new User();
        user.setNom(request.nom());
        user.setPrenom(request.prenom());
        user.setEmail(request.email());
        user.setTel(request.tel());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActif(false);
        user.setRoles(Set.of(roleRepository.findByLibelle(ERole.COLLABORATEUR).orElseThrow()));
        User saved = userRepository.save(user);

        // point 5 du cahier des charges : email à l'admin
        emailService.notifierAdminInscription(saved);

        return new MessageResponse("Inscription réussie. Votre compte sera activé par l'administrateur.");
    }
}
