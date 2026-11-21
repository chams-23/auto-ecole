CREATE TABLE candidats (
  id BIGSERIAL PRIMARY KEY,
  utilisateur_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  cin VARCHAR(15) NOT NULL,
  categorie_permis VARCHAR(10) NOT NULL,
  statut_parcours VARCHAR(40) NOT NULL,
  forfait_id BIGINT,
  heures_conduite_validees INT DEFAULT 0,
  heures_theorie_validees INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_candidats_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE RESTRICT,
  CONSTRAINT fk_candidats_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT fk_candidats_forfait FOREIGN KEY (forfait_id) REFERENCES forfaits(id) ON DELETE RESTRICT,
  CONSTRAINT uk_candidats_utilisateur UNIQUE (utilisateur_id),
  CONSTRAINT uk_candidats_cin UNIQUE (cin)
);

CREATE TABLE moniteurs (
  id BIGSERIAL PRIMARY KEY,
  utilisateur_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  numero_carte_pro VARCHAR(50) NOT NULL,
  categories_autorisees VARCHAR(255),
  vehicule_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_moniteurs_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE RESTRICT,
  CONSTRAINT fk_moniteurs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT uk_moniteurs_utilisateur UNIQUE (utilisateur_id),
  CONSTRAINT uk_moniteurs_carte_pro UNIQUE (numero_carte_pro)
);

CREATE TABLE vehicules (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  immatriculation VARCHAR(20) NOT NULL,
  categorie_permis VARCHAR(10) NOT NULL,
  date_mise_service DATE,
  date_controle_technique DATE,
  date_expiration_assurance DATE,
  moniteur_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_vehicules_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,
  CONSTRAINT fk_vehicules_moniteur FOREIGN KEY (moniteur_id) REFERENCES moniteurs(id) ON DELETE RESTRICT,
  CONSTRAINT uk_vehicules_immatriculation UNIQUE (immatriculation)
);

ALTER TABLE moniteurs ADD CONSTRAINT fk_moniteurs_vehicule FOREIGN KEY (vehicule_id) REFERENCES vehicules(id) ON DELETE RESTRICT;

CREATE INDEX idx_candidats_tenant_cin_forfait ON candidats(tenant_id, cin, forfait_id);
CREATE INDEX idx_moniteurs_tenant ON moniteurs(tenant_id);
CREATE INDEX idx_vehicules_tenant_immatriculation ON vehicules(tenant_id, immatriculation);
