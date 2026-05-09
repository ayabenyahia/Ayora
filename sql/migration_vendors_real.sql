-- ============================================
-- AYORA - Migration : prestataires reels (depuis sources verifiees)
-- UPSERT par nom : si le nom existe, on met a jour ; sinon on insere.
-- A executer apres ayora_db_full.sql et migration_pro_tier.sql.
-- ============================================

USE ayora_db;

-- Index unique sur le nom pour permettre un vrai upsert
ALTER TABLE vendors ADD UNIQUE KEY ux_vendors_name (name);

-- Helpers : fetch category id par nom (les noms doivent exister via le seed)
SET @cat_neggafa     = (SELECT id FROM vendor_categories WHERE name='NEGGAFA');
SET @cat_makeup      = (SELECT id FROM vendor_categories WHERE name='MAKEUP');
SET @cat_coiffure    = (SELECT id FROM vendor_categories WHERE name='COIFFURE');
SET @cat_photo       = (SELECT id FROM vendor_categories WHERE name='PHOTOGRAPHE');
SET @cat_video       = (SELECT id FROM vendor_categories WHERE name IN ('VIDEASTE','VIDÉASTE') ORDER BY id LIMIT 1);
SET @cat_cake        = (SELECT id FROM vendor_categories WHERE name='CAKE_DESIGNER');
SET @cat_issawa     = (SELECT id FROM vendor_categories WHERE name='ISSAWA');
SET @cat_orchestre  = (SELECT id FROM vendor_categories WHERE name='ORCHESTRE');
SET @cat_decoration = (SELECT id FROM vendor_categories WHERE name='DECORATION');
SET @cat_fleuriste  = (SELECT id FROM vendor_categories WHERE name='FLEURISTE');
SET @cat_salle      = (SELECT id FROM vendor_categories WHERE name='SALLE');
SET @cat_traiteur   = (SELECT id FROM vendor_categories WHERE name='TRAITEUR');
SET @cat_planner    = (SELECT id FROM vendor_categories WHERE name='WEDDING_PLANNER');

-- ============================================
-- TRAITEURS (sources verifiees)
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Doreve Traiteur — Karima Riffi', 'Fes',
  'Societe Doreve Event Fes — Your Events are our Job. Service traiteur complet, mariages et evenements prestige a Fes.',
  280.00, 700.00, 'PREMIUM', '0657073073', '@doreve.traiteur', 'Ville Nouvelle, 2e etage cafe Cafemaroccinofes, Fes',
  'prestige,evenements,mariage,fassi,doreve,ville-nouvelle', 4.85, 165)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram),
  address=VALUES(address), prix_min=VALUES(prix_min), prix_max=VALUES(prix_max);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Fes''tin Traiteur', 'Fes',
  'FES''TIN — offre de traiteur complete, originale et raffinee. Au plaisir de vous servir partout au Maroc.',
  300.00, 700.00, 'PREMIUM', '0661420546', '@festin_traiteur', 'Fes',
  'raffine,complet,partout-maroc,festin,prestige', 4.90, 234)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), prix_min=VALUES(prix_min), prix_max=VALUES(prix_max);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Traiteur El Kortbi', 'Fes',
  '25 ans d''expertise au service du mariage fassi. Cuisine marocaine traditionnelle pour grandes receptions.',
  200.00, 500.00, 'MOYEN', '0535550628', '@traiteurelkortbi', 'Ville Nouvelle, Fes',
  'experience,traditionnel,grandes-receptions,fassi', 4.75, 178)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Sekkate Traiteur', 'Fes',
  'Mariages, fiancailles, baptemes, evenements prives et professionnels — service polyvalent reconnu.',
  220.00, 500.00, 'MOYEN', '0661609217', '@sekkate_traiteur', 'Fes',
  'polyvalent,fiancailles,bapteme,evenements,sekkate', 4.78, 156)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), prix_min=VALUES(prix_min), prix_max=VALUES(prix_max);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Traiteur El Amane', 'Fes',
  'Service traiteur situe a la Ville Nouvelle, a cote de la clinique Razi. Cuisine marocaine et internationale.',
  200.00, 480.00, 'MOYEN', '0661491997', '@elamanetraiteur', '14 lot Amal Marocain n.4, Ville Nouvelle, Fes',
  'razi,ville-nouvelle,marocain,international', 4.70, 134)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Sahel Traiteur Events', 'Fes',
  'Hadafouna ho itmam zifafikom bi ahsan hala — service traiteur evenementiel premium a Fes.',
  250.00, 550.00, 'PREMIUM', '0661704881', '@traiteur.sahel.events', '18 lotissement Riad al Yassamine, route Ain Chkaf, Fes',
  'premium,evenementiel,sahel,ain-chkaf,raffine', 4.80, 145)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_traiteur, 'Traiteur Al Jawda', 'Fes',
  'Organisateur d''evenements partout au Maroc. Bureau a Fes Hay Azhar, Avenue Ibn Khatib.',
  220.00, 520.00, 'MOYEN', '0535197683', '@traiteur_aljawda_anass', 'Avenue Ibn Khatib, Hay Azhar, Fes',
  'partout-maroc,al-jawda,hay-azhar,evenements', 4.75, 132)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

