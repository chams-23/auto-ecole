package com.autoecole.app.dto;

import java.time.LocalDate;

public record VehiculeAlerteDTO(
        Long vehiculeId,
        String immatriculation,
        String typeAlerte,
        String libelle,
        LocalDate dateEvenement,
        Long joursRestants,
        String niveau
) {}
