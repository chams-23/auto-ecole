package com.autoecole.app.repositories;

import com.autoecole.app.entites.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findAllByTenantId(Long tenantId);

    Optional<Vehicule> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    List<Vehicule> findAllByTenantIdAndCategoriePermis(Long tenantId, com.autoecole.app.enums.CategoriePermis categoriePermis);
}
