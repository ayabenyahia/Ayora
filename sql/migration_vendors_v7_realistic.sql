-- ============================================================
-- Migration v7 — Prestataires reels + prix realistes 2026
-- ============================================================
-- 1) Recalibre les prix irrealistes sur la base du marche fassi 2026
--    (Palais Benjelloun = top reference 35K, riads 8-12K, etc.)
--
-- A executer dans phpMyAdmin sur la base ayora_db.
-- ============================================================

USE ayora_db;

-- ============================================================
-- A. RECALIBRAGE DES PRIX EXISTANTS (realisme marche Fes 2026)
-- ============================================================
-- Palais Alyakout : etait 35-80K (irrealiste). Marche reel : 18-35K (palais
-- moyen-haut, salle bien decoree mais pas le top).
UPDATE vendors SET prix_min=18000, prix_max=35000,
  description='Palais d''evenements traditionnel a Fes. Salle decoree avec elements fassi (zellige, lustres), capacite moyenne a grande. Bon rapport qualite-prix dans la categorie palais sans atteindre le tarif des references comme Benjelloun ou Faraj.'
  WHERE name='Palais Alyakout';

-- Salles standards : etaient 18-40K (un peu sur-cote)
UPDATE vendors SET prix_min=8000, prix_max=18000,
  description='Salle de fete familiale a Fes. Capacite moyenne (jusqu''a 250 invites), prestation simple et chaleureuse. Ideale pour mariages traditionnels avec budget maitrise. Possibilite traiteur partenaire.'
  WHERE name='Salle de fete El Ouazzani';

UPDATE vendors SET prix_min=8000, prix_max=18000,
  description='Salle des Fetes Dar Hajji — espace evenementiel polyvalent a Fes. Salon traditionnel avec tapis et banquettes, scene pour orchestre. Tarifs accessibles pour les mariages a budget moyen.'
  WHERE name='Salle des Fetes Dar Hajji';

-- Issawa Berrada : etait 35-75K, deraisonnable. Reference issawa premium :
-- 10-25K maximum (les groupes superstars comme Marouane Hajji sont en CAT
-- ORCHESTRE, pas issawa).
UPDATE vendors SET prix_min=10000, prix_max=25000,
  description='Groupe issawa renomme dirige par Haj Said Berrada. Specialise dans l''entree de la mariee (zaffa) et les ceremonies traditionnelles fassia. Prestation premium avec tenues d''epoque, percussions et chants liturgiques.'
  WHERE name='Haj Said Berrada';

-- Orchestre Marouane Lebbar : prix initial 25-60K trop eleve pour orchestre standard
UPDATE vendors SET prix_min=18000, prix_max=40000,
  description='Orchestre marocain professionnel base a Fes. Repertoire chaabi, andalou et mariage. Formation 6 a 10 musiciens, chanteur principal et chanteuses choeurs. Soiree complete (4-6h).'
  WHERE name='Orchestre Marouane Lebbar';

-- Neggafa Dar Benjelloun (cat=1) : 25-90K etait absurde
UPDATE vendors SET prix_min=18000, prix_max=45000,
  description='Maison Negafa Dar Benjelloun a Casablanca. Wedding Planner complet : tenues mariee (5-7 lebssa), coordination, scenographie. Reference reconnue dans le mariage haut-de-gamme casaoui.'
  WHERE name='Negafa Dar Benjelloun - Wedding Planner';

UPDATE vendors SET prix_min=15000, prix_max=45000,
  description='Negafa Majda Benjelloun — Tenguif de Luxe. Atelier neggafa specialise tenues fassi traditionnelles : caftan, takchita, lebssa fassia ancestrale. Showroom a Fes avec collection exclusive.'
  WHERE name='Negafa Majda Benjelloun - Tenguif de Luxe';

-- Riad Decor Fes (cat=9 decoration, ce n''est PAS un riad mais un decorateur)
UPDATE vendors SET prix_min=6000, prix_max=18000,
  description='Riad Decor Fes — atelier decoration mariage. Specialiste compositions florales, scenographies fassi traditionnelles (zellige, tapis, pouf), ambiances royales et orientales.'
  WHERE name='Riad Decor Fes';

