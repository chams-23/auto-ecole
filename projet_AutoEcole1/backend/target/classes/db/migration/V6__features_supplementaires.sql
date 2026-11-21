ALTER TABLE tenants ADD COLUMN IF NOT EXISTS plan_abonnement VARCHAR(30) DEFAULT 'BASIC';
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS date_debut_abonnement DATE;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS date_fin_abonnement DATE;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS date_resiliation DATE;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS montant_abonnement DECIMAL(10,2) DEFAULT 0;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS admin_email VARCHAR(150);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS date_suspension DATE;

ALTER TABLE candidats ADD COLUMN IF NOT EXISTS date_examen_theorie DATE;
ALTER TABLE candidats ADD COLUMN IF NOT EXISTS resultat_examen_theorie VARCHAR(15) DEFAULT 'EN_ATTENTE';
ALTER TABLE candidats ADD COLUMN IF NOT EXISTS date_examen_pratique DATE;
ALTER TABLE candidats ADD COLUMN IF NOT EXISTS resultat_examen_pratique VARCHAR(15) DEFAULT 'EN_ATTENTE';
ALTER TABLE candidats ADD COLUMN IF NOT EXISTS nombre_tentatives_theorie INT DEFAULT 0;
ALTER TABLE candidats ADD COLUMN IF NOT EXISTS nombre_tentatives_pratique INT DEFAULT 0;
ALTER TABLE candidats ADD COLUMN IF NOT EXISTS date_inscription DATE DEFAULT CURRENT_DATE;

ALTER TABLE paiements ADD COLUMN IF NOT EXISTS numero_recu VARCHAR(50);
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS est_annule BOOLEAN DEFAULT FALSE;
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS date_echeance DATE;
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS nombre_relances INT DEFAULT 0;
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS date_derniere_relance DATE;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT,
  utilisateur_id BIGINT,
  email_utilisateur VARCHAR(150),
  role_utilisateur VARCHAR(30),
  action VARCHAR(100) NOT NULL,
  entite_type VARCHAR(50),
  entite_id BIGINT,
  description TEXT,
  avant TEXT,
  apres TEXT,
  adresse_ip VARCHAR(45),
  user_agent VARCHAR(500),
  date_action TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_tenant_date ON audit_logs(tenant_id, date_action DESC);
CREATE INDEX IF NOT EXISTS idx_audit_utilisateur ON audit_logs(utilisateur_id, date_action DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action, date_action DESC);

CREATE TABLE IF NOT EXISTS documents_candidat (
  id BIGSERIAL PRIMARY KEY,
  candidat_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  type_document VARCHAR(50) NOT NULL,
  nom_fichier VARCHAR(255) NOT NULL,
  chemin_fichier VARCHAR(500) NOT NULL,
  taille_octets BIGINT DEFAULT 0,
  type_mime VARCHAR(100),
  statut_validation VARCHAR(20) DEFAULT 'EN_ATTENTE',
  valide_par BIGINT,
  date_validation TIMESTAMP,
  date_expiration DATE,
  commentaire TEXT,
  uploaded_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_doc_candidat FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE CASCADE,
  CONSTRAINT fk_doc_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_doc_candidat ON documents_candidat(candidat_id, type_document);
CREATE INDEX IF NOT EXISTS idx_doc_tenant ON documents_candidat(tenant_id);
CREATE INDEX IF NOT EXISTS idx_doc_statut ON documents_candidat(statut_validation);

CREATE TABLE IF NOT EXISTS relances_impayes (
  id BIGSERIAL PRIMARY KEY,
  paiement_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  candidat_id BIGINT NOT NULL,
  numero_relance INT NOT NULL,
  date_envoi TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  canal_envoi VARCHAR(20) DEFAULT 'EMAIL',
  statut_envoi VARCHAR(20) DEFAULT 'ENVOYE',
  message TEXT,
  CONSTRAINT fk_relance_paiement FOREIGN KEY (paiement_id) REFERENCES paiements(id) ON DELETE CASCADE,
  CONSTRAINT fk_relance_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT fk_relance_candidat FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_relance_paiement ON relances_impayes(paiement_id);
CREATE INDEX IF NOT EXISTS idx_relance_date ON relances_impayes(date_envoi DESC);

CREATE TABLE IF NOT EXISTS sauvegardes (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT,
  type_sauvegarde VARCHAR(20) NOT NULL DEFAULT 'COMPLETE',
  nom_fichier VARCHAR(255) NOT NULL,
  chemin_fichier VARCHAR(500),
  taille_octets BIGINT DEFAULT 0,
  statut VARCHAR(20) DEFAULT 'EN_COURS',
  declencheur VARCHAR(20) DEFAULT 'AUTO',
  erreur_message TEXT,
  date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  date_fin TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sauvegarde_tenant ON sauvegardes(tenant_id, date_debut DESC);
