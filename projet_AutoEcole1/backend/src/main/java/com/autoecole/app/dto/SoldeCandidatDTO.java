package com.autoecole.app.dto;

import java.math.BigDecimal;

public record SoldeCandidatDTO(
        BigDecimal totalDu,
        BigDecimal totalPaye,
        BigDecimal soldeRestant
) {}