-- ============================================================
-- B. AJOUT DES PRESTATAIRES REELS (sources : Instagram + Top Jour)
-- ============================================================
-- Idempotent : INSERT...SELECT WHERE NOT EXISTS evite les doublons en
-- relancant la migration.
-- ============================================================

-- ----- B.1 SALLES DE FETE (cat=11) — 6 nouveaux ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Salle The Queen', 'Fes',
  'Salle polyvalente moderne situee dans le complexe touristique La Perla, Km 10 Route de Meknes a Fes. Decoration contemporaine raffinee, capacite moyenne a grande (200-400 invites), scenographie premium incluse. Espace lumineux ideal pour mariages chics et modernes. Disponibilite traiteur partenaire et parking securise.',
  12000, 25000, 'PREMIUM', '0535000000', '@salle_polyvalente_the.queen',
  'Complexe La Perla, Km 10, Route de Meknes, Fes',
  'salle,moderne,grand,polyvalent,complet,scenographie,tendance,premium,fes,parking,lumineux,chic',
  4.6, 143, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Salle The Queen');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Palais D''or Fes', 'Fes',
  'Palais d''evenements luxe situe a Ain Chqef, Fes. "Luxury Weddings & Events" : scenographie tres riche, mises en lumiere premium, allee VIP, palanquin amariya integre. Parfait pour mariages royaux et grandes ceremonies (250-500 invites). Cafe palais disponible pour les invites.',
  22000, 40000, 'PREMIUM', '0663572605', '@palaisdorfes',
  'Palais d''or, Ain Chqef, Fes',
  'palais,royal,luxe,premium,grand,prestige,or,scenographie,fes,haut-gamme,amariya,vip',
  4.7, 68, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Palais D''or Fes');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Riad Arabesque', 'Fes',
  'Riad de charme situe au coeur de la medina ancienne de Fes. Architecture traditionnelle fassie authentique : zellige multicolore, plafonds en bois sculpte, patio central avec fontaine. Capacite intime (60-120 invites), ambiance heritage andalous incomparable. Possibilite restauration sur place.',
  9000, 16000, 'PREMIUM', '0703191113', '@riadarabesque',
  'Medina ancienne, Fes',
  'riad,medina,fassi,authentique,intime,heritage,zellige,oriental,charme,traditionnel,andalous,patio',
  4.8, 91, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Riad Arabesque');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Riad Salam Fes', 'Fes',
  'Complexe hotelier de tradition fassie. Espace evenementiel polyvalent : grande salle interieure + patio andalous + chambres pour les invites internationaux. Site officiel : riadsalamfes.com. Ideal pour mariages avec famille elargie et cousins venus de l''etranger (logement sur place possible).',
  10000, 22000, 'PREMIUM', '0535000000', '@riad_salam_fes',
  'Fes',
  'riad,complexe,hotel,fassi,authentique,heritage,grand,polyvalent,traditionnel,fes,logement,internationaux',
  4.5, 39, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Riad Salam Fes');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Palais Laraki', 'Meknes',
  'Palais Laraki — institution evenementielle a Meknes, region Fes-Meknes. Salle prestigieuse pour mariages traditionnels et ceremonies marocaines avec option traiteur integre. Equipe coordination experimentee (37K abonnes IG, +1200 publications mariage). Tres demande dans la region pour les mariages dits "royaux".',
  22000, 45000, 'PREMIUM', '0661807893', '@palais_laraki',
  'Meknes',
  'palais,royal,prestige,grand,traiteur,traditionnel,fassi,heritage,meknes,coordination,renomme',
  4.7, 200, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Palais Laraki');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Salle Billionaire Fes', 'Fes',
  'Event & Conference — Salle des fetes Billionaire a Fes. Espace moderne et spacieux avec piscine exterieure (idee originale pour photos de couple), capacite tres grande (300-500 invites). Parfaite pour mariages nombreux et meetings/conferences. Route Immouzer avant Golf Royal.',
  15000, 30000, 'MOYEN', '0661060039', '@salle_des_fete_billionaire',
  'Route Immouzer avant Golf Royal, Medina, Fes',
  'salle,moderne,grand,spacieux,piscine,polyvalent,complet,fes,exterieur,photos',
  4.5, 100, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Salle Billionaire Fes');
