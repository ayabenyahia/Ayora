-- ============================================
-- AYORA - Migration: Demandes de devis + Rendez-vous
-- ============================================

CREATE TABLE IF NOT EXISTS demandes_devis (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    vendor_id INT NOT NULL,
    budget_min DECIMAL(10,2),
    budget_max DECIMAL(10,2),
    message TEXT,
    date_mariage VARCHAR(50),
    nb_invites INT DEFAULT 0,
    statut ENUM('EN_ATTENTE','ACCEPTE','REFUSE') DEFAULT 'EN_ATTENTE',
    reponse_prestataire TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rendez_vous (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    vendor_id INT NOT NULL,
    date_rdv DATE NOT NULL,
    heure_rdv VARCHAR(10),
    lieu VARCHAR(255) DEFAULT 'A definir',
    note TEXT,
    statut ENUM('EN_ATTENTE','CONFIRME','ANNULE') DEFAULT 'EN_ATTENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);
