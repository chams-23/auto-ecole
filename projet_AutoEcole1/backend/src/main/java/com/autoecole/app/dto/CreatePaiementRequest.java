package com.autoecole.app.dto;

import com.autoecole.app.enums.ModePaiement;
import com.autoecole.app.enums.TypePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaiementRequest(
        @NotNull(message = "Le candidat est obligatoire")
        Long candidatId,

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit etre positif")
        BigDecimal montant,

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement,

        @NotNull(message = "La date est obligatoire")
        LocalDate date,

        @NotNull(message = "Le type de paiement est obligatoire")
        TypePaiement type,

        String recuUrl
) {}
