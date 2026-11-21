package com.autoecole.app.repositories;

import com.autoecole.app.entites.Sauvegarde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SauvegardeRepository extends JpaRepository<Sauvegarde, Long> {

    List<Sauvegarde> findAllByTenantId(Long tenantId);

    Optional<Sauvegarde> findByTenantIdAndId(Long tenantId, Long id);

    List<Sauvegarde> findAllByTenantIdAndTypeSauvegarde(Long tenantId, String typeSauvegarde);

    List<Sauvegarde> findAllByTenantIdAndStatut(Long tenantId, String statut);

    List<Sauvegarde> findAllByTenantIdAndDateDebutBetween(Long tenantId, LocalDateTime dateDebut, LocalDateTime dateFin);

    Optional<Sauvegarde> findFirstByTenantIdAndStatutOrderByDateDebutDesc(Long tenantId, String statut);
}
