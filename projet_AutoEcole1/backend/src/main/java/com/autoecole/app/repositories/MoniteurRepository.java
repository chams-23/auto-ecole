package com.autoecole.app.repositories;

import com.autoecole.app.entites.Moniteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoniteurRepository extends JpaRepository<Moniteur, Long> {

    List<Moniteur> findAllByTenantId(Long tenantId);

    Optional<Moniteur> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Moniteur> findByUtilisateurId(Long utilisateurId);

    Optional<Moniteur> findByNumeroCartePro(String numeroCartePro);

    Optional<Moniteur> findByVehiculeId(Long vehiculeId);
}
