package com.autoecole.app.repositories;

import com.autoecole.app.entites.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findAllByTenantId(Long tenantId);

    Optional<Paiement> findByTenantIdAndId(Long tenantId, Long id);

    List<Paiement> findAllByTenantIdAndCandidatId(Long tenantId, Long candidatId);

    List<Paiement> findAllByTenantIdAndDateBetween(Long tenantId, LocalDate dateDebut, LocalDate dateFin);

    List<Paiement> findAllByTenantIdAndType(Long tenantId, com.autoecole.app.enums.TypePaiement type);
}
