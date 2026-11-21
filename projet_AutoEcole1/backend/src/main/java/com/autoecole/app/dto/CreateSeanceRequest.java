package com.autoecole.app.dto;

import com.autoecole.app.enums.TypeSeance;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSeanceRequest(
        @NotNull(message = "Le type de seance est obligatoire")
        TypeSeance type,

        @NotNull(message = "La date est obligatoire")
        LocalDate date,

        @NotNull(message = "L'heure de debut est obligatoire")
        LocalTime heureDebut,

        @NotNull(message = "L'heure de fin est obligatoire")
        LocalTime heureFin,

        @NotNull(message = "Le candidat est obligatoire")
        Long candidatId,

        @NotNull(message = "L'intervenant est obligatoire")
        Long intervenantId,

        Double kilometrage,
        String observations
) {}
