package com.autoecole.app.services;

import com.autoecole.app.entites.AuditLog;
import com.autoecole.app.enums.ActionAudit;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog enregistrer(
            ActionAudit action,
            String entiteType,
            Long entiteId,
            String description,
            String avant,
            String apres,
            Long tenantId,
            Long utilisateurId,
            String email,
            String role,
            String ip,
            String userAgent
    ) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entiteType(entiteType)
                .entiteId(entiteId)
                .description(description)
                .avant(avant)
                .apres(apres)
                .tenantId(tenantId)
                .utilisateurId(utilisateurId)
                .emailUtilisateur(email)
                .roleUtilisateur(role)
                .adresseIp(ip)
                .userAgent(userAgent)
                .build();

        return auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> listerParTenant(Long tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateAction"));
        return auditLogRepository.findAllByTenantId(tenantId, pageable);
    }

    public Page<AuditLog> listerParTenantEtAction(Long tenantId, ActionAudit action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateAction"));
        return auditLogRepository.findAllByTenantIdAndAction(tenantId, action, pageable);
    }

    public Page<AuditLog> listerParTenantEtUtilisateur(Long tenantId, Long utilisateurId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateAction"));
        return auditLogRepository.findAllByTenantIdAndUtilisateurId(tenantId, utilisateurId, pageable);
    }

    public Page<AuditLog> listerParTenantActionEtUtilisateur(Long tenantId, ActionAudit action, Long utilisateurId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateAction"));
        return auditLogRepository.findAllByTenantIdAndActionAndUtilisateurId(tenantId, action, utilisateurId, pageable);
    }

    public List<AuditLog> listerParUtilisateur(Long utilisateurId) {
        return auditLogRepository.findAllByUtilisateurId(utilisateurId);
    }

    public List<AuditLog> listerParAction(ActionAudit action) {
        return auditLogRepository.findAllByAction(action);
    }

    public AuditLog getById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
    }

    public AuditLog getByIdEtTenant(Long id, Long tenantId) {
        return auditLogRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
    }
}
