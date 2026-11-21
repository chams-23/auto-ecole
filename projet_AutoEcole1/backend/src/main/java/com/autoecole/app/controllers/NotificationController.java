package com.autoecole.app.controllers;

import com.autoecole.app.dto.NotificationDTO;
import com.autoecole.app.entites.Notification;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.NotificationRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
  @PreAuthorize("hasAnyAuthority('SECRETAIRE','MONITEUR','CANDIDAT','ADMIN','SUPER_ADMIN')")
  public ResponseEntity<List<NotificationDTO>> lister() {
        Long tenantId = TenantContext.getTenantId();
        Long userId = getUtilisateurConnecteId();

        List<Notification> notifs = notificationRepository
                .findAllByTenantIdAndDestinataireId(tenantId, userId);

        List<NotificationDTO> dtos = notifs.stream()
                .sorted(Comparator.comparing(Notification::getDate).reversed())
                .map(n -> new NotificationDTO(
                        n.getId(), n.getDestinataireId(), n.getTypeEvenement(),
                        n.getContenu(), n.getStatutLecture(), n.getDate()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/lue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> marquerLue(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = getUtilisateurConnecteId();

        Notification notif = notificationRepository
                .findByTenantIdAndIdAndDestinataireId(tenantId, id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notif.setStatutLecture(true);
        Notification sauvegardee = notificationRepository.save(notif);

        return ResponseEntity.ok(new NotificationDTO(
                sauvegardee.getId(), sauvegardee.getDestinataireId(),
                sauvegardee.getTypeEvenement(), sauvegardee.getContenu(),
                sauvegardee.getStatutLecture(), sauvegardee.getDate()
        ));
    }

    @PatchMapping("/tout-lire")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> marquerToutLue() {
        Long tenantId = TenantContext.getTenantId();
        Long userId = getUtilisateurConnecteId();

        List<Notification> nonLues = notificationRepository
                .findAllByTenantIdAndDestinataireIdAndStatutLecture(tenantId, userId, false);

        int marquees = 0;
        for (Notification n : nonLues) {
            n.setStatutLecture(true);
            notificationRepository.save(n);
            marquees++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("marquees", marquees);
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    private Long getUtilisateurConnecteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Utilisateur non authentifie");
        }
        String email = auth.getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
        return u.getId();
    }
}
