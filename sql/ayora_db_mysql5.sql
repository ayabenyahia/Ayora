-- ============================================
-- AYORA - Script complet pour phpMyAdmin
-- A importer en une seule fois.
-- Variante MySQL 5.1 : utf8 (3 bytes) + updated_at en DATETIME
-- ============================================

DROP DATABASE IF EXISTS ayora_db;
CREATE DATABASE ayora_db CHARACTER SET utf8 COLLATE utf8_general_ci;
USE ayora_db;

-- ============================================
-- TABLE : vendor_categories
-- (creee avant vendors a cause de la cle etrangere)
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
-- TABLE : users
-- (avec role et vendor_id deja inclus)
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
    role ENUM('CLIENT', 'ADMIN', 'PRESTATAIRE') DEFAULT 'CLIENT',
    vendor_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT fk_users_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE SET NULL
);

-- ============================================
-- TABLE : questionnaire_answers
-- ============================================
CREATE TABLE questionnaire_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    budget_total DECIMAL(12,2),
    budget_flexibility ENUM('STRICT', 'FLEXIBLE', 'TRES_FLEXIBLE'),
    nb_invites INT,
    nb_invites_femmes INT,
    nb_invites_hommes INT,
    date_mariage DATE,
    saison_preferee ENUM('PRINTEMPS', 'ETE', 'AUTOMNE', 'HIVER'),
    lieu_ceremonie VARCHAR(255),
    style_mariage ENUM('TRADITIONNEL', 'MODERNE', 'MIXTE', 'LUXE', 'SIMPLE'),
    ambiance ENUM('FESTIVE', 'INTIME', 'GRANDIOSE', 'ROMANTIQUE', 'FAMILIALE'),
    theme_couleur VARCHAR(100),
    niveau_luxe ENUM('ECONOMIQUE', 'MOYEN', 'PREMIUM', 'ULTRA_LUXE'),
    priorite_salle INT DEFAULT 3,
    priorite_traiteur INT DEFAULT 3,
    priorite_photo INT DEFAULT 3,
    priorite_musique INT DEFAULT 3,
    priorite_decoration INT DEFAULT 3,
    priorite_neggafa INT DEFAULT 3,
    priorite_makeup INT DEFAULT 3,
    type_cuisine ENUM('MAROCAINE', 'INTERNATIONALE', 'MIXTE'),
    type_musique ENUM('TRADITIONNELLE', 'MODERNE', 'DJ', 'ORCHESTRE', 'ISSAWA', 'MIXTE'),
    pref_photo ENUM('CLASSIQUE', 'ARTISTIQUE', 'REPORTAGE', 'DRONE'),
    pref_decoration ENUM('TRADITIONNELLE', 'MODERNE', 'FLORALE', 'MINIMALISTE', 'LUXUEUSE'),
    nb_tenues_neggafa INT DEFAULT 3,
    style_neggafa ENUM('TRADITIONNEL', 'MODERNE', 'MIXTE'),
    postes_economie TEXT,
    notes_speciales TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- TABLE : guests
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

