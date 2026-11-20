package com.sip.hsm.security;

import com.sip.hsm.entities.User;
import com.sip.hsm.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Spring Security appelle cette classe pour charger un utilisateur à partir de son email
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        // nos rôles ADMIN / COLLABORATEUR deviennent des "autorités" Spring : ROLE_ADMIN / ROLE_COLLABORATEUR
        List<SimpleGrantedAuthority> autorites = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getLibelle().name()))
                .toList();

        // objet "utilisateur" compris par Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())        // hash BCrypt
                .authorities(autorites)
                .disabled(!user.isActif())           // R8 : compte inactif -> connexion refusée
                .build();
    }
}
