package com.autoecole.app.controllers;

import com.autoecole.app.dto.BalanceTresorerieDTO;
import com.autoecole.app.services.TresorerieService;
import com.autoecole.app.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tresorerie")
@RequiredArgsConstructor
public class TresorerieController {

    private final TresorerieService tresorerieService;

    @GetMapping("/balance")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<BalanceTresorerieDTO> getBalance(
            @RequestParam(required = false, defaultValue = "MOIS") String periode
    ) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(tresorerieService.calculerBalance(tenantId, periode));
    }
}
