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
