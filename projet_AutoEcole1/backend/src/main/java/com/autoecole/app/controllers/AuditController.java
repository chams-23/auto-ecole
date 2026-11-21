package com.autoecole.app.controllers;

import com.autoecole.app.entites.AuditLog;
import com.autoecole.app.enums.ActionAudit;
import com.autoecole.app.services.AuditService;
import com.autoecole.app.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLog>> lister(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) ActionAudit action,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long tenant = tenantId != null ? tenantId : TenantContext.getTenantId();

        Page<AuditLog> result;

        if (action != null && utilisateurId != null) {
            result = auditService.listerParTenantActionEtUtilisateur(tenant, action, utilisateurId, page, size);
        } else if (action != null) {
            result = auditService.listerParTenantEtAction(tenant, action, page, size);
        } else if (utilisateurId != null) {
            result = auditService.listerParTenantEtUtilisateur(tenant, utilisateurId, page, size);
        } else {
            result = auditService.listerParTenant(tenant, page, size);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<AuditLog> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        AuditLog auditLog = auditService.getByIdEtTenant(id, tenantId);
        return ResponseEntity.ok(auditLog);
    }
}
