package com.sip.hsm.repositories;

import com.sip.hsm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // login (Phase 3)
    Optional<User> findByEmail(String email);

    // R9 : email unique
    boolean existsByEmail(String email);

    // Dashboard / page Utilisateurs : nombre de comptes en attente d'activation
    long countByActifFalse();
}