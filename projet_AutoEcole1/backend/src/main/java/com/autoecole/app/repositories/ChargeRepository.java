package com.autoecole.app.repositories;

import com.autoecole.app.entites.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findAllByTenantId(Long tenantId);

    Optional<Charge> findByTenantIdAndId(Long tenantId, Long id);

    List<Charge> findAllByTenantIdAndCategorie(Long tenantId, com.autoecole.app.enums.CategorieCharge categorie);

    List<Charge> findAllByTenantIdAndDateBetween(Long tenantId, LocalDate dateDebut, LocalDate dateFin);

    List<Charge> findAllByTenantIdAndPeriodicite(Long tenantId, com.autoecole.app.enums.PeriodiciteCharge periodicite);
}
