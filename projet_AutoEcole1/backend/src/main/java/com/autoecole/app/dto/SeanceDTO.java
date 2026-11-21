package com.autoecole.app.dto;

import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.enums.TypeSeance;

import java.time.LocalDate;
import java.time.LocalTime;

public record SeanceDTO(
        Long id,
        Long tenantId,
        TypeSeance type,
        LocalDate date,
        LocalTime heureDebut,
        LocalTime heureFin,
        Long candidatId,
        String candidatNom,
        String candidatPrenom,
        Long intervenantId,
        String intervenantNom,
        String intervenantPrenom,
        StatutValidation statutValidation,
        Double kilometrage,
        String observations
) {}
