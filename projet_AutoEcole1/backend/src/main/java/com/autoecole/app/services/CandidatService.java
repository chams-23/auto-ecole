package com.autoecole.app.services;

import com.autoecole.app.dto.CandidatDTO;
import com.autoecole.app.dto.CompteurHeuresDTO;
import com.autoecole.app.dto.CreateCandidatRequest;
import com.autoecole.app.dto.SoldeCandidatDTO;
import com.autoecole.app.dto.StatutQuotaHeuresDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Forfait;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.ResultatExamen;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutParcours;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.ForfaitRepository;
import com.autoecole.app.repositories.PaiementRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ForfaitRepository forfaitRepository;
    private final PaiementRepository paiementRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuotaHeuresService quotaHeuresService;

    public List<CandidatDTO> lister(Long tenantId) {
        return candidatRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToDTOWithUser)
                .collect(Collectors.toList());
    }

    public CandidatDTO getById(Long tenantId, Long id) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", id));
        return mapToDTOWithUser(candidat);
    }

    @Transactional
    public CandidatDTO creer(CreateCandidatRequest request, Long tenantId) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
        }

        if (candidatRepository.findByTenantIdAndCin(tenantId, request.cin()).isPresent()) {
            throw new IllegalArgumentException("Un candidat avec ce CIN existe deja");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .tenantId(tenantId)
                .role(Role.CANDIDAT)
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .statut(StatutUtilisateur.ACTIF)
                .build();

        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);

        Candidat candidat = Candidat.builder()
                .utilisateurId(utilisateurSauvegarde.getId())
                .tenantId(tenantId)
                .cin(request.cin())
                .categoriePermis(request.categoriePermis())
                .forfaitId(request.forfaitId())
                .build();

        Candidat candidatSauvegarde = candidatRepository.save(candidat);

        return mapToDTOWithUser(candidatSauvegarde);
    }

    @Transactional
    public CandidatDTO modifier(Long tenantId, Long id, CandidatDTO dto) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", id));

        Utilisateur utilisateur = utilisateurRepository.findById(candidat.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", candidat.getUtilisateurId()));

        if (dto.cin() != null) {
            candidat.setCin(dto.cin());
        }
        if (dto.categoriePermis() != null) {
            candidat.setCategoriePermis(dto.categoriePermis());
        }
        if (dto.statutParcours() != null && !dto.statutParcours().equals(candidat.getStatutParcours())) {
            verifierTransitionStatut(tenantId, candidat, dto.statutParcours());
            candidat.setStatutParcours(dto.statutParcours());
        }
        if (dto.forfaitId() != null) {
            candidat.setForfaitId(dto.forfaitId());
        }
        if (dto.heuresConduiteValidees() != null) {
            candidat.setHeuresConduiteValidees(dto.heuresConduiteValidees());
        }
        if (dto.heuresTheorieValidees() != null) {
            candidat.setHeuresTheorieValidees(dto.heuresTheorieValidees());
        }

        if (dto.nom() != null) {
            utilisateur.setNom(dto.nom());
        }
        if (dto.prenom() != null) {
            utilisateur.setPrenom(dto.prenom());
        }
        if (dto.email() != null && !dto.email().equals(utilisateur.getEmail())) {
            if (utilisateurRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
            }
            utilisateur.setEmail(dto.email());
        }
        if (dto.telephone() != null) {
            utilisateur.setTelephone(dto.telephone());
        }

        candidatRepository.save(candidat);
        utilisateurRepository.save(utilisateur);

        return mapToDTOWithUser(candidat);
    }

    public CompteurHeuresDTO compteurHeures(Long tenantId, Long candidatId) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));

        int heuresConduiteValidees = candidat.getHeuresConduiteValidees() != null ? candidat.getHeuresConduiteValidees() : 0;
        int heuresTheorieValidees = candidat.getHeuresTheorieValidees() != null ? candidat.getHeuresTheorieValidees() : 0;
        int heuresConduiteRestantes = 0;
        int heuresTheorieRestantes = 0;

        if (candidat.getForfaitId() != null) {
            Forfait forfait = forfaitRepository.findByTenantIdAndId(tenantId, candidat.getForfaitId()).orElse(null);
            if (forfait != null) {
                int conduiteMin = forfait.getHeuresConduiteMin() != null ? forfait.getHeuresConduiteMin() : 0;
                int codeMin = forfait.getHeuresCodeMin() != null ? forfait.getHeuresCodeMin() : 0;
                heuresConduiteRestantes = Math.max(0, conduiteMin - heuresConduiteValidees);
                heuresTheorieRestantes = Math.max(0, codeMin - heuresTheorieValidees);
            }
        }

        return new CompteurHeuresDTO(
                heuresConduiteValidees,
                heuresTheorieValidees,
                heuresConduiteRestantes,
                heuresTheorieRestantes
        );
    }

    public SoldeCandidatDTO solde(Long tenantId, Long candidatId) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));

        BigDecimal totalDu = BigDecimal.ZERO;
        BigDecimal totalPaye = BigDecimal.ZERO;

        if (candidat.getForfaitId() != null) {
            Forfait forfait = forfaitRepository.findByTenantIdAndId(tenantId, candidat.getForfaitId()).orElse(null);
            if (forfait != null && forfait.getPrix() != null) {
                totalDu = forfait.getPrix();
            }
        }

        List<Paiement> paiements = paiementRepository.findAllByTenantIdAndCandidatId(tenantId, candidatId);
        for (Paiement p : paiements) {
            if (p.getMontant() != null) {
                totalPaye = totalPaye.add(p.getMontant());
            }
        }

        BigDecimal soldeRestant = totalDu.subtract(totalPaye);

        return new SoldeCandidatDTO(totalDu, totalPaye, soldeRestant);
    }

    public Candidat getCandidatEntityByUtilisateurId(Long utilisateurId) {
        return candidatRepository.findByUtilisateurId(utilisateurId).orElse(null);
    }

    private void verifierTransitionStatut(Long tenantId, Candidat candidat, StatutParcours nouveauStatut) {
        log.debug("Vérification transition statut candidat {} : {} → {}",
                candidat.getId(), candidat.getStatutParcours(), nouveauStatut);

        if (StatutParcours.PRESENTE_EXAMEN.equals(nouveauStatut)) {
            StatutQuotaHeuresDTO statutQuota = quotaHeuresService.calculerStatutPourCandidat(tenantId, candidat.getId());
            if (!Boolean.TRUE.equals(statutQuota.quotaAtteint())) {
                String msg = String.format(
                        "Impossible de passer le candidat au statut PRESENTE_EXAMEN : quota non atteint. " +
                                "Code %d/%d, Conduite %d/%d",
                        statutQuota.heuresCodeValidees(), statutQuota.quotaCodeMin(),
                        statutQuota.heuresConduiteValidees(), statutQuota.quotaConduiteMin()
                );
                log.warn(msg);
                throw new IllegalStateException(msg);
            }
            log.debug("Quota CNEP atteint pour candidat {} - transition PRESENTE_EXAMEN autorisée", candidat.getId());
        }
    }

    @Transactional
    public CandidatDTO planifierExamenTheorie(Long tenantId, Long candidatId, LocalDate date) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));
        candidat.setDateExamenTheorie(date);
        candidat.setResultatExamenTheorie(ResultatExamen.EN_ATTENTE);
        candidatRepository.save(candidat);
        return mapToDTOWithUser(candidat);
    }

    @Transactional
    public CandidatDTO enregistrerResultatTheorie(Long tenantId, Long candidatId, ResultatExamen resultat) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));
        candidat.setResultatExamenTheorie(resultat);
        if (!ResultatExamen.EN_ATTENTE.equals(resultat)) {
            int tentatives = candidat.getNombreTentativesTheorie() != null ? candidat.getNombreTentativesTheorie() : 0;
            candidat.setNombreTentativesTheorie(tentatives + 1);
        }
        candidatRepository.save(candidat);
        return mapToDTOWithUser(candidat);
    }

    @Transactional
    public CandidatDTO planifierExamenPratique(Long tenantId, Long candidatId, LocalDate date) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));
        candidat.setDateExamenPratique(date);
        candidat.setResultatExamenPratique(ResultatExamen.EN_ATTENTE);
        candidatRepository.save(candidat);
        return mapToDTOWithUser(candidat);
    }

    @Transactional
    public CandidatDTO enregistrerResultatPratique(Long tenantId, Long candidatId, ResultatExamen resultat) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));
        candidat.setResultatExamenPratique(resultat);
        if (!ResultatExamen.EN_ATTENTE.equals(resultat)) {
            int tentatives = candidat.getNombreTentativesPratique() != null ? candidat.getNombreTentativesPratique() : 0;
            candidat.setNombreTentativesPratique(tentatives + 1);
        }
        candidatRepository.save(candidat);
        return mapToDTOWithUser(candidat);
    }

    private CandidatDTO mapToDTOWithUser(Candidat c) {
        Utilisateur u = utilisateurRepository.findById(c.getUtilisateurId()).orElse(null);
        return new CandidatDTO(
                c.getId(),
                c.getUtilisateurId(),
                c.getTenantId(),
                c.getCin(),
                c.getCategoriePermis(),
                c.getStatutParcours(),
                c.getForfaitId(),
                c.getHeuresConduiteValidees(),
                c.getHeuresTheorieValidees(),
                u != null ? u.getNom() : null,
                u != null ? u.getPrenom() : null,
                u != null ? u.getEmail() : null,
                u != null ? u.getTelephone() : null,
                c.getDateExamenTheorie(),
                c.getResultatExamenTheorie(),
                c.getNombreTentativesTheorie(),
                c.getDateExamenPratique(),
                c.getResultatExamenPratique(),
                c.getNombreTentativesPratique()
        );
    }
}
