package com.autoecole.app.controllers;

import com.autoecole.app.dto.CreateSeanceRequest;
import com.autoecole.app.dto.DeclarerEffectueeRequest;
import com.autoecole.app.dto.ReprogrammerSeanceRequest;
import com.autoecole.app.dto.SeanceDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.Role;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.services.SeanceService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
public class SeanceController {

    private final SeanceService seanceService;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatRepository candidatRepository;

    /**
     * Planning : Admin/Secrétaire voient tout ;
     * Moniteur / Candidat = planning personnel uniquement (matrice CDC).
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','SUPER_ADMIN','CANDIDAT')")
    public ResponseEntity<List<SeanceDTO>> filtrer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long moniteur_id,
            @RequestParam(required = false) Long candidat_id
    ) {
        Long tenantId = TenantContext.getTenantId();
        Utilisateur user = getUtilisateurConnecte();

        Long moniteurId = moniteur_id;
        Long candidatId = candidat_id;

        if (user.getRole() == Role.MONITEUR) {
            moniteurId = user.getId();
            candidatId = null;
        } else if (user.getRole() == Role.CANDIDAT) {
            Candidat c = candidatRepository.findByUtilisateurId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidat", "utilisateurId", user.getId()));
            candidatId = c.getId();
            moniteurId = null;
        }

        return ResponseEntity.ok(seanceService.filtrer(tenantId, date, moniteurId, candidatId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<SeanceDTO> creer(@Valid @RequestBody CreateSeanceRequest request) {
        Long tenantId = TenantContext.getTenantId();
        SeanceDTO cree = seanceService.creer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PatchMapping("/{id}/reprogrammer")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<SeanceDTO> reprogrammer(
            @PathVariable Long id,
            @RequestBody ReprogrammerSeanceRequest request
    ) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(seanceService.reprogrammer(tenantId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        seanceService.supprimer(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    /** Moniteur = conduite ; Secrétaire = théorie (matrice + double validation). */
    @PostMapping("/{id}/declarer-effectuee")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','SUPER_ADMIN')")
    public ResponseEntity<SeanceDTO> declarerEffectuee(
            @PathVariable Long id,
            @RequestBody(required = false) DeclarerEffectueeRequest request
    ) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = getUtilisateurConnecte().getId();
        DeclarerEffectueeRequest req = request != null ? request : new DeclarerEffectueeRequest(null, null);
        return ResponseEntity.ok(seanceService.declarerEffectuee(tenantId, id, req, userId));
    }

    /** Candidat confirme ; Admin/Secrétaire arbitrent un litige. */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN','CANDIDAT')")
    public ResponseEntity<SeanceDTO> valider(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = getUtilisateurConnecte().getId();
        return ResponseEntity.ok(seanceService.valider(tenantId, id, userId));
    }

    @PostMapping("/{id}/contester")
    @PreAuthorize("hasAuthority('CANDIDAT')")
    public ResponseEntity<SeanceDTO> contester(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = getUtilisateurConnecte().getId();
        String observations = body != null ? body.get("observations") : null;
        return ResponseEntity.ok(seanceService.contester(tenantId, id, userId, observations));
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
