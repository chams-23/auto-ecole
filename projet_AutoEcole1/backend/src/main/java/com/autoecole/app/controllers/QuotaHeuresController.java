package com.autoecole.app.controllers;

import com.autoecole.app.dto.QuotaHeuresDTO;
import com.autoecole.app.dto.UpsertQuotaHeuresRequest;
import com.autoecole.app.services.QuotaHeuresService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotas-heures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
public class QuotaHeuresController {

    private final QuotaHeuresService quotaHeuresService;

    @GetMapping
    public ResponseEntity<List<QuotaHeuresDTO>> listerParTenant() {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(quotaHeuresService.listerParTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotaHeuresDTO> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(quotaHeuresService.getById(tenantId, id));
    }

    @PostMapping
    public ResponseEntity<QuotaHeuresDTO> creer(@Valid @RequestBody UpsertQuotaHeuresRequest req) {
        Long tenantId = TenantContext.getTenantId();
        QuotaHeuresDTO cree = quotaHeuresService.creer(tenantId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotaHeuresDTO> modifier(@PathVariable Long id,
                                                   @Valid @RequestBody UpsertQuotaHeuresRequest req) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(quotaHeuresService.modifier(tenantId, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        quotaHeuresService.supprimer(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
