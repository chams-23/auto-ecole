package com.autoecole.app.repositories;

import com.autoecole.app.entites.RelanceImpaye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RelanceImpayeRepository extends JpaRepository<RelanceImpaye, Long> {

    List<RelanceImpaye> findAllByTenantId(Long tenantId);

    Optional<RelanceImpaye> findByTenantIdAndId(Long tenantId, Long id);

    List<RelanceImpaye> findAllByTenantIdAndCandidatId(Long tenantId, Long candidatId);

    List<RelanceImpaye> findAllByTenantIdAndPaiementId(Long tenantId, Long paiementId);

    List<RelanceImpaye> findAllByTenantIdAndStatutEnvoi(Long tenantId, String statutEnvoi);

    List<RelanceImpaye> findAllByTenantIdAndDateEnvoiBetween(Long tenantId, LocalDateTime dateDebut, LocalDateTime dateFin);
}
