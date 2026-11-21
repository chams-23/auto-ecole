package com.autoecole.app.dto;

import com.autoecole.app.enums.TypeSeance;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReprogrammerSeanceRequest(
        LocalDate date,
        LocalTime heureDebut,
        LocalTime heureFin,
        TypeSeance type,
        Long intervenantId
) {}