-- ============================================
-- TABLE : demandes_devis
-- ============================================
CREATE TABLE demandes_devis (
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
    updated_at DATETIME NULL,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- ============================================
-- TABLE : rendez_vous
-- ============================================
CREATE TABLE rendez_vous (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    vendor_id INT NOT NULL,
    date_rdv DATE NOT NULL,
    heure_rdv VARCHAR(10),
    lieu VARCHAR(255) DEFAULT 'A definir',
    note TEXT,
    statut ENUM('EN_ATTENTE','CONFIRME','ANNULE') DEFAULT 'EN_ATTENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
);

-- ============================================
-- DONNEES : categories
-- ============================================
INSERT INTO vendor_categories (name, name_fr, description, icon) VALUES
('NEGGAFA', 'Neggafa', 'Habilleuse traditionnelle de la mariee', 'neggafa'),
('MAKEUP', 'Maquillage', 'Artiste maquilleur pour mariee', 'makeup'),
('COIFFURE', 'Coiffure', 'Coiffeur specialise mariage', 'coiffure'),
('PHOTOGRAPHE', 'Photographe', 'Photographe de mariage professionnel', 'photo'),
('VIDEASTE', 'Videaste', 'Videaste et cinematographe de mariage', 'video'),
('CAKE_DESIGNER', 'Cake Designer', 'Patissier specialise gateaux de mariage', 'cake'),
('ISSAWA', 'Issawa', 'Groupe de musique traditionnelle Issawa', 'issawa'),
('ORCHESTRE', 'Orchestre', 'Orchestre et groupe musical', 'orchestre'),
('DECORATION', 'Decoration', 'Decorateur de mariage et evenement', 'decoration'),
('FLEURISTE', 'Fleuriste', 'Composition florale pour mariage', 'fleuriste'),
('SALLE', 'Salle de fete', 'Salle de reception et de mariage', 'salle'),
('TRAITEUR', 'Traiteur', 'Service traiteur pour mariage', 'traiteur'),
('MYADI', 'Myadi / Tyafr', 'Service Myadi et Tyafr traditionnel', 'myadi'),
('DJ', 'DJ', 'DJ professionnel pour soiree', 'dj'),
('TRANSPORT', 'Transport', 'Location voiture de luxe pour mariage', 'transport'),
('HENNAYA', 'Hennaya', 'Artiste de henne traditionnel', 'hennaya'),
('WEDDING_PLANNER', 'Wedding Planner', 'Organisateur de mariage professionnel', 'planner');

-- ============================================
-- DONNEES : prestataires (echantillon)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(1, 'Neggafa El Farssi', 'Fes', 'Institution incontournable de la neggafa a Fes. Tenues royales, caftans de haute couture.', 15000.00, 50000.00, 'PREMIUM', '0661-223344', '@elfarssi_neggafa', 'Fes Medina', 'luxe,fassi,royal,caftan', 4.90, 187),
(1, 'Dar Benjelloun Neggafa', 'Fes', 'Maison de prestige pour la neggafa a Fes.', 20000.00, 60000.00, 'PREMIUM', '0662-334455', '@dar_benjelloun_neggafa', 'Derb Benjelloun, Fes Medina', 'prestige,exclusif,amariya', 4.95, 203),
(1, 'Haja Zakia Neggafa', 'Fes', 'Neggafa traditionnelle authentique. Plus de 30 ans d''experience.', 8000.00, 20000.00, 'MOYEN', '0664-556677', '@haja_zakia_neggafa', 'Fes El Bali', 'authentique,tradition,fassi', 4.80, 241),
(2, 'Nadia El Guerch', 'Fes', 'Makeup artist de renom a Fes. Style glamour et sophistique.', 3000.00, 8000.00, 'PREMIUM', '0668-112233', '@nadia.elguerch', 'Ville Nouvelle, Fes', 'glamour,HD,sophistique', 4.90, 298),
(2, 'Makeup by Hala', 'Fes', 'Style lumineux et naturel rehausse. Produits premium.', 2500.00, 6000.00, 'PREMIUM', '0669-223344', '@makeupbyhala_fes', 'Fes', 'lumineux,naturel,premium', 4.85, 216),
(3, 'Salon Yasmine Fes', 'Fes', 'Salon haut de gamme. Chignons, coiffures orientales et modernes.', 1500.00, 4000.00, 'PREMIUM', '0672-556677', '@salon_yasmine_fes', 'Avenue Hassan II, Fes', 'haut-gamme,chignon,oriental', 4.80, 167),
(4, 'Yassine Photography', 'Fes', 'Photographe de mariage. Style reportage naturel et emotionnel.', 5000.00, 15000.00, 'PREMIUM', '0675-889900', '@yassine_photo_fes', 'Fes', 'reportage,naturel,album', 4.85, 203),
(4, 'Studio Lumiere Fes', 'Fes', 'Studio photo mariage. Couverture complete jour et nuit.', 3000.00, 8000.00, 'MOYEN', '0676-990011', '@studio_lumiere_fes', 'Bab Boujloud, Fes', 'studio,medina,classique', 4.70, 145),
(7, 'Troupe Issawa Sidi Ahmed Tijani', 'Fes', 'Troupe Issawa authentique de la zaouia Tijania.', 3000.00, 8000.00, 'MOYEN', '0685-889900', '@issawa_tijani_fes', 'Medina, Fes', 'authentique,tijani,traditionnel', 4.80, 156),
(8, 'Orchestre Mohamed Laasry', 'Fes', 'Orchestre de reference a Fes. Chaabi fassi, andalous et variete.', 15000.00, 40000.00, 'PREMIUM', '0688-112233', '@mohamed_laasry_officiel', 'Fes', 'chaabi,andalous,fassi,maestro', 4.95, 312),
(8, 'Orchestre Marouane Lebbar', 'Fes', 'Style moderne mele a la tradition marocaine.', 12000.00, 35000.00, 'PREMIUM', '0689-223344', '@marouane_lebbar', 'Fes', 'moderne,tradition,puissant', 4.90, 287),
(9, 'Riad Decor Fes', 'Fes', 'Decoration de mariage haut de gamme.', 10000.00, 40000.00, 'PREMIUM', '0693-667788', '@riad_decor_fes', 'Fes', 'haut-gamme,oriental,floral', 4.85, 156),
(10, 'Fleurs de Fes', 'Fes', 'Fleuriste specialise mariage. Bouquets et arche florale.', 3000.00, 12000.00, 'PREMIUM', '0696-990011', '@fleurs_de_fes', 'Avenue Mohammed V, Fes', 'frais,bouquet,arche', 4.80, 134),
(11, 'Palais Faraj Fes', 'Fes', 'Palais historique en medina de Fes. Capacite 500 personnes.', 50000.00, 150000.00, 'PREMIUM', '0699-223344', '@palais_faraj_fes', 'Bab Ziat, Fes Medina', 'palais,medina,500-personnes', 4.95, 203),
(11, 'Salle Al Andalous', 'Fes', 'Salle moderne climatisee. Capacite 300 personnes.', 15000.00, 40000.00, 'MOYEN', '0601-334455', '@salle_andalous_fes', 'Ville Nouvelle, Fes', 'moderne,climatise,parking', 4.65, 145),
(12, 'Traiteur Festin', 'Fes', 'Traiteur de prestige a Fes. Service gastronomique haut de gamme.', 300.00, 700.00, 'PREMIUM', '0604-667788', '@festin_traiteur_fes', 'Fes', 'prestige,gastronomique', 4.90, 234),
(12, 'Traiteur Al Jawda', 'Fes', 'Cuisine marocaine authentique de qualite superieure.', 200.00, 500.00, 'MOYEN', '0605-778899', '@traiteur_aljawda_fes', 'Fes', 'authentique,tajine,couscous', 4.80, 198),
(14, 'DJ Night Vibes Fes', 'Fes', 'DJ specialise soirees mariage premium.', 5000.00, 15000.00, 'PREMIUM', '0613-556677', '@dj_night_vibes_fes', 'Fes', 'premium,bose,laser', 4.80, 98),
(16, 'Hennaya Lalla Aisha', 'Fes', 'Artiste henne traditionnelle de Fes.', 1500.00, 5000.00, 'PREMIUM', '0616-889900', '@hennaya_lalla_aisha', 'Medina, Fes', 'traditionnel,fassi,motifs', 4.85, 198),
(17, 'Ayora Events', 'Fes', 'Wedding planner professionnel. Organisation complete A a Z.', 10000.00, 40000.00, 'PREMIUM', '0619-112233', '@ayora_events_fes', 'Ville Nouvelle, Fes', 'complet,coordination,jour-j', 4.90, 78);

-- ============================================
-- DONNEES : utilisateurs de test
-- ============================================
INSERT INTO users (email, password, first_name, last_name, phone, city, subscription_type, role)
VALUES ('admin@ayora.ma', 'admin123', 'Admin', 'Ayora', '0600-000000', 'Fes', 'PREMIUM', 'ADMIN');

INSERT INTO users (email, password, first_name, last_name, phone, city, subscription_type, role)
VALUES ('test@ayora.ma', 'test123', 'Salma', 'Bennani', '0661-000000', 'Fes', 'FREE', 'CLIENT');

-- Abonnements correspondants
INSERT INTO subscriptions (user_id, plan, invitations_sent) VALUES (1, 'PREMIUM', 0);
INSERT INTO subscriptions (user_id, plan, invitations_sent) VALUES (2, 'FREE', 0);
