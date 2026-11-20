package com.sip.hsm.repositories;
import com.sip.hsm.entities.ERole;

import com.sip.hsm.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Role, Long> : entité Role, clé primaire de type Long
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Spring génère la requête à partir du nom de la méthode :
    // SELECT * FROM roles WHERE libelle = ?
    Optional<Role> findByLibelle(ERole libelle);
}
