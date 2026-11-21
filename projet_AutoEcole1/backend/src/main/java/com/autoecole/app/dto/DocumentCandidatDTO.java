package com.autoecole.app.dto;

import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.enums.TypeDocumentCandidat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DocumentCandidatDTO(
        Long id,
        Long candidatId,
        String candidatNom,
        String candidatPrenom,
        Long tenantId,
        TypeDocumentCandidat typeDocument,
        String nomFichier,
        String cheminFichier,
        Long tailleOctets,
        String typeMime,
        StatutValidation statutValidation,
        Long validePar,
        LocalDateTime dateValidation,
        LocalDate dateExpiration,
        String commentaire,
        Long uploadedBy,
        LocalDateTime createdAt
) {}
