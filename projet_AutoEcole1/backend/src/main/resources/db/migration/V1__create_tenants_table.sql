CREATE TABLE tenants (
  id BIGSERIAL PRIMARY KEY,
  raison_sociale VARCHAR(255) NOT NULL,
  numero_agrement VARCHAR(100),
  wilaya VARCHAR(100),
  statut_abonnement VARCHAR(20) NOT NULL DEFAULT 'ACTIF',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
