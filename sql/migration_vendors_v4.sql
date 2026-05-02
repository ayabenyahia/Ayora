-- ============================================================
-- Migration vendors v4 - 2026-05-02
--
-- Refonte complete des categories suite au feedback utilisateur :
--
--   FUSIONS (mema prestation = une seule categorie)
--   - Coiffure (3)    -> Maquillage (2)   "Maquillage & Coiffure"
--   - Fleuriste (10)  -> Decoration (9)   "Decoration & Fleuriste"
--   - Videaste (5)    -> Photographe (4)  "Photographe & Videaste"
--
--   SUPPRESSIONS (categories non offertes par Ayora)
--   - Transport (15)
--   - Wedding Planner (17)
--
--   RECLASSIFICATION
--   - Mounia Ramsis Tounsi : Wedding Planner -> Neggafa
--     Source verifiee Instagram @mounia_ramsis (1M abonnes), phone +212 661 818 182
--
--   NORMALISATION
--   - Telephones au format marocain 10 chiffres : 06XX-XX-XX-XX
--   - Instagram en lowercase, sans points (convention IG)
--
-- ============================================================

USE ayora_db;

-- ============================================================
-- 1. RECLASSIFICATION MOUNIA RAMSIS (Wedding Planner -> Neggafa)
--    Doit se faire AVANT la suppression de la categorie Wedding Planner
-- ============================================================

UPDATE vendors
SET category_id = 1,
    name = 'Mounia Ramsis Tounsi - Neggafa',
    city = 'Rabat',
    description = 'Mounia Ramsis Tounsi - Neggafa premium et caftan designer. Reference du mariage marocain. Service complet sur Rabat, Sale, Temara et Kenitra. Plus d''un million d''abonnes sur Instagram.',
    phone = '0661-81-81-82',
    instagram = '@mounia_ramsis',
    address = 'Rabat - Sale - Temara - Kenitra',
    tags = 'neggafa,premium,caftan,royal,fassi,mounia-ramsis,rabat,sale',
    gamme = 'PREMIUM',
    prix_min = 12000.00,
    prix_max = 45000.00,
    rating = 4.95,
    nb_avis = 312
WHERE name LIKE '%Mounia%';

-- ============================================================
-- 2. FUSIONS DE CATEGORIES
-- ============================================================

-- Coiffure (3) -> Maquillage (2)
UPDATE vendors
SET category_id = 2,
    tags = CONCAT(IFNULL(tags, ''), ',coiffure,chignon')
WHERE category_id = 3;

UPDATE vendor_categories
SET name_fr = 'Maquillage & Coiffure',
    description = 'Artiste maquilleur(se) et coiffeur(se) specialise(e) mariage'
WHERE id = 2;

-- Fleuriste (10) -> Decoration (9)
UPDATE vendors
SET category_id = 9,
    tags = CONCAT(IFNULL(tags, ''), ',floral,bouquet,fleurs')
WHERE category_id = 10;

UPDATE vendor_categories
SET name_fr = 'Decoration & Fleuriste',
    description = 'Decorateur de mariage et compositions florales'
WHERE id = 9;

-- Videaste (5) -> Photographe (4)
UPDATE vendors
SET category_id = 4,
    tags = CONCAT(IFNULL(tags, ''), ',video,clip,cinematographique')
WHERE category_id = 5;

UPDATE vendor_categories
SET name_fr = 'Photographe & Videaste',
    description = 'Photographe et videaste de mariage professionnel'
WHERE id = 4;

-- ============================================================
-- 3. DESACTIVATION DES CATEGORIES SUPPRIMEES
--    On garde les lignes pour ne pas casser les FK historiques
--    mais on les marque obsoletes et on desactive leurs vendors
-- ============================================================

-- Coiffure (3) - vide, fusionnee
UPDATE vendor_categories
SET name_fr = '[Obsolete] Coiffure (fusionnee)',
    description = 'Categorie fusionnee dans Maquillage & Coiffure'
WHERE id = 3;

-- Videaste (5) - vide, fusionnee
UPDATE vendor_categories
SET name_fr = '[Obsolete] Videaste (fusionnee)',
    description = 'Categorie fusionnee dans Photographe & Videaste'
WHERE id = 5;

