package com.autoecole.app.dto;

import com.autoecole.app.enums.CategorieCharge;
import com.autoecole.app.enums.PeriodiciteCharge;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChargeDTO(
        Long id,
        Long tenantId,
        CategorieCharge categorie,
        BigDecimal montant,
        LocalDate date,
        PeriodiciteCharge periodicite,
        String justificatifUrl
) {}
