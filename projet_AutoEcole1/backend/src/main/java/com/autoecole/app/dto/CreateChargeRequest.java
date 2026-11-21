package com.autoecole.app.dto;

import com.autoecole.app.enums.CategorieCharge;
import com.autoecole.app.enums.PeriodiciteCharge;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateChargeRequest(
        @NotNull(message = "La categorie est obligatoire")
        CategorieCharge categorie,

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit etre positif")
        BigDecimal montant,

        @NotNull(message = "La date est obligatoire")
        LocalDate date,

        @NotNull(message = "La periodicite est obligatoire")
        PeriodiciteCharge periodicite,

        String justificatifUrl
) {}
