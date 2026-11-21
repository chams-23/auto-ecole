package com.autoecole.app.services;

import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Notification;
import com.autoecole.app.entites.Seance;
import com.autoecole.app.enums.TypeEvenementNotification;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CandidatRepository candidatRepository;

    public void creerNotificationSeance(Long tenantId, Seance seance, String typeEvenement) {
        TypeEvenementNotification type;
        try {
            type = TypeEvenementNotification.valueOf(typeEvenement);
        } catch (IllegalArgumentException e) {
            type = TypeEvenementNotification.NOUVELLE_SEANCE;
        }

        String contenu = switch (type) {
            case NOUVELLE_SEANCE -> "Nouvelle seance programmee le " + seance.getDate()
                    + " a " + seance.getHeureDebut() + " (" + seance.getType() + ")";
            case SEANCE_REPROGRAMMEE -> "Seance reprogrammee: " + seance.getDate()
                    + " a " + seance.getHeureDebut() + " (" + seance.getType() + ")";
            case SEANCE_DECLAREE -> "Seance declaree effectuee - merci de confirmer (double validation)";
            case SEANCE_VALIDEE -> "Seance validee et verrouillee avec succes";
            case SEANCE_CONTESTEE -> "Seance contestee - intervention requise";
            default -> "Mise a jour seance #" + seance.getId();
        };

        Long candidatUtilisateurId = candidatRepository.findById(seance.getCandidatId())
                .map(Candidat::getUtilisateurId)
                .orElse(null);
        if (candidatUtilisateurId != null) {
            creerNotification(tenantId, candidatUtilisateurId, type, contenu);
        }
        if (!seance.getIntervenantId().equals(candidatUtilisateurId)) {
            creerNotification(tenantId, seance.getIntervenantId(), type, contenu);
        }
    }

    public void creerNotification(Long tenantId, Long destinataireId, TypeEvenementNotification type, String contenu) {
        Notification notif = Notification.builder()
                .tenantId(tenantId)
                .destinataireId(destinataireId)
                .typeEvenement(type)
                .contenu(contenu)
                .statutLecture(false)
                .date(LocalDateTime.now())
                .build();
        notificationRepository.save(notif);
    }
}
