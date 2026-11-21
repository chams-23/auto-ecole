INSERT INTO tenants (id, raison_sociale, numero_agrement, wilaya, statut_abonnement, created_at)
VALUES (1, 'Auto-École Chaimaa', 'AGR2024001', 'Casablanca', 'ACTIF', CURRENT_TIMESTAMP);

INSERT INTO forfaits (id, tenant_id, nom, categorie_permis, heures_code_min, heures_conduite_min, prix, created_at) VALUES
(1, 1, 'Forfait B Standard', 'B', 40, 20, 3500.00, CURRENT_TIMESTAMP),
(2, 1, 'Forfait C Premium', 'C', 50, 30, 5500.00, CURRENT_TIMESTAMP);

INSERT INTO utilisateurs (id, tenant_id, role, nom, prenom, email, telephone, statut, password_hash, created_at) VALUES
(1, 1, 'SUPER_ADMIN', 'Admin', 'System', 'admin@autoecole.ma', '0522000000', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(2, 1, 'ADMIN', 'Fatima', 'Zahi', 'fatima@autoecole.ma', '0600000001', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(3, 1, 'MONITEUR', 'Youssef', 'Amrani', 'youssef@autoecole.ma', '0600000002', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(4, 1, 'MONITEUR', 'Leila', 'Bennani', 'leila@autoecole.ma', '0600000003', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(5, 1, 'SECRETAIRE', 'Salma', 'Idrissi', 'salma@autoecole.ma', '0600000004', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(6, 1, 'CANDIDAT', 'Karim', 'Tazi', 'karim@email.ma', '0611111111', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(7, 1, 'CANDIDAT', 'Sara', 'Fassi', 'sara@email.ma', '0622222222', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP),
(8, 1, 'CANDIDAT', 'Omar', 'Haddad', 'omar@email.ma', '0633333333', 'ACTIF', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP);

INSERT INTO candidats (id, utilisateur_id, tenant_id, cin, categorie_permis, statut_parcours, forfait_id, heures_conduite_validees, heures_theorie_validees, created_at) VALUES
(1, 6, 1, 'K123456', 'B', 'INSCRIT', 1, 0, 0, CURRENT_TIMESTAMP),
(2, 7, 1, 'S789012', 'B', 'DOSSIER_COMPLET', 1, 0, 10, CURRENT_TIMESTAMP),
(3, 8, 1, 'H456789', 'C', 'CODE_VALIDE', 2, 0, 40, CURRENT_TIMESTAMP);

INSERT INTO moniteurs (id, utilisateur_id, tenant_id, numero_carte_pro, categories_autorisees, vehicule_id, created_at) VALUES
(1, 3, 1, 'MCP2024001', 'B,C,D', NULL, CURRENT_TIMESTAMP),
(2, 4, 1, 'MCP2024002', 'B', NULL, CURRENT_TIMESTAMP);

INSERT INTO vehicules (id, tenant_id, immatriculation, categorie_permis, date_mise_service, date_controle_technique, date_expiration_assurance, moniteur_id, created_at) VALUES
(1, 1, '12345-A-14', 'B', '2023-01-15', '2026-02-01', '2026-06-15', 1, CURRENT_TIMESTAMP),
(2, 1, '23456-B-14', 'B', '2022-06-20', '2025-09-01', '2025-12-20', 2, CURRENT_TIMESTAMP),
(3, 1, '34567-C-14', 'C', '2021-03-10', '2025-12-01', '2026-03-10', NULL, CURRENT_TIMESTAMP);

UPDATE moniteurs SET vehicule_id = 1 WHERE id = 1;
UPDATE moniteurs SET vehicule_id = 2 WHERE id = 2;

INSERT INTO seances (id, tenant_id, type, date, heure_debut, heure_fin, candidat_id, intervenant_id, statut_validation, kilometrage, observations, created_at) VALUES
(1, 1, 'THEORIE', '2026-08-12', '09:00:00', '12:00:00', 3, 3, 'EN_ATTENTE', NULL, 'Cours de code - Permis C', CURRENT_TIMESTAMP),
(2, 1, 'CONDUITE', '2026-08-13', '14:00:00', '15:30:00', 1, 3, 'EN_ATTENTE', 45.5, 'Manoeuvres et ville', CURRENT_TIMESTAMP),
(3, 1, 'CONDUITE', '2026-08-14', '10:00:00', '12:00:00', 2, 4, 'EN_ATTENTE', 38.0, 'Autoroute et voies rapides', CURRENT_TIMESTAMP);

INSERT INTO paiements (id, tenant_id, candidat_id, montant, mode_paiement, date, type, recu_url, created_at) VALUES
(1, 1, 1, 1000.00, 'ESPECES', '2026-07-15', 'AVANCE', NULL, CURRENT_TIMESTAMP),
(2, 1, 2, 2000.00, 'CHEQUE', '2026-08-01', 'VERSEMENT', NULL, CURRENT_TIMESTAMP);

INSERT INTO charges (id, tenant_id, categorie, montant, date, periodicite, justificatif_url, created_at) VALUES
(1, 1, 'LOYER', 5000.00, '2026-08-01', 'MENSUELLE', NULL, CURRENT_TIMESTAMP),
(2, 1, 'CARBURANT', 1500.00, '2026-08-05', 'PONCTUELLE', NULL, CURRENT_TIMESTAMP);

SELECT setval('tenants_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tenants));
SELECT setval('forfaits_id_seq', (SELECT COALESCE(MAX(id), 1) FROM forfaits));
SELECT setval('utilisateurs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM utilisateurs));
SELECT setval('candidats_id_seq', (SELECT COALESCE(MAX(id), 1) FROM candidats));
SELECT setval('moniteurs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM moniteurs));
SELECT setval('vehicules_id_seq', (SELECT COALESCE(MAX(id), 1) FROM vehicules));
SELECT setval('seances_id_seq', (SELECT COALESCE(MAX(id), 1) FROM seances));
SELECT setval('paiements_id_seq', (SELECT COALESCE(MAX(id), 1) FROM paiements));
SELECT setval('charges_id_seq', (SELECT COALESCE(MAX(id), 1) FROM charges));
SELECT setval('notifications_id_seq', (SELECT COALESCE(MAX(id), 1) FROM notifications));
