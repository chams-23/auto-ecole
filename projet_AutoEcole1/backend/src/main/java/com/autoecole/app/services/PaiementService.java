package com.autoecole.app.services;

import com.autoecole.app.dto.CreatePaiementRequest;
import com.autoecole.app.dto.PaiementDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.TypeEvenementNotification;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.PaiementRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final CandidatRepository candidatRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    public List<PaiementDTO> lister(Long tenantId, Long candidatId) {
        List<Paiement> paiements;
        if (candidatId != null) {
            paiements = paiementRepository.findAllByTenantIdAndCandidatId(tenantId, candidatId);
        } else {
            paiements = paiementRepository.findAllByTenantId(tenantId);
        }
        return paiements.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public PaiementDTO getById(Long tenantId, Long id) {
        Paiement paiement = paiementRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", id));
        return mapToDTO(paiement);
    }

    @Transactional
    public PaiementDTO creer(CreatePaiementRequest request, Long tenantId) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, request.candidatId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", request.candidatId()));

        Paiement paiement = Paiement.builder()
                .tenantId(tenantId)
                .candidatId(request.candidatId())
                .montant(request.montant())
                .modePaiement(request.modePaiement())
                .date(request.date())
                .type(request.type())
                .recuUrl(request.recuUrl())
                .build();

        Paiement sauvegarde = paiementRepository.save(paiement);

        notificationService.creerNotification(tenantId, candidat.getUtilisateurId(),
                TypeEvenementNotification.PAIEMENT_CREE,
                "Paiement de " + request.montant() + " DH enregistre le " + request.date());

        return mapToDTO(sauvegarde);
    }

    public String getRecuUrl(Long tenantId, Long id) {
        Paiement paiement = paiementRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", id));
        return paiement.getRecuUrl();
    }

    private PaiementDTO mapToDTO(Paiement p) {
        String candNom = null, candPrenom = null;
        Candidat c = candidatRepository.findById(p.getCandidatId()).orElse(null);
        if (c != null) {
            Utilisateur cu = utilisateurRepository.findById(c.getUtilisateurId()).orElse(null);
            if (cu != null) {
                candNom = cu.getNom();
                candPrenom = cu.getPrenom();
            }
        }

        return new PaiementDTO(
                p.getId(), p.getTenantId(),
                p.getCandidatId(), candNom, candPrenom,
                p.getMontant(), p.getModePaiement(),
                p.getDate(), p.getType(), p.getRecuUrl()
        );
    }
}
