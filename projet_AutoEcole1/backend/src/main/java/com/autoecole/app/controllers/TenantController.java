package com.autoecole.app.controllers;

import com.autoecole.app.enums.PlanAbonnement;
import com.autoecole.app.enums.Role;
import com.autoecole.app.security.JwtService;
import com.autoecole.app.services.TenantService;
import com.autoecole.app.tenant.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final JwtService jwtService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<Tenant>> listerTous() {
        List<Tenant> tenants = tenantService.listerTous();
        return ResponseEntity.ok(tenants);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Tenant> creer(@RequestBody Tenant tenant) {
        Tenant cree = tenantService.creer(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getById(@PathVariable Long id, HttpServletRequest request) {
        verifierAccesTenant(id, request);
        Tenant tenant = tenantService.getById(id);
        return ResponseEntity.ok(tenant);
    }

    @PatchMapping("/{id}/statut-abonnement")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Tenant> modifierStatutAbonnement(
            @PathVariable Long id,
            @RequestParam Tenant.StatutAbonnement statut
    ) {
        Tenant misAJour = tenantService.modifierStatutAbonnement(id, statut);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Tenant> activerAbonnement(@PathVariable Long id) {
        Tenant misAJour = tenantService.activerAbonnement(id);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/suspendre")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Tenant> suspendreAbonnement(
            @PathVariable Long id,
            @RequestParam LocalDate dateSuspension
    ) {
        Tenant misAJour = tenantService.suspendreAbonnement(id, dateSuspension);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/resilier")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Tenant> resilierAbonnement(
            @PathVariable Long id,
            @RequestParam LocalDate dateResiliation
    ) {
        Tenant misAJour = tenantService.resilierAbonnement(id, dateResiliation);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/plan")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Tenant> modifierPlanAbonnement(
            @PathVariable Long id,
            @RequestParam PlanAbonnement plan,
            @RequestParam LocalDate debut,
            @RequestParam LocalDate fin,
            @RequestParam BigDecimal montant
    ) {
        Tenant misAJour = tenantService.modifierPlanAbonnement(id, plan, debut, fin, montant);
        return ResponseEntity.ok(misAJour);
    }

    private void verifierAccesTenant(Long tenantId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Acces refuse");
        }
        String token = authHeader.substring(7);
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Long tokenTenantId = jwtService.extractClaim(token, claims -> claims.get("tenant_id", Long.class));

        if (Role.SUPER_ADMIN.name().equals(role)) {
            return;
        }

        if (!Role.ADMIN.name().equals(role)) {
            throw new AccessDeniedException("Acces refuse");
        }

        if (!tenantId.equals(tokenTenantId)) {
            throw new AccessDeniedException("Acces refuse a ce tenant");
        }
    }
}
