package com.autoecole.app.services;

import com.autoecole.app.dto.DocumentCandidatDTO;
import com.autoecole.app.dto.UploadDocumentCandidatRequest;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.DocumentCandidat;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.DocumentCandidatRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentCandidatService {

    private final DocumentCandidatRepository documentCandidatRepository;
    private final CandidatRepository candidatRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<DocumentCandidatDTO> listerParCandidat(Long tenantId, Long candidatId) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));
        return documentCandidatRepository.findAllByTenantIdAndCandidatId(tenantId, candidatId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DocumentCandidatDTO getById(Long tenantId, Long id) {
        DocumentCandidat document = documentCandidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentCandidat", "id", id));
        return mapToDTO(document);
    }

    public DocumentCandidat getEntityById(Long tenantId, Long id) {
        return documentCandidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentCandidat", "id", id));
    }

    @Transactional
    public DocumentCandidatDTO uploaderDocument(Long tenantId, Long candidatId, UploadDocumentCandidatRequest request, Long uploadedBy) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));

        DocumentCandidat document = DocumentCandidat.builder()
                .candidatId(candidatId)
                .tenantId(tenantId)
                .typeDocument(request.typeDocument())
                .nomFichier(request.nomFichier())
                .cheminFichier(request.cheminFichier())
                .tailleOctets(request.tailleOctets())
                .typeMime(request.typeMime())
                .dateExpiration(request.dateExpiration())
                .commentaire(request.commentaire())
                .statutValidation(StatutValidation.EN_ATTENTE)
                .uploadedBy(uploadedBy)
                .build();

        DocumentCandidat sauvegarde = documentCandidatRepository.save(document);
        return mapToDTO(sauvegarde);
    }

    @Transactional
    public DocumentCandidatDTO validerDocument(Long tenantId, Long id, Long validePar, String commentaire) {
        DocumentCandidat document = documentCandidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentCandidat", "id", id));

        document.setStatutValidation(StatutValidation.VALIDEE);
        document.setValidePar(validePar);
        document.setDateValidation(LocalDateTime.now());
        if (commentaire != null && !commentaire.isBlank()) {
            document.setCommentaire(commentaire);
        }

        DocumentCandidat sauvegarde = documentCandidatRepository.save(document);
        return mapToDTO(sauvegarde);
    }

    @Transactional
    public DocumentCandidatDTO refuserDocument(Long tenantId, Long id, Long validePar, String commentaire) {
        DocumentCandidat document = documentCandidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentCandidat", "id", id));

        document.setStatutValidation(StatutValidation.CONTESTEE);
        document.setValidePar(validePar);
        document.setDateValidation(LocalDateTime.now());
        if (commentaire != null && !commentaire.isBlank()) {
            document.setCommentaire(commentaire);
        }

        DocumentCandidat sauvegarde = documentCandidatRepository.save(document);
        return mapToDTO(sauvegarde);
    }

    @Transactional
    public void supprimer(Long tenantId, Long id) {
        DocumentCandidat document = documentCandidatRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentCandidat", "id", id));
        documentCandidatRepository.delete(document);
    }

    private DocumentCandidatDTO mapToDTO(DocumentCandidat doc) {
        String candidatNom = null;
        String candidatPrenom = null;
        Candidat candidat = candidatRepository.findById(doc.getCandidatId()).orElse(null);
        if (candidat != null) {
            Utilisateur utilisateur = utilisateurRepository.findById(candidat.getUtilisateurId()).orElse(null);
            if (utilisateur != null) {
                candidatNom = utilisateur.getNom();
                candidatPrenom = utilisateur.getPrenom();
            }
        }
        return new DocumentCandidatDTO(
                doc.getId(),
                doc.getCandidatId(),
                candidatNom,
                candidatPrenom,
                doc.getTenantId(),
                doc.getTypeDocument(),
                doc.getNomFichier(),
                doc.getCheminFichier(),
                doc.getTailleOctets(),
                doc.getTypeMime(),
                doc.getStatutValidation(),
                doc.getValidePar(),
                doc.getDateValidation(),
                doc.getDateExpiration(),
                doc.getCommentaire(),
                doc.getUploadedBy(),
                doc.getCreatedAt()
        );
    }
}
