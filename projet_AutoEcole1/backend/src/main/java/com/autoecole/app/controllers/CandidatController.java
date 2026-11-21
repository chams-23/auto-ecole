package com.autoecole.app.controllers;

import com.autoecole.app.dto.CandidatDTO;
import com.autoecole.app.dto.CompteurHeuresDTO;
import com.autoecole.app.dto.CreateCandidatRequest;
import com.autoecole.app.dto.SoldeCandidatDTO;
import com.autoecole.app.dto.StatutQuotaHeuresDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.ResultatExamen;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.services.CandidatService;
import com.autoecole.app.services.QuotaHeuresService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService candidatService;
    private final UtilisateurRepository utilisateurRepository;
    private final QuotaHeuresService quotaHeuresService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','SUPER_ADMIN')")
    public ResponseEntity<List<CandidatDTO>> lister() {
        Long tenantId = TenantContext.getTenantId();
        List<CandidatDTO> candidats = candidatService.lister(tenantId);
        return ResponseEntity.ok(candidats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','SUPER_ADMIN','CANDIDAT')")
    public ResponseEntity<CandidatDTO> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        verifierAccesCandidat(id, tenantId);
        CandidatDTO candidat = candidatService.getById(tenantId, id);
        return ResponseEntity.ok(candidat);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<CandidatDTO> creer(@Valid @RequestBody CreateCandidatRequest request) {
        Long tenantId = TenantContext.getTenantId();
        CandidatDTO cree = candidatService.creer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<CandidatDTO> modifier(
            @PathVariable Long id,
            @RequestBody CandidatDTO dto
    ) {
        Long tenantId = TenantContext.getTenantId();
        CandidatDTO misAJour = candidatService.modifier(tenantId, id, dto);
        return ResponseEntity.ok(misAJour);
    }

    @GetMapping("/{id}/compteur-heures")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','CANDIDAT','SUPER_ADMIN')")
    public ResponseEntity<CompteurHeuresDTO> compteurHeures(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        verifierAccesCandidat(id, tenantId);
        CompteurHeuresDTO compteur = candidatService.compteurHeures(tenantId, id);
        return ResponseEntity.ok(compteur);
    }

    @GetMapping("/{id}/solde")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','CANDIDAT','SUPER_ADMIN')")
    public ResponseEntity<SoldeCandidatDTO> solde(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        verifierAccesCandidat(id, tenantId);
        SoldeCandidatDTO solde = candidatService.solde(tenantId, id);
        return ResponseEntity.ok(solde);
    }

    @PatchMapping("/{id}/examen-theorie/planifier")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<CandidatDTO> planifierExamenTheorie(
            @PathVariable Long id,
            @RequestParam LocalDate date
    ) {
        Long tenantId = TenantContext.getTenantId();
        CandidatDTO misAJour = candidatService.planifierExamenTheorie(tenantId, id, date);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/examen-theorie/resultat")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<CandidatDTO> enregistrerResultatTheorie(
            @PathVariable Long id,
            @RequestParam ResultatExamen resultat
    ) {
        Long tenantId = TenantContext.getTenantId();
        CandidatDTO misAJour = candidatService.enregistrerResultatTheorie(tenantId, id, resultat);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/examen-pratique/planifier")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<CandidatDTO> planifierExamenPratique(
            @PathVariable Long id,
            @RequestParam LocalDate date
    ) {
        Long tenantId = TenantContext.getTenantId();
        CandidatDTO misAJour = candidatService.planifierExamenPratique(tenantId, id, date);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/{id}/examen-pratique/resultat")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<CandidatDTO> enregistrerResultatPratique(
            @PathVariable Long id,
            @RequestParam ResultatExamen resultat
    ) {
        Long tenantId = TenantContext.getTenantId();
        CandidatDTO misAJour = candidatService.enregistrerResultatPratique(tenantId, id, resultat);
        return ResponseEntity.ok(misAJour);
    }

    @GetMapping("/{id}/statut-quota")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','CANDIDAT','SUPER_ADMIN')")
    public ResponseEntity<StatutQuotaHeuresDTO> getStatutQuota(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        verifierAccesCandidat(id, tenantId);
        StatutQuotaHeuresDTO statut = quotaHeuresService.calculerStatutPourCandidat(tenantId, id);
        return ResponseEntity.ok(statut);
    }

    private void verifierAccesCandidat(Long candidatId, Long tenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new AccessDeniedException("Acces refuse");
        }

        boolean isAdminOrStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")
                        || a.getAuthority().equals("SUPER_ADMIN")
                        || a.getAuthority().equals("SECRETAIRE")
                        || a.getAuthority().equals("MONITEUR"));

        if (isAdminOrStaff) {
            return;
        }

        boolean isCandidatRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("CANDIDAT"));

        if (!isCandidatRole) {
            throw new AccessDeniedException("Acces refuse");
        }

        String email = auth.getName();
        Utilisateur connectedUser = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        Candidat connectedCandidat = candidatService.getCandidatEntityByUtilisateurId(connectedUser.getId());
        if (connectedCandidat == null || !connectedCandidat.getId().equals(candidatId)) {
            throw new AccessDeniedException("Acces refuse a ce candidat");
        }
    }
}