-- ============================================
-- SALLES DE FETE
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_salle, 'Salle de fete El Ouazzani', 'Fes',
  'Salle de fete reconnue a Fes. Capacite confortable, bien situee Avenue Panama.',
  18000.00, 45000.00, 'MOYEN', '0666075536', '@salle_de_fete_el_ouazzani', 'Avenue Panama, Route Imouzzar n.2, Fes',
  'panama,imouzzar,fassi,reception', 4.72, 145)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_salle, 'Salle des Fetes Dar Hajji', 'Fes',
  'Bienvenue chez Salle des fetes Dar Hajji — espace agreable pour vos fetes : mariage, anniversaire et autres evenements.',
  15000.00, 38000.00, 'MOYEN', '0661654212', '@darhajji_officiel', 'Fes',
  'agreable,famille,mariage,anniversaire,dar-hajji', 4.70, 128)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_salle, 'Palais Alyakout', 'Fes',
  'Services evenementiels prestige a Fes : Salle des fetes, Garden Wedding, piscine et lieu ideal pour vos evenements.',
  35000.00, 120000.00, 'PREMIUM', '02165636763', '@palaisalyakout', 'Route Aeroport Fes Saiss, Fes',
  'palais,piscine,garden-wedding,prestige,saiss,aeroport', 4.92, 189)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address), prix_min=VALUES(prix_min), prix_max=VALUES(prix_max);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_salle, 'Palais Benjelloun', 'Fes',
  'Le lieu ideal pour vos evenements a la hauteur de vos envies. Palais de prestige fassi.',
  40000.00, 130000.00, 'PREMIUM', '0659970157', '@palaisbenjelloun', 'Fes',
  'palais,prestige,benjelloun,fassi,reception-luxe', 4.88, 174)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- ============================================
-- ORCHESTRES / ARTISTES
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Orchestre Marouane Lebbar', 'Fes',
  'Artiste reconnu — votre satisfaction est la cle de voute de notre succes. Adaptabilite, exigence et diversite sont notre devise.',
  14000.00, 38000.00, 'PREMIUM', NULL, '@marouane_lebbar', 'Fes (deplacement Maroc entier)',
  'marouane-lebbar,artiste,exigence,diversite,prestige', 4.92, 287)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), instagram=VALUES(instagram), address=VALUES(address), prix_min=VALUES(prix_min), prix_max=VALUES(prix_max);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Marouane Hajji', 'Fes',
  'Artiste accompli — repertoire varie de musique marocaine. Account official avec presence televisee et concerts internationaux.',
  20000.00, 55000.00, 'PREMIUM', NULL, '@marouanehajjiofficial', 'Fes',
  'official,television,concerts,marocain,marouane-hajji,prestige', 4.95, 312)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Younes Rbati', 'Rabat',
  'Artiste chanteur — repertoire chaabi et oriental, presence scenique remarquee.',
  8000.00, 22000.00, 'MOYEN', NULL, '@younes_rbati', 'Rabat (deplacement Maroc)',
  'chaabi,oriental,scene,younes-rbati,rabat', 4.78, 156)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Youssef Wahbi', 'Fes',
  'Artiste chanteur populaire — animations de mariages et soirees festives. Repertoire eclectique.',
  9000.00, 25000.00, 'MOYEN', '0661383279', '@youssef.wahbi', 'Fes',
  'chanteur,festif,polyvalent,youssef-wahbi,populaire', 4.82, 198)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- ============================================
