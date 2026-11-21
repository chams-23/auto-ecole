package com.autoecole.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RelanceImpayeDTO(
        Long id,
        Long paiementId,
        BigDecimal montantImpaye,
        Long tenantId,
        Long candidatId,
        String candidatNom,
        String candidatPrenom,
        String candidatEmail,
        String candidatTelephone,
        Integer numeroRelance,
        LocalDateTime dateEnvoi,
        String canalEnvoi,
        String statutEnvoi,
        String message
) {}
