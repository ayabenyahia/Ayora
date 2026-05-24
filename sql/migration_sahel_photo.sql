-- ============================================================
-- Migration : photo de couverture pour Sahel Traiteur Events
-- ============================================================
-- Prerequis : la migration migration_vendors_media.sql doit avoir
-- ete jouee (colonne photo_url presente sur la table vendors).
--
-- Le fichier image lui-meme doit etre depose ici :
--   WebContent/images/vendors/sahel-traiteur.png
--
-- (collage Instagram du compte @traiteur.sahel.events, meme style
-- que les autres prestataires : adil-otmani.png, aziz-iyachi.png,
-- doreve-traiteur.png, festin-traiteur.png, ...).
-- ============================================================

UPDATE vendors
SET photo_url = '/ayora/images/vendors/sahel-traiteur.png'
WHERE name = 'Sahel Traiteur Events';
