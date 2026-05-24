-- Migration : ajoute venue_type aux prestataires (idempotent).
-- Utilise par AyoraRecommendationEngine pour appliquer une CONTRAINTE FORTE
-- sur lieuCeremonie. Lance via : java com.ayora.util.RunVenueTypeMigration

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='vendors' AND column_name='venue_type');
SET @ddl = IF(@has_col=0, 'ALTER TABLE vendors ADD COLUMN venue_type VARCHAR(20) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='vendors' AND index_name='idx_vendors_venue_type');
SET @ddl2 = IF(@has_idx=0, 'CREATE INDEX idx_vendors_venue_type ON vendors(category_id, venue_type)', 'SELECT 1');
PREPARE stmt2 FROM @ddl2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Reset : on remet a NULL toute la categorie SALLE pour re-classifier proprement
-- (necessaire car on introduit la distinction RIAD vs PALAIS).
UPDATE vendors SET venue_type = NULL WHERE category_id = 11;

-- PALAIS : grand, royal, capacite elevee. On detecte AVANT RIAD pour que les
-- "Palais X" ne soient pas avales par le pattern riad.
UPDATE vendors SET venue_type = 'PALAIS' WHERE category_id = 11 AND venue_type IS NULL AND (LOWER(name) REGEXP 'palais|palace|royal|sheherazade|faraj' OR LOWER(IFNULL(tags, '')) REGEXP 'palais|royal|grandiose|prestige|grande-capacite|500-personnes|350-personnes|300-personnes');

-- RIAD : intime, maison traditionnelle. Apres PALAIS pour ne pas voler ses entrees.
UPDATE vendors SET venue_type = 'RIAD' WHERE category_id = 11 AND venue_type IS NULL AND (LOWER(name) REGEXP 'riad|dar lys|dar ba|dar al|maison bleue' OR LOWER(IFNULL(tags, '')) REGEXP 'riad|medina|fassi|andalous|arabo|patio|zellige|heritage|authentique|intime');

-- HOTEL : resort, complexe avec chambres.
UPDATE vendors SET venue_type = 'HOTEL' WHERE category_id = 11 AND venue_type IS NULL AND (LOWER(name) REGEXP 'hotel|resort|chateaux|relais' OR LOWER(IFNULL(tags, '')) REGEXP 'hotel|resort|5-etoiles|boutique-hotel|complexe|chateaux|relais');

-- JARDIN : exterieur, verdure.
UPDATE vendors SET venue_type = 'JARDIN' WHERE category_id = 11 AND venue_type IS NULL AND (LOWER(name) REGEXP 'jardin|garden' OR LOWER(IFNULL(tags, '')) REGEXP 'jardin|verdure|outdoor');

-- PISCINE : bord de piscine.
UPDATE vendors SET venue_type = 'PISCINE' WHERE category_id = 11 AND venue_type IS NULL AND (LOWER(name) REGEXP 'piscine|pool' OR LOWER(IFNULL(tags, '')) REGEXP 'piscine|pool|bord-piscine');

-- SALLE : moderne par defaut.
UPDATE vendors SET venue_type = 'SALLE' WHERE category_id = 11 AND venue_type IS NULL AND (LOWER(name) REGEXP 'salle' OR LOWER(IFNULL(tags, '')) REGEXP 'salle|moderne|climatise|polyvalent|spacieux');

-- Fallback : tout le reste = SALLE.
UPDATE vendors SET venue_type = 'SALLE' WHERE category_id = 11 AND venue_type IS NULL;

SELECT venue_type, COUNT(*) AS nb FROM vendors WHERE category_id = 11 GROUP BY venue_type ORDER BY venue_type;
