package com.autoecole.app.dto;

import com.autoecole.app.enums.ModePaiement;
import com.autoecole.app.enums.TypePaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaiementDTO(
        Long id,
        Long tenantId,
        Long candidatId,
        String candidatNom,
        String candidatPrenom,
        BigDecimal montant,
        ModePaiement modePaiement,
        LocalDate date,
        TypePaiement type,
        String recuUrl
) {}
