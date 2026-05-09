-- ============================================
-- AYORA - Fix encodage, doublons, noms et prix realistes
-- ============================================

USE ayora_db;

-- 1) Supprimer doublon Festin : on garde un seul nom, "Festin Traiteur"
DELETE FROM vendors WHERE name = 'Traiteur Festin';
DELETE FROM vendors WHERE name = 'Fes''tin Traiteur';

-- Inserer Festin Traiteur unique
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
SELECT id, 'Festin Traiteur', 'Fes',
  'FESTIN propose une offre de traiteur complete, originale et raffinee. Au plaisir de vous servir partout au Maroc.',
  450.00, 850.00, 'PREMIUM', '0661420546', '@festin_traiteur', 'Fes',
  'raffine,complet,partout-maroc,festin,prestige', 4.90, 234
FROM vendor_categories WHERE name='TRAITEUR'
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram),
  prix_min=VALUES(prix_min), prix_max=VALUES(prix_max);

-- 2) Renommer "Tenguit de Luxe" -> "Tenguif de Luxe" (sur le nom et la description)
UPDATE vendors
SET name = REPLACE(name, 'Tenguit', 'Tenguif'),
    description = REPLACE(description, 'Tenguit', 'Tenguif'),
    tags = REPLACE(tags, 'tenguit', 'tenguif')
WHERE name LIKE '%Tenguit%' OR description LIKE '%Tenguit%' OR tags LIKE '%tenguit%';

-- 3) Nettoyer les em-dashes dans tous les prestataires (probleme d'encodage UTF-8)
--    Remplace les caracteres "—" et "–" par un tiret simple " - " ou simplement par espace.
UPDATE vendors SET name = REPLACE(name, ' — ', ' - ');
UPDATE vendors SET name = REPLACE(name, '—', '-');
UPDATE vendors SET name = REPLACE(name, ' – ', ' - ');
UPDATE vendors SET name = REPLACE(name, '–', '-');
UPDATE vendors SET description = REPLACE(description, '—', '-');
UPDATE vendors SET description = REPLACE(description, '–', '-');
UPDATE vendors SET address = REPLACE(address, '—', '-');
UPDATE vendors SET address = REPLACE(address, '–', '-');

-- 4) Prix realistes (donnees marche reel Maroc 2026)

-- Youssef Wahbi : a partir de 40 000 jusqu'a 80 000 DHS
UPDATE vendors SET prix_min = 40000.00, prix_max = 80000.00
WHERE name LIKE '%Youssef Wahbi%';

-- Marouane Hajji : artiste TV, prestige
UPDATE vendors SET prix_min = 60000.00, prix_max = 150000.00
WHERE name LIKE '%Marouane Hajji%';

-- Marouane Lebbar : prestige Fes
UPDATE vendors SET prix_min = 25000.00, prix_max = 60000.00
WHERE name LIKE '%Marouane Lebbar%';

-- Younes Rbati : Rabat, moyen-prestige
UPDATE vendors SET prix_min = 18000.00, prix_max = 35000.00
WHERE name LIKE '%Younes Rbati%';

-- Issawa Zakaria Faida : luxe
UPDATE vendors SET prix_min = 8000.00, prix_max = 18000.00
WHERE name LIKE '%Zakaria Faida%';

-- Issawa Salim
UPDATE vendors SET prix_min = 5000.00, prix_max = 12000.00
WHERE name LIKE '%Issawa Salim%';

-- Traiteurs : prix par personne realiste (DHS / personne)
UPDATE vendors SET prix_min = 450.00, prix_max = 1200.00
WHERE name LIKE '%Al Jawda%';

UPDATE vendors SET prix_min = 380.00, prix_max = 950.00
WHERE name LIKE '%El Kortbi%';

UPDATE vendors SET prix_min = 400.00, prix_max = 1000.00
WHERE name LIKE '%Sekkate Traiteur%';

UPDATE vendors SET prix_min = 350.00, prix_max = 900.00
WHERE name LIKE '%El Amane%';

UPDATE vendors SET prix_min = 400.00, prix_max = 1100.00
WHERE name LIKE '%Sahel%';

UPDATE vendors SET prix_min = 500.00, prix_max = 1500.00
WHERE name LIKE '%Doreve%';

-- Salles : forfait realiste 2026
UPDATE vendors SET prix_min = 60000.00, prix_max = 200000.00
WHERE name LIKE '%Palais Faraj%';

UPDATE vendors SET prix_min = 50000.00, prix_max = 180000.00
WHERE name LIKE '%Palais Alyakout%';

UPDATE vendors SET prix_min = 45000.00, prix_max = 160000.00
WHERE name LIKE '%Palais Benjelloun%';

UPDATE vendors SET prix_min = 25000.00, prix_max = 70000.00
WHERE name LIKE '%El Ouazzani%';

UPDATE vendors SET prix_min = 22000.00, prix_max = 60000.00
WHERE name LIKE '%Dar Hajji%';

-- Neggafa : marche premium fassi
UPDATE vendors SET prix_min = 25000.00, prix_max = 90000.00
WHERE name LIKE '%Dar Benjelloun%' AND name LIKE '%Wedding Planner%';

UPDATE vendors SET prix_min = 22000.00, prix_max = 80000.00
WHERE name LIKE '%Majda Benjelloun%';

UPDATE vendors SET prix_min = 12000.00, prix_max = 35000.00
WHERE name LIKE '%Haja Zakia%';

-- Photographes : tarif jour Maroc realiste
UPDATE vendors SET prix_min = 8000.00, prix_max = 25000.00
WHERE name LIKE '%Photo Tazi%';

UPDATE vendors SET prix_min = 7000.00, prix_max = 22000.00
WHERE name LIKE '%Prestige Camera%';

UPDATE vendors SET prix_min = 6500.00, prix_max = 20000.00
WHERE name LIKE '%El Achouri%';

-- Makeup / Coiffure
UPDATE vendors SET prix_min = 4000.00, prix_max = 12000.00
WHERE name LIKE '%Make Me Fab%';

UPDATE vendors SET prix_min = 3500.00, prix_max = 10000.00
WHERE name LIKE '%Salamoun%';

UPDATE vendors SET prix_min = 2500.00, prix_max = 7000.00
WHERE name LIKE '%Yassine Makeup%';

UPDATE vendors SET prix_min = 2000.00, prix_max = 5500.00
WHERE name LIKE '%Kouki%';

-- Cake / Patisserie
UPDATE vendors SET prix_min = 3500.00, prix_max = 12000.00
WHERE name LIKE '%Emeraude%';

UPDATE vendors SET prix_min = 2000.00, prix_max = 7000.00
WHERE name LIKE '%Chocolat de Joie%';

UPDATE vendors SET prix_min = 1200.00, prix_max = 5000.00
WHERE name LIKE '%Cake Ahlame%';

-- Wedding planner
UPDATE vendors SET prix_min = 15000.00, prix_max = 50000.00
WHERE name LIKE '%Mounia Tounsi%';

-- Fleuriste
UPDATE vendors SET prix_min = 4000.00, prix_max = 18000.00
WHERE name LIKE '%Edikki%';

-- Verification
SELECT 'Migration fix OK' AS status, COUNT(*) AS total_vendors FROM vendors;
SELECT name, prix_min, prix_max, phone, instagram FROM vendors WHERE name LIKE '%Wahbi%' OR name LIKE '%Festin%' OR name LIKE '%Tenguif%' OR name LIKE '%Al Jawda%';
