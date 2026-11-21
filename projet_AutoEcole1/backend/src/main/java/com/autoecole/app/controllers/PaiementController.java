package com.autoecole.app.controllers;

import com.autoecole.app.dto.CreatePaiementRequest;
import com.autoecole.app.dto.PaiementDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.Role;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.services.PaiementService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatRepository candidatRepository;
    private final com.autoecole.app.services.RecuService recuService;

    /** Admin/Secrétaire : tous ; Candidat : son solde uniquement (matrice). */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN','CANDIDAT')")
    public ResponseEntity<List<PaiementDTO>> lister(
            @RequestParam(required = false) Long candidat_id
    ) {
        Long tenantId = TenantContext.getTenantId();
        Utilisateur user = getUtilisateurConnecte();
        Long candidatId = candidat_id;

        if (user.getRole() == Role.CANDIDAT) {
            Candidat c = candidatRepository.findByUtilisateurId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidat", "utilisateurId", user.getId()));
            candidatId = c.getId();
        }

        return ResponseEntity.ok(paiementService.lister(tenantId, candidatId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN')")
    public ResponseEntity<PaiementDTO> creer(@Valid @RequestBody CreatePaiementRequest request) {
        Long tenantId = TenantContext.getTenantId();
        PaiementDTO cree = paiementService.creer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping("/{id}/recu")
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN','CANDIDAT')")
    public ResponseEntity<Map<String, String>> getRecu(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        Utilisateur user = getUtilisateurConnecte();
        if (user.getRole() == Role.CANDIDAT) {
            Candidat c = candidatRepository.findByUtilisateurId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidat", "utilisateurId", user.getId()));
            PaiementDTO p = paiementService.getById(tenantId, id);
            if (!c.getId().equals(p.candidatId())) {
                throw new AccessDeniedException("Acces refuse a ce recu");
            }
        }
        String url = paiementService.getRecuUrl(tenantId, id);
        return ResponseEntity.ok(Map.of("recuUrl", url != null ? url : ""));
    }

    @GetMapping(path = "/{id}/recu.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETAIRE','SUPER_ADMIN','CANDIDAT')")
    public ResponseEntity<byte[]> getRecuPdf(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        Utilisateur user = getUtilisateurConnecte();

        if (user.getRole() == Role.CANDIDAT) {
            Candidat c = candidatRepository.findByUtilisateurId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidat", "utilisateurId", user.getId()));
            PaiementDTO p = paiementService.getById(tenantId, id);
            if (!c.getId().equals(p.candidatId())) {
                throw new AccessDeniedException("Acces refuse a ce recu");
            }
        }

        byte[] pdf = recuService.genererRecuPdf(tenantId, id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "recu-" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
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
