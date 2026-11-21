package com.autoecole.app.controllers;

import com.autoecole.app.dto.ChargeDTO;
import com.autoecole.app.dto.CreateChargeRequest;
import com.autoecole.app.services.ChargeService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charges")
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<List<ChargeDTO>> lister() {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(chargeService.lister(tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<ChargeDTO> creer(@Valid @RequestBody CreateChargeRequest request) {
        Long tenantId = TenantContext.getTenantId();
        ChargeDTO cree = chargeService.creer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }
}
