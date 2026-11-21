CREATE TABLE utilisateurs (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  role VARCHAR(30) NOT NULL,
  nom VARCHAR(100) NOT NULL,
  prenom VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  telephone VARCHAR(20),
  statut VARCHAR(20) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_utilisateurs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT uk_utilisateurs_email UNIQUE (email)
);

CREATE TABLE forfaits (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  nom VARCHAR(100) NOT NULL,
  categorie_permis VARCHAR(10) NOT NULL,
  heures_code_min INTEGER NOT NULL,
  heures_conduite_min INTEGER NOT NULL,
  prix DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_forfaits_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_utilisateurs_tenant_email ON utilisateurs(tenant_id, email);
CREATE INDEX idx_forfaits_tenant ON forfaits(tenant_id);
