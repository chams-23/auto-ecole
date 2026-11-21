package com.autoecole.app.services;

import com.autoecole.app.dto.BalanceTresorerieDTO;
import com.autoecole.app.entites.Charge;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.enums.CategorieCharge;
import com.autoecole.app.enums.ModePaiement;
import com.autoecole.app.enums.PeriodiciteCharge;
import com.autoecole.app.enums.TypePaiement;
import com.autoecole.app.repositories.ChargeRepository;
import com.autoecole.app.repositories.PaiementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TresorerieServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private ChargeRepository chargeRepository;

    @InjectMocks
    private TresorerieService tresorerieService;

    private static final Long TENANT_ID = 1L;

    private Paiement paiementAvance;
    private Paiement paiementVersement;
    private Paiement paiementNullMontant;
    private Charge chargeLoyer;
    private Charge chargeCarburant;

    @BeforeEach
    void setUp() {
        paiementAvance = Paiement.builder()
                .id(1L).tenantId(TENANT_ID).candidatId(10L)
                .montant(new BigDecimal("1500.00"))
                .modePaiement(ModePaiement.ESPECES)
                .date(LocalDate.of(2025, 5, 10))
                .type(TypePaiement.AVANCE)
                .build();

        paiementVersement = Paiement.builder()
                .id(2L).tenantId(TENANT_ID).candidatId(11L)
                .montant(new BigDecimal("2000.00"))
                .modePaiement(ModePaiement.VIREMENT)
                .date(LocalDate.of(2025, 5, 15))
                .type(TypePaiement.VERSEMENT)
                .build();

        paiementNullMontant = Paiement.builder()
                .id(3L).tenantId(TENANT_ID).candidatId(12L)
                .montant(null)
                .date(LocalDate.of(2025, 5, 20))
                .type(TypePaiement.AVANCE)
                .build();

        chargeLoyer = Charge.builder()
                .id(10L).tenantId(TENANT_ID)
                .categorie(CategorieCharge.LOYER)
                .montant(new BigDecimal("4500.00"))
                .date(LocalDate.of(2025, 5, 1))
                .periodicite(PeriodiciteCharge.MENSUELLE)
                .build();

        chargeCarburant = Charge.builder()
                .id(11L).tenantId(TENANT_ID)
                .categorie(CategorieCharge.CARBURANT)
                .montant(new BigDecimal("600.00"))
                .date(LocalDate.of(2025, 5, 12))
                .periodicite(PeriodiciteCharge.PONCTUELLE)
                .build();
    }

    @Test
    void calculerBalancePeriode_shouldAggregateCorrectly() {
        LocalDate debut = LocalDate.of(2025, 5, 1);
        LocalDate fin = LocalDate.of(2025, 5, 31);

        when(paiementRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(paiementAvance, paiementVersement));
        when(chargeRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(chargeLoyer, chargeCarburant));

        BalanceTresorerieDTO result = tresorerieService.calculerBalancePeriode(TENANT_ID, debut, fin);

        assertEquals(debut, result.dateDebut());
        assertEquals(fin, result.dateFin());
        assertEquals(new BigDecimal("3500.00"), result.totalPaiements());
        assertEquals(new BigDecimal("5100.00"), result.totalCharges());
        assertEquals(new BigDecimal("-1600.00"), result.soldeNet());
        assertEquals(2L, result.nombrePaiements());
        assertEquals(2L, result.nombreCharges());
        assertEquals(new BigDecimal("1500.00"), result.totalPaiementsAvance());
        assertEquals(new BigDecimal("2000.00"), result.totalPaiementsVersement());
    }

    @Test
    void calculerBalancePeriode_shouldSkipNullMontants() {
        LocalDate debut = LocalDate.of(2025, 5, 1);
        LocalDate fin = LocalDate.of(2025, 5, 31);

        when(paiementRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(paiementNullMontant, paiementAvance));
        when(chargeRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalancePeriode(TENANT_ID, debut, fin);

        assertEquals(new BigDecimal("1500.00"), result.totalPaiements());
        assertEquals(BigDecimal.ZERO, result.totalCharges());
        assertEquals(2L, result.nombrePaiements());
    }

    @Test
    void calculerBalancePeriode_shouldHandleEmptyLists() {
        LocalDate debut = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 1, 31);

        when(paiementRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of());
        when(chargeRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalancePeriode(TENANT_ID, debut, fin);

        assertEquals(BigDecimal.ZERO, result.totalPaiements());
        assertEquals(BigDecimal.ZERO, result.totalCharges());
        assertEquals(BigDecimal.ZERO, result.soldeNet());
        assertEquals(0L, result.nombrePaiements());
        assertEquals(0L, result.nombreCharges());
        assertEquals(BigDecimal.ZERO, result.totalPaiementsAvance());
        assertEquals(BigDecimal.ZERO, result.totalPaiementsVersement());
    }

    @Test
    void calculerBalancePeriode_shouldSumCorrectlyMultipleAvance() {
        LocalDate debut = LocalDate.of(2025, 5, 1);
        LocalDate fin = LocalDate.of(2025, 5, 31);

        Paiement avance2 = Paiement.builder()
                .id(4L).tenantId(TENANT_ID).candidatId(13L)
                .montant(new BigDecimal("500.00")).date(debut.plusDays(3))
                .type(TypePaiement.AVANCE).build();

        when(paiementRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(paiementAvance, avance2, paiementVersement));
        when(chargeRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalancePeriode(TENANT_ID, debut, fin);

        assertEquals(new BigDecimal("4000.00"), result.totalPaiements());
        assertEquals(new BigDecimal("2000.00"), result.totalPaiementsAvance());
        assertEquals(new BigDecimal("2000.00"), result.totalPaiementsVersement());
    }

    @Test
    void calculerBalance_shouldUseMoisAsDefault() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = debutMois.plusMonths(1).minusDays(1);

        when(paiementRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of(paiementAvance));
        when(chargeRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of(chargeLoyer));

        BalanceTresorerieDTO result = tresorerieService.calculerBalance(TENANT_ID, null);

        assertNotNull(result);
        verify(paiementRepository).findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any());
    }

    @Test
    void calculerBalance_shouldUseSemaine() {
        LocalDate debutSemaine = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemaine = debutSemaine.plusDays(6);

        when(paiementRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());
        when(chargeRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalance(TENANT_ID, "SEMAINE");

        assertEquals(debutSemaine, result.dateDebut());
        assertEquals(finSemaine, result.dateFin());
    }

    @Test
    void calculerBalance_shouldHandleCaseInsensitivePeriode() {
        when(paiementRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());
        when(chargeRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalance(TENANT_ID, "mois");

        assertNotNull(result);
    }

    @Test
    void calculerBalance_shouldUseTrimestre() {
        when(paiementRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());
        when(chargeRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalance(TENANT_ID, "TRIMESTRE");

        assertNotNull(result);
        assertNotNull(result.dateDebut());
        assertNotNull(result.dateFin());
        assertTrue(result.dateFin().isAfter(result.dateDebut()));
    }

    @Test
    void calculerBalance_shouldUseAnnee() {
        when(paiementRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());
        when(chargeRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalance(TENANT_ID, "ANNEE");

        assertNotNull(result);
        assertEquals(1, result.dateDebut().getMonthValue());
        assertEquals(1, result.dateDebut().getDayOfMonth());
        assertEquals(12, result.dateFin().getMonthValue());
        assertEquals(31, result.dateFin().getDayOfMonth());
    }

    @Test
    void calculerBalance_shouldUseDefaultMoisForUnknownPeriode() {
        when(paiementRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());
        when(chargeRepository.findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(), any()))
                .thenReturn(List.of());

        BalanceTresorerieDTO result = tresorerieService.calculerBalance(TENANT_ID, "PERIODE_INCONNUE");

        assertNotNull(result);
        assertNotNull(result.dateDebut());
    }

    @Test
    void calculerBalancePeriode_shouldReturnSoldePositif_whenPaiementsGreaterThanCharges() {
        LocalDate debut = LocalDate.of(2025, 5, 1);
        LocalDate fin = LocalDate.of(2025, 5, 31);

        when(paiementRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(paiementAvance, paiementVersement, paiementAvance));
        when(chargeRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(chargeCarburant));

        BalanceTresorerieDTO result = tresorerieService.calculerBalancePeriode(TENANT_ID, debut, fin);

        assertTrue(result.soldeNet().compareTo(BigDecimal.ZERO) > 0);
    }
}