-- ISSAWA / MUSIQUE TRADITIONNELLE
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_issawa, 'Issawa Zakaria Faida', 'Casablanca',
  'Tradition marocaine et luxe moderne — Bola Bola, Dakka Marrakchia. La quintessence de l''art folklore casablancais.',
  4500.00, 12000.00, 'PREMIUM', '0212610149434', '@issawa_zakaria_faida', 'Casablanca (deplacement Maroc entier)',
  'tradition,bola-bola,dakka-marrakchia,casablanca,folklore-luxe', 4.88, 198)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_issawa, 'Issawa Salim — Ahmed Salim', 'Fes',
  'Issawa Salim — Bola Bola de luxe pour vos evenements. Repertoire issawa traditionnel haut de gamme.',
  3500.00, 9000.00, 'MOYEN', '0661996543', '@issawa_salim', 'Fes',
  'issawa,bola-bola,traditionnel,ahmed-salim,fassi', 4.80, 167)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- ============================================
-- NEGGAFA
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_neggafa, 'Negafa Dar Haja Zakia', 'Fes',
  'Negafa traditionnelle authentique. Heritage du savoir-faire fassi pour des mariages d''exception. Plus de 30 ans d''experience.',
  9000.00, 22000.00, 'MOYEN', '0677094129', '@negafa_dar_haja_zakia', 'Fes',
  'authentique,heritage,fassi,traditionnel,30-ans-experience', 4.85, 241)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_neggafa, 'Negafa Dar Benjelloun — Wedding Planner', 'Fes',
  'Wedding planner & negafa de prestige. Maison Benjelloun, reference historique du mariage fassi.',
  22000.00, 65000.00, 'PREMIUM', '0212661736902', '@negafa_dar_benjelloun', 'Derb Benjelloun, Fes Medina',
  'wedding-planner,prestige,benjelloun,medina,heritage,exclusif', 4.95, 220)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_neggafa, 'Negafa Majda Benjelloun — Tenguit de Luxe', 'Fes',
  'Service d''organisation de mariages partout au Maroc. Prestige fassi avec collection Tenguit de Luxe exclusive. Antenne Casablanca.',
  18000.00, 60000.00, 'PREMIUM', '0660182649', '@negafa.majda.benjelloun', 'Fes & Casablanca',
  'tenguit-luxe,partout-maroc,casablanca,prestige,majda-benjelloun', 4.92, 187)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

-- ============================================
-- PHOTOGRAPHES
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_photo, 'Photo Tazi', 'Rabat',
  'Photographe & cineaste mariage reconnu — Rabat-Sale, Maroc. Studio LaboPhotoTazi, prestation prestige.',
  6000.00, 18000.00, 'PREMIUM', '0212661113534', '@phototazi', 'Rabat / Sale, Maroc',
  'photographe,cineaste,prestige,rabat,sale,studio,labophototazi', 4.92, 234)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_photo, 'Prestige Camera — Hamza', 'Fes',
  'Photographie & videographie mariages, evenements, produits. Disponible partout au Maroc. Reservations rapides via WhatsApp.',
  5500.00, 16000.00, 'PREMIUM', '0663807685', '@prestige.camera', 'Fes Hay Bader (deplacement Maroc entier)',
  'photo-video,mariage,evenements,prestige-camera,hamza,partout-maroc', 4.88, 198)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_photo, 'Khalid El Achouri Photographe', 'Fes',
  'Createur de souvenirs — base a Fes. Specialiste photo de mariage, henna et evenements de prestige.',
  4500.00, 13000.00, 'PREMIUM', '0675897941', '@elachouri_photographe_of', 'Fes',
  'createur-souvenirs,henna,mariage,prestige,khalid-achouri,fassi', 4.90, 175)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- ============================================
