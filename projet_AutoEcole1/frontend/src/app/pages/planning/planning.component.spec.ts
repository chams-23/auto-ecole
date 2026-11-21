import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { PlanningComponent } from './planning.component';
import { SeanceService } from '../../core/services/seance.service';
import { CandidatService } from '../../core/services/candidat.service';
import { UtilisateurService } from '../../core/services/utilisateur.service';
import { AuthService } from '../../core/services/auth.service';
import { SeanceDTO, TypeSeance, StatutValidation } from '../../core/models/seance';
import { CandidatDTO } from '../../core/models/candidat';
import { UserDTO } from '../../core/models/user';

describe('PlanningComponent', () => {
  let component: PlanningComponent;
  let fixture: ComponentFixture<PlanningComponent>;
  let seanceServiceSpy: jasmine.SpyObj<SeanceService>;
  let candidatServiceSpy: jasmine.SpyObj<CandidatService>;
  let utilisateurServiceSpy: jasmine.SpyObj<UtilisateurService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let httpMock: HttpTestingController;

  const today = new Date();
  const toIso = (d: Date) => d.toISOString().split('T')[0];

  const mockCandidats: CandidatDTO[] = [
    {
      id: 1, utilisateurId: 10, tenantId: 1, cin: 'AB1', categoriePermis: 'B',
      statutParcours: 'INSCRIT', forfaitId: 1, heuresConduiteValidees: 0,
      heuresTheorieValidees: 0,
      nom: 'El Amrani', prenom: 'Youssef', email: 'y@email.com', telephone: '06',
      dateExamenTheorie: null, resultatExamenTheorie: null, nombreTentativesTheorie: 0,
      dateExamenPratique: null, resultatExamenPratique: null, nombreTentativesPratique: 0
    },
    {
      id: 2, utilisateurId: 20, tenantId: 1, cin: 'CD2', categoriePermis: 'B',
      statutParcours: 'CODE_VALIDE', forfaitId: 1, heuresConduiteValidees: 0,
      heuresTheorieValidees: 0,
      nom: 'Benali', prenom: 'Sara', email: 's@email.com', telephone: '07',
      dateExamenTheorie: null, resultatExamenTheorie: null, nombreTentativesTheorie: 0,
      dateExamenPratique: null, resultatExamenPratique: null, nombreTentativesPratique: 0
    }
  ];

  const mockMoniteurs: UserDTO[] = [
    { id: 1, tenantId: 1, role: 'MONITEUR', nom: 'Bennani', prenom: 'Karim', email: 'k@m.com', telephone: '06', statut: 'ACTIF' },
    { id: 2, tenantId: 1, role: 'MONITEUR', nom: 'Fassi', prenom: 'Ali', email: 'a@m.com', telephone: '07', statut: 'ACTIF' },
    { id: 5, tenantId: 1, role: 'SECRETAIRE', nom: 'Idrissi', prenom: 'Salma', email: 's@m.com', telephone: '05', statut: 'ACTIF' }
  ];

  const mockSeances: SeanceDTO[] = [
    {
      id: 101, tenantId: 1, type: 'CONDUITE',
      date: toIso(today), heureDebut: '09:00', heureFin: '10:00',
      candidatId: 1, candidatNom: 'El Amrani', candidatPrenom: 'Youssef',
      intervenantId: 1, intervenantNom: 'Bennani', intervenantPrenom: 'Karim',
      statutValidation: 'EN_ATTENTE', kilometrage: null, observations: null
    }
  ];

  const adminUser: UserDTO = {
    id: 2, tenantId: 1, role: 'ADMIN', nom: 'Zahi', prenom: 'Fatima',
    email: 'f@autoecole.ma', telephone: '06', statut: 'ACTIF'
  };

  const moniteurUser: UserDTO = {
    id: 1, tenantId: 1, role: 'MONITEUR', nom: 'Bennani', prenom: 'Karim',
    email: 'k@autoecole.ma', telephone: '06', statut: 'ACTIF'
  };

  const candidatUser: UserDTO = {
    id: 10, tenantId: 1, role: 'CANDIDAT', nom: 'El Amrani', prenom: 'Youssef',
    email: 'youssef@email.ma', telephone: '06', statut: 'ACTIF'
  };

  beforeEach(async () => {
    seanceServiceSpy = jasmine.createSpyObj('SeanceService', [
      'filtrer', 'creer', 'supprimer', 'declarerEffectuee', 'valider', 'contester'
    ]);
    candidatServiceSpy = jasmine.createSpyObj('CandidatService', ['lister']);
    utilisateurServiceSpy = jasmine.createSpyObj('UtilisateurService', ['lister']);
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isAdmin', 'isSecretaire', 'isMoniteur', 'isCandidat', 'canWrite',
      'getCurrentUser', 'isDemoMode'
    ]);

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, HttpClientTestingModule, PlanningComponent],
      providers: [
        { provide: SeanceService, useValue: seanceServiceSpy },
        { provide: CandidatService, useValue: candidatServiceSpy },
        { provide: UtilisateurService, useValue: utilisateurServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  function setupAuth(user: UserDTO, roleChecks?: Partial<Record<'isAdmin'|'isSecretaire'|'isMoniteur'|'isCandidat'|'canWrite'>>) {
    authServiceSpy.getCurrentUser.and.returnValue(user);
    authServiceSpy.isAdmin.and.returnValue(roleChecks?.isAdmin ?? user.role === 'ADMIN' || user.role === 'SUPER_ADMIN');
    authServiceSpy.isSecretaire.and.returnValue(roleChecks?.isSecretaire ?? user.role === 'SECRETAIRE');
    authServiceSpy.isMoniteur.and.returnValue(roleChecks?.isMoniteur ?? user.role === 'MONITEUR');
    authServiceSpy.isCandidat.and.returnValue(roleChecks?.isCandidat ?? user.role === 'CANDIDAT');
    authServiceSpy.canWrite.and.callFake((e) => {
      if (roleChecks?.canWrite !== undefined) return roleChecks.canWrite;
      return user.role === 'ADMIN' || user.role === 'SUPER_ADMIN' || user.role === 'SECRETAIRE';
    });
  }

  function createComponent(): void {
    fixture = TestBed.createComponent(PlanningComponent);
    component = fixture.componentInstance;
  }

  it('devrait creer le composant', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(of(mockSeances));
    createComponent();
    fixture.detectChanges();
    tick();
    expect(component).toBeTruthy();
  }));

  it('ngOnInit devrait charger candidats, moniteurs et seances', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(of(mockSeances));
    createComponent();
    component.ngOnInit();
    tick();
    expect(component.candidats.length).toBe(2);
    expect(component.moniteurs.length).toBe(2);
    expect(component.seances.length).toBe(1);
    expect(seanceServiceSpy.filtrer).toHaveBeenCalled();
  }));

  it('devrait utiliser les mocks en cas d\'erreur de chargement des seances', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(throwError(() => new Error('API down')));
    createComponent();
    component.ngOnInit();
    tick();
    expect(component.seances.length).toBe(0);
    expect(component.seancesMock.length).toBeGreaterThan(0);
    expect(component.loading).toBeFalse();
  }));

  it('devrait appliquer le filtre moniteur automatiquement pour un role MONITEUR', fakeAsync(() => {
    setupAuth(moniteurUser);
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(of(mockSeances));
    createComponent();
    component.ngOnInit();
    tick();
    expect(component.filtreMoniteur).toBe(1);
  }));

  it('devrait appliquer le filtre candidat automatiquement pour un role CANDIDAT', fakeAsync(() => {
    setupAuth(candidatUser);
    const listeCandAvecUtilisateurId: CandidatDTO[] = [
      { ...mockCandidats[0], utilisateurId: 10 }
    ];
    candidatServiceSpy.lister.and.returnValue(of(listeCandAvecUtilisateurId));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    expect(component.filtreCandidat).toBe(1);
  }));

  it('isAdminOrSecretaire should return true for ADMIN', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    expect(component.isAdminOrSecretaire()).toBeTrue();
  }));

  it('isAdminOrSecretaire should return false for CANDIDAT', fakeAsync(() => {
    setupAuth(candidatUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    expect(component.isAdminOrSecretaire()).toBeFalse();
  }));

  it('peutDeclarer should return true for ADMIN on EN_ATTENTE seance', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = {
      ...mockSeances[0],
      statutValidation: 'EN_ATTENTE',
      type: 'CONDUITE',
      intervenantId: 99
    };
    expect(component.peutDeclarer(s)).toBeTrue();
  }));

  it('peutDeclarer should return true for MONITEUR intervenant on CONDUITE', fakeAsync(() => {
    setupAuth(moniteurUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = {
      ...mockSeances[0],
      statutValidation: 'EN_ATTENTE',
      type: 'CONDUITE',
      intervenantId: 1
    };
    expect(component.peutDeclarer(s)).toBeTrue();
  }));

  it('peutDeclarer should return false for non-intervenant MONITEUR', fakeAsync(() => {
    setupAuth(moniteurUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = {
      ...mockSeances[0],
      statutValidation: 'EN_ATTENTE',
      type: 'CONDUITE',
      intervenantId: 99
    };
    expect(component.peutDeclarer(s)).toBeFalse();
  }));

  it('peutDeclarer should return false if seance is not EN_ATTENTE', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = {
      ...mockSeances[0],
      statutValidation: 'DECLAREE_EFFECTUEE'
    };
    expect(component.peutDeclarer(s)).toBeFalse();
  }));

  it('peutSupprimer should return true for ADMIN and EN_ATTENTE', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0], statutValidation: 'EN_ATTENTE' };
    expect(component.peutSupprimer(s)).toBeTrue();
  }));

  it('peutSupprimer should return false for VALIDEE seance', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0], statutValidation: 'VALIDEE' };
    expect(component.peutSupprimer(s)).toBeFalse();
  }));

  it('peutValider should return true for CANDIDAT owner on DECLAREE_EFFECTUEE', fakeAsync(() => {
    setupAuth(candidatUser);
    const liste: CandidatDTO[] = [ { ...mockCandidats[0], utilisateurId: 10 } ];
    candidatServiceSpy.lister.and.returnValue(of(liste));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = {
      ...mockSeances[0],
      statutValidation: 'DECLAREE_EFFECTUEE',
      candidatId: 1
    };
    expect(component.peutValider(s)).toBeTrue();
  }));

  it('peutContester should return true for CANDIDAT owner on DECLAREE_EFFECTUEE', fakeAsync(() => {
    setupAuth(candidatUser);
    const liste: CandidatDTO[] = [ { ...mockCandidats[0], utilisateurId: 10 } ];
    candidatServiceSpy.lister.and.returnValue(of(liste));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = {
      ...mockSeances[0],
      statutValidation: 'DECLAREE_EFFECTUEE',
      candidatId: 1
    };
    expect(component.peutContester(s)).toBeTrue();
  }));

  it('ouvrirForm should set showForm true and init form', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    component.ouvrirForm();
    expect(component.showForm).toBeTrue();
    expect(component.form.type).toBe('CONDUITE');
    expect(component.form.candidatId).toBe(mockCandidats[0].id);
    expect(component.form.intervenantId).toBe(mockMoniteurs[0].id);
  }));

  it('fermerForm should set showForm false', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    component.fermerForm();
    expect(component.showForm).toBeFalse();
  }));

  it('soumettreForm should call creer and update seances list', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    utilisateurServiceSpy.lister.and.returnValue(of(mockMoniteurs));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const nouvelle: SeanceDTO = {
      ...mockSeances[0], id: 999
    };
    seanceServiceSpy.creer.and.returnValue(of(nouvelle));
    component.form = {
      type: 'CONDUITE',
      date: toIso(today),
      heureDebut: '09:00', heureFin: '10:00',
      candidatId: 1, intervenantId: 1,
      kilometrage: null, observations: null
    };
    component.soumettreForm();
    tick();
    expect(seanceServiceSpy.creer).toHaveBeenCalled();
    expect(component.seances.find(s => s.id === 999)).toBeDefined();
    expect(component.showForm).toBeFalse();
    expect(component.successMessage).toBe('Séance créée');
  }));

  it('loadSeances should call filtrer', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of(mockSeances));
    createComponent();
    component.ngOnInit();
    tick();
    component.seances = [];
    component.filtreDate = '2025-05-20';
    component.filtreMoniteur = 1;
    component.filtreCandidat = 2;
    component.loadSeances();
    tick();
    expect(seanceServiceSpy.filtrer).toHaveBeenCalledWith({
      date: '2025-05-20', moniteur_id: 1, candidat_id: 2
    });
    expect(component.loading).toBeFalse();
  }));

  it('getStatutBadge should return correct badge for EN_ATTENTE', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const badge = component.getStatutBadge('EN_ATTENTE');
    expect(badge.class).toBe('status-warn');
    expect(badge.text).toContain('En attente');
  }));

  it('getStatutBadge should return correct badge for VALIDEE', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const badge = component.getStatutBadge('VALIDEE');
    expect(badge.class).toBe('status-go');
  }));

  it('seancesGroupeesParJour should group by date', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    component.seancesMock = [
      ...mockSeances,
      { ...mockSeances[0], id: 200, date: toIso(new Date(today.getTime() + 86400000)) }
    ];
    const groupes = component.seancesGroupeesParJour();
    expect(groupes.length).toBeGreaterThanOrEqual(1);
  }));

  it('formatJour should format a date correctly', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const result = component.formatJour('2025-05-20');
    expect(result.length).toBeGreaterThan(0);
  }));

  it('getCandidatNom should return full name', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0] };
    expect(component.getCandidatNom(s)).toContain('Youssef');
    expect(component.getCandidatNom(s)).toContain('El Amrani');
  }));

  it('toIsoDate should return YYYY-MM-DD', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const d = new Date(2025, 4, 20);
    const iso = component.toIsoDate(d);
    expect(iso).toMatch(/^\d{4}-\d{2}-\d{2}/);
  }));

  it('declarerEffectuee should call service and update statut', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0], id: 500 };
    component.selectedSeance = s;
    const declared: SeanceDTO = { ...s, statutValidation: 'DECLAREE_EFFECTUEE' };
    seanceServiceSpy.declarerEffectuee.and.returnValue(of(declared));
    component.declarerEffectuee();
    tick();
    expect(seanceServiceSpy.declarerEffectuee).toHaveBeenCalledWith(500);
    expect(component.showWorkflowModal).toBeFalse();
  }));

  it('valider should call service and close modal', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0], id: 501, statutValidation: 'DECLAREE_EFFECTUEE' };
    component.selectedSeance = s;
    const locked: SeanceDTO = { ...s, statutValidation: 'VERROUILLEE' };
    seanceServiceSpy.valider.and.returnValue(of(locked));
    component.valider();
    tick();
    expect(seanceServiceSpy.valider).toHaveBeenCalledWith(501);
    expect(component.showWorkflowModal).toBeFalse();
  }));

  it('confirmerContestation should call contester', fakeAsync(() => {
    setupAuth(candidatUser);
    const liste: CandidatDTO[] = [ { ...mockCandidats[0], utilisateurId: 10 } ];
    candidatServiceSpy.lister.and.returnValue(of(liste));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0], id: 502, statutValidation: 'DECLAREE_EFFECTUEE', candidatId: 1 };
    component.selectedSeance = s;
    component.contestationObs = 'Pas présent';
    const contested: SeanceDTO = { ...s, statutValidation: 'CONTESTEE' };
    seanceServiceSpy.contester.and.returnValue(of(contested));
    component.confirmerContestation();
    tick();
    expect(seanceServiceSpy.contester).toHaveBeenCalledWith(502, 'Pas présent');
  }));

  it('labelValider returns "Arbitrer" for CONTESTEE', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const s: SeanceDTO = { ...mockSeances[0], statutValidation: 'CONTESTEE' };
    expect(component.labelValider(s)).toContain('Arbitrer');
  }));

  it('changerSemaine should update filtreDate and reload', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const avant = component.filtreDate;
    component.changerSemaine(7);
    tick();
    expect(component.filtreDate).not.toBe(avant);
    expect(seanceServiceSpy.filtrer.calls.count()).toBeGreaterThan(1);
  }));

  it('buildMock should return 4 seances', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.lister.and.returnValue(of([]));
    utilisateurServiceSpy.lister.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    createComponent();
    component.ngOnInit();
    tick();
    const mocks = component.buildMock();
    expect(mocks.length).toBe(4);
  }));
});
