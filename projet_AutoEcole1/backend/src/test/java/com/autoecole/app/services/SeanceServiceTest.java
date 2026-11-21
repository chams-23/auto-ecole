package com.autoecole.app.services;

import com.autoecole.app.dto.CreateSeanceRequest;
import com.autoecole.app.dto.DeclarerEffectueeRequest;
import com.autoecole.app.dto.ReprogrammerSeanceRequest;
import com.autoecole.app.dto.SeanceDTO;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.Seance;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.CategoriePermis;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutParcours;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.enums.TypeSeance;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.SeanceRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeanceServiceTest {

    @Mock
    private SeanceRepository seanceRepository;

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SeanceService seanceService;

    private static final Long TENANT_ID = 1L;
    private static final Long SEANCE_ID = 50L;
    private static final Long CANDIDAT_ID = 10L;
    private static final Long CANDIDAT_UTILISATEUR_ID = 20L;
    private static final Long MONITEUR_ID = 30L;
    private static final Long SECRETAIRE_ID = 40L;
    private static final Long ADMIN_ID = 5L;

    private Seance seanceConduite;
    private Seance seanceTheorie;
    private Candidat candidat;
    private Utilisateur moniteurUser;
    private Utilisateur candidatUser;
    private Utilisateur secretaireUser;
    private Utilisateur adminUser;

    @BeforeEach
    void setUp() {
        candidatUser = Utilisateur.builder()
                .id(CANDIDAT_UTILISATEUR_ID)
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
                .utilisateurId(CANDIDAT_UTILISATEUR_ID)
                .tenantId(TENANT_ID)
                .cin("AB123456")
                .categoriePermis(CategoriePermis.B)
                .statutParcours(StatutParcours.INSCRIT)
                .heuresConduiteValidees(10)
                .heuresTheorieValidees(20)
                .build();

        moniteurUser = Utilisateur.builder()
                .id(MONITEUR_ID)
                .tenantId(TENANT_ID)
                .role(Role.MONITEUR)
                .nom("Bennani")
                .prenom("Karim")
                .email("karim.moniteur@email.com")
                .statut(StatutUtilisateur.ACTIF)
                .passwordHash("encoded")
                .build();

        secretaireUser = Utilisateur.builder()
                .id(SECRETAIRE_ID)
                .tenantId(TENANT_ID)
                .role(Role.SECRETAIRE)
                .nom("Tazi")
                .prenom("Salma")
                .email("salma@email.com")
                .statut(StatutUtilisateur.ACTIF)
                .passwordHash("encoded")
                .build();

        adminUser = Utilisateur.builder()
                .id(ADMIN_ID)
                .tenantId(TENANT_ID)
                .role(Role.ADMIN)
                .nom("Admin")
                .prenom("Principal")
                .email("admin@email.com")
                .statut(StatutUtilisateur.ACTIF)
                .passwordHash("encoded")
                .build();

        seanceConduite = Seance.builder()
                .id(SEANCE_ID)
                .tenantId(TENANT_ID)
                .type(TypeSeance.CONDUITE)
                .date(LocalDate.of(2025, 5, 20))
                .heureDebut(LocalTime.of(9, 0))
                .heureFin(LocalTime.of(10, 0))
                .candidatId(CANDIDAT_ID)
                .intervenantId(MONITEUR_ID)
                .statutValidation(StatutValidation.EN_ATTENTE)
                .observations(null)
                .build();

        seanceTheorie = Seance.builder()
                .id(100L)
                .tenantId(TENANT_ID)
                .type(TypeSeance.THEORIE)
                .date(LocalDate.of(2025, 5, 20))
                .heureDebut(LocalTime.of(14, 0))
                .heureFin(LocalTime.of(16, 0))
                .candidatId(CANDIDAT_ID)
                .intervenantId(SECRETAIRE_ID)
                .statutValidation(StatutValidation.EN_ATTENTE)
                .build();
    }

    @Test
    void filtrer_shouldFilterByDate_whenOnlyDateProvided() {
        LocalDate date = LocalDate.of(2025, 5, 20);
        when(seanceRepository.findAllByTenantIdAndDate(TENANT_ID, date)).thenReturn(List.of(seanceConduite, seanceTheorie));
        setupFindByIdMocks();

        List<SeanceDTO> result = seanceService.filtrer(TENANT_ID, date, null, null);

        assertEquals(2, result.size());
        verify(seanceRepository).findAllByTenantIdAndDate(TENANT_ID, date);
    }

    @Test
    void filtrer_shouldFilterByMoniteurId_whenOnlyMoniteurProvided() {
        when(seanceRepository.findAllByTenantIdAndIntervenantId(TENANT_ID, MONITEUR_ID))
                .thenReturn(List.of(seanceConduite));
        setupFindByIdMocks();

        List<SeanceDTO> result = seanceService.filtrer(TENANT_ID, null, MONITEUR_ID, null);

        assertEquals(1, result.size());
        verify(seanceRepository).findAllByTenantIdAndIntervenantId(TENANT_ID, MONITEUR_ID);
    }

    @Test
    void filtrer_shouldUseDefaultWeek_whenNoFilters() {
        LocalDate debut = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate fin = debut.plusDays(6);
        when(seanceRepository.findAllByTenantIdAndDateBetween(TENANT_ID, debut, fin))
                .thenReturn(List.of(seanceConduite));
        setupFindByIdMocks();

        List<SeanceDTO> result = seanceService.filtrer(TENANT_ID, null, null, null);

        assertEquals(1, result.size());
        verify(seanceRepository).findAllByTenantIdAndDateBetween(eq(TENANT_ID), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getById_shouldReturnSeanceDTO_whenFound() {
        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        setupFindByIdMocks();

        SeanceDTO result = seanceService.getById(TENANT_ID, SEANCE_ID);

        assertNotNull(result);
        assertEquals(SEANCE_ID, result.id());
        assertEquals(TypeSeance.CONDUITE, result.type());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(seanceRepository.findByTenantIdAndId(TENANT_ID, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> seanceService.getById(TENANT_ID, 999L));
    }

    @Test
    void creer_shouldCreateSeanceAndSendNotification() {
        CreateSeanceRequest request = new CreateSeanceRequest(
                TypeSeance.CONDUITE,
                LocalDate.of(2025, 6, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                CANDIDAT_ID,
                MONITEUR_ID,
                null, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findByTenantIdAndId(TENANT_ID, MONITEUR_ID)).thenReturn(Optional.of(moniteurUser));
        when(seanceRepository.findAllByTenantIdAndIntervenantId(TENANT_ID, MONITEUR_ID)).thenReturn(List.of());
        when(seanceRepository.save(any(Seance.class))).thenAnswer(inv -> {
            Seance s = inv.getArgument(0);
            s.setId(SEANCE_ID);
            return s;
        });
        setupFindByIdMocks();

        SeanceDTO result = seanceService.creer(request, TENANT_ID);

        assertNotNull(result);
        assertEquals(SEANCE_ID, result.id());
        assertEquals(StatutValidation.EN_ATTENTE, result.statutValidation());
        verify(notificationService).creerNotificationSeance(eq(TENANT_ID), any(Seance.class), eq("NOUVELLE_SEANCE"));
    }

    @Test
    void creer_shouldThrow_whenHeureFinBeforeHeureDebut() {
        CreateSeanceRequest request = new CreateSeanceRequest(
                TypeSeance.CONDUITE,
                LocalDate.of(2025, 6, 1),
                LocalTime.of(11, 0),
                LocalTime.of(10, 0),
                CANDIDAT_ID, MONITEUR_ID, null, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findByTenantIdAndId(TENANT_ID, MONITEUR_ID)).thenReturn(Optional.of(moniteurUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> seanceService.creer(request, TENANT_ID));
        assertTrue(ex.getMessage().contains("heure de fin doit etre apres"));
    }

    @Test
    void creer_shouldThrow_whenConflitHoraire() {
        CreateSeanceRequest request = new CreateSeanceRequest(
                TypeSeance.CONDUITE,
                seanceConduite.getDate(),
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                CANDIDAT_ID, MONITEUR_ID, null, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(utilisateurRepository.findByTenantIdAndId(TENANT_ID, MONITEUR_ID)).thenReturn(Optional.of(moniteurUser));
        when(seanceRepository.findAllByTenantIdAndIntervenantId(TENANT_ID, MONITEUR_ID))
                .thenReturn(List.of(seanceConduite));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> seanceService.creer(request, TENANT_ID));
        assertTrue(ex.getMessage().contains("Conflit horaire"));
    }

    @Test
    void creer_shouldThrow_whenCandidatNotFound() {
        CreateSeanceRequest request = new CreateSeanceRequest(
                TypeSeance.CONDUITE,
                LocalDate.of(2025, 6, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                999L, MONITEUR_ID, null, null
        );

        when(candidatRepository.findByTenantIdAndId(TENANT_ID, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> seanceService.creer(request, TENANT_ID));
    }

    @Test
    void reprogrammer_shouldUpdateSeanceDetails() {
        ReprogrammerSeanceRequest request = new ReprogrammerSeanceRequest(
                LocalDate.of(2025, 6, 10),
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                TypeSeance.CONDUITE,
                null
        );

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(seanceRepository.findAllByTenantIdAndIntervenantId(TENANT_ID, MONITEUR_ID)).thenReturn(List.of());
        when(seanceRepository.save(any(Seance.class))).thenReturn(seanceConduite);
        setupFindByIdMocks();

        SeanceDTO result = seanceService.reprogrammer(TENANT_ID, SEANCE_ID, request);

        assertNotNull(result);
        assertEquals(LocalDate.of(2025, 6, 10), seanceConduite.getDate());
        assertEquals(LocalTime.of(14, 0), seanceConduite.getHeureDebut());
        assertEquals(StatutValidation.EN_ATTENTE, seanceConduite.getStatutValidation());
        verify(notificationService).creerNotificationSeance(eq(TENANT_ID), any(Seance.class), eq("SEANCE_REPROGRAMMEE"));
    }

    @Test
    void reprogrammer_shouldThrow_whenSeanceValidee() {
        seanceConduite.setStatutValidation(StatutValidation.VALIDEE);
        ReprogrammerSeanceRequest request = new ReprogrammerSeanceRequest(
                LocalDate.of(2025, 6, 10), null, null, null, null
        );

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.reprogrammer(TENANT_ID, SEANCE_ID, request));
        assertTrue(ex.getMessage().contains("validee ou verrouillee"));
    }

    @Test
    void supprimer_shouldDeleteSeance_whenNotValidee() {
        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));

        seanceService.supprimer(TENANT_ID, SEANCE_ID);

        verify(seanceRepository).delete(seanceConduite);
    }

    @Test
    void supprimer_shouldThrow_whenSeanceVerrouillee() {
        seanceConduite.setStatutValidation(StatutValidation.VERROUILLEE);
        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.supprimer(TENANT_ID, SEANCE_ID));
        assertTrue(ex.getMessage().contains("validee ou verrouillee"));
        verify(seanceRepository, never()).delete(any());
    }

    @Test
    void declarerEffectuee_shouldWorkForMoniteurInstructeur_onConduite() {
        DeclarerEffectueeRequest request = new DeclarerEffectueeRequest(45.5, "Bon travail");

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(MONITEUR_ID)).thenReturn(Optional.of(moniteurUser));
        when(seanceRepository.save(any(Seance.class))).thenReturn(seanceConduite);
        setupFindByIdMocks();

        SeanceDTO result = seanceService.declarerEffectuee(TENANT_ID, SEANCE_ID, request, MONITEUR_ID);

        assertEquals(StatutValidation.DECLAREE_EFFECTUEE, seanceConduite.getStatutValidation());
        assertEquals(45.5, seanceConduite.getKilometrage());
        verify(notificationService).creerNotificationSeance(eq(TENANT_ID), any(Seance.class), eq("SEANCE_DECLAREE"));
    }

    @Test
    void declarerEffectuee_shouldWorkForSecretaire_onTheorie() {
        DeclarerEffectueeRequest request = new DeclarerEffectueeRequest(null, null);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, 100L)).thenReturn(Optional.of(seanceTheorie));
        when(utilisateurRepository.findById(SECRETAIRE_ID)).thenReturn(Optional.of(secretaireUser));
        when(seanceRepository.save(any(Seance.class))).thenReturn(seanceTheorie);
        setupFindByIdMocks();

        SeanceDTO result = seanceService.declarerEffectuee(TENANT_ID, 100L, request, SECRETAIRE_ID);

        assertEquals(StatutValidation.DECLAREE_EFFECTUEE, seanceTheorie.getStatutValidation());
    }

    @Test
    void declarerEffectuee_shouldThrow_whenNotMoniteurInstructeurForConduite() {
        DeclarerEffectueeRequest request = new DeclarerEffectueeRequest(null, null);
        Utilisateur autreMoniteur = Utilisateur.builder()
                .id(999L).role(Role.MONITEUR).tenantId(TENANT_ID)
                .nom("X").prenom("Y").email("x@y.com").statut(StatutUtilisateur.ACTIF).passwordHash("h").build();

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.of(autreMoniteur));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.declarerEffectuee(TENANT_ID, SEANCE_ID, request, 999L));
        assertTrue(ex.getMessage().contains("Seul le moniteur intervenant"));
    }

    @Test
    void declarerEffectuee_shouldThrow_whenSeanceDejaTraitee() {
        seanceConduite.setStatutValidation(StatutValidation.VALIDEE);
        DeclarerEffectueeRequest request = new DeclarerEffectueeRequest(null, null);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.declarerEffectuee(TENANT_ID, SEANCE_ID, request, ADMIN_ID));
        assertTrue(ex.getMessage().contains("Seance deja traitee"));
    }

    @Test
    void valider_shouldConfirmByCandidate_andIncrementHeures() {
        seanceConduite.setStatutValidation(StatutValidation.DECLAREE_EFFECTUEE);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(CANDIDAT_UTILISATEUR_ID)).thenReturn(Optional.of(candidatUser));
        when(candidatRepository.findByUtilisateurId(CANDIDAT_UTILISATEUR_ID)).thenReturn(Optional.of(candidat));
        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(seanceRepository.save(any(Seance.class))).thenReturn(seanceConduite);
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        setupFindByIdMocks();

        SeanceDTO result = seanceService.valider(TENANT_ID, SEANCE_ID, CANDIDAT_UTILISATEUR_ID);

        assertEquals(StatutValidation.VERROUILLEE, seanceConduite.getStatutValidation());
        assertEquals(11, candidat.getHeuresConduiteValidees());
        verify(notificationService).creerNotificationSeance(eq(TENANT_ID), any(Seance.class), eq("SEANCE_VALIDEE"));
    }

    @Test
    void valider_shouldArbitrerByAdmin_onContestee() {
        seanceTheorie.setStatutValidation(StatutValidation.CONTESTEE);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, 100L)).thenReturn(Optional.of(seanceTheorie));
        when(utilisateurRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
        when(candidatRepository.findByTenantIdAndId(TENANT_ID, CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        when(seanceRepository.save(any(Seance.class))).thenReturn(seanceTheorie);
        when(candidatRepository.save(any(Candidat.class))).thenReturn(candidat);
        setupFindByIdMocks();

        SeanceDTO result = seanceService.valider(TENANT_ID, 100L, ADMIN_ID);

        assertEquals(StatutValidation.VERROUILLEE, seanceTheorie.getStatutValidation());
        assertEquals(22, candidat.getHeuresTheorieValidees());
    }

    @Test
    void valider_shouldThrow_whenNotCandidateForDeclaree() {
        seanceConduite.setStatutValidation(StatutValidation.DECLAREE_EFFECTUEE);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(SECRETAIRE_ID)).thenReturn(Optional.of(secretaireUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.valider(TENANT_ID, SEANCE_ID, SECRETAIRE_ID));
        assertTrue(ex.getMessage().contains("Seul le candidat concerne peut confirmer"));
    }

    @Test
    void valider_shouldThrow_whenNotAdminForContestee() {
        seanceConduite.setStatutValidation(StatutValidation.CONTESTEE);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(MONITEUR_ID)).thenReturn(Optional.of(moniteurUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.valider(TENANT_ID, SEANCE_ID, MONITEUR_ID));
        assertTrue(ex.getMessage().contains("administrateur ou une secretaire peut arbitrer"));
    }

    @Test
    void contester_shouldSetStatutContestee_forCandidate() {
        seanceConduite.setStatutValidation(StatutValidation.DECLAREE_EFFECTUEE);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(CANDIDAT_UTILISATEUR_ID)).thenReturn(Optional.of(candidatUser));
        when(candidatRepository.findByUtilisateurId(CANDIDAT_UTILISATEUR_ID)).thenReturn(Optional.of(candidat));
        when(seanceRepository.save(any(Seance.class))).thenReturn(seanceConduite);
        setupFindByIdMocks();

        SeanceDTO result = seanceService.contester(TENANT_ID, SEANCE_ID, CANDIDAT_UTILISATEUR_ID, "Je n'étais pas présent");

        assertEquals(StatutValidation.CONTESTEE, seanceConduite.getStatutValidation());
        assertTrue(seanceConduite.getObservations().contains("[CONTESTATION]"));
        verify(notificationService).creerNotificationSeance(eq(TENANT_ID), any(Seance.class), eq("SEANCE_CONTESTEE"));
    }

    @Test
    void contester_shouldThrow_whenNotDeclareeEffectuee() {
        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.contester(TENANT_ID, SEANCE_ID, CANDIDAT_UTILISATEUR_ID, "X"));
        assertTrue(ex.getMessage().contains("Seule une seance declaree effectuee peut etre contestee"));
    }

    @Test
    void contester_shouldThrow_whenNotCandidateConcerned() {
        seanceConduite.setStatutValidation(StatutValidation.DECLAREE_EFFECTUEE);

        when(seanceRepository.findByTenantIdAndId(TENANT_ID, SEANCE_ID)).thenReturn(Optional.of(seanceConduite));
        when(utilisateurRepository.findById(SECRETAIRE_ID)).thenReturn(Optional.of(secretaireUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seanceService.contester(TENANT_ID, SEANCE_ID, SECRETAIRE_ID, "X"));
        assertTrue(ex.getMessage().contains("Seul le candidat concerne peut contester"));
    }

    private void setupFindByIdMocks() {
        lenient().when(candidatRepository.findById(CANDIDAT_ID)).thenReturn(Optional.of(candidat));
        lenient().when(utilisateurRepository.findById(MONITEUR_ID)).thenReturn(Optional.of(moniteurUser));
        lenient().when(utilisateurRepository.findById(SECRETAIRE_ID)).thenReturn(Optional.of(secretaireUser));
        lenient().when(utilisateurRepository.findById(CANDIDAT_UTILISATEUR_ID)).thenReturn(Optional.of(candidatUser));
    }
}
