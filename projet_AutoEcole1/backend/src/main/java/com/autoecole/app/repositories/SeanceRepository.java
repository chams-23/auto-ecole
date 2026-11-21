package com.autoecole.app.repositories;

import com.autoecole.app.entites.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    List<Seance> findAllByTenantId(Long tenantId);

    Optional<Seance> findByTenantIdAndId(Long tenantId, Long id);

    List<Seance> findAllByTenantIdAndCandidatId(Long tenantId, Long candidatId);

    List<Seance> findAllByTenantIdAndIntervenantId(Long tenantId, Long intervenantId);

    List<Seance> findAllByTenantIdAndDate(Long tenantId, LocalDate date);

    List<Seance> findAllByTenantIdAndDateBetween(Long tenantId, LocalDate dateDebut, LocalDate dateFin);

    List<Seance> findAllByTenantIdAndStatutValidation(Long tenantId, com.autoecole.app.enums.StatutValidation statutValidation);
}
