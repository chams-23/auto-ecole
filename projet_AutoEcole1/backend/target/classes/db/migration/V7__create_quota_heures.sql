CREATE TABLE IF NOT EXISTS quota_heures (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    categorie_permis VARCHAR(10) NOT NULL,
    heures_code_min INTEGER NOT NULL DEFAULT 0,
    heures_conduite_min INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tenant_categorie UNIQUE (tenant_id, categorie_permis)
);

CREATE INDEX IF NOT EXISTS idx_quota_heures_tenant ON quota_heures(tenant_id);

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'B',  60, 20 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'B');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'A',  30, 15 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'A');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'C',  80, 40 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'C');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'D', 100, 50 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'D');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'EB', 60, 20 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'EB');