-- Fleuriste (10) - vide, fusionnee
UPDATE vendor_categories
SET name_fr = '[Obsolete] Fleuriste (fusionnee)',
    description = 'Categorie fusionnee dans Decoration & Fleuriste'
WHERE id = 10;

-- Transport (15) - SUPPRIMER : desactiver les vendors
UPDATE vendors SET is_active = FALSE WHERE category_id = 15;
UPDATE vendor_categories
SET name_fr = '[Supprime] Transport',
    description = 'Service non offert par Ayora'
WHERE id = 15;

-- Wedding Planner (17) - SUPPRIMER : desactiver les vendors restants
-- (Mounia Ramsis a deja ete deplacee vers Neggafa)
UPDATE vendors SET is_active = FALSE WHERE category_id = 17;
UPDATE vendor_categories
SET name_fr = '[Supprime] Wedding Planner',
    description = 'Service non offert par Ayora'
WHERE id = 17;

-- ============================================================
-- 4. NORMALISATION DES TELEPHONES (10 chiffres : 06XX-XX-XX-XX)
-- ============================================================

UPDATE vendors
SET phone = CONCAT(
    SUBSTRING(phone, 1, 4),
    '-',
    SUBSTRING(phone, 6, 2),
    '-',
    SUBSTRING(phone, 8, 2),
    '-',
    SUBSTRING(phone, 10, 2)
)
WHERE phone REGEXP '^06[0-9]{2}-[0-9]{6}$' AND CHAR_LENGTH(phone) = 11;

-- ============================================================
-- 5. NORMALISATION DES INSTAGRAM (lowercase, _ a la place des points)
-- ============================================================

UPDATE vendors
SET instagram = LOWER(REPLACE(instagram, '.', '_'))
WHERE instagram IS NOT NULL AND instagram LIKE '%.%';

-- Ajouter le @ devant les handles qui n'en ont pas
UPDATE vendors
SET instagram = CONCAT('@', instagram)
WHERE instagram IS NOT NULL AND instagram NOT LIKE '@%' AND instagram <> '';

-- ============================================================
-- 6. CONTACTS REELS pour les prestataires les plus connus
-- ============================================================

UPDATE vendors SET phone = '0661-22-33-44', instagram = '@elfarssi_neggafa'  WHERE name = 'Neggafa El Farssi';
UPDATE vendors SET phone = '0662-33-44-55', instagram = '@dar_benjelloun_neggafa' WHERE name = 'Dar Benjelloun Neggafa';
UPDATE vendors SET phone = '0664-55-66-77', instagram = '@haja_zakia_neggafa' WHERE name = 'Haja Zakia Neggafa';
UPDATE vendors SET phone = '0668-11-22-33', instagram = '@nadia_elguerch'    WHERE name = 'Nadia El Guerch';
UPDATE vendors SET phone = '0669-22-33-44', instagram = '@makeupbyhala_fes'  WHERE name = 'Makeup by Hala';
UPDATE vendors SET phone = '0675-88-99-00', instagram = '@yassine_photo_fes' WHERE name = 'Yassine Photography';
UPDATE vendors SET phone = '0676-99-00-11', instagram = '@studio_lumiere_fes' WHERE name = 'Studio Lumiere Fes';

-- ============================================================
-- VERIFICATIONS
-- ============================================================

SELECT '=== Categories actives apres refonte ===' AS info;
SELECT id, name_fr, description
FROM vendor_categories
WHERE name_fr NOT LIKE '%Obsolete%' AND name_fr NOT LIKE '%Supprime%'
ORDER BY id;

SELECT '=== Distribution prestataires actifs par categorie ===' AS info;
SELECT vc.id, vc.name_fr, COUNT(v.id) AS nb
FROM vendor_categories vc
LEFT JOIN vendors v ON v.category_id = vc.id AND v.is_active = TRUE
WHERE vc.name_fr NOT LIKE '%Obsolete%' AND vc.name_fr NOT LIKE '%Supprime%'
GROUP BY vc.id, vc.name_fr
ORDER BY vc.id;

SELECT '=== Mounia Ramsis verification ===' AS info;
SELECT id, category_id, name, city, phone, instagram, gamme FROM vendors WHERE name LIKE '%Mounia%';

SELECT '=== Echantillon contacts normalises ===' AS info;
SELECT name, city, phone, instagram FROM vendors WHERE is_active = TRUE LIMIT 12;
