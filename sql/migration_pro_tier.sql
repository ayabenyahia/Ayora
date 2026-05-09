-- ============================================
-- AYORA - Migration : ajout du palier PRO
-- A executer dans phpMyAdmin OU via mysql -u root ayora_db < migration_pro_tier.sql
-- ============================================

USE ayora_db;

-- 1) Etendre l'ENUM des plans pour la table users
ALTER TABLE users
    MODIFY COLUMN subscription_type ENUM('FREE', 'PRO', 'PREMIUM') DEFAULT 'FREE';

-- 2) Etendre l'ENUM des plans pour la table subscriptions
ALTER TABLE subscriptions
    MODIFY COLUMN plan ENUM('FREE', 'PRO', 'PREMIUM') DEFAULT 'FREE';

-- 3) Optionnel : compte de demonstration PRO
INSERT INTO users (email, password, first_name, last_name, phone, city, subscription_type, role)
SELECT 'pro@ayora.ma', 'pro123', 'Yasmine', 'Pro', '0600-111111', 'Fes', 'PRO', 'CLIENT'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'pro@ayora.ma');

INSERT INTO subscriptions (user_id, plan, invitations_sent)
SELECT id, 'PRO', 0 FROM users WHERE email = 'pro@ayora.ma'
  AND id NOT IN (SELECT user_id FROM subscriptions);

-- Verification
SELECT 'Migration PRO tier OK' AS status;
