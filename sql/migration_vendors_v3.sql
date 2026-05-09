-- ============================================
-- AYORA - Migration v3
-- Corrections + nouveaux prestataires (lot 2)
-- ============================================

USE ayora_db;

-- ============================================
-- 1. NETTOYAGE ENCODAGE (em-dashes residuels)
-- ============================================

-- Remplacer tout ce qui n'est pas ASCII propre dans les noms par '-'
UPDATE vendors SET name = REPLACE(name, CHAR(0xC3, 0x94, 0xC3, 0xA7, 0x4F USING utf8mb4), '-') WHERE name LIKE '%ÔçO%';

-- Fallback : remplacer les unicodes em/en dash et leurs derives encoding
UPDATE vendors SET name = REPLACE(name, UNHEX('E28094'), '-');
UPDATE vendors SET name = REPLACE(name, UNHEX('E28093'), '-');
UPDATE vendors SET name = REPLACE(name, UNHEX('C396C3A7C3A7'), '-');
UPDATE vendors SET name = REPLACE(name, UNHEX('C394C3A74F'), '-');
UPDATE vendors SET name = REPLACE(name, UNHEX('C39FC3A7'), '-');

UPDATE vendors SET description = REPLACE(description, UNHEX('E28094'), '-');
UPDATE vendors SET description = REPLACE(description, UNHEX('E28093'), '-');
UPDATE vendors SET description = REPLACE(description, UNHEX('C396C3A7C3A7'), '-');

-- ============================================
-- 2. CORRECTIONS NOMS / VILLE / DESC
-- ============================================

-- Youssef Wahbi : artiste de Casablanca
UPDATE vendors
SET city = 'Casablanca',
    address = 'Casablanca (deplacement partout au Maroc)',
    description = 'Artiste chanteur populaire base a Casablanca. Animations de mariages et soirees festives partout au Maroc. Repertoire eclectique chaabi et oriental.'
WHERE name LIKE '%Youssef Wahbi%';

-- Mounia Tounsi -> Mounia Ramsis Tounsi (Rabat)
UPDATE vendors
SET name = 'Mounia Ramsis Tounsi - Wedding Planner',
    city = 'Rabat',
    address = 'Rabat (Sale, Temara, Kenitra et environs)',
    description = 'Mounia Ramsis Tounsi - Wedding & event planning premium. Service complet sur Rabat, Sale, Temara, Kenitra et environs.'
WHERE name LIKE '%Mounia%' OR name LIKE '%Ramsis%';

-- Troupe Issawa -> Groupe Issawa partout
UPDATE vendors SET name = REPLACE(name, 'Troupe Issawa', 'Groupe Issawa');
UPDATE vendors SET description = REPLACE(description, 'Troupe Issawa', 'Groupe Issawa');
UPDATE vendors SET description = REPLACE(description, 'Troupe ', 'Groupe ');

-- Fleurs Edikki -> Seddiki Fleur
UPDATE vendors
SET name = 'Seddiki Fleur',
    description = 'Seddiki Fleur - Fleuriste mariage et evenements a Fes. Compositions florales raffinees, bouquets, arches et centres de table. Deplacement partout au Maroc.',
    instagram = '@seddikifleurs',
    tags = 'seddiki,bouquet,arche,centres-table,raffine,fleuriste-fassi'
WHERE name LIKE '%Edikki%' OR name LIKE '%Seddiki%';

-- ============================================
-- 3. SALLES DE FETES : MAX 40 000 DHS
-- ============================================
-- Toutes les "Salles de fete" (categorie SALLE) plafonnees a 40 000 DHS max
-- (Les Palais restent un peu plus haut car ce sont des palais, pas des "salles de fete")

UPDATE vendors v
JOIN vendor_categories c ON v.category_id = c.id
SET v.prix_max = 40000.00, v.prix_min = LEAST(v.prix_min, 18000.00)
WHERE c.name = 'SALLE'
  AND (v.name LIKE '%Salle%' OR v.name LIKE '%El Andalous%' OR v.name LIKE '%Saada%' OR v.name LIKE '%Dar Hajji%' OR v.name LIKE '%El Ouazzani%');

