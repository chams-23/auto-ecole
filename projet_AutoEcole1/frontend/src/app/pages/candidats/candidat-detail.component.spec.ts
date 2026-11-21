import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, convertToParamMap } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { CandidatDetailComponent } from './candidat-detail.component';
import { CandidatService } from '../../core/services/candidat.service';
import { TresorerieService } from '../../core/services/tresorerie.service';
import { SeanceService } from '../../core/services/seance.service';
import { DocumentCandidatService } from '../../core/services/document-candidat.service';
import { AuthService } from '../../core/services/auth.service';
import {
  CandidatDTO, CompteurHeures, SoldeCandidat, StatutQuotaHeures
} from '../../core/models/candidat';
import { PaiementDTO } from '../../core/models/tresorerie';
import { SeanceDTO } from '../../core/models/seance';
import {
  DocumentCandidatDTO, TypeDocumentCandidat
} from '../../core/models/document-candidat';

describe('CandidatDetailComponent', () => {
  let component: CandidatDetailComponent;
  let fixture: ComponentFixture<CandidatDetailComponent>;
  let candidatServiceSpy: jasmine.SpyObj<CandidatService>;
  let tresorerieServiceSpy: jasmine.SpyObj<TresorerieService>;
  let seanceServiceSpy: jasmine.SpyObj<SeanceService>;
  let documentCandidatServiceSpy: jasmine.SpyObj<DocumentCandidatService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockCandidat: CandidatDTO = {
    id: 1, utilisateurId: 10, tenantId: 1,
    nom: 'El Amrani', prenom: 'Youssef',
    email: 'youssef@email.com', telephone: '0612345678',
    cin: 'AB123456', categoriePermis: 'B',
    statutParcours: 'ADMIS', forfaitId: 1,
    heuresConduiteValidees: 20, heuresTheorieValidees: 8,
    dateExamenTheorie: null, resultatExamenTheorie: null, nombreTentativesTheorie: 0,
    dateExamenPratique: null, resultatExamenPratique: null, nombreTentativesPratique: 0
  };

  const mockCompteur: CompteurHeures = {
    heuresConduiteValidees: 20, heuresTheorieValidees: 8,
    heuresConduiteRestantes: 5, heuresTheorieRestantes: 2
  };

  const mockSolde: SoldeCandidat = {
    totalDu: 3500, totalPaye: 3500, soldeRestant: 0
  };

  const mockStatutQuota: StatutQuotaHeures = {
    categoriePermis: 'B',
    quotaCodeMin: 60, quotaConduiteMin: 20,
    heuresCodeValidees: 60, heuresConduiteValidees: 20,
    codeAtteint: true, conduiteAtteint: true, quotaAtteint: true
  };

  const mockPaiements: PaiementDTO[] = [
    {
      id: 1, tenantId: 1, candidatId: 1,
      candidatNom: 'El Amrani', candidatPrenom: 'Youssef',
      montant: 3000, modePaiement: 'ESPECES',
      date: '2025-05-01', type: 'AVANCE', recuUrl: null
    }
  ];

  const mockSeances: SeanceDTO[] = [
    {
      id: 10, tenantId: 1, type: 'CONDUITE',
      date: '2025-05-15', heureDebut: '09:00', heureFin: '10:00',
      candidatId: 1, candidatNom: 'El Amrani', candidatPrenom: 'Youssef',
      intervenantId: 1, intervenantNom: 'Bennani', intervenantPrenom: 'Karim',
      statutValidation: 'VALIDEE', kilometrage: 45, observations: 'Bon travail'
    }
  ];

  const mockDocuments: DocumentCandidatDTO[] = [
    {
      id: 1, candidatId: 1, tenantId: 1,
      typeDocument: 'CIN_COPIE', nomFichier: 'cin.pdf',
      urlFichier: null, statut: 'VALIDE',
      dateUpload: new Date().toISOString(),
      dateExpiration: null, uploadParUtilisateurId: 2,
      valideParUtilisateurId: 2, dateValidation: new Date().toISOString()
    }
  ];

  const adminUser = {
    id: 2, tenantId: 1, role: 'ADMIN', nom: 'Zahi', prenom: 'Fatima',
    email: 'f@autoecole.ma', telephone: '06', statut: 'ACTIF'
  };

  const candidatUser = {
    id: 10, tenantId: 1, role: 'CANDIDAT', nom: 'El Amrani', prenom: 'Youssef',
    email: 'youssef@email.ma', telephone: '06', statut: 'ACTIF'
  };

  beforeEach(async () => {
    candidatServiceSpy = jasmine.createSpyObj('CandidatService', [
      'getById', 'compteurHeures', 'solde', 'statutQuota', 'modifier'
    ]);
    tresorerieServiceSpy = jasmine.createSpyObj('TresorerieService', [
      'listerPaiements'
    ]);
    seanceServiceSpy = jasmine.createSpyObj('SeanceService', [
      'filtrer'
    ]);
    documentCandidatServiceSpy = jasmine.createSpyObj('DocumentCandidatService', [
      'listerParCandidat', 'uploaderDocument', 'validerDocument',
      'refuserDocument', 'supprimer'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isAdmin', 'isSecretaire', 'isCandidat',
      'getCurrentUser', 'getCurrentUserId', 'isDemoMode'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterModule.forRoot([]),
        HttpClientTestingModule,
        CandidatDetailComponent
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } }
        },
        { provide: CandidatService, useValue: candidatServiceSpy },
        { provide: TresorerieService, useValue: tresorerieServiceSpy },
        { provide: SeanceService, useValue: seanceServiceSpy },
        { provide: DocumentCandidatService, useValue: documentCandidatServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();
  });

  function setupAuth(user: any, isDemo = false) {
    authServiceSpy.getCurrentUser.and.returnValue(user);
    authServiceSpy.getCurrentUserId.and.returnValue(user?.id ?? null);
    authServiceSpy.isAdmin.and.returnValue(user.role === 'ADMIN' || user.role === 'SUPER_ADMIN');
    authServiceSpy.isSecretaire.and.returnValue(user.role === 'SECRETAIRE');
    authServiceSpy.isCandidat.and.returnValue(user.role === 'CANDIDAT');
    authServiceSpy.isDemoMode.and.returnValue(isDemo);
  }

  function setupAllServiceReturns() {
    candidatServiceSpy.getById.and.returnValue(of(mockCandidat));
    candidatServiceSpy.compteurHeures.and.returnValue(of(mockCompteur));
    candidatServiceSpy.solde.and.returnValue(of(mockSolde));
    candidatServiceSpy.statutQuota.and.returnValue(of(mockStatutQuota));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of(mockPaiements));
    seanceServiceSpy.filtrer.and.returnValue(of(mockSeances));
    documentCandidatServiceSpy.listerParCandidat.and.returnValue(of(mockDocuments));
  }

  function create() {
    fixture = TestBed.createComponent(CandidatDetailComponent);
    component = fixture.componentInstance;
  }

  it('devrait creer le composant', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component).toBeTruthy();
  }));

  it('candidatId devrait etre lu depuis la route (id=1)', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.candidatId).toBe(1);
  }));

  it('loadAll devrait charger toutes les donnees', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.candidat).toEqual(mockCandidat);
    expect(component.compteur).toEqual(mockCompteur);
    expect(component.solde).toEqual(mockSolde);
    expect(component.statutQuota).toEqual(mockStatutQuota);
    expect(component.paiements).toEqual(mockPaiements);
    expect(component.seances).toEqual(mockSeances);
    expect(component.documents).toEqual(mockDocuments);
    expect(component.loading).toBeFalse();
  }));

  it('loadAll devrait utiliser les mocks en cas d\'erreur API', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.getById.and.returnValue(throwError(() => new Error('KO')));
    candidatServiceSpy.compteurHeures.and.returnValue(throwError(() => new Error('KO')));
    candidatServiceSpy.solde.and.returnValue(throwError(() => new Error('KO')));
    candidatServiceSpy.statutQuota.and.returnValue(throwError(() => new Error('KO')));
    tresorerieServiceSpy.listerPaiements.and.returnValue(throwError(() => new Error('KO')));
    seanceServiceSpy.filtrer.and.returnValue(throwError(() => new Error('KO')));
    documentCandidatServiceSpy.listerParCandidat.and.returnValue(throwError(() => new Error('KO')));
    create();
    component.ngOnInit();
    tick();
    expect(component.candidat).toBeDefined();
    expect(component.compteur).toBeDefined();
    expect(component.solde).toBeDefined();
    expect(component.statutQuota).toBeDefined();
    expect(component.documents.length).toBeGreaterThan(0);
    expect(component.loading).toBeFalse();
  }));

  it('canUploadDocument should return true for ADMIN', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.canUploadDocument()).toBeTrue();
  }));

  it('canUploadDocument should return true for CANDIDAT', fakeAsync(() => {
    setupAuth(candidatUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.canUploadDocument()).toBeTrue();
  }));

  it('canValidateDocument should return true for ADMIN', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.canValidateDocument()).toBeTrue();
  }));

  it('canValidateDocument should return false for CANDIDAT', fakeAsync(() => {
    setupAuth(candidatUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.canValidateDocument()).toBeFalse();
  }));

  it('canEditStatut should return true for ADMIN', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.canEditStatut()).toBeTrue();
  }));

  it('canEditStatut should return false for CANDIDAT', fakeAsync(() => {
    setupAuth(candidatUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.canEditStatut()).toBeFalse();
  }));

  it('getPourcentageTheorie should compute correctly', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.getPourcentageTheorie()).toBe(80);
  }));

  it('getPourcentageConduite should compute correctly', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.getPourcentageConduite()).toBe(80);
  }));

  it('getPourcentageTheorie returns 0 if no compteur', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.getById.and.returnValue(of(mockCandidat));
    candidatServiceSpy.compteurHeures.and.returnValue(of({
      heuresConduiteValidees: 0, heuresTheorieValidees: 0,
      heuresConduiteRestantes: 0, heuresTheorieRestantes: 0
    }));
    candidatServiceSpy.solde.and.returnValue(of(mockSolde));
    candidatServiceSpy.statutQuota.and.returnValue(of(mockStatutQuota));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    documentCandidatServiceSpy.listerParCandidat.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getPourcentageTheorie()).toBe(0);
  }));

  it('getRestantColor returns text-go when solde = 0', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.getRestantColor()).toBe('text-go');
  }));

  it('getRestantColor returns text-stop for large balance', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.getById.and.returnValue(of(mockCandidat));
    candidatServiceSpy.compteurHeures.and.returnValue(of(mockCompteur));
    candidatServiceSpy.solde.and.returnValue(of({
      totalDu: 5000, totalPaye: 1000, soldeRestant: 4000
    }));
    candidatServiceSpy.statutQuota.and.returnValue(of(mockStatutQuota));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    documentCandidatServiceSpy.listerParCandidat.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    expect(component.getRestantColor()).toBe('text-stop');
  }));

  it('formatMontant formats with DH', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.formatMontant(3500)).toContain('DH');
  }));

  it('formatHeure truncates to 5 chars', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.formatHeure('09:00:00')).toBe('09:00');
  }));

  it('formatStatut replaces underscores with spaces', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.formatStatut('PRESENTE_EXAMEN')).toContain(' ');
    expect(component.formatStatut('PRESENTE_EXAMEN')).not.toContain('_');
  }));

  it('setTab changes activeTab', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.setTab('documents');
    expect(component.activeTab).toBe('documents');
  }));

  it('getStatutQuotaBadge returns success when quotaAtteint is true', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    const badge = component.getStatutQuotaBadge();
    expect(badge.class).toBe('status-go');
    expect(badge.text).toContain('atteint');
  }));

  it('getStatutQuotaBadge returns error when quotaAtteint is false', fakeAsync(() => {
    setupAuth(adminUser);
    candidatServiceSpy.getById.and.returnValue(of(mockCandidat));
    candidatServiceSpy.compteurHeures.and.returnValue(of(mockCompteur));
    candidatServiceSpy.solde.and.returnValue(of(mockSolde));
    candidatServiceSpy.statutQuota.and.returnValue(of({
      ...mockStatutQuota, quotaAtteint: false, codeAtteint: false
    }));
    tresorerieServiceSpy.listerPaiements.and.returnValue(of([]));
    seanceServiceSpy.filtrer.and.returnValue(of([]));
    documentCandidatServiceSpy.listerParCandidat.and.returnValue(of([]));
    create();
    component.ngOnInit();
    tick();
    const badge = component.getStatutQuotaBadge();
    expect(badge.class).toBe('status-stop');
  }));

  it('debuterEditionStatut enables editing', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.editingStatut).toBeFalse();
    component.debuterEditionStatut();
    expect(component.editingStatut).toBeTrue();
  }));

  it('debuterEditionStatut is blocked for CANDIDAT role', fakeAsync(() => {
    setupAuth(candidatUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.debuterEditionStatut();
    expect(component.editingStatut).toBeFalse();
  }));

  it('annulerEditionStatut cancels edit', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.debuterEditionStatut();
    component.statutEditValue = 'ADMIS';
    component.annulerEditionStatut();
    expect(component.editingStatut).toBeFalse();
    expect(component.statutEditValue).toBe(mockCandidat.statutParcours);
  }));

  it('confirmerChangementStatut calls service and updates', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    const updated: CandidatDTO = { ...mockCandidat, statutParcours: 'ADMIS' };
    candidatServiceSpy.modifier.and.returnValue(of(updated));
    candidatServiceSpy.statutQuota.and.returnValue(of(mockStatutQuota));
    create();
    component.ngOnInit();
    tick();
    component.debuterEditionStatut();
    component.statutEditValue = 'ADMIS';
    component.confirmerChangementStatut();
    tick();
    expect(candidatServiceSpy.modifier).toHaveBeenCalledWith(1, {
      statutParcours: 'ADMIS'
    });
    expect(component.editingStatut).toBeFalse();
    expect(component.operationMsg).not.toBe('');
  }));

  it('confirmerChangementStatut reports error', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    candidatServiceSpy.modifier.and.returnValue(throwError(() => ({
      error: { message: 'Interdit' }
    })));
    create();
    component.ngOnInit();
    tick();
    component.debuterEditionStatut();
    component.statutEditValue = 'REFUSE';
    component.confirmerChangementStatut();
    tick();
    expect(component.operationError).toContain('Interdit');
  }));

  it('ouvrirUpload sets showUploadForm true and resets form', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.ouvrirUpload();
    expect(component.showUploadForm).toBeTrue();
    expect(component.uploadForm.typeDocument).toBe('CIN_COPIE');
    expect(component.operationMsg).toBe('');
  }));

  it('fermerUpload hides form', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.fermerUpload();
    expect(component.showUploadForm).toBeFalse();
  }));

  it('soumettreUpload adds document in demo mode', fakeAsync(() => {
    setupAuth(candidatUser, true);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    const initialLen = component.documents.length;
    component.uploadForm.nomFichier = 'new.pdf';
    component.uploadForm.typeDocument = 'CERTIFICAT_MEDICAL';
    component.soumettreUpload();
    tick();
    expect(component.documents.length).toBe(initialLen + 1);
    expect(component.showUploadForm).toBeFalse();
  }));

  it('soumettreUpload does nothing with empty filename', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.uploadForm.nomFichier = '';
    component.soumettreUpload();
    tick();
    expect(documentCandidatServiceSpy.uploaderDocument).not.toHaveBeenCalled();
  }));

  it('soumettreUpload calls service', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    const nouvDoc: DocumentCandidatDTO = {
      id: 999, candidatId: 1, tenantId: 1,
      typeDocument: 'PHOTO_IDENTITE',
      nomFichier: 'photo.jpg',
      urlFichier: null, statut: 'EN_ATTENTE',
      dateUpload: new Date().toISOString(),
      dateExpiration: null, uploadParUtilisateurId: 2
    };
    documentCandidatServiceSpy.uploaderDocument.and.returnValue(of(nouvDoc));
    create();
    component.ngOnInit();
    tick();
    component.uploadForm.typeDocument = 'PHOTO_IDENTITE';
    component.uploadForm.nomFichier = 'photo.jpg';
    component.soumettreUpload();
    tick();
    expect(documentCandidatServiceSpy.uploaderDocument).toHaveBeenCalled();
    expect(component.documents[0].id).toBe(999);
  }));

  it('validerDocument sets VALIDE in demo mode', fakeAsync(() => {
    setupAuth(adminUser, true);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    component.validerDocument(1);
    expect(component.documents[0].statut).toBe('VALIDE');
  }));

  it('validerDocument calls service', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    const valide: DocumentCandidatDTO = { ...mockDocuments[0], statut: 'VALIDE' };
    documentCandidatServiceSpy.validerDocument.and.returnValue(of(valide));
    create();
    component.ngOnInit();
    tick();
    component.validerDocument(1);
    tick();
    expect(documentCandidatServiceSpy.validerDocument).toHaveBeenCalledWith(1);
  }));

  it('supprimerDocument removes from list in demo mode', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    setupAuth(adminUser, true);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    const lenAvant = component.documents.length;
    component.supprimerDocument(1);
    tick();
    expect(component.documents.length).toBe(lenAvant - 1);
  }));

  it('getStatutDisplay returns correct badge for ADMIS', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    const display = component.getStatutDisplay('ADMIS');
    expect(display.class).toBe('status-go');
    expect(display.text).toContain('ADMIS');
  }));

  it('getStatutSeanceClass returns correct class', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    expect(component.getStatutSeanceClass('VALIDEE')).toBe('status-go');
    expect(component.getStatutSeanceClass('EN_ATTENTE')).toBe('status-warn');
    expect(component.getStatutSeanceClass('CONTESTEE')).toBe('status-stop');
  }));

  it('formatDate formats to fr-FR', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    const formatted = component.formatDate('2025-05-15');
    expect(formatted.length).toBeGreaterThan(0);
  }));

  it('getTypeDocLabel returns label for CIN_COPIE', fakeAsync(() => {
    setupAuth(adminUser);
    setupAllServiceReturns();
    create();
    component.ngOnInit();
    tick();
    const label = component.getTypeDocLabel('CIN_COPIE' as TypeDocumentCandidat);
    expect(label).toBe('Copie CIN');
  }));
});
