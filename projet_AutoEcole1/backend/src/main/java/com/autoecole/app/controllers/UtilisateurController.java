package com.autoecole.app.controllers;

import com.autoecole.app.dto.CreateUtilisateurRequest;
import com.autoecole.app.dto.UtilisateurDTO;
import com.autoecole.app.services.UtilisateurService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','SECRETAIRE')")
    public ResponseEntity<List<UtilisateurDTO>> lister() {
        Long tenantId = TenantContext.getTenantId();
        List<UtilisateurDTO> utilisateurs = utilisateurService.lister(tenantId);
        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','SECRETAIRE')")
    public ResponseEntity<UtilisateurDTO> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        UtilisateurDTO utilisateur = utilisateurService.getById(tenantId, id);
        return ResponseEntity.ok(utilisateur);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UtilisateurDTO> creer(@Valid @RequestBody CreateUtilisateurRequest request) {
        Long tenantId = TenantContext.getTenantId();
        UtilisateurDTO cree = utilisateurService.creer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UtilisateurDTO> modifier(
            @PathVariable Long id,
            @RequestBody UtilisateurDTO dto
    ) {
        Long tenantId = TenantContext.getTenantId();
        UtilisateurDTO misAJour = utilisateurService.modifier(tenantId, id, dto);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        utilisateurService.desactiver(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
