package com.autoecole.app.dto;

import com.autoecole.app.enums.CategoriePermis;

public record StatutQuotaHeuresDTO(
        CategoriePermis categoriePermis,
        Integer quotaCodeMin,
        Integer quotaConduiteMin,
        Integer heuresCodeValidees,
        Integer heuresConduiteValidees,
        Boolean codeAtteint,
        Boolean conduiteAtteint,
        Boolean quotaAtteint
) {}
