package com.sip.hsm.services;

import com.sip.hsm.dto.ChangePasswordRequest;
import com.sip.hsm.dto.MessageResponse;
import com.sip.hsm.dto.ProfilRequest;
import com.sip.hsm.entities.ERole;
import com.sip.hsm.entities.Role;
import com.sip.hsm.entities.User;
import com.sip.hsm.exceptions.BusinessException;
import com.sip.hsm.exceptions.ResourceNotFoundException;
import com.sip.hsm.repositories.RoleRepository;
import com.sip.hsm.repositories.UserRepository;
import com.sip.hsm.security.SecurityUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    // ================= GESTION DES UTILISATEURS (ADMIN) =================

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll(Sort.by("nom").ascending());
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id " + id));
    }

    @Override
    public User create(User user) {
        user.setId(null);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé : " + user.getEmail());
        }
        verifierMotDePasse(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));   // BCrypt

        Set<ERole> libelles = new HashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(r -> libelles.add(r.getLibelle()));
        }
        if (libelles.isEmpty()) {
            libelles.add(ERole.COLLABORATEUR);
        }
        user.setRoles(resoudreRoles(libelles));
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User data) {
        User user = findById(id);
        if (!user.getEmail().equalsIgnoreCase(data.getEmail())
                && userRepository.existsByEmail(data.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé : " + data.getEmail());
        }
        user.setNom(data.getNom());
        user.setPrenom(data.getPrenom());
        user.setEmail(data.getEmail());
        user.setTel(data.getTel());
        return userRepository.save(user);
    }

    @Override
    public User activer(Long id, boolean actif) {
        User user = findById(id);
        user.setActif(actif);
        return userRepository.save(user);
    }

    @Override
    public User changerRoles(Long id, Set<ERole> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException("Un utilisateur doit avoir au moins un rôle");
        }
        User user = findById(id);
        user.setRoles(resoudreRoles(roles));
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        // sécurité : on ne peut pas supprimer son propre compte
        User moi = securityUtils.getUtilisateurConnecte();
        if (moi != null && moi.getId().equals(id)) {
            throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte");
        }
        userRepository.delete(user);
    }

    // ================= MON PROFIL (TOUT UTILISATEUR CONNECTÉ) =================

    @Override
    @Transactional(readOnly = true)
    public User getProfil() {
        return utilisateurConnecte();
    }

    @Override
    public User updateProfil(ProfilRequest request) {
        User moi = utilisateurConnecte();
        moi.setNom(request.nom());
        moi.setPrenom(request.prenom());
        moi.setTel(request.tel());
        return userRepository.save(moi);
    }

    @Override
    public MessageResponse changerMotDePasse(ChangePasswordRequest request) {
        User moi = utilisateurConnecte();
        // l'ancien mot de passe doit être correct (comparaison avec le hash BCrypt)
        if (!passwordEncoder.matches(request.ancienMotDePasse(), moi.getPassword())) {
            throw new BusinessException("L'ancien mot de passe est incorrect");
        }
        moi.setPassword(passwordEncoder.encode(request.nouveauMotDePasse()));
        userRepository.save(moi);
        return new MessageResponse("Mot de passe modifié avec succès");
    }

    // ================= OUTILS =================

    private User utilisateurConnecte() {
        User moi = securityUtils.getUtilisateurConnecte();
        if (moi == null) {
            throw new ResourceNotFoundException("Utilisateur connecté introuvable");
        }
        return moi;
    }

    private Set<Role> resoudreRoles(Set<ERole> libelles) {
        Set<Role> roles = new HashSet<>();
        for (ERole libelle : libelles) {
            roles.add(roleRepository.findByLibelle(libelle)
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + libelle)));
        }
        return roles;
    }

    private void verifierMotDePasse(String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException("Le mot de passe doit contenir au moins 6 caractères");
        }
    }
}