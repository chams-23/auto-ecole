package com.autoecole.app.dto;

import com.autoecole.app.enums.CategoriePermis;

import java.time.LocalDate;

public record VehiculeDTO(
        Long id,
        Long tenantId,
        String immatriculation,
        CategoriePermis categoriePermis,
        LocalDate dateMiseService,
        LocalDate dateControleTechnique,
        LocalDate dateExpirationAssurance,
        Long moniteurId,
        String moniteurNom,
        String moniteurPrenom
) {}
