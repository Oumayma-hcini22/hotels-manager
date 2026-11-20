package com.sip.hsm.services;

import com.sip.hsm.dto.ChangePasswordRequest;
import com.sip.hsm.dto.MessageResponse;
import com.sip.hsm.dto.ProfilRequest;
import com.sip.hsm.entities.ERole;
import com.sip.hsm.entities.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    // ----- gestion des utilisateurs (ADMIN) -----
    List<User> findAll();

    User findById(Long id);

    User create(User user);

    User update(Long id, User user);

    User activer(Long id, boolean actif);

    User changerRoles(Long id, Set<ERole> roles);

    void delete(Long id);

    // ----- mon profil (tout utilisateur connecté) -----
    User getProfil();

    User updateProfil(ProfilRequest request);

    MessageResponse changerMotDePasse(ChangePasswordRequest request);
}
