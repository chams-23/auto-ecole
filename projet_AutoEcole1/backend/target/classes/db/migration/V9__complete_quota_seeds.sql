INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'A1', 20,  8 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'A1');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'A2', 25, 12 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'A2');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'EC', 80, 40 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'EC');

INSERT INTO quota_heures (tenant_id, categorie_permis, heures_code_min, heures_conduite_min)
SELECT 1, 'ED', 100, 50 WHERE NOT EXISTS (SELECT 1 FROM quota_heures WHERE tenant_id = 1 AND categorie_permis = 'ED');