-- COIFFURE / MAKEUP
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_coiffure, 'KK Kouki Hairstyle', 'Fes',
  'Pro hairstylist — specialiste invitee, fiancaille, mariee. Deplacement partout au Maroc.',
  1500.00, 4200.00, 'PREMIUM', NULL, '@hairstyle_bykouki', 'Fes (deplacement Maroc)',
  'pro-hairstylist,fiancaille,mariee,deplacement-maroc,kouki', 4.85, 178)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_makeup, 'Make Me Fab', 'Maroc / International',
  'Beauty Expert — Bride Specialist & Shooting Planer. Exquisite glow, timeless elegance. Worldwide.',
  3500.00, 9500.00, 'PREMIUM', NULL, '@makemefab.maroc', 'Maroc / Worldwide',
  'beauty-expert,bride-specialist,timeless-elegance,worldwide,make-me-fab', 4.95, 245)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_makeup, 'Salamoun Makeup Artist — Salma Hammioui', 'Fes',
  'International MUA certifiedby ESRAA Makeup Artist. Diplomes internationaux, mariee & formation. Founder de @bridebysalamoun.',
  3000.00, 8500.00, 'PREMIUM', '0653555244', '@salamounmakeup', 'Fes',
  'international,certified,bride,formation,salamoun,salma-hammioui', 4.92, 220)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_makeup, 'Yassine Makeup & Hair', 'Fes',
  'Professional Makeup Artist & Coiffeur de mariee. Deplacement a domicile dans toutes les villes marocaines.',
  2200.00, 6500.00, 'MOYEN', '0618231756', '@makeup_and_hair_by_yas', 'Fes / Casablanca (deplacement)',
  'professional,coiffeur,mariee,domicile,deplacement,yassine', 4.80, 167)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

-- ============================================
-- CAKE DESIGNERS / PATISSERIE
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_cake, 'Emeraude by Malika Alaoui', 'Fes',
  'Chocolatier — Patissier. Reception, Mariage, Tyafer, Bapteme, Robes. Specialiste haut de gamme.',
  2800.00, 8500.00, 'PREMIUM', '0661520516', '@emraude_by_malika', 'Fes',
  'chocolatier,patissier,tyafer,bapteme,robes,emeraude,malika-alaoui', 4.90, 154)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_cake, 'Chocolat de Joie', 'Fes',
  'Buffets, Tyaffer, fiancailles. Specialiste tres pret a Fes — service raffine et personnalise.',
  1500.00, 5500.00, 'MOYEN', '0661978478', '@chocolat.de.joie', 'Fes',
  'buffet,tyaffer,fiancailles,raffine,personnalise,chocolat-joie', 4.78, 134)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_cake, 'Cake Ahlame — Ahlam Zayyoun', 'Fes',
  'Gateaux sur commande. Livraison sur Fes et environs. Livraison cake mariage partout au Maroc. Virement ou especes acceptes.',
  900.00, 3500.00, 'MOYEN', '0665221142', '@cake_ahlame', 'Fes Hay Tarik 1',
  'sur-commande,livraison,maroc,wedding-cake,ahlam-zayyoun,hay-tarik', 4.82, 142)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

-- ============================================
-- WEDDING PLANNER
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_planner, 'Mounia Tounsi — Wedding & Event Planning', 'Rabat',
  'Mounia Ramsis — Wedding and event planning. Service complet : Rabat, Sale, Temara, Kenitra et environs.',
  9500.00, 32000.00, 'PREMIUM', '0212661818182', '@mounia_ramsis', 'Rabat — Sale — Temara — Kenitra',
  'wedding-planner,event-planning,mounia-ramsis,rabat,sale,kenitra,temara', 4.92, 156)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), address=VALUES(address);

-- ============================================
-- FLEURISTE
-- ============================================

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_fleuriste, 'Fleurs Edikki Fes', 'Fes',
  'Fleuriste mariage et evenements — compositions florales raffinees. Bouquets, arches, centres de table.',
  2500.00, 9500.00, 'PREMIUM', '0661476078', '@fleursedikki_fes', 'Fes',
  'compositions,bouquet,arche,centres-table,raffine,edikki', 4.85, 142)
ON DUPLICATE KEY UPDATE
  description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Verification finale
SELECT 'Migration vendors_real OK' AS status,
       (SELECT COUNT(*) FROM vendors) AS nb_vendors,
       (SELECT COUNT(*) FROM vendor_categories) AS nb_categories;
