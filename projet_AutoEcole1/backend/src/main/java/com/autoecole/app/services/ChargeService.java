package com.autoecole.app.services;

import com.autoecole.app.dto.ChargeDTO;
import com.autoecole.app.dto.CreateChargeRequest;
import com.autoecole.app.entites.Charge;
import com.autoecole.app.enums.TypeEvenementNotification;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.ChargeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final NotificationService notificationService;

    public List<ChargeDTO> lister(Long tenantId) {
        return chargeRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ChargeDTO getById(Long tenantId, Long id) {
        Charge charge = chargeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge", "id", id));
        return mapToDTO(charge);
    }

    @Transactional
    public ChargeDTO creer(CreateChargeRequest request, Long tenantId) {
        Charge charge = Charge.builder()
                .tenantId(tenantId)
                .categorie(request.categorie())
                .montant(request.montant())
                .date(request.date())
                .periodicite(request.periodicite())
                .justificatifUrl(request.justificatifUrl())
                .build();

        Charge sauvegardee = chargeRepository.save(charge);
        return mapToDTO(sauvegardee);
    }

    private ChargeDTO mapToDTO(Charge c) {
        return new ChargeDTO(
                c.getId(), c.getTenantId(),
                c.getCategorie(), c.getMontant(),
                c.getDate(), c.getPeriodicite(),
                c.getJustificatifUrl()
        );
    }
}
