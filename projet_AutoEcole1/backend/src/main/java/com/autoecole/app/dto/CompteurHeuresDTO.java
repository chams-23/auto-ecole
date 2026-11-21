package com.autoecole.app.dto;

public record CompteurHeuresDTO(
        Integer heuresConduiteValidees,
        Integer heuresTheorieValidees,
        Integer heuresConduiteRestantes,
        Integer heuresTheorieRestantes
) {}
