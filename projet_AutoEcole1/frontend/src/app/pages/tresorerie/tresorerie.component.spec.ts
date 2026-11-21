import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { TresorerieComponent } from './tresorerie.component';
import { TresorerieService } from '../../core/services/tresorerie.service';
import { CandidatService } from '../../core/services/candidat.service';
import { AuthService } from '../../core/services/auth.service';
import {
  PaiementDTO, ChargeDTO, BalanceTresorerieDTO
} from '../../core/models/tresorerie';
import { CandidatDTO } from '../../core/models/candidat';

describe('TresorerieComponent', () => {
  let component: TresorerieComponent;
  let fixture: ComponentFixture<TresorerieComponent>;
  let tresorerieServiceSpy: jasmine.SpyObj<TresorerieService>;
  let candidatServiceSpy: jasmine.SpyObj<CandidatService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const today = new Date();
  const iso = (d: Date) => d.toISOString().split('T')[0];

  const mockBalance: BalanceTresorerieDTO = {
    dateDebut: iso(new Date(today.getTime() - 30 * 86400000)),
    dateFin: iso(today),
    totalPaiements: 6500, totalCharges: 6300, soldeNet: 200,
    nombrePaiements: 3, nombreCharges: 3,
    totalPaiementsAvance: 4500, totalPaiementsVersement: 2000
  };

  const mockPaiements: PaiementDTO[] = [
    {
      id: 1, tenantId: 1, candidatId: 1,
      candidatNom: 'El Amrani', candidatPrenom: 'Youssef',
      montant: 3000, modePaiement: 'ESPECES',
      date: iso(today), type: 'AVANCE', recuUrl: null
    },
    {
      id: 2, tenantId: 1, candidatId: 2,
      candidatNom: 'Benali', candidatPrenom: 'Sara',
      montant: 2000, modePaiement: 'VIREMENT',
      date: iso(new Date(today.getTime() - 3 * 86400000)),
      type: 'VERSEMENT', recuUrl: null
    },
    {
      id: 3, tenantId: 1, candidatId: 3,
      candidatNom: 'Charif', candidatPrenom: 'Omar',
      montant: 1500, modePaiement: 'CHEQUE',
      date: iso(new Date(today.getTime() - 7 * 86400000)),
      type: 'AVANCE', recuUrl: null
    }
  ];

  const mockCharges: ChargeDTO[] = [
    {
      id: 10, tenantId: 1, categorie: 'LOYER',
      montant: 4500, date: iso(new Date(today.getTime() - 2 * 86400000)),
      periodicite: 'MENSUELLE', justificatifUrl: null
    },
    {
      id: 11, tenantId: 1, categorie: 'ASSURANCE',
      montant: 1200, date: iso(new Date(today.getTime() - 10 * 86400000)),
      periodicite: 'ANNUELLE', justificatifUrl: null
    },
    {
      id: 12, tenantId: 1, categorie: 'CARBURANT',
      montant: 600, date: iso(new Date(today.getTime() - 4 * 86400000)),
      periodicite: 'PONCTUELLE', justificatifUrl: null
    }
  ];

  const mockCandidats: CandidatDTO[] = [
    {
      id: 1, utilisateurId: 10, tenantId: 1, cin: 'AB1', categoriePermis: 'B',
      statutParcours: 'INSCRIT', forfaitId: 1,
      heuresConduiteValidees: 0, heuresTheorieValidees: 0,
      nom: 'El Amrani', prenom: 'Youssef',
      email: 'y@email.com', telephone: '06',
      dateExamenTheorie: null, resultatExamenTheorie: null, nombreTentativesTheorie: 0,
      dateExamenPratique: null, resultatExamenPratique: null, nombreTentativesPratique: 0
    }
  ];

  const adminUser = {
    id: 2, tenantId: 1, role: 'ADMIN', nom: 'Zahi', prenom: 'Fatima',
    email: 'f@autoecole.ma', telephone: '06', statut: 'ACTIF'
  };

  const candidatUser = {
    id: 6, tenantId: 1, role: 'CANDIDAT', nom: 'Tazi', prenom: 'Karim',
    email: 'karim@email.ma', telephone: '06', statut: 'ACTIF'
  };

  beforeEach(async () => {
    tresorerieServiceSpy = jasmine.createSpyObj('TresorerieService', [
      'getBalance', 'listerPaiements', 'listerCharges',
      'creerPaiement', 'creerCharge', 'exporterComptable'
    ]);
    candidatServiceSpy = jasmine.createSpyObj('CandidatService', ['lister']);
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isAdmin', 'isSecretaire', 'isCandidat',
      'canWrite', 'getCurrentUser', 'isDemoMode'
    ]);

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, HttpClientTestingModule, TresorerieComponent],
      providers: [
        { provide: TresorerieService, useValue: tresorerieServiceSpy },
        { provide: CandidatService, useValue: candidatServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();
  });

  function setupAuth(user: any, isDemo = false) {
    authServiceSpy.getCurrentUser.and.returnValue(user);
    authServiceSpy.isAdmin.and.returnValue(user.role === 'ADMIN' || user.role === 'SUPER_ADMIN');
    authServiceSpy.isSecretaire.and.returnValue(user.role === 'SECRETAIRE');
    authServiceSpy.isCandidat.and.returnValue(user.role === 'CANDIDAT');
    authServiceSpy.isDemoMode.and.returnValue(isDemo);
    authServiceSpy.canWrite.and.callFake((e) => {
      if (e === 'tresorerie') {
        return user.role === 'ADMIN' || user.role === 'SUPER_ADMIN' || user.role === 'SECRETAIRE';
      }
      return false;
    });
  }

  function create(): void {
    fixture = TestBed.createComponent(TresorerieComponent);
    component = fixture.componentInstance;
  }

  it('devrait creer le composant', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    tresorerieServiceSpy.listerCharges.and.returnValue(of(mockCharges));
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    create();
    component.ngOnInit();
    tick();
    expect(component).toBeTruthy();
  }));

  it('loadAll charge balance, paiements et charges pour ADMIN', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    tresorerieServiceSpy.listerCharges.and.returnValue(of(mockCharges));
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    create();
    component.ngOnInit();
    tick();
    expect(component.balance).toEqual(mockBalance);
    expect(component.paiements.length).toBe(3);
    expect(component.charges.length).toBe(3);
    expect(component.loading).toBeFalse();
  }));

  it('loadAll utilise les mocks en cas d\'erreur', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(throwError(() => new Error('KO')));
    tresorerieServiceSpy.listerPaiements.and.returnValue(throwError(() => new Error('KO')));
    tresorerieServiceSpy.listerCharges.and.returnValue(throwError(() => new Error('KO')));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.balance).toBeDefined();
    expect(component.paiements.length).toBeGreaterThan(0);
    expect(component.charges.length).toBeGreaterThan(0);
    expect(component.loading).toBeFalse();
  }));

  it('CANDIDAT ne voit que ses paiements et pas les charges', fakeAsync(() => {
    setupAuth(candidatUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(tresorerieServiceSpy.listerCharges).not.toHaveBeenCalled();
    expect(component.showChargesTab()).toBeFalse();
  }));

  it('ADMIN voit les charges', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    tresorerieServiceSpy.listerCharges.and.returnValue(of(mockCharges));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.showChargesTab()).toBeTrue();
  }));

  it('canWriteTresorerie true pour ADMIN', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.canWriteTresorerie()).toBeTrue();
  }));

  it('canWriteTresorerie false pour CANDIDAT', fakeAsync(() => {
    setupAuth(candidatUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.canWriteTresorerie()).toBeFalse();
  }));

  it('getMontantDH formats with DH suffix', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getMontantDH(3500)).toContain('DH');
  }));

  it('getSoldeClass returns text-go for positive solde', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getSoldeClass(200)).toBe('text-go');
    expect(component.getSoldeClass(-500)).toBe('text-stop');
    expect(component.getSoldeClass(0)).toBe('text-secondary');
  }));

  it('getPeriodeTexte returns correct French text', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    component.periode = 'SEMAINE';
    expect(component.getPeriodeTexte()).toBe('la semaine');
    component.periode = 'MOIS';
    expect(component.getPeriodeTexte()).toBe('le mois');
    component.periode = 'ANNEE';
    expect(component.getPeriodeTexte()).toBe("l'année");
  }));

  it('getModePaiementLabel returns correct label', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getModePaiementLabel('ESPECES')).toBe('Espèces');
    expect(component.getModePaiementLabel('VIREMENT')).toBe('Virement');
    expect(component.getModePaiementLabel('CHEQUE')).toBe('Chèque');
  }));

  it('getCategorieChargeLabel returns correct label', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getCategorieChargeLabel('LOYER')).toBe('Loyer');
    expect(component.getCategorieChargeLabel('CARBURANT')).toBe('Carburant');
  }));

  it('getCategorieChargeBadgeClass returns correct badge color', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getCategorieChargeBadgeClass('LOYER')).toBe('badge-purple');
    expect(component.getCategorieChargeBadgeClass('CARBURANT')).toBe('badge-orange');
  }));

  it('ouvrirPaiement initialise formPaiement with defaults', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    create();
    component.ngOnInit();
    tick();
    component.ouvrirPaiement();
    expect(component.showPaiementForm).toBeTrue();
    expect(component.formPaiement.candidatId).toBe(mockCandidats[0].id);
    expect(component.formPaiement.modePaiement).toBe('ESPECES');
    expect(component.formPaiement.type).toBe('VERSEMENT');
  }));

  it('ouvrirCharge initialise formCharge with defaults', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    component.ouvrirCharge();
    expect(component.showChargeForm).toBeTrue();
    expect(component.formCharge.categorie).toBe('AUTRE');
    expect(component.formCharge.periodicite).toBe('PONCTUELLE');
  }));

  it('soumettrePaiement calls creerPaiement and reloads on success', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    tresorerieServiceSpy.listerCharges.and.returnValue(of(mockCharges));
    candidatServiceSpy.lister.and.returnValue(of(mockCandidats));
    create();
    component.ngOnInit();
    tick();
    const nouveau: PaiementDTO = {
      id: 500, tenantId: 1, candidatId: 1,
      candidatNom: 'Nouveau', candidatPrenom: 'Candidat',
      montant: 1200, modePaiement: 'CARTE_BANCAIRE',
      date: iso(today), type: 'VERSEMENT', recuUrl: null
    };
    tresorerieServiceSpy.creerPaiement.and.returnValue(of(nouveau));
    component.formPaiement = {
      candidatId: 1, montant: 1200,
      modePaiement: 'CARTE_BANCAIRE',
      date: iso(today), type: 'VERSEMENT', recuUrl: null
    };
    component.soumettrePaiement();
    tick();
    expect(tresorerieServiceSpy.creerPaiement).toHaveBeenCalled();
    expect(component.paiements[0].id).toBe(500);
    expect(component.showPaiementForm).toBeFalse();
    expect(component.successMessage).toBe('Paiement enregistré');
  }));

  it('soumettreCharge calls creerCharge and reloads on success', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of(mockCharges));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const nouvelle: ChargeDTO = {
      id: 600, tenantId: 1, categorie: 'ENTRETIEN',
      montant: 800, date: iso(today),
      periodicite: 'PONCTUELLE', justificatifUrl: null
    };
    tresorerieServiceSpy.creerCharge.and.returnValue(of(nouvelle));
    component.formCharge = {
      categorie: 'ENTRETIEN', montant: 800,
      date: iso(today), periodicite: 'PONCTUELLE', justificatifUrl: null
    };
    component.soumettreCharge();
    tick();
    expect(tresorerieServiceSpy.creerCharge).toHaveBeenCalled();
    expect(component.charges[0].id).toBe(600);
    expect(component.showChargeForm).toBeFalse();
    expect(component.successMessage).toBe('Charge enregistrée');
  }));

  it('soumettrePaiement does nothing with zero montant', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    component.formPaiement = {
      candidatId: 1, montant: 0,
      modePaiement: 'ESPECES',
      date: iso(today), type: 'AVANCE', recuUrl: null
    };
    component.soumettrePaiement();
    tick();
    expect(tresorerieServiceSpy.creerPaiement).not.toHaveBeenCalled();
  }));

  it('setPeriode changes periode and reloads', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const callsBefore = tresorerieServiceSpy.getBalance.calls.count();
    component.setPeriode('ANNEE');
    tick();
    expect(component.periode).toBe('ANNEE');
    expect(tresorerieServiceSpy.getBalance.calls.count()).toBeGreaterThan(callsBefore);
  }));

  it('getPeriodeLabel returns French label', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getPeriodeLabel('MOIS')).toBe('Mois');
    expect(component.getPeriodeLabel('TRIMESTRE')).toBe('Trimestre');
  }));

  it('getPeriodiciteLabel returns correct label', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getPeriodiciteLabel('MENSUELLE')).toBe('Mensuelle');
    expect(component.getPeriodiciteLabel('ANNUELLE')).toBe('Annuelle');
  }));

  it('getPaiementsLength returns mocks length when paiements is empty', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(throwError(() => new Error('KO')));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getPaiementsLength()).toBe(3);
  }));

  it('getChargesLength returns mocks length when charges empty', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(throwError(() => new Error('KO')));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getChargesLength()).toBe(3);
  }));

  it('getPaiementsList returns paiements when populated', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getPaiementsList()).toEqual(mockPaiements);
  }));

  it('getChargesList returns charges mock if no real data', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(throwError(() => new Error('KO')));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const list = component.getChargesList();
    expect(list.length).toBe(3);
  }));

  it('buildBalanceMock computes totals from mock arrays', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const b = component.buildBalanceMock();
    expect(b.totalPaiements).toBe(6500);
    expect(b.totalCharges).toBe(6300);
    expect(b.soldeNet).toBe(200);
    expect(b.totalPaiementsAvance).toBe(4500);
    expect(b.totalPaiementsVersement).toBe(2000);
  }));

  it('getTypePaiementBadgeClass returns badge-orange for AVANCE', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getTypePaiementBadgeClass('AVANCE')).toBe('badge-orange');
    expect(component.getTypePaiementBadgeClass('VERSEMENT')).toBe('badge-green');
  }));

  it('getCandidatNomComplet returns full name or fallback', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const p: PaiementDTO = mockPaiements[0];
    expect(component.getCandidatNomComplet(p)).toContain('Youssef');
    expect(component.getCandidatNomComplet(p)).toContain('El Amrani');
    const pWithoutNames: PaiementDTO = { ...p, candidatNom: undefined as any, candidatPrenom: undefined as any };
    expect(component.getCandidatNomComplet(pWithoutNames)).toContain('Candidat');
  }));

  it('filterCandidatPaiements filters list for CANDIDAT role', fakeAsync(() => {
    setupAuth(candidatUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([
      { ...mockPaiements[0], candidatId: 6 },
      { ...mockPaiements[1], candidatId: 1 }
    ]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.paiements.every(p => p.candidatId === 6)).toBeTrue();
  }));

  it('formatDate formats date fr-FR', fakeAsync(() => {
    setupAuth(adminUser);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    tresorerieServiceSpy.listerCharges.and.returnValue(of([]));
    candidatServiceSpy.lister.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const result = component.formatDate('2025-05-20');
    expect(result.length).toBeGreaterThan(0);
  }));

  it('exporterComptable declenche export demo CSV', fakeAsync(() => {
    setupAuth(adminUser, true);
    tresorerieServiceSpy.getBalance.and.returnValue(of(mockBalance));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    tresorerieServiceSpy.listerCharges.and.returnValue(of(mockCharges));
    candidatServiceSpy.lister.and.returnValue(of([]));
    spyOn(document.body, 'appendChild').and.callFake(() => document.createElement('div'));
    spyOn(document.body, 'removeChild').and.stub();
    create();
    component.ngOnInit();
    tick();
    component.exporterComptable();
    expect(tresorerieServiceSpy.exporterComptable).not.toHaveBeenCalled();
    expect(component.successMessage).toBe('Export généré (mode démo : format CSV)');
  }));
});