-- Les palais : montants plus modestes aussi (plafond raisonnable)
UPDATE vendors SET prix_max = 80000.00, prix_min = 35000.00
WHERE name LIKE '%Palais Faraj%' OR name LIKE '%Palais Alyakout%' OR name LIKE '%Palais Benjelloun%';

UPDATE vendors SET prix_max = 50000.00, prix_min = 20000.00
WHERE name LIKE '%Maison Bleue%' OR name LIKE '%Riad%';

-- ============================================
-- 4. AJOUTER 'partout au Maroc' / 'deplacement' pour tous les prestataires
--    qui ne le mentionnent pas encore (les prestataires se deplacent)
-- ============================================

UPDATE vendors
SET description = CONCAT(description, ' Deplacement possible partout au Maroc.')
WHERE description NOT LIKE '%artout au Maroc%'
  AND description NOT LIKE '%eplacement%'
  AND description NOT LIKE '%orldwide%';

-- ============================================
-- 5. NOUVEAUX PRESTATAIRES (lot 2)
-- ============================================

SET @cat_cake = (SELECT id FROM vendor_categories WHERE name='CAKE_DESIGNER');
SET @cat_hennaya = (SELECT id FROM vendor_categories WHERE name='HENNAYA');
SET @cat_orchestre = (SELECT id FROM vendor_categories WHERE name='ORCHESTRE');
SET @cat_dj = (SELECT id FROM vendor_categories WHERE name='DJ');
SET @cat_issawa = (SELECT id FROM vendor_categories WHERE name='ISSAWA');
SET @cat_salle = (SELECT id FROM vendor_categories WHERE name='SALLE');

-- Cake : My Kiko Cake (Yassmine Guennoun)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_cake, 'My Kiko Cake - Yassmine Guennoun', 'Fes',
  'Chef cuisinier a domicile. Birthday cakes, Wedding cakes, cakes for all occasions. Deplacement partout au Maroc.',
  900.00, 4500.00, 'MOYEN', '0648238761', '@my_kiko_cookies', 'Fes',
  'wedding-cake,birthday,domicile,my-kiko,yassmine-guennoun', 4.85, 145)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Cake : The Cake House by Chaym (Cake Chama)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_cake, 'The Cake House by Chaym', 'Fes',
  'Cake designer - Homemade with love. Wedding cakes, fiancailles, Tyaffer. Deplacement partout au Maroc.',
  1200.00, 5500.00, 'MOYEN', '0615070330', '@thecakehousebychaym', 'Fes, Maroc',
  'cake-designer,homemade,wedding,fiancailles,chaym', 4.88, 178)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Henna : Hnaya Fati Fes
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_hennaya, 'Hnaya Fati Fes', 'Fes',
  'Specialiste henna pour mariees fassies - artiste reconnu. Deplacement partout au Maroc.',
  800.00, 2800.00, 'MOYEN', '0676130037', '@hnaya_fati_fes_officiel', 'Fes',
  'henna,mariee,fassi,fati,traditionnel', 4.85, 165)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Orchestre : Adil Otmani
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Adil Otmani - Artiste & Compositeur', 'Casablanca',
  'Artiste chanteur compositeur. Reservations TikTok @adilotmani7 et Facebook Adil Otmani. Deplacement partout au Maroc.',
  18000.00, 45000.00, 'PREMIUM', '0212631374578', '@adil_otmani', 'Casablanca (deplacement Maroc)',
  'artiste,chanteur,compositeur,adil-otmani,prestige', 4.85, 198)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Orchestre : Haj Said Berrada (Casa, Tanger sold out)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Haj Said Berrada', 'Casablanca',
  'Artiste reference - concerts sold out a Casablanca, Tanger, Zerhoun, Al Jadida. Repertoire chaabi marocain. Deplacement partout au Maroc.',
  35000.00, 75000.00, 'PREMIUM', NULL, '@hajsaidberrada', 'Casablanca (deplacement Maroc)',
  'sold-out,reference,chaabi,casablanca,tanger,berrada', 4.92, 245)
