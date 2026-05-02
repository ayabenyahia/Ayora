-- ============================================================
-- Migration vendors v5 - 2026-05-02
--
-- Corrections de classification suite au feedback utilisateur :
--
--   - Chocolat de Joie       : Cake Designer (6) -> Myadi/Tyafr (13)
--     C'est un service de tyafer/myadi traditionnel (buffets, fiancailles),
--     pas un cake designer.
--
--   - Emeraude by Malika Alaoui : Cake Designer (6) -> Myadi/Tyafr (13)
--     Idem, prestation tyafer/myadi.
--
--   - Haj Said Berrada       : Orchestre (8) -> Issawa (7)
--     C'est un groupe Issawa (musique soufie traditionnelle), pas un
--     orchestre populaire.
--
-- A executer apres : migration_vendors_v4.sql
-- ============================================================

USE ayora_db;

-- 1. Chocolat de Joie -> Myadi
UPDATE vendors
SET category_id = 13,
    description = REPLACE(description, 'Cake Designer', 'Myadi / Tyafer'),
    tags = CONCAT(IFNULL(tags, ''), ',myadi,tyafer,traditionnel')
WHERE name LIKE '%Chocolat%Joie%';

-- 2. Emeraude by Malika Alaoui -> Myadi
UPDATE vendors
SET category_id = 13,
    description = REPLACE(description, 'Cake Designer', 'Myadi / Tyafer'),
    tags = CONCAT(IFNULL(tags, ''), ',myadi,tyafer,traditionnel')
WHERE name LIKE '%Emeraude%Malika%';

-- 3. Haj Said Berrada -> Issawa
UPDATE vendors
SET category_id = 7,
    description = REPLACE(description, 'Orchestre', 'Groupe Issawa'),
    tags = CONCAT(IFNULL(tags, ''), ',issawa,soufi,traditionnel,fassi')
WHERE name LIKE '%Berrada%' OR name LIKE '%Haj Said%';

-- ============================================================
-- VERIFICATIONS
-- ============================================================

SELECT id, category_id, name, gamme, prix_min FROM vendors
WHERE name LIKE '%Chocolat%Joie%'
   OR name LIKE '%Emeraude%Malika%'
   OR name LIKE '%Berrada%';

SELECT '=== Distribution apres v5 ===' AS info;
SELECT vc.id, vc.name_fr, COUNT(v.id) AS nb
FROM vendor_categories vc
LEFT JOIN vendors v ON v.category_id = vc.id AND v.is_active = TRUE
WHERE vc.name_fr NOT LIKE '%Obsolete%' AND vc.name_fr NOT LIKE '%Supprime%'
GROUP BY vc.id, vc.name_fr
ORDER BY vc.id;
