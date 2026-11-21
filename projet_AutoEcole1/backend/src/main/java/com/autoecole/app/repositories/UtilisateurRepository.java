package com.autoecole.app.repositories;

import com.autoecole.app.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    List<Utilisateur> findAllByTenantId(Long tenantId);

    Optional<Utilisateur> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findAllByTenantIdAndRole(Long tenantId, com.autoecole.app.enums.Role role);

    boolean existsByEmail(String email);
}
