-- ============================================================
-- Migration v10 — Nouveau lot de prestataires reels (4)
-- ============================================================
-- Source : Instagram + photos fournies par la mariee.
-- Idempotent : chaque INSERT...SELECT est garde par WHERE NOT EXISTS
-- pour pouvoir relancer la migration sans creer de doublons.
--
-- Liste ajoutee :
--   - Faly Events                   (myadi/tyafer, cat=13)
--   - Abdellah El Yaakoubi          (issawa,       cat=7)
--   - Dar Ba Sidi                   (salle,        cat=11)
--   - Moktaka Alossar               (salle,        cat=11)
--
-- Photos / galerie / reel : volontairement laissees NULL — les fichiers
-- images correspondants devront etre ajoutes ulterieurement dans
-- WebContent/images/vendors/<slug>.png puis renseignes via UPDATE
-- (cf. migration_vendors_photos_batch1.sql pour le pattern).
-- ============================================================

USE ayora_db;

-- ----- 1. Faly Events (myadi / tyafer, cat=13) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 13, 'Faly Events', 'Fes',
  'Faly Events — creations prestigieuses pour tyafer, henna, mariage et grandes occasions. Compositions luxueuses de dragees, chocolats fins, dattes garnies et fleurs fraiches. Collections personnalisees pour mariage et henna day. Presentation tres soignee, qualite haut-de-gamme.',
  3500, 12000, 'PREMIUM', '0641516348', '@falyfes_events',
  'Fes',
  'tyafer,myadi,dragee,chocolat,dattes,fleurs,henna,mariage,fassi,luxe,premium,fes,personnalise,grandes-occasions',
  4.8, 215, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Faly Events');

-- ----- 2. Abdellah El Yaakoubi (issawa, cat=7) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 7, 'Abdellah El Yaakoubi', 'Fes',
  'Groupe Issawa dirige par Abdellah El Yaakoubi (Chrif). Repertoire authentique fassi pour entree de la mariee, henna et ceremonies traditionnelles. Formation experimentee, voix puissante, tenues d''epoque et percussions. Animation premium pour mariages haut de gamme a Fes et alentours.',
  9000, 22000, 'PREMIUM', '0661722005', '@yaakoubichrif',
  'Fes',
  'issawa,traditionnel,fassi,fes,authentique,henna,zaffa,ceremonie,premium,mariage,chants,percussions',
  4.85, 198, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Abdellah El Yaakoubi');

-- ----- 3. Dar Ba Sidi (salle de fete, cat=11) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Dar Ba Sidi', 'Fes',
  'Dar Ba Sidi — salle des fetes a Fes. Espace chaleureux et raffine, parfaitement pense pour mariages, anniversaires, soirees privees et evenements professionnels. Decoration traditionnelle fassia avec touches modernes : lustres en cristal, scenographies elegantes, allee VIP, ambiance "Fierte marocaine". Capacite moyenne a grande.',
  12000, 28000, 'PREMIUM', '0660088995', '@dar_ba_sidi',
  'Fes',
  'salle,palais,fassi,traditionnel,moderne,fes,scenographie,lustres,premium,polyvalent,allee-vip,grand,fierte-marocaine',
  4.7, 167, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Dar Ba Sidi');

-- ----- 4. Moktaka Alossar (salle de fete, cat=11) ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Moktaka Alossar', 'Fes',
  'Moktaka Alossar — salle des fetes a Fes. Cadre elegant pour mariages traditionnels fassis et evenements familiaux. Decoration soignee, scene pour orchestre / issawa, capacite confortable, possibilite traiteur partenaire. Bon rapport qualite-prix sur Fes.',
  10000, 22000, 'MOYEN', '0668199758', '@moltalalossar',
  'Fes',
  'salle,fassi,traditionnel,moderne,fes,polyvalent,mariage,traiteur,scene,orchestre,issawa',
  4.6, 124, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Moktaka Alossar');

-- ============================================================
-- Verification : doit retourner 4 lignes apres la premiere execution
-- ============================================================
-- SELECT id, category_id, name, gamme, prix_min, prix_max, instagram
-- FROM vendors
-- WHERE name IN ('Faly Events','Abdellah El Yaakoubi','Dar Ba Sidi','Moktaka Alossar')
-- ORDER BY category_id, name;
