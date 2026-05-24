-- ============================================================
-- Migration vendors v8 : champs media (photo, galerie, reel IG)
-- ============================================================
-- A executer une seule fois sur la base. Toutes les colonnes sont
-- NULLABLE -- les vendors existants ne sont pas impactes et
-- l'application fonctionne meme sans avoir joue cette migration
-- (le DAO lit ces colonnes en mode defensif).
--
-- Conventions :
--   photo_url     : URL absolue de l'image de couverture (1 par vendor).
--   gallery_urls  : liste d'URLs separees par "|" (pipe), 0..N images.
--   reel_url      : URL publique d'un reel Instagram (pas de scraping,
--                   l'URL est fournie manuellement par l'admin).
--
-- Toutes les URLs doivent etre des liens publics (CDN, S3, Cloudinary,
-- ou hebergement statique dans /images/...). Aucune donnee binaire
-- n'est stockee en base.
-- ============================================================

ALTER TABLE vendors
    ADD COLUMN photo_url    VARCHAR(500) NULL AFTER instagram,
    ADD COLUMN gallery_urls TEXT         NULL AFTER photo_url,
    ADD COLUMN reel_url     VARCHAR(500) NULL AFTER gallery_urls;
