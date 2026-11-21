package com.autoecole.app.controllers;

import com.autoecole.app.dto.CreateVehiculeRequest;
import com.autoecole.app.dto.VehiculeAlerteDTO;
import com.autoecole.app.dto.VehiculeDTO;
import com.autoecole.app.entites.Moniteur;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.Role;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.MoniteurRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.services.VehiculeService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;
    private final UtilisateurRepository utilisateurRepository;
    private final MoniteurRepository moniteurRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','MONITEUR','SUPER_ADMIN')")
    public ResponseEntity<List<VehiculeDTO>> lister() {
        Long tenantId = TenantContext.getTenantId();
        Utilisateur user = getUtilisateurConnecte();

        if (user.getRole() == Role.MONITEUR) {
            Moniteur moniteur = moniteurRepository.findByUtilisateurId(user.getId()).orElse(null);
            if (moniteur == null || moniteur.getVehiculeId() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(List.of(vehiculeService.getById(tenantId, moniteur.getVehiculeId())));
        }

        return ResponseEntity.ok(vehiculeService.lister(tenantId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MONITEUR','SUPER_ADMIN')")
    public ResponseEntity<VehiculeDTO> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        Utilisateur user = getUtilisateurConnecte();
        if (user.getRole() == Role.MONITEUR) {
            Moniteur moniteur = moniteurRepository.findByUtilisateurId(user.getId())
                    .orElseThrow(() -> new AccessDeniedException("Aucun profil moniteur"));
            if (moniteur.getVehiculeId() == null || !moniteur.getVehiculeId().equals(id)) {
                throw new AccessDeniedException("Acces refuse a ce vehicule");
            }
        }
        return ResponseEntity.ok(vehiculeService.getById(tenantId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<VehiculeDTO> creer(@Valid @RequestBody CreateVehiculeRequest request) {
        Long tenantId = TenantContext.getTenantId();
        VehiculeDTO cree = vehiculeService.creer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<VehiculeDTO> modifier(
            @PathVariable Long id,
            @RequestBody VehiculeDTO dto
    ) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(vehiculeService.modifier(tenantId, id, dto));
    }

    @PatchMapping("/{id}/attribuer-moniteur")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<VehiculeDTO> attribuerMoniteur(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body
    ) {
        Long tenantId = TenantContext.getTenantId();
        Long moniteurId = body.get("moniteurId");
        return ResponseEntity.ok(vehiculeService.attribuerMoniteur(tenantId, id, moniteurId));
    }

    @GetMapping("/alertes")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<VehiculeAlerteDTO>> getAlertes() {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(vehiculeService.getAlertes(tenantId));
    }

    private Utilisateur getUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Utilisateur non authentifie");
        }
        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", auth.getName()));
    }
}
