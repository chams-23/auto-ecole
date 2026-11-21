package com.autoecole.app.dto;

import com.autoecole.app.enums.TypeDocumentCandidat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UploadDocumentCandidatRequest(
        @NotNull(message = "Le type de document est obligatoire")
        TypeDocumentCandidat typeDocument,

        @NotBlank(message = "Le nom du fichier est obligatoire")
        String nomFichier,

        @NotBlank(message = "Le chemin du fichier est obligatoire")
        String cheminFichier,

        Long tailleOctets,

        String typeMime,

        LocalDate dateExpiration,

        String commentaire
) {}