ON DUPLICATE KEY UPDATE description=VALUES(description), instagram=VALUES(instagram);

-- Orchestre : Omar Hanoun (Fes)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Omar Hanoun', 'Fes',
  'Chanteur fassi reconnu. Reservations directes. Repertoire varie pour mariages et soirees. Deplacement partout au Maroc.',
  9000.00, 25000.00, 'MOYEN', '0666059905', '@omar_hanoun.artiste', 'Fes',
  'chanteur,fassi,omar-hanoun,reservation-directe,polyvalent', 4.80, 178)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Orchestre : Mehdi Artiste (Chef orchestre, chanteur)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_orchestre, 'Mehdi Artiste Orchestre', 'Fes',
  'Chef orchestre et chanteur professionnel. Repertoire varie : chaabi, oriental, festif. Deplacement partout au Maroc.',
  12000.00, 35000.00, 'PREMIUM', '0212661364068', '@artiste.mehdi', 'Maroc - Fes',
  'chef-orchestre,chanteur,mehdi,polyvalent,prestige', 4.85, 167)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- DJ : Kacem (Kacem Mghabbar)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_dj, 'DJ Kacem - Kacem Mghabbar', 'Fes',
  'Chanteur et DJ professionnel pour tout genre evenements. Animations mariages, soirees prive. Deplacement partout au Maroc.',
  4500.00, 12000.00, 'PREMIUM', '0661693051', '@dj__kacem', 'Fes',
  'dj,chanteur,polyvalent,kacem,evenements,prestige', 4.82, 156)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- DJ : Mehdi (DJ Mehdi Bouchal)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_dj, 'DJ Mehdi Bouchal', 'Fes',
  'DJ Professionnel specialise dans les Mariages et Soirees. Transforme vos vibes en experience unique. Deplacement partout au Maroc.',
  3500.00, 10000.00, 'MOYEN', NULL, '@dj_mehdi_b', 'Fes',
  'dj,mariage,soiree,mehdi-bouchal,vibes,prestige', 4.75, 132)
ON DUPLICATE KEY UPDATE description=VALUES(description), instagram=VALUES(instagram);

-- Issawa : Moustafa Meseyah
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_issawa, 'Issawa Moustafa Meseyah - Bola Bola de Luxe', 'Fes',
  'Issawa Bola Bola de Luxe - reservation pour mariages partout au Maroc. Repertoire issawa et folklore prestige.',
  6500.00, 14000.00, 'PREMIUM', '0212661361956', '@issawa_moustafa', 'Fes (deplacement Maroc)',
  'issawa,bola-bola,luxe,moustafa-meseyah,folklore,prestige', 4.88, 187)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram);

-- Salle : Le Pavillon Dor (Pavillon d'or)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis)
VALUES (@cat_salle, 'Le Pavillon Dor', 'Fes',
  'Weddings & Luxury Events - Salle des fetes et de spectacles, maison d hotes. Cadre prestigieux pour evenements haut de gamme.',
  18000.00, 40000.00, 'PREMIUM', '0212661365449', '@lepavillon_dor', 'Fes',
  'pavillon-or,luxury,salle,maison-hotes,prestige,fes', 4.88, 178)
ON DUPLICATE KEY UPDATE description=VALUES(description), phone=VALUES(phone), instagram=VALUES(instagram), prix_max=VALUES(prix_max);

-- Verification
SELECT 'Migration v3 OK' AS status, COUNT(*) AS total_vendors FROM vendors;
SELECT name, city, prix_min, prix_max FROM vendors WHERE name LIKE '%Wahbi%' OR name LIKE '%Ramsis%' OR name LIKE '%Seddiki%' OR name LIKE '%Groupe%' OR name LIKE '%Pavillon%';
