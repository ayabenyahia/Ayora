-- ============================================
-- AYORA - Migration : ajout colonne video_url
-- Pour les modeles d'invitations VIDEO premium.
-- ============================================

USE ayora_db;

ALTER TABLE invitations
    ADD COLUMN video_url VARCHAR(500) NULL AFTER message_perso;

-- Verification
SELECT 'Migration video_url OK' AS status;
SHOW COLUMNS FROM invitations LIKE 'video_url';
