CREATE TABLE seances (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  date DATE NOT NULL,
  heure_debut TIME NOT NULL,
  heure_fin TIME NOT NULL,
  candidat_id BIGINT,
  intervenant_id BIGINT,
  statut_validation VARCHAR(30) DEFAULT 'EN_ATTENTE',
  kilometrage DECIMAL(10,2),
  observations TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_seances_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT fk_seances_candidat FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE RESTRICT,
  CONSTRAINT fk_seances_intervenant FOREIGN KEY (intervenant_id) REFERENCES utilisateurs(id) ON DELETE RESTRICT
);

CREATE TABLE paiements (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  candidat_id BIGINT,
  montant DECIMAL(10,2) NOT NULL,
  mode_paiement VARCHAR(30) NOT NULL,
  date DATE NOT NULL,
  type VARCHAR(20) NOT NULL,
  recu_url VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_paiements_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT fk_paiements_candidat FOREIGN KEY (candidat_id) REFERENCES candidats(id) ON DELETE RESTRICT
);

CREATE TABLE charges (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  categorie VARCHAR(30) NOT NULL,
  montant DECIMAL(10,2) NOT NULL,
  date DATE NOT NULL,
  periodicite VARCHAR(20) NOT NULL,
  justificatif_url VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_charges_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT
);

CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  destinataire_id BIGINT NOT NULL,
  type_evenement VARCHAR(50) NOT NULL,
  contenu VARCHAR(500) NOT NULL,
  statut_lecture BOOLEAN DEFAULT FALSE,
  date TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notifications_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT fk_notifications_destinataire FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE RESTRICT
);

CREATE INDEX idx_seances_tenant_date_candidat_intervenant ON seances(tenant_id, date, candidat_id, intervenant_id);
CREATE INDEX idx_paiements_tenant_date_candidat ON paiements(tenant_id, date, candidat_id);
CREATE INDEX idx_charges_tenant_date ON charges(tenant_id, date);
CREATE INDEX idx_notifications_tenant_destinataire_date ON notifications(tenant_id, destinataire_id, date);
CREATE INDEX idx_notifications_destinataire_date ON notifications(destinataire_id, date);
