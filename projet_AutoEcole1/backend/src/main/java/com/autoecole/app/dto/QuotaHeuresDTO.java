package com.autoecole.app.dto;

import com.autoecole.app.enums.CategoriePermis;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuotaHeuresDTO(
        Long id,
        Long tenantId,
        @NotNull CategoriePermis categoriePermis,
        @Min(0) Integer heuresCodeMin,
        @Min(0) Integer heuresConduiteMin,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
) {}
