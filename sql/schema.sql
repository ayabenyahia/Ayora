-- ============================================
-- AYORA - Planification de Mariage a Fes
-- Schema de la base de donnees MySQL
-- ============================================

CREATE DATABASE IF NOT EXISTS ayora_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ayora_db;

-- ============================================
-- TABLE : users
-- ============================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    city VARCHAR(100) DEFAULT 'Fes',
    subscription_type ENUM('FREE', 'PREMIUM') DEFAULT 'FREE',
    questionnaire_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE : questionnaire_answers
-- ============================================
CREATE TABLE questionnaire_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    -- Budget
    budget_total DECIMAL(12,2),
    budget_flexibility ENUM('STRICT', 'FLEXIBLE', 'TRES_FLEXIBLE'),
    -- Invites
    nb_invites INT,
    nb_invites_femmes INT,
    nb_invites_hommes INT,
    -- Date et lieu
    date_mariage DATE,
    saison_preferee ENUM('PRINTEMPS', 'ETE', 'AUTOMNE', 'HIVER'),
    lieu_ceremonie VARCHAR(255),
    -- Style et ambiance
    style_mariage ENUM('TRADITIONNEL', 'MODERNE', 'MIXTE', 'LUXE', 'SIMPLE'),
    ambiance ENUM('FESTIVE', 'INTIME', 'GRANDIOSE', 'ROMANTIQUE', 'FAMILIALE'),
    theme_couleur VARCHAR(100),
    -- Niveau de luxe
    niveau_luxe ENUM('ECONOMIQUE', 'MOYEN', 'PREMIUM', 'ULTRA_LUXE'),
    -- Priorites (1 a 5)
    priorite_salle INT DEFAULT 3,
    priorite_traiteur INT DEFAULT 3,
    priorite_photo INT DEFAULT 3,
    priorite_musique INT DEFAULT 3,
    priorite_decoration INT DEFAULT 3,
    priorite_neggafa INT DEFAULT 3,
    priorite_makeup INT DEFAULT 3,
    -- Preferences specifiques
    type_cuisine ENUM('MAROCAINE', 'INTERNATIONALE', 'MIXTE'),
    type_musique ENUM('TRADITIONNELLE', 'MODERNE', 'DJ', 'ORCHESTRE', 'ISSAWA', 'MIXTE'),
    pref_photo ENUM('CLASSIQUE', 'ARTISTIQUE', 'REPORTAGE', 'DRONE'),
    pref_decoration ENUM('TRADITIONNELLE', 'MODERNE', 'FLORALE', 'MINIMALISTE', 'LUXUEUSE'),
    -- Neggafa preferences
    nb_tenues_neggafa INT DEFAULT 3,
    style_neggafa ENUM('TRADITIONNEL', 'MODERNE', 'MIXTE'),
    -- Economies souhaitees
    postes_economie TEXT,
    -- Notes additionnelles
    notes_speciales TEXT,
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- TABLE : vendor_categories
-- ============================================
CREATE TABLE vendor_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    name_fr VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50)
);

-- ============================================
-- TABLE : vendors (prestataires)
-- ============================================
CREATE TABLE vendors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) DEFAULT 'Fes',
    description TEXT,
    prix_min DECIMAL(10,2),
    prix_max DECIMAL(10,2),
    gamme ENUM('ECONOMIQUE', 'MOYEN', 'PREMIUM') DEFAULT 'MOYEN',
    phone VARCHAR(20),
    email VARCHAR(255),
    instagram VARCHAR(255),
    address VARCHAR(500),
    tags VARCHAR(500),
    rating DECIMAL(3,2) DEFAULT 0.00,
    nb_avis INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES vendor_categories(id)
);

-- ============================================
-- TABLE : guests (invites)
-- ============================================
CREATE TABLE guests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    groupe ENUM('FAMILLE_MARIEE', 'FAMILLE_MARIE', 'AMIS_MARIEE', 'AMIS_MARIE', 'COLLEGUES', 'AUTRES') DEFAULT 'AUTRES',
    nb_personnes INT DEFAULT 1,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- TABLE : invitations
-- ============================================
CREATE TABLE invitations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    user_id INT NOT NULL,
    statut ENUM('EN_ATTENTE', 'ENVOYEE', 'CONFIRMEE', 'DECLINEE') DEFAULT 'EN_ATTENTE',
    template_name VARCHAR(100) DEFAULT 'classique',
    date_envoi TIMESTAMP NULL,
    date_reponse TIMESTAMP NULL,
    message_perso TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- TABLE : subscriptions
-- ============================================
CREATE TABLE subscriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    plan ENUM('FREE', 'PREMIUM') DEFAULT 'FREE',
    invitations_sent INT DEFAULT 0,
    max_invitations_free INT DEFAULT 10,
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- TABLE : recommendations
-- ============================================
CREATE TABLE recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vendor_id INT NOT NULL,
    score DECIMAL(5,2) DEFAULT 0.00,
    raison TEXT,
    is_viewed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);
