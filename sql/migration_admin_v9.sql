-- ============================================================
-- Migration v9 — Admin back-office
-- Ajoute is_active sur users pour permettre suspension/reactivation
-- depuis l'interface admin (sans suppression dure).
-- ============================================================

USE ayora_db;

-- Ajout colonne uniquement si elle n'existe pas (idempotent).
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ayora_db' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'is_active'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1 AFTER vendor_id',
    'SELECT "is_active deja present" AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Verification
SELECT COUNT(*) AS users_actifs FROM users WHERE is_active = 1;
