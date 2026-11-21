package com.autoecole.app.dto;

import com.autoecole.app.enums.CategoriePermis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateVehiculeRequest(
        @NotBlank(message = "L'immatriculation est obligatoire")
        String immatriculation,

        @NotNull(message = "La categorie de permis est obligatoire")
        CategoriePermis categoriePermis,

        LocalDate dateMiseService,
        LocalDate dateControleTechnique,
        LocalDate dateExpirationAssurance,
        Long moniteurId
) {}
