-- ============================================================
-- Passe le compte alisel@ayora.ma au plan PREMIUM
-- A executer dans phpMyAdmin (base : ayora_db) ou via mysql CLI
-- ============================================================

USE ayora_db;

-- 1) Verifier que le compte existe
SELECT id, email, first_name, subscription_type, role
FROM users
WHERE email = 'alisel@ayora.ma';

-- 2) Passer le compte en PREMIUM
UPDATE users
SET subscription_type = 'PREMIUM'
WHERE email = 'alisel@ayora.ma';

-- 3) Confirmation
SELECT id, email, first_name, subscription_type, role
FROM users
WHERE email = 'alisel@ayora.ma';
