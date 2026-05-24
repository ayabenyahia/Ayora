-- ============================================================
-- Migration : photos de couverture (lot 1)
-- ============================================================
-- Prerequis : migration_vendors_media.sql doit avoir ete jouee
-- (colonnes photo_url / gallery_urls / reel_url presentes).
--
-- Les fichiers images correspondants doivent etre presents dans :
--   WebContent/images/vendors/<slug>.png
--
-- Collages Instagram preserves au format PNG (~2 Mo chacun), meme
-- gabarit que les images existantes (festin-traiteur.png, etc.).
-- ============================================================

USE ayora_db;

UPDATE vendors SET photo_url='/ayora/images/vendors/sahel-traiteur.png'
  WHERE name='Sahel Traiteur Events';

UPDATE vendors SET photo_url='/ayora/images/vendors/riad-salam-fes.png'
  WHERE name='Riad Salam Fes';

UPDATE vendors SET photo_url='/ayora/images/vendors/salamoun-makeup.png'
  WHERE name='Salamoun Makeup Artist - Salma Hammioui';

UPDATE vendors SET photo_url='/ayora/images/vendors/salle-elouazzani.png'
  WHERE name='Salle de fete El Ouazzani';

UPDATE vendors SET photo_url='/ayora/images/vendors/salle-billionaire.png'
  WHERE name='Salle Billionaire Fes';

-- ---- Lot 2 -------------------------------------------------
UPDATE vendors SET photo_url='/ayora/images/vendors/dar-hajji.png'
  WHERE name='Salle des Fetes Dar Hajji';

UPDATE vendors SET photo_url='/ayora/images/vendors/the-queen.png'
  WHERE name='Salle The Queen';

UPDATE vendors SET photo_url='/ayora/images/vendors/sekkate-traiteur.png'
  WHERE name='Sekkate Traiteur';

UPDATE vendors SET photo_url='/ayora/images/vendors/souma-makeup.png'
  WHERE name='Souma Makeup';

UPDATE vendors SET photo_url='/ayora/images/vendors/cake-house-chaym.png'
  WHERE name='The Cake House by Chaym';

-- ---- Lot 3 -------------------------------------------------
UPDATE vendors SET photo_url='/ayora/images/vendors/traiteur-aljawda.png'
  WHERE name='Traiteur Al Jawda';

UPDATE vendors SET photo_url='/ayora/images/vendors/traiteur-elamane.png'
  WHERE name='Traiteur El Amane';

UPDATE vendors SET photo_url='/ayora/images/vendors/traiteur-elkortbi.png'
  WHERE name='Traiteur El Kortbi';

UPDATE vendors SET photo_url='/ayora/images/vendors/yassine-makeup.png'
  WHERE name='Yassine Makeup & Hair';

UPDATE vendors SET photo_url='/ayora/images/vendors/younes-rbati.png'
  WHERE name='Younes Rbati';

-- Verification
SELECT name, photo_url FROM vendors
WHERE name IN (
  'Sahel Traiteur Events',
  'Riad Salam Fes',
  'Salamoun Makeup Artist - Salma Hammioui',
  'Salle de fete El Ouazzani',
  'Salle Billionaire Fes'
) ORDER BY name;
