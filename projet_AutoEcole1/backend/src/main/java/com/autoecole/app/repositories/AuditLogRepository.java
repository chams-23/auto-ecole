package com.autoecole.app.repositories;

import com.autoecole.app.entites.AuditLog;
import com.autoecole.app.enums.ActionAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<AuditLog> findAllByTenantIdAndAction(Long tenantId, ActionAudit action, Pageable pageable);

    Page<AuditLog> findAllByTenantIdAndUtilisateurId(Long tenantId, Long utilisateurId, Pageable pageable);

    Page<AuditLog> findAllByTenantIdAndActionAndUtilisateurId(Long tenantId, ActionAudit action, Long utilisateurId, Pageable pageable);

    List<AuditLog> findAllByTenantId(Long tenantId);

    Optional<AuditLog> findByTenantIdAndId(Long tenantId, Long id);

    List<AuditLog> findAllByTenantIdAndUtilisateurId(Long tenantId, Long utilisateurId);

    List<AuditLog> findAllByUtilisateurId(Long utilisateurId);

    List<AuditLog> findAllByAction(ActionAudit action);

    List<AuditLog> findAllByTenantIdAndAction(Long tenantId, ActionAudit action);

    List<AuditLog> findAllByTenantIdAndEntiteType(Long tenantId, String entiteType);

    List<AuditLog> findAllByTenantIdAndDateActionBetween(Long tenantId, LocalDateTime dateDebut, LocalDateTime dateFin);
}
