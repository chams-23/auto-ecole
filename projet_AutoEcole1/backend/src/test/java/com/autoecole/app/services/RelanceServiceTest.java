package com.autoecole.app.services;

import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.entites.RelanceImpaye;
import com.autoecole.app.enums.TypeEvenementNotification;
import com.autoecole.app.repositories.PaiementRepository;
import com.autoecole.app.repositories.RelanceImpayeRepository;
import com.autoecole.app.tenant.Tenant;
import com.autoecole.app.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelanceServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private RelanceImpayeRepository relanceImpayeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RelanceService relanceService;

    @Test
    void runDailyRelancesShouldCreateRelanceAndNotificationForExpiredPayment() {
        Tenant tenant = Tenant.builder().id(7L).build();
        Paiement paiement = new Paiement();
        paiement.setId(99L);
        paiement.setTenantId(7L);
        paiement.setCandidatId(123L);
        paiement.setMontant(new BigDecimal("350.00"));
        paiement.setDateEcheance(LocalDate.now().minusDays(2));
        paiement.setNombreRelances(0);
        paiement.setEstAnnule(false);

        Candidat candidat = new Candidat();
        candidat.setUtilisateurId(88L);
        paiement.setCandidat(candidat);

        when(tenantRepository.findAll()).thenReturn(List.of(tenant));
        when(paiementRepository.findAllByTenantId(7L)).thenReturn(List.of(paiement));

        relanceService.runDailyRelances();

        ArgumentCaptor<RelanceImpaye> relanceCaptor = ArgumentCaptor.forClass(RelanceImpaye.class);
        verify(relanceImpayeRepository).save(relanceCaptor.capture());
        RelanceImpaye relance = relanceCaptor.getValue();

        assertNotNull(relance);
        assertEquals(99L, relance.getPaiementId());
        assertEquals(7L, relance.getTenantId());
        assertEquals(123L, relance.getCandidatId());
        assertEquals(1, relance.getNumeroRelance());
        assertEquals("Relance automatique: montant 350.00 DH, echeance '" + LocalDate.now().minusDays(2) + "'", relance.getMessage());

        verify(paiementRepository).save(paiement);
        verify(notificationService).creerNotification(
                eq(7L),
                eq(88L),
                eq(TypeEvenementNotification.PAIEMENT_EN_RETARD),
                contains("Relance #1")
        );
    }
}
