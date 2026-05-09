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

-- ----- B.2 MYADI / TYAFER (cat=13) — 5 nouveaux ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 13, 'La Dragee d''Or Fes', 'Fes',
  'Chocolaterie artisanale de reference a Fes — depuis 1997. Specialisee dragees, chocolats, mariages, fiancailles, baptemes. Ouvert tous les jours (09h30-19h30). Plus de 8K abonnes IG, 500+ publications. Site officiel : ladrageedor.com',
  1500, 5000, 'MOYEN', '0661437950', '@la_dragee_dor_fes',
  '8 Avenue Roi Hussein, Route d''Immouzer, Fes',
  'chocolat,dragee,traditionnel,fassi,artisanal,mariage,fiancailles,fes,authentique,reference',
  4.7, 500, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='La Dragee d''Or Fes');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 13, 'Le Cacaochi', 'Fes',
  'Chocolaterie + Myadi Buffets a Fes. Triple specialite : chocolat fin, dattes garnies premium et myadi/buffets pour mariages, baptemes, anniversaires. Hammam-mariage, buffet mariage, presentation tres soignee. Route Ain Chkef.',
  2000, 7000, 'MOYEN', '0535000000', '@le_cacaochi_chocolatier',
  'Route Ain Chkef, Fes',
  'chocolat,myadi,buffet,dattes,artisanal,fes,mariage,traditionnel,hammam,bapteme',
  4.6, 224, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Le Cacaochi');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 13, 'Jeff de Bruges Fes', 'Fes',
  'Boutique Jeff de Bruges Fes — chocolatier francais reference (32K abonnes IG, +1300 publications). Chocolats, fiancailles, mariages, baptemes, cadeaux entreprises. Marque internationale belge avec adaptation aux ceremonies marocaines (myadi modernes).',
  2500, 8000, 'PREMIUM', '0535606646', '@jeffdebrugesfes',
  'Fes',
  'chocolat,premium,international,marque,fiancailles,mariage,moderne,fes,marque-renommee,europeen',
  4.8, 1313, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Jeff de Bruges Fes');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 13, 'Filali Tyafer', 'Fes',
  'Maison de createur Filali — Tyafer d''exception, "L''art d''elegance". Reference luxe pour les tyafer fassia : cofreittes (cassettes precieuses), l''mida, vert-emeraude, blanc et or, vert royal. 15.5K abonnes IG, 2.6K publications. Travail artisanal sur mesure. Tres demande pour mariages haut-de-gamme.',
  8000, 25000, 'PREMIUM', '0600185698', '@filali_tyafer',
  'Fes, Maroc',
  'tyafer,createur,luxe,premium,fassi,heritage,prestige,exclusif,artisanal,fes,sur-mesure,or',
  4.9, 2681, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Filali Tyafer');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 13, 'Aziz Iyachi Tyafer', 'Casablanca',
  'Boutique de mariage Aziz Iyachi — Tyafer signature : "un dfou3 digne des mille et une nuits". 165K abonnes IG, reference luxe a l''echelle nationale. Boulevard Al Qods, Casablanca. Disponible partout au Maroc avec deplacement (Fes, Rabat, Marrakech).',
  10000, 30000, 'PREMIUM', '0661979016', '@tyafer_aziz_iyachi',
  'Bd Al Qods, Casablanca',
  'tyafer,luxe,prestige,fassi,heritage,exclusif,sur-mesure,mille-et-une-nuits,casa,deplacement,national,reference',
  4.8, 323, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Aziz Iyachi Tyafer');

-- ----- B.3 NEGGAFA (cat=1) — 1 nouvelle ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 1, 'Maison Mariee Soltana', 'Casablanca',
  'Maison Mariee Soltana — styliste neggafa de reference (237K abonnes IG, +700 publications). Specialiste tenues mariage : caftan, lebssa fassia, takchita, scenographie complete pour mariages traditionnels marocains. Casablanca-Maarif, ouvert lundi-samedi 10h-21h.',
  18000, 50000, 'PREMIUM', '0661088151', '@negafa.soltana',
  'Casablanca, Maarif, Maroc',
  'neggafa,styliste,fassi,traditionnel,royal,premium,heritage,authentique,casa,exclusif,collection,scenographie',
  4.8, 735, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Maison Mariee Soltana');

-- ----- B.4 MAQUILLAGE & COIFFURE (cat=2) — 4 nouveaux ----------
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 2, 'Glamsmakeup by Ghita', 'Fes',
  'Certified Makeup Artist (62K abonnes IG). Make-up artist + Hair pour la mariee, basee a Fes avec deplacement partout au Maroc (Casa, Rabat, Marrakech, Tanger). Specialiste maquillage caftan, mariee fassia traditionnelle, mises en beaute glow et tendance.',
  2000, 5000, 'PREMIUM', '0606440302', '@glam_smakeup_by_ghita',
  'Fes (deplacement partout au Maroc)',
  'maquillage,coiffure,bridal,certifie,deplacement,oriental,fassi,traditionnel,glow,premium,fes,national,tendance',
  4.8, 341, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Glamsmakeup by Ghita');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 2, 'Makeup by Ghita (GhB)', 'Fes',
  'Make-up artiste Ghita — coiffure, maquillage, esthetique (22.9K abonnes IG). Studio a Fes. Tarifs abordables, qualite professionnelle, deplacement local possible. Specialisee maquillage naturel et tendance pour mariees jeunes.',
  1500, 4000, 'MOYEN', '0668570987', '@ghita_beauty.1',
  'Fes',
  'maquillage,coiffure,esthetique,abordable,moyen,bridal,fes,naturel,tendance,jeune',
  4.5, 104, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Makeup by Ghita (GhB)');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 2, 'Beauty by Saoussane', 'Fes',
  'Beauty by Saoussane — Professional Makeup Artist & Hair (18.6K abonnes IG, +400 publications). Champs de Cours, Fes. Deplacement partout au Maroc. Specialiste mariees fassia traditionnelles : maquillage couleurs profondes, coiffure caftan + lebssa.',
  2000, 5000, 'PREMIUM', '0664193381', '@makeup_by_saoussane',
  'Champs de Cours, Fes',
  'maquillage,coiffure,bridal,fassi,traditionnel,oriental,heritage,mariee,fes,deplacement,couleurs-profondes',
  4.7, 432, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Beauty by Saoussane');

INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 2, 'Souma Makeup', 'Casablanca',
  'TEAM SOUMA MAKEUP — Specialised team on bridal (475K abonnes IG, +1500 publications). Casablanca. Equipe complete dediee a la mariee uniquement (pas d''invitees). Reference luxe nationale, contact uniquement WhatsApp.',
  3000, 7000, 'PREMIUM', '0644931869', '@souma__makeup',
  'Casablanca',
  'maquillage,coiffure,bridal,specialise,team,luxe,premium,glow,tendance,casa,reference,nationale',
  4.9, 1571, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Souma Makeup');

-- ============================================================
-- C. VERIFICATION
-- ============================================================
SELECT vc.name_fr AS categorie, COUNT(*) AS nb,
       MIN(v.prix_min) AS prix_min_cat, MAX(v.prix_max) AS prix_max_cat
FROM vendors v JOIN vendor_categories vc ON v.category_id = vc.id
WHERE v.is_active = 1
GROUP BY v.category_id
ORDER BY v.category_id;
