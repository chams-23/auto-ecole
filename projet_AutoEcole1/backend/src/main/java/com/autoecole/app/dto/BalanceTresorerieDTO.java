package com.autoecole.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BalanceTresorerieDTO(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal totalPaiements,
        BigDecimal totalCharges,
        BigDecimal soldeNet,
        Long nombrePaiements,
        Long nombreCharges,
        BigDecimal totalPaiementsAvance,
        BigDecimal totalPaiementsVersement
) {}
