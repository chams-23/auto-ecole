package com.autoecole.app.services;

import com.autoecole.app.dto.*;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Forfait;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.CategoriePermis;
import com.autoecole.app.enums.ResultatExamen;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutParcours;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.ForfaitRepository;
import com.autoecole.app.repositories.PaiementRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidatServiceTest {

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ForfaitRepository forfaitRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private QuotaHeuresService quotaHeuresService;

    @InjectMocks
    private CandidatService candidatService;

    private static final Long TENANT_ID = 1L;
    private static final Long CANDIDAT_ID = 10L;
    private static final Long UTILISATEUR_ID = 20L;
    private static final Long FORFAIT_ID = 30L;

    private Candidat candidat;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = Utilisateur.builder()
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
                .forfaitId(FORFAIT_ID)
                .heuresConduiteValidees(10)
                .heuresTheorieValidees(20)
                .build();
    }

    @Test
    void lister_shouldReturnListOfCandidatDTOs() {
        when(candidatRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        List<CandidatDTO> result = candidatService.lister(TENANT_ID);

        assertEquals(1, result.size());
        CandidatDTO dto = result.get(0);
        assertEquals(CANDIDAT_ID, dto.id());
        assertEquals("El Amrani", dto.nom());
        assertEquals("Youssef", dto.prenom());
        verify(candidatRepository).findAllByTenantId(TENANT_ID);
    }

    @Test
    void getById_shouldReturnCandidatDTO_whenFound() {
        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        CandidatDTO result = candidatService.getById(TENANT_ID, CANDIDAT_ID);

        assertNotNull(result);
        assertEquals(CANDIDAT_ID, result.id());
        assertEquals(CategoriePermis.B, result.categoriePermis());
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(candidatRepository.findByTenantIdAndId(TENANT_ID, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> candidatService.getById(TENANT_ID, 999L));
    }

    @Test
    void creer_shouldCreateCandidatAndUser() {
        CreateCandidatRequest request = new CreateCandidatRequest(
                "El Amrani", "Youssef", "youssef@email.com",
                "0612345678", "pass123", "AB123456",
                CategoriePermis.B, FORFAIT_ID
        );

        when(utilisateurRepository.existsByEmail(request.email())).thenReturn(false);
        when(candidatRepository.findByTenantIdAndCin(TENANT_ID, request.cin())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedHash");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> {
            Utilisateur u = inv.getArgument(0);
            u.setId(UTILISATEUR_ID);
            return u;
        });
        when(candidatRepository.save(any(Candidat.class))).thenAnswer(inv -> {
            Candidat c = inv.getArgument(0);
            c.setId(CANDIDAT_ID);
            return c;
        });
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        CandidatDTO result = candidatService.creer(request, TENANT_ID);

        assertNotNull(result);
        assertEquals(CANDIDAT_ID, result.id());
        assertEquals("AB123456", result.cin());
        verify(utilisateurRepository).existsByEmail(request.email());
        verify(candidatRepository).findByTenantIdAndCin(TENANT_ID, request.cin());
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void creer_shouldThrowException_whenEmailExists() {
        CreateCandidatRequest request = new CreateCandidatRequest(
                "Dupont", "Jean", "existing@email.com",
                "0600000000", "pass", "CD789012",
                CategoriePermis.B, null
        );

        when(utilisateurRepository.existsByEmail(request.email())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> candidatService.creer(request, TENANT_ID));
        assertTrue(ex.getMessage().contains("email existe deja"));
        verify(candidatRepository, never()).save(any());
    }

    @Test
    void creer_shouldThrowException_whenCinExists() {
        CreateCandidatRequest request = new CreateCandidatRequest(
                "Dupont", "Jean", "new@email.com",
                "0600000000", "pass", "DUPLICATE_CIN",
                CategoriePermis.B, null
        );

        when(utilisateurRepository.existsByEmail(request.email())).thenReturn(false);
        when(candidatRepository.findByTenantIdAndCin(TENANT_ID, request.cin()))
                .thenReturn(Optional.of(new Candidat()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> candidatService.creer(request, TENANT_ID));
        assertTrue(ex.getMessage().contains("CIN existe deja"));
    }

    @Test
    void modifier_shouldUpdateCandidatAndUtilisateur() {
        CandidatDTO dto = new CandidatDTO(
                CANDIDAT_ID, UTILISATEUR_ID, TENANT_ID,
                "NEW_CIN", CategoriePermis.C, StatutParcours.CODE_VALIDE,
                FORFAIT_ID, 15, 25,
                "NouveauNom", "NouveauPrenom", "nouveau@email.com", "0699999999",
                null, null, null, null, null, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.existsByEmail("nouveau@email.com")).thenReturn(false);
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        CandidatDTO result = candidatService.modifier(TENANT_ID, CANDIDAT_ID, dto);

        assertNotNull(result);
        verify(candidatRepository).save(candidat);
        verify(utilisateurRepository).save(utilisateur);
        assertEquals("NEW_CIN", candidat.getCin());
        assertEquals(CategoriePermis.C, candidat.getCategoriePermis());
        assertEquals("NouveauNom", utilisateur.getNom());
        assertEquals("nouveau@email.com", utilisateur.getEmail());
    }

    @Test
    void modifier_shouldThrowException_whenEmailAlreadyUsed() {
        CandidatDTO dto = new CandidatDTO(
                CANDIDAT_ID, UTILISATEUR_ID, TENANT_ID,
                null, null, null, null, null, null,
                null, null, "taken@email.com", null,
                null, null, null, null, null, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> candidatService.modifier(TENANT_ID, CANDIDAT_ID, dto));
    }

    @Test
    void modifier_shouldRejectPresenteExamen_whenQuotaNotReached() {
        CandidatDTO dto = new CandidatDTO(
                CANDIDAT_ID, UTILISATEUR_ID, TENANT_ID,
                null, null, StatutParcours.PRESENTE_EXAMEN, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null
        );

        StatutQuotaHeuresDTO statutQuota = new StatutQuotaHeuresDTO(
                CategoriePermis.B, 60, 20, 20, 10,
                false, false, false
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(quotaHeuresService.calculerStatutPourCandidat(TENANT_ID, CANDIDAT_ID)).thenReturn(statutQuota);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> candidatService.modifier(TENANT_ID, CANDIDAT_ID, dto));
        assertTrue(ex.getMessage().contains("quota non atteint"));
        verify(candidatRepository, never()).save(any());
    }

    @Test
    void modifier_shouldAllowPresenteExamen_whenQuotaReached() {
        candidat.setHeuresTheorieValidees(60);
        candidat.setHeuresConduiteValidees(20);

        CandidatDTO dto = new CandidatDTO(
                CANDIDAT_ID, UTILISATEUR_ID, TENANT_ID,
                null, null, StatutParcours.PRESENTE_EXAMEN, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null
        );

        StatutQuotaHeuresDTO statutQuota = new StatutQuotaHeuresDTO(
                CategoriePermis.B, 60, 20, 60, 20,
                true, true, true
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(quotaHeuresService.calculerStatutPourCandidat(TENANT_ID, CANDIDAT_ID)).thenReturn(statutQuota);
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        CandidatDTO result = candidatService.modifier(TENANT_ID, CANDIDAT_ID, dto);

        assertNotNull(result);
        assertEquals(StatutParcours.PRESENTE_EXAMEN, candidat.getStatutParcours());
    }

    @Test
    void compteurHeures_shouldCalculateRemaining_withForfait() {
        Forfait forfait = Forfait.builder()
                .id(FORFAIT_ID)
                .heuresConduiteMin(20)
                .heuresCodeMin(40)
                .build();

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(forfaitRepository.findByTenantIdAndId(TENANT_ID, FORFAIT_ID)).thenReturn(Optional.of(forfait));

        CompteurHeuresDTO result = candidatService.compteurHeures(TENANT_ID, CANDIDAT_ID);

        assertEquals(10, result.heuresConduiteValidees());
        assertEquals(20, result.heuresTheorieValidees());
        assertEquals(10, result.heuresConduiteRestantes());
        assertEquals(20, result.heuresTheorieRestantes());
    }

    @Test
    void compteurHeures_shouldReturnZeroRemaining_whenNoForfait() {
        candidat.setForfaitId(null);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));

        CompteurHeuresDTO result = candidatService.compteurHeures(TENANT_ID, CANDIDAT_ID);

        assertEquals(0, result.heuresConduiteRestantes());
        assertEquals(0, result.heuresTheorieRestantes());
    }

    @Test
    void solde_shouldCalculateSoldeCorrectly() {
        Forfait forfait = Forfait.builder()
                .id(FORFAIT_ID)
                .prix(new BigDecimal("3500.00"))
                .build();

        Paiement p1 = new Paiement();
        p1.setMontant(new BigDecimal("1500.00"));
        Paiement p2 = new Paiement();
        p2.setMontant(new BigDecimal("1000.00"));

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(forfaitRepository.findByTenantIdAndId(TENANT_ID, FORFAIT_ID)).thenReturn(Optional.of(forfait));
        when(paiementRepository.findAllByTenantIdAndCandidatId(TENANT_ID, CANDIDAT_ID))
                .thenReturn(List.of(p1, p2));

        SoldeCandidatDTO result = candidatService.solde(TENANT_ID, CANDIDAT_ID);

        assertEquals(new BigDecimal("3500.00"), result.totalDu());
        assertEquals(new BigDecimal("2500.00"), result.totalPaye());
        assertEquals(new BigDecimal("1000.00"), result.soldeRestant());
    }

    @Test
    void solde_shouldReturnZeroTotalDu_whenNoForfait() {
        candidat.setForfaitId(null);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(paiementRepository.findAllByTenantIdAndCandidatId(TENANT_ID, CANDIDAT_ID)).thenReturn(List.of());

        SoldeCandidatDTO result = candidatService.solde(TENANT_ID, CANDIDAT_ID);

        assertEquals(BigDecimal.ZERO, result.totalDu());
    }

    @Test
    void getCandidatEntityByUtilisateurId_shouldReturnCandidat_whenFound() {
        when(candidatRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(Optional.of(candidat));

        Candidat result = candidatService.getCandidatEntityByUtilisateurId(UTILISATEUR_ID);

        assertNotNull(result);
        assertEquals(CANDIDAT_ID, result.getId());
    }

    @Test
    void getCandidatEntityByUtilisateurId_shouldReturnNull_whenNotFound() {
        when(candidatRepository.findByUtilisateurId(999L)).thenReturn(Optional.empty());

        Candidat result = candidatService.getCandidatEntityByUtilisateurId(999L);

        assertNull(result);
    }

    @Test
    void planifierExamenTheorie_shouldSetDateAndResultat() {
        LocalDate date = LocalDate.of(2025, 6, 15);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        CandidatDTO result = candidatService.planifierExamenTheorie(TENANT_ID, CANDIDAT_ID, date);

        assertNotNull(result);
        assertEquals(date, candidat.getDateExamenTheorie());
        assertEquals(ResultatExamen.EN_ATTENTE, candidat.getResultatExamenTheorie());
    }

    @Test
    void enregistrerResultatTheorie_shouldIncrementTentatives() {
        candidat.setNombreTentativesTheorie(1);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        CandidatDTO result = candidatService.enregistrerResultatTheorie(
                TENANT_ID, CANDIDAT_ID, ResultatExamen.ADMIS);

        assertEquals(2, candidat.getNombreTentativesTheorie());
        assertEquals(ResultatExamen.ADMIS, candidat.getResultatExamenTheorie());
    }

    @Test
    void planifierExamenPratique_shouldSetDateAndResultat() {
        LocalDate date = LocalDate.of(2025, 7, 20);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        CandidatDTO result = candidatService.planifierExamenPratique(TENANT_ID, CANDIDAT_ID, date);

        assertNotNull(result);
        assertEquals(date, candidat.getDateExamenPratique());
        assertEquals(ResultatExamen.EN_ATTENTE, candidat.getResultatExamenPratique());
    }

    @Test
    void enregistrerResultatPratique_shouldIncrementTentatives() {
        candidat.setNombreTentativesPratique(0);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        CandidatDTO result = candidatService.enregistrerResultatPratique(
                TENANT_ID, CANDIDAT_ID, ResultatExamen.REFUSE);

        assertEquals(1, candidat.getNombreTentativesPratique());
        assertEquals(ResultatExamen.REFUSE, candidat.getResultatExamenPratique());
    }

    @Test
    void enregistrerResultat_shouldNotIncrement_whenEnAttente() {
        candidat.setNombreTentativesPratique(2);

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));

        candidatService.enregistrerResultatPratique(
                TENANT_ID, CANDIDAT_ID, ResultatExamen.EN_ATTENTE);

        assertEquals(2, candidat.getNombreTentativesPratique());
    }
}
