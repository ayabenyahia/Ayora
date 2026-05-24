-- ============================================================
-- Migration v9 — Photos pour le lot v9 (5 prestataires)
-- ============================================================
-- Prerequis :
--   1) migration_vendors_v9_new_batch.sql jouee (5 vendors crees)
--   2) migration_vendors_media.sql jouee (colonnes photo_url / gallery_urls / reel_url)
--   3) Fichiers images deposes dans WebContent/images/vendors/ :
--        - nisrine-benkirane.png
--        - bloom-by-amal.png
--        - camera-youness.png
--        - noujoum-bola-bola.png
--        - adnane-nafie.png
--
-- Convention identique a migration_vendors_photos_batch1.sql :
--   photo_url = '/ayora/images/vendors/<slug>.png'
--
-- Format des images recommande :
--   - PNG ou JPG, ratio carre (square crop)
--   - Taille ~1080x1080 (collage Instagram 3x3 ou photo unique)
--   - Poids ~1-2 Mo par fichier (pas de base64, pas de gros fichiers)
--
-- Idempotent : un UPDATE qui ne trouve rien ne fait rien.
-- ============================================================

USE ayora_db;

-- ----- 1. Nisrine Benkirane (cake designer) ----------
UPDATE vendors
   SET photo_url = '/ayora/images/vendors/nisrine-benkirane.png'
 WHERE name = 'Nisrine Benkirane';

-- ----- 2. Bloom by Amal (cake designer + event design) ----------
UPDATE vendors
   SET photo_url = '/ayora/images/vendors/bloom-by-amal.png'
 WHERE name = 'Bloom by Amal';

-- ----- 3. Camera Youness (photographe) ----------
UPDATE vendors
   SET photo_url = '/ayora/images/vendors/camera-youness.png'
 WHERE name = 'Camera Youness';

-- ----- 4. Noujoum Fes Bola Bola (issawa) ----------
UPDATE vendors
   SET photo_url = '/ayora/images/vendors/noujoum-bola-bola.png'
 WHERE name = 'Noujoum Fes Bola Bola';

-- ----- 5. Adnane Nafie (issawa) ----------
UPDATE vendors
   SET photo_url = '/ayora/images/vendors/adnane-nafie.png'
 WHERE name = 'Adnane Nafie';

-- ============================================================
-- Verification : doit retourner 5 lignes avec photo_url renseignee
-- ============================================================
-- SELECT id, name, photo_url
-- FROM vendors
-- WHERE name IN ('Nisrine Benkirane','Bloom by Amal','Camera Youness',
--                'Noujoum Fes Bola Bola','Adnane Nafie')
-- ORDER BY name;

-- ============================================================
-- (Optionnel) Galerie : si tu veux ajouter plusieurs photos par
-- prestataire, depose-les sous <slug>-1.png, <slug>-2.png, ... puis
-- decommente et adapte les UPDATE ci-dessous. Format : URLs separees
-- par "|" (pipe).
-- ============================================================
-- UPDATE vendors SET gallery_urls = '/ayora/images/vendors/nisrine-benkirane-1.png|/ayora/images/vendors/nisrine-benkirane-2.png|/ayora/images/vendors/nisrine-benkirane-3.png' WHERE name='Nisrine Benkirane';
-- UPDATE vendors SET gallery_urls = '/ayora/images/vendors/bloom-by-amal-1.png|/ayora/images/vendors/bloom-by-amal-2.png' WHERE name='Bloom by Amal';
-- UPDATE vendors SET gallery_urls = '/ayora/images/vendors/camera-youness-1.png|/ayora/images/vendors/camera-youness-2.png' WHERE name='Camera Youness';
-- UPDATE vendors SET gallery_urls = '/ayora/images/vendors/noujoum-bola-bola-1.png|/ayora/images/vendors/noujoum-bola-bola-2.png' WHERE name='Noujoum Fes Bola Bola';
-- UPDATE vendors SET gallery_urls = '/ayora/images/vendors/adnane-nafie-1.png|/ayora/images/vendors/adnane-nafie-2.png' WHERE name='Adnane Nafie';

-- ============================================================
-- (Optionnel) Reel Instagram : URL publique d'un reel pour la modale.
-- ============================================================
-- UPDATE vendors SET reel_url = 'https://www.instagram.com/reel/XXXX/' WHERE name='Nisrine Benkirane';
-- UPDATE vendors SET reel_url = 'https://www.instagram.com/reel/XXXX/' WHERE name='Bloom by Amal';
-- UPDATE vendors SET reel_url = 'https://www.instagram.com/reel/XXXX/' WHERE name='Camera Youness';
-- UPDATE vendors SET reel_url = 'https://www.instagram.com/reel/XXXX/' WHERE name='Noujoum Fes Bola Bola';
-- UPDATE vendors SET reel_url = 'https://www.instagram.com/reel/XXXX/' WHERE name='Adnane Nafie';
