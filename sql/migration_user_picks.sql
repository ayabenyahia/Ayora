-- ============================================================
-- Migration user_picks - 2026-05-04
--
-- Table qui stocke les choix de la mariee parmi les recommandations.
-- Une seule selection par categorie (= un seul prestataire retenu pour
-- chaque type de service).
--
-- Le bouton "Choisir" sur recommendations.html ecrit dans cette table.
-- La page mychoices.html lit cette table pour afficher les choix.
-- ============================================================

USE ayora_db;

CREATE TABLE IF NOT EXISTS user_picks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vendor_id INT NOT NULL,
    category_id INT NOT NULL,
    picked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES vendor_categories(id),
    -- une seule selection par categorie pour un user donne
    UNIQUE KEY uk_user_category (user_id, category_id)
);

-- Index pour requetes rapides "tous les picks d'un user"
CREATE INDEX idx_user_picks_user ON user_picks(user_id);
