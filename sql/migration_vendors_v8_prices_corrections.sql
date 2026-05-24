-- ============================================================
-- Migration v8 — Corrections prix exactes (terrain) + suppressions
-- + 3 nouveaux prestataires (Crystal salle, Tamouh, Tahour)
-- ============================================================
-- Liste des prix fournie directement par la mariee/cliente.
-- Les prestataires non listes restent intouches.
--
-- A executer dans phpMyAdmin sur la base ayora_db.
-- ============================================================

USE ayora_db;

-- ============================================================
-- A. SUPPRESSIONS
-- ============================================================
-- Salle Al Andalous : a retirer entierement
DELETE FROM vendors WHERE name = 'Salle Al Andalous';

-- Doublon Negafa Dar Benjelloun - Wedding Planner (croix bleue)
-- (la version sans suffixe "Wedding Planner" reste : "Dar Benjelloun Neggafa")
DELETE FROM vendors WHERE name = 'Negafa Dar Benjelloun - Wedding Planner';

-- ============================================================
-- B. CORRECTIONS PRIX (a partir de XXX DHS)
-- ============================================================

-- Orchestres
UPDATE vendors SET prix_min=9000  WHERE name='Adil Otmani - Artiste & Compositeur';
UPDATE vendors SET prix_min=10000 WHERE name='Mehdi Artiste Orchestre';
UPDATE vendors SET prix_min=10000 WHERE name='Orchestre Marouane Lebbar';
UPDATE vendors SET prix_min=10000 WHERE name='Omar Hanoun';
UPDATE vendors SET prix_min=30000 WHERE name='Orchestre Mohamed Laasry';
UPDATE vendors SET prix_min=30000 WHERE name='Younes Rbati';
UPDATE vendors SET prix_min=45000 WHERE name='Marouane Hajji';

-- Tyafer / Myadi
UPDATE vendors SET prix_min=5000 WHERE name='Aziz Iyachi Tyafer';
UPDATE vendors SET prix_min=5000 WHERE name='Jeff de Bruges Fes';
UPDATE vendors SET prix_min=4000 WHERE name='Chocolat de Joie';
UPDATE vendors SET prix_min=3000 WHERE name='Emeraude by Malika Alaoui';

-- Maquillage / Coiffure
UPDATE vendors SET prix_min=1500 WHERE name='Beauty by Saoussane';
UPDATE vendors SET prix_min=4000 WHERE name='Makeup by Hala';
UPDATE vendors SET prix_min=4000 WHERE name='Nadia El Guerch';
UPDATE vendors SET prix_min=10000 WHERE name='Make Me Fab';
UPDATE vendors SET prix_min=6000 WHERE name='KK Kouki Hairstyle';
UPDATE vendors SET prix_min=6000 WHERE name='Souma Makeup';

-- Neggafa
UPDATE vendors SET prix_min=30000 WHERE name='Dar Benjelloun Neggafa';
UPDATE vendors SET prix_min=5000  WHERE name='Haja Zakia Neggafa';
UPDATE vendors SET prix_min=5000  WHERE name='Negafa Dar Haja Zakia';
UPDATE vendors SET prix_min=6000  WHERE name='Neggafa El Farssi';
UPDATE vendors SET prix_min=25000 WHERE name='Mounia Ramsis Tounsi - Neggafa';
UPDATE vendors SET prix_min=30000 WHERE name='Negafa Majda Benjelloun - Tenguif de Luxe';

-- Cake
UPDATE vendors SET prix_min=1200 WHERE name='Cake Ahlame - Ahlam Zayyoun';

-- DJ
UPDATE vendors SET prix_min=1500 WHERE name='DJ Kacem - Kacem Mghabbar';
UPDATE vendors SET prix_min=2000 WHERE name='DJ Mehdi Bouchal';
UPDATE vendors SET prix_min=1500 WHERE name='DJ Night Vibes Fes';

-- Issawa
UPDATE vendors SET prix_min=1500 WHERE name='Issawa Moustafa Meseyah - Bola Bola de Luxe';
UPDATE vendors SET prix_min=7000 WHERE name='Issawa Salim - Ahmed Salim';

-- Salles
UPDATE vendors SET prix_min=8000  WHERE name='Palais D''or Fes';
UPDATE vendors SET prix_min=20000 WHERE name='Palais Faraj Fes';
UPDATE vendors SET prix_min=12000 WHERE name='Salle Billionaire Fes';
UPDATE vendors SET prix_min=6000  WHERE name='Salle de fete El Ouazzani';
UPDATE vendors SET prix_min=4500  WHERE name='Salle des Fetes Dar Hajji';

-- Traiteur
UPDATE vendors SET prix_min=300 WHERE name='Doreve Traiteur - Karima Riffi';
UPDATE vendors SET prix_min=600 WHERE name='Sekkate Traiteur';
UPDATE vendors SET prix_min=600 WHERE name='Traiteur Al Jawda';

-- ============================================================
-- C. AJOUT DES 3 NOUVEAUX PRESTATAIRES
-- ============================================================

-- Crystal Salle des Fetes (cat=11 SALLE) — a partir de 18 000
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 11, 'Crystal Salle des Fetes', 'Fes',
  'Crystal Salle des Fetes a Fes — espace evenementiel moderne avec scenographie cristal et lumieres premium. Capacite moyenne a grande (200-400 invites), tres demande pour les mariages chics et contemporains.',
  18000, 35000, 'PREMIUM', '0535000000', '@crystal_salle_fes',
  'Fes',
  'salle,moderne,cristal,grand,polyvalent,scenographie,premium,fes,chic,lumineux',
  4.6, 80, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Crystal Salle des Fetes');

-- Orchestre Tamouh (cat=8 ORCHESTRE) — a partir de 10 000
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 8, 'Orchestre Tamouh', 'Fes',
  'Orchestre Tamouh, Fes — formation marocaine professionnelle. Repertoire chaabi, andalou et mariage moderne. 8-10 musiciens, soiree complete (4-6h), repertoire adapte aux goûts du couple.',
  10000, 25000, 'MOYEN', '0661000000', '@orchestre_tamouh',
  'Fes',
  'orchestre,marocain,chaabi,andalou,mariage,fes,professionnel,polyvalent',
  4.5, 60, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Orchestre Tamouh');

-- Orchestre Tahour (cat=8 ORCHESTRE) — a partir de 60 000 (top range)
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis, is_active)
SELECT 8, 'Orchestre Tahour', 'Fes',
  'Orchestre Tahour — formation premium / star nationale. Tarif eleve mais prestation memorable : grande formation orchestrale, scenographie inclus, chanteur principal de renom. Reserve aux mariages haut de gamme.',
  60000, 120000, 'PREMIUM', '0661000001', '@orchestre_tahour',
  'Fes',
  'orchestre,premium,luxe,star,national,grand,prestige,scenographie,renomme,fes',
  4.9, 200, 1
WHERE NOT EXISTS (SELECT 1 FROM vendors WHERE name='Orchestre Tahour');

-- ============================================================
-- D. VERIFICATION FINALE
-- ============================================================
SELECT vc.name_fr AS categorie, COUNT(*) AS nb,
       MIN(v.prix_min) AS prix_min_cat, MAX(v.prix_min) AS prix_max_cat
FROM vendors v JOIN vendor_categories vc ON v.category_id = vc.id
WHERE v.is_active = 1
GROUP BY v.category_id
ORDER BY v.category_id;
