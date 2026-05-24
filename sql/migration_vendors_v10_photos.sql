-- ============================================================
-- Migration v10 — Photos pour le lot v10 (4 prestataires)
-- ============================================================
-- Prerequis :
--   1) migration_vendors_v10_new_batch.sql jouee (4 vendors crees)
--   2) migration_vendors_media.sql jouee (colonnes photo_url / gallery_urls / reel_url)
--   3) Fichiers images deposes dans WebContent/images/vendors/ :
--        - faly-events.png
--        - abdellah-yaakoubi.png
--        - dar-ba-sidi.png
--        - moktaka-alossar.png
-- ============================================================

USE ayora_db;

UPDATE vendors SET photo_url='/ayora/images/vendors/faly-events.png'         WHERE name='Faly Events';
UPDATE vendors SET photo_url='/ayora/images/vendors/abdellah-yaakoubi.png'   WHERE name='Abdellah El Yaakoubi';
UPDATE vendors SET photo_url='/ayora/images/vendors/dar-ba-sidi.png'         WHERE name='Dar Ba Sidi';
UPDATE vendors SET photo_url='/ayora/images/vendors/moktaka-alossar.png'     WHERE name='Moktaka Alossar';

-- (Optionnel) Galerie multi-photos : decommenter et adapter quand prets
-- UPDATE vendors SET gallery_urls='/ayora/images/vendors/faly-events-1.png|/ayora/images/vendors/faly-events-2.png' WHERE name='Faly Events';
-- UPDATE vendors SET gallery_urls='/ayora/images/vendors/abdellah-yaakoubi-1.png|/ayora/images/vendors/abdellah-yaakoubi-2.png' WHERE name='Abdellah El Yaakoubi';
-- UPDATE vendors SET gallery_urls='/ayora/images/vendors/dar-ba-sidi-1.png|/ayora/images/vendors/dar-ba-sidi-2.png' WHERE name='Dar Ba Sidi';
-- UPDATE vendors SET gallery_urls='/ayora/images/vendors/moktaka-alossar-1.png|/ayora/images/vendors/moktaka-alossar-2.png' WHERE name='Moktaka Alossar';

-- (Optionnel) Reel Instagram
-- UPDATE vendors SET reel_url='https://www.instagram.com/reel/XXXX/' WHERE name='Faly Events';
-- UPDATE vendors SET reel_url='https://www.instagram.com/reel/XXXX/' WHERE name='Abdellah El Yaakoubi';
-- UPDATE vendors SET reel_url='https://www.instagram.com/reel/XXXX/' WHERE name='Dar Ba Sidi';
-- UPDATE vendors SET reel_url='https://www.instagram.com/reel/XXXX/' WHERE name='Moktaka Alossar';
