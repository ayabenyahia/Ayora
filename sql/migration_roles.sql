-- ============================================
-- AYORA - Migration: Ajout systeme de roles
-- ============================================

-- Ajouter le role a la table users
ALTER TABLE users ADD COLUMN role ENUM('CLIENT', 'ADMIN', 'PRESTATAIRE') DEFAULT 'CLIENT' AFTER questionnaire_completed;

-- Ajouter le lien vers le prestataire (pour les comptes PRESTATAIRE)
ALTER TABLE users ADD COLUMN vendor_id INT NULL AFTER role;
ALTER TABLE users ADD CONSTRAINT fk_users_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE SET NULL;

-- Creer un compte admin par defaut (a changer le mot de passe en production)
INSERT INTO users (email, password, first_name, last_name, city, subscription_type, questionnaire_completed, role)
VALUES ('admin@ayora.ma', 'admin123', 'Admin', 'Ayora', 'Fes', 'PREMIUM', TRUE, 'ADMIN');
