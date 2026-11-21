package com.autoecole.app.services;

import com.autoecole.app.dto.CreateSeanceRequest;
import com.autoecole.app.dto.DeclarerEffectueeRequest;
import com.autoecole.app.dto.ReprogrammerSeanceRequest;
import com.autoecole.app.dto.SeanceDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Seance;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.enums.TypeSeance;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.SeanceRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final CandidatRepository candidatRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    public List<SeanceDTO> filtrer(Long tenantId, LocalDate date, Long moniteurId, Long candidatId) {
        List<Seance> seances;
        if (date != null && moniteurId != null && candidatId != null) {
            seances = seanceRepository.findAllByTenantIdAndDate(tenantId, date).stream()
                    .filter(s -> s.getIntervenantId().equals(moniteurId) && s.getCandidatId().equals(candidatId))
                    .collect(Collectors.toList());
        } else if (date != null && moniteurId != null) {
            seances = seanceRepository.findAllByTenantIdAndDate(tenantId, date).stream()
                    .filter(s -> s.getIntervenantId().equals(moniteurId))
                    .collect(Collectors.toList());
        } else if (date != null && candidatId != null) {
            seances = seanceRepository.findAllByTenantIdAndDate(tenantId, date).stream()
                    .filter(s -> s.getCandidatId().equals(candidatId))
                    .collect(Collectors.toList());
        } else if (date != null) {
            seances = seanceRepository.findAllByTenantIdAndDate(tenantId, date);
        } else if (moniteurId != null) {
            seances = seanceRepository.findAllByTenantIdAndIntervenantId(tenantId, moniteurId);
        } else if (candidatId != null) {
            seances = seanceRepository.findAllByTenantIdAndCandidatId(tenantId, candidatId);
        } else {
            LocalDate debutSemaine = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate finSemaine = debutSemaine.plusDays(6);
            seances = seanceRepository.findAllByTenantIdAndDateBetween(tenantId, debutSemaine, finSemaine);
        }
        return seances.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public SeanceDTO getById(Long tenantId, Long id) {
        Seance seance = seanceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance", "id", id));
        return mapToDTO(seance);
    }

    @Transactional
    public SeanceDTO creer(CreateSeanceRequest request, Long tenantId) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, request.candidatId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", request.candidatId()));
        Utilisateur intervenant = utilisateurRepository.findByTenantIdAndId(tenantId, request.intervenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Intervenant", "id", request.intervenantId()));

        if (request.heureFin().isBefore(request.heureDebut()) || request.heureFin().equals(request.heureDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit etre apres l'heure de debut");
        }

        verifierConflitHoraire(tenantId, request.intervenantId(), request.date(), request.heureDebut(), request.heureFin(), null);

        Seance seance = Seance.builder()
                .tenantId(tenantId)
                .type(request.type())
                .date(request.date())
                .heureDebut(request.heureDebut())
                .heureFin(request.heureFin())
                .candidatId(request.candidatId())
                .intervenantId(request.intervenantId())
                .statutValidation(StatutValidation.EN_ATTENTE)
                .kilometrage(request.kilometrage())
                .observations(request.observations())
                .build();

        Seance sauvegardee = seanceRepository.save(seance);

        notificationService.creerNotificationSeance(tenantId, sauvegardee, "NOUVELLE_SEANCE");

        return mapToDTO(sauvegardee);
    }

    @Transactional
    public SeanceDTO reprogrammer(Long tenantId, Long id, ReprogrammerSeanceRequest request) {
        Seance seance = seanceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance", "id", id));

        if (seance.getStatutValidation() == StatutValidation.VALIDEE
                || seance.getStatutValidation() == StatutValidation.VERROUILLEE) {
            throw new IllegalStateException("Impossible de reprogrammer une seance validee ou verrouillee");
        }

        if (request.date() != null) {
            seance.setDate(request.date());
        }
        if (request.heureDebut() != null) {
            seance.setHeureDebut(request.heureDebut());
        }
        if (request.heureFin() != null) {
            seance.setHeureFin(request.heureFin());
        }
        if (request.type() != null) {
            seance.setType(request.type());
        }
        if (request.intervenantId() != null) {
            if (!utilisateurRepository.existsById(request.intervenantId())) {
                throw new ResourceNotFoundException("Intervenant", "id", request.intervenantId());
            }
            seance.setIntervenantId(request.intervenantId());
        }

        if (seance.getHeureFin().isBefore(seance.getHeureDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit etre apres l'heure de debut");
        }

        verifierConflitHoraire(tenantId, seance.getIntervenantId(), seance.getDate(),
                seance.getHeureDebut(), seance.getHeureFin(), id);

        seance.setStatutValidation(StatutValidation.EN_ATTENTE);

        Seance sauvegardee = seanceRepository.save(seance);
        notificationService.creerNotificationSeance(tenantId, sauvegardee, "SEANCE_REPROGRAMMEE");
        return mapToDTO(sauvegardee);
    }

    @Transactional
    public void supprimer(Long tenantId, Long id) {
        Seance seance = seanceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance", "id", id));

        if (seance.getStatutValidation() == StatutValidation.VALIDEE
                || seance.getStatutValidation() == StatutValidation.VERROUILLEE) {
            throw new IllegalStateException("Impossible de supprimer une seance validee ou verrouillee");
        }

        seanceRepository.delete(seance);
    }

    @Transactional
    public SeanceDTO declarerEffectuee(Long tenantId, Long id, DeclarerEffectueeRequest request, Long utilisateurConnecteId) {
        Seance seance = seanceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance", "id", id));

        if (seance.getStatutValidation() != StatutValidation.EN_ATTENTE) {
            throw new IllegalStateException("Seance deja traitee (statut: " + seance.getStatutValidation() + ")");
        }

        Utilisateur declarant = utilisateurRepository.findById(utilisateurConnecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", utilisateurConnecteId));
        verifierDroitDeclaration(seance, declarant);

        seance.setStatutValidation(StatutValidation.DECLAREE_EFFECTUEE);
        if (request.kilometrage() != null) {
            seance.setKilometrage(request.kilometrage());
        }
        if (request.observations() != null) {
            seance.setObservations(request.observations());
        }

        Seance sauvegardee = seanceRepository.save(seance);
        notificationService.creerNotificationSeance(tenantId, sauvegardee, "SEANCE_DECLAREE");
        return mapToDTO(sauvegardee);
    }

    /**
     * Double validation CDC §3.6 :
     * - DECLAREE_EFFECTUEE → confirmation par le candidat concerné
     * - CONTESTEE → arbitrage Admin / Secrétaire
     * Une fois validée, la séance est verrouillée et les compteurs d'heures sont mis à jour.
     */
    @Transactional
    public SeanceDTO valider(Long tenantId, Long id, Long utilisateurConnecteId) {
        Seance seance = seanceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance", "id", id));

        Utilisateur validateur = utilisateurRepository.findById(utilisateurConnecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", utilisateurConnecteId));

        if (seance.getStatutValidation() == StatutValidation.DECLAREE_EFFECTUEE) {
            if (!estCandidatDeLaSeance(seance, validateur)) {
                throw new IllegalStateException(
                        "Seul le candidat concerne peut confirmer la seance (double validation)");
            }
        } else if (seance.getStatutValidation() == StatutValidation.CONTESTEE) {
            if (validateur.getRole() != Role.ADMIN
                    && validateur.getRole() != Role.SUPER_ADMIN
                    && validateur.getRole() != Role.SECRETAIRE) {
                throw new IllegalStateException(
                        "Seul un administrateur ou une secretaire peut arbitrer une seance contestee");
            }
        } else {
            throw new IllegalStateException("La seance doit etre declaree effectuee ou contestee avant validation");
        }

        seance.setStatutValidation(StatutValidation.VERROUILLEE);
        mettreAJourCompteurHeures(tenantId, seance);

        Seance sauvegardee = seanceRepository.save(seance);
        notificationService.creerNotificationSeance(tenantId, sauvegardee, "SEANCE_VALIDEE");
        return mapToDTO(sauvegardee);
    }

    @Transactional
    public SeanceDTO contester(Long tenantId, Long id, Long utilisateurConnecteId, String observations) {
        Seance seance = seanceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance", "id", id));

        if (seance.getStatutValidation() != StatutValidation.DECLAREE_EFFECTUEE) {
            throw new IllegalStateException("Seule une seance declaree effectuee peut etre contestee");
        }

        Utilisateur contestataire = utilisateurRepository.findById(utilisateurConnecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", utilisateurConnecteId));
        if (!estCandidatDeLaSeance(seance, contestataire)) {
            throw new IllegalStateException("Seul le candidat concerne peut contester la seance");
        }

        seance.setStatutValidation(StatutValidation.CONTESTEE);
        if (observations != null && !observations.isBlank()) {
            String obsExistantes = seance.getObservations() != null ? seance.getObservations() + " | " : "";
            seance.setObservations(obsExistantes + "[CONTESTATION] " + observations);
        }

        Seance sauvegardee = seanceRepository.save(seance);
        notificationService.creerNotificationSeance(tenantId, sauvegardee, "SEANCE_CONTESTEE");
        return mapToDTO(sauvegardee);
    }

    private void verifierDroitDeclaration(Seance seance, Utilisateur declarant) {
        Role role = declarant.getRole();
        if (role == Role.ADMIN || role == Role.SUPER_ADMIN) {
            return;
        }
        if (seance.getType() == TypeSeance.CONDUITE) {
            if (role != Role.MONITEUR || !seance.getIntervenantId().equals(declarant.getId())) {
                throw new IllegalStateException(
                        "Seul le moniteur intervenant (ou un admin) peut declarer une seance de conduite");
            }
            return;
        }
        if (seance.getType() == TypeSeance.THEORIE) {
            if (role != Role.SECRETAIRE) {
                throw new IllegalStateException(
                        "Seule la secretaire (ou un admin) peut declarer une seance de theorie");
            }
            return;
        }
        throw new IllegalStateException("Declaration non autorisee pour ce role");
    }

    private boolean estCandidatDeLaSeance(Seance seance, Utilisateur utilisateur) {
        if (utilisateur.getRole() != Role.CANDIDAT) {
            return false;
        }
        return candidatRepository.findByUtilisateurId(utilisateur.getId())
                .map(c -> c.getId().equals(seance.getCandidatId()))
                .orElse(false);
    }

    private void mettreAJourCompteurHeures(Long tenantId, Seance seance) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, seance.getCandidatId()).orElse(null);
        if (candidat == null) return;

        long minutes = Duration.between(seance.getHeureDebut(), seance.getHeureFin()).toMinutes();
        int heures = (int) Math.round(minutes / 60.0);

        switch (seance.getType()) {
            case CONDUITE -> {
                int actuelles = candidat.getHeuresConduiteValidees() != null ? candidat.getHeuresConduiteValidees() : 0;
                candidat.setHeuresConduiteValidees(actuelles + heures);
            }
            case THEORIE -> {
                int actuelles = candidat.getHeuresTheorieValidees() != null ? candidat.getHeuresTheorieValidees() : 0;
                candidat.setHeuresTheorieValidees(actuelles + heures);
            }
        }
        candidatRepository.save(candidat);
    }

    private void verifierConflitHoraire(Long tenantId, Long intervenantId, LocalDate date,
                                        LocalTime debut, LocalTime fin, Long seanceIdExclue) {
        List<Seance> existantes = seanceRepository.findAllByTenantIdAndIntervenantId(tenantId, intervenantId).stream()
                .filter(s -> s.getDate().equals(date))
                .filter(s -> seanceIdExclue == null || !s.getId().equals(seanceIdExclue))
                .filter(s -> s.getStatutValidation() != StatutValidation.CONTESTEE)
                .toList();

        for (Seance s : existantes) {
            if (debut.isBefore(s.getHeureFin()) && fin.isAfter(s.getHeureDebut())) {
                throw new IllegalArgumentException(
                        "Conflit horaire avec la seance #" + s.getId() + " (" + s.getHeureDebut() + "-" + s.getHeureFin() + ")"
                );
            }
        }
    }

    private SeanceDTO mapToDTO(Seance s) {
        String candNom = null, candPrenom = null;
        Candidat c = candidatRepository.findById(s.getCandidatId()).orElse(null);
        if (c != null) {
            Utilisateur cu = utilisateurRepository.findById(c.getUtilisateurId()).orElse(null);
            if (cu != null) {
                candNom = cu.getNom();
                candPrenom = cu.getPrenom();
            }
        }

        String intNom = null, intPrenom = null;
        Utilisateur iu = utilisateurRepository.findById(s.getIntervenantId()).orElse(null);
        if (iu != null) {
            intNom = iu.getNom();
            intPrenom = iu.getPrenom();
        }

        return new SeanceDTO(
                s.getId(), s.getTenantId(), s.getType(), s.getDate(),
                s.getHeureDebut(), s.getHeureFin(),
                s.getCandidatId(), candNom, candPrenom,
                s.getIntervenantId(), intNom, intPrenom,
                s.getStatutValidation(), s.getKilometrage(), s.getObservations()
        );
    }
}
