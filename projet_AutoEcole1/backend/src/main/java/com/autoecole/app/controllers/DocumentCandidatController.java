package com.autoecole.app.controllers;

import com.autoecole.app.dto.DocumentCandidatDTO;
import com.autoecole.app.dto.UploadDocumentCandidatRequest;
import com.autoecole.app.dto.ValidationDocumentRequest;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.services.CandidatService;
import com.autoecole.app.services.DocumentCandidatService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentCandidatController {

    private final DocumentCandidatService documentCandidatService;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatService candidatService;

    @PostMapping("/api/candidats/{candidatId}/documents")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','CANDIDAT','SUPER_ADMIN')")
    public ResponseEntity<DocumentCandidatDTO> uploaderDocument(
            @PathVariable Long candidatId,
            @Valid @RequestBody UploadDocumentCandidatRequest request
    ) {
        Long tenantId = TenantContext.getTenantId();
        Long uploadedBy = getCurrentUtilisateurId(tenantId);
        DocumentCandidatDTO cree = documentCandidatService.uploaderDocument(tenantId, candidatId, request, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping("/api/candidats/{candidatId}/documents")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','CANDIDAT','SUPER_ADMIN')")
    public ResponseEntity<List<DocumentCandidatDTO>> listerParCandidat(@PathVariable Long candidatId) {
        Long tenantId = TenantContext.getTenantId();
        verifierAccesCandidat(candidatId, tenantId);
        List<DocumentCandidatDTO> documents = documentCandidatService.listerParCandidat(tenantId, candidatId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/api/documents/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','MONITEUR','CANDIDAT','SUPER_ADMIN')")
    public ResponseEntity<DocumentCandidatDTO> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        DocumentCandidatDTO document = documentCandidatService.getById(tenantId, id);
        verifierAccesCandidat(document.candidatId(), tenantId);
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/api/documents/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<DocumentCandidatDTO> validerDocument(
            @PathVariable Long id,
            @RequestBody(required = false) ValidationDocumentRequest request
    ) {
        Long tenantId = TenantContext.getTenantId();
        Long validePar = getCurrentUtilisateurId(tenantId);
        String commentaire = request != null ? request.commentaire() : null;
        DocumentCandidatDTO misAJour = documentCandidatService.validerDocument(tenantId, id, validePar, commentaire);
        return ResponseEntity.ok(misAJour);
    }

    @PatchMapping("/api/documents/{id}/refuser")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<DocumentCandidatDTO> refuserDocument(
            @PathVariable Long id,
            @RequestBody(required = false) ValidationDocumentRequest request
    ) {
        Long tenantId = TenantContext.getTenantId();
        Long validePar = getCurrentUtilisateurId(tenantId);
        String commentaire = request != null ? request.commentaire() : null;
        DocumentCandidatDTO misAJour = documentCandidatService.refuserDocument(tenantId, id, validePar, commentaire);
        return ResponseEntity.ok(misAJour);
    }

    @DeleteMapping("/api/documents/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        documentCandidatService.supprimer(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUtilisateurId(Long tenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Acces refuse");
        }
        String email = auth.getName();
        Utilisateur connectedUser = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
        return connectedUser.getId();
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
