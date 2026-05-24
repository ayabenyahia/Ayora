-- ============================================================
-- Migration v9 — Nouveau lot de prestataires reels (5)
-- ============================================================
-- Source : Instagram + photos fournies par la mariee.
-- Idempotent : chaque INSERT...SELECT est garde par WHERE NOT EXISTS
-- pour pouvoir relancer la migration sans creer de doublons.
--
-- Liste ajoutee :
--   - Nisrine Benkirane             (cake designer, cat=6)
--   - Bloom by Amal                 (cake designer, cat=6)
--   - Camera Youness                (photographe,   cat=4)
--   - Noujoum Fes Bola Bola         (issawa,        cat=7)
--   - Adnane Nafie                  (issawa,        cat=7)
--
-- Photos / galerie / reel : volontairement laissees NULL — les fichiers
-- images correspondants devront etre ajoutes ulterieurement dans
-- WebContent/images/vendors/<slug>.png puis renseignes via UPDATE
-- (cf. migration_vendors_photos_batch1.sql pour le pattern).
-- ============================================================

USE ayora_db;

-- ----- 1. Nisrine Benkirane (cake designer, cat=6) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 6, 'Nisrine Benkirane', 'Fes',
  'Cake designer fassia specialisee dans les wedding cakes haute couture. Creations personnalisees avec fleurs en sucre, dorures, etages sculptes et thematique sur mesure. Atelier dans le quartier Atlas, livraison sur Fes et alentours.',
  2500, 8000, 'PREMIUM', '0665410197', '@nisrinebenkirane_',
  'Avenue Mohamed Zerktouni, Atlas, Fes',
  'cake-design,wedding-cake,personnalise,luxe,fassi,artisanal,fes,haut-gamme,fleurs-sucre,sur-mesure',
  4.85, 187, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Nisrine Benkirane');

-- ----- 2. Bloom by Amal (cake designer + event design, cat=6) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 6, 'Bloom by Amal', 'Fes',
  'Cake designer et event design a Fes. Gateaux d''evenements (mariage, bapteme, fiancailles, anniversaire), thematique princesse et univers chic. Service complet : cake + sweet table + ballons + decoration de table. Bon rapport qualite-prix.',
  1200, 4500, 'MOYEN', '0661255210', '@bloomette.maroc',
  'Fes',
  'cake-design,sweet-table,bapteme,mariage,fiancailles,anniversaire,theme,decoration,fes,event-design',
  4.70, 142, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Bloom by Amal');

-- ----- 3. Camera Youness (photographe mariage, cat=4) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 4, 'Camera Youness', 'Fes',
  'Photographe de mariage professionnel base a Fes. Couverture complete de la journee : preparation de la mariee, ceremonies (henna, dvac, soiree), portraits couple, photos de famille. Livraison photos retouchees haute resolution.',
  2500, 7000, 'MOYEN', '0661113691', '@camera_youness',
  'Fes',
  'photographe,mariage,reportage,fes,professionnel,portrait,famille,couple,haute-resolution,couverture-complete',
  4.70, 134, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Camera Youness');

-- ----- 4. Noujoum Fes Bola Bola (issawa, cat=7) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 7, 'Noujoum Fes Bola Bola', 'Fes',
  'Groupe Issawa Bola Bola base a Fes avec deplacement partout au Maroc. Repertoire traditionnel fassi et folklore marocain : zaffa, entree mariee, ceremonies religieuses et soirees. Formation experimentee avec percussions, chants liturgiques et tenues d''epoque.',
  8000, 20000, 'PREMIUM', '0663195025', '@noujoum_fes.bola_bola',
  'Fes (deplacement partout au Maroc)',
  'issawa,bola-bola,traditionnel,fassi,fes,deplacement,maroc,zaffa,ceremonie,folklore,authentique,percussions',
  4.75, 156, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Noujoum Fes Bola Bola');

-- ----- 5. Adnane Nafie (issawa, cat=7) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 7, 'Adnane Nafie', 'Fes',
  'Groupe Issawa Adnane Nafie base a Fes. Repertoire authentique pour ceremonies traditionnelles fassia : entree de la mariee, henna, soiree de mariage. Animation energique et respect des codes du folklore marocain.',
  6000, 15000, 'MOYEN', '0661609964', '@adnane_nafie_officiel',
  'Fes',
  'issawa,traditionnel,fassi,ceremonie,fes,authentique,henna,zaffa,folklore,mariage,energique',
  4.78, 128, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Adnane Nafie');

-- ============================================================
-- Verification : doit retourner 5 lignes apres la premiere execution
-- ============================================================
-- SELECT id, category_id, name, gamme, prix_min, prix_max, instagram
-- FROM vendors
-- WHERE name IN ('Nisrine Benkirane','Bloom by Amal','Camera Youness',
--                'Noujoum Fes Bola Bola','Adnane Nafie')
-- ORDER BY category_id, name;
