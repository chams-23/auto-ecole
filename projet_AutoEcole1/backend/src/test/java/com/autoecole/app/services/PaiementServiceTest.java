package com.autoecole.app.services;

import com.autoecole.app.dto.CreatePaiementRequest;
import com.autoecole.app.dto.PaiementDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.CategoriePermis;
import com.autoecole.app.enums.ModePaiement;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutParcours;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.enums.TypeEvenementNotification;
import com.autoecole.app.enums.TypePaiement;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.PaiementRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaiementService paiementService;

    private static final Long TENANT_ID = 1L;
    private static final Long PAIEMENT_ID = 100L;
    private static final Long CANDIDAT_ID = 10L;
    private static final Long UTILISATEUR_ID = 20L;

    private Paiement paiement;
    private Paiement paiement2;
    private Candidat candidat;
    private Utilisateur candidatUser;

    @BeforeEach
    void setUp() {
        candidatUser = Utilisateur.builder()
                .id(UTILISATEUR_ID)
                .tenantId(TENANT_ID)
                .role(Role.CANDIDAT)
                .nom("El Amrani")
                .prenom("Youssef")
                .email("youssef@email.com")
                .telephone("0612345678")
                .statut(StatutUtilisateur.ACTIF)
                .passwordHash("encoded")
                .build();

        candidat = Candidat.builder()
                .id(CANDIDAT_ID)
                .utilisateurId(UTILISATEUR_ID)
                .tenantId(TENANT_ID)
                .cin("AB123456")
                .categoriePermis(CategoriePermis.B)
                .statutParcours(StatutParcours.INSCRIT)
                .forfaitId(1L)
                .build();

        paiement = Paiement.builder()
                .id(PAIEMENT_ID)
                .tenantId(TENANT_ID)
                .candidatId(CANDIDAT_ID)
                .montant(new BigDecimal("1500.00"))
                .modePaiement(ModePaiement.ESPECES)
                .date(LocalDate.of(2025, 5, 10))
                .type(TypePaiement.AVANCE)
                .recuUrl("/recus/100.pdf")
                .build();

        paiement2 = Paiement.builder()
                .id(200L)
                .tenantId(TENANT_ID)
                .candidatId(CANDIDAT_ID)
                .montant(new BigDecimal("2000.00"))
                .modePaiement(ModePaiement.VIREMENT)
                .date(LocalDate.of(2025, 6, 1))
                .type(TypePaiement.VERSEMENT)
                .recuUrl(null)
                .build();
    }

    @Test
    void lister_shouldReturnAllByTenant_whenNoCandidatId() {
        when(paiementRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(paiement, paiement2));
        setupFindByIdMocks();

        List<PaiementDTO> result = paiementService.lister(TENANT_ID, null);

        assertEquals(2, result.size());
        verify(paiementRepository).findAllByTenantId(TENANT_ID);
    }

    @Test
    void lister_shouldFilterByCandidatId_whenProvided() {
        when(paiementRepository.findAllByTenantIdAndCandidatId(TENANT_ID, CANDIDAT_ID))
                .thenReturn(List.of(paiement));
        setupFindByIdMocks();

        List<PaiementDTO> result = paiementService.lister(TENANT_ID, CANDIDAT_ID);

        assertEquals(1, result.size());
        assertEquals(PAIEMENT_ID, result.get(0).id());
        verify(paiementRepository).findAllByTenantIdAndCandidatId(TENANT_ID, CANDIDAT_ID);
    }

    @Test
    void lister_shouldMapCandidatInfoCorrectly() {
        when(paiementRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(paiement));
        setupFindByIdMocks();

        List<PaiementDTO> result = paiementService.lister(TENANT_ID, null);

        PaiementDTO dto = result.get(0);
        assertEquals("El Amrani", dto.candidatNom());
        assertEquals("Youssef", dto.candidatPrenom());
    }

    @Test
    void getById_shouldReturnPaiementDTO_whenFound() {
        when(paiementRepository.findByTenantIdAndId(TENANT_ID, PAIEMENT_ID)).thenReturn(Optional.of(paiement));
        setupFindByIdMocks();

        PaiementDTO result = paiementService.getById(TENANT_ID, PAIEMENT_ID);

        assertNotNull(result);
        assertEquals(PAIEMENT_ID, result.id());
        assertEquals(new BigDecimal("1500.00"), result.montant());
        assertEquals(ModePaiement.ESPECES, result.modePaiement());
        assertEquals(TypePaiement.AVANCE, result.type());
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(paiementRepository.findByTenantIdAndId(TENANT_ID, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paiementService.getById(TENANT_ID, 999L));
    }

    @Test
    void creer_shouldSavePaiementAndSendNotification() {
        CreatePaiementRequest request = new CreatePaiementRequest(
                CANDIDAT_ID,
                new BigDecimal("500.00"),
                ModePaiement.CHEQUE,
                LocalDate.of(2025, 6, 15),
                TypePaiement.VERSEMENT,
                null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> {
            Paiement p = inv.getArgument(0);
            p.setId(PAIEMENT_ID);
            return p;
        });
        setupFindByIdMocks();

        PaiementDTO result = paiementService.creer(request, TENANT_ID);

        assertNotNull(result);
        assertEquals(PAIEMENT_ID, result.id());
        assertEquals(new BigDecimal("500.00"), result.montant());
        assertEquals(ModePaiement.CHEQUE, result.modePaiement());
        assertEquals(TypePaiement.VERSEMENT, result.type());

        verify(notificationService).creerNotification(
                eq(TENANT_ID),
                eq(UTILISATEUR_ID),
                eq(TypeEvenementNotification.PAIEMENT_CREE),
                contains("500.00")
        );
    }

    @Test
    void creer_shouldThrow_whenCandidatNotFound() {
        CreatePaiementRequest request = new CreatePaiementRequest(
                999L, new BigDecimal("500.00"),
                ModePaiement.ESPECES, LocalDate.now(),
                TypePaiement.AVANCE, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paiementService.creer(request, TENANT_ID));
        verify(paiementRepository, never()).save(any());
        verify(notificationService, never()).creerNotification(any(), any(), any(), any());
    }

    @Test
    void creer_shouldIncludeRecuUrl_whenProvided() {
        CreatePaiementRequest request = new CreatePaiementRequest(
                CANDIDAT_ID,
                new BigDecimal("300.00"),
                ModePaiement.CARTE_BANCAIRE,
                LocalDate.of(2025, 7, 1),
                TypePaiement.AVANCE,
                "/recus/nouveau.pdf"
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));
        setupFindByIdMocks();

        PaiementDTO result = paiementService.creer(request, TENANT_ID);

        assertEquals("/recus/nouveau.pdf", result.recuUrl());
    }

    @Test
    void getRecuUrl_shouldReturnUrl_whenFound() {
        when(paiementRepository.findByTenantIdAndId(TENANT_ID, PAIEMENT_ID)).thenReturn(Optional.of(paiement));

        String result = paiementService.getRecuUrl(TENANT_ID, PAIEMENT_ID);

        assertEquals("/recus/100.pdf", result);
    }

    @Test
    void getRecuUrl_shouldReturnNull_whenNoRecu() {
        when(paiementRepository.findByTenantIdAndId(TENANT_ID, 200L)).thenReturn(Optional.of(paiement2));

        String result = paiementService.getRecuUrl(TENANT_ID, 200L);

        assertNull(result);
    }

    @Test
    void getRecuUrl_shouldThrow_whenPaiementNotFound() {
        when(paiementRepository.findByTenantIdAndId(TENANT_ID, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paiementService.getRecuUrl(TENANT_ID, 999L));
    }

    @Test
    void lister_shouldHandleNullCandidatGracefully() {
        Paiement orphelin = Paiement.builder()
                .id(300L).tenantId(TENANT_ID).candidatId(999L)
                .montant(BigDecimal.TEN).modePaiement(ModePaiement.ESPECES)
                .date(LocalDate.now()).type(TypePaiement.AVANCE).build();

        when(paiementRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(orphelin));
        when(candidatRepository.findById(999L)).thenReturn(Optional.empty());

        List<PaiementDTO> result = paiementService.lister(TENANT_ID, null);

        assertEquals(1, result.size());
        assertNull(result.get(0).candidatNom());
        assertNull(result.get(0).candidatPrenom());
    }

    private void setupFindByIdMocks() {
        when(candidatRepository.findById(CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(candidatUser));
    }
}
