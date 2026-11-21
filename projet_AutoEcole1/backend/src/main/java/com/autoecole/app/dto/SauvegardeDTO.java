package com.autoecole.app.dto;

import java.time.LocalDateTime;

public record SauvegardeDTO(
        Long id,
        Long tenantId,
        String typeSauvegarde,
        String nomFichier,
        String cheminFichier,
        Long tailleOctets,
        String statut,
        String declencheur,
        String erreurMessage,
        LocalDateTime dateDebut,
        LocalDateTime dateFin
) {}
