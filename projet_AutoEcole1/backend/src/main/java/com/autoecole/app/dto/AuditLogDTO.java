package com.autoecole.app.dto;

import com.autoecole.app.enums.ActionAudit;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        Long tenantId,
        Long utilisateurId,
        String emailUtilisateur,
        String roleUtilisateur,
        ActionAudit action,
        String entiteType,
        Long entiteId,
        String description,
        String avant,
        String apres,
        String adresseIp,
        String userAgent,
        LocalDateTime dateAction
) {}
