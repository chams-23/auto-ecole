package com.autoecole.app.controllers;

import com.autoecole.app.dto.SauvegardeDTO;
import com.autoecole.app.entites.Sauvegarde;
import com.autoecole.app.services.SauvegardeService;
import com.autoecole.app.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sauvegardes")
@RequiredArgsConstructor
public class SauvegardeController {

    private final SauvegardeService sauvegardeService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SauvegardeDTO> creer(@RequestParam(defaultValue = "COMPLETE") String type) {
        Long tenantId = TenantContext.getTenantId();
        Sauvegarde s = sauvegardeService.creerSauvegarde(tenantId, type, "MANUEL");
        SauvegardeDTO dto = new SauvegardeDTO(
            s.getId(),
            s.getTenantId(),
            s.getTypeSauvegarde(),
            s.getNomFichier(),
            s.getCheminFichier(),
            s.getTailleOctets(),
            s.getStatut(),
            s.getDeclencheur(),
            s.getErreurMessage(),
            s.getDateDebut(),
            s.getDateFin()
        );
        return ResponseEntity.ok(dto);
    }
}
