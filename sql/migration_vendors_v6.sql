-- ============================================================
-- Migration vendors v6 - 2026-05-04
--
-- Corrections de villes suite au feedback utilisateur :
--   - Adil Otmani               : Casablanca -> Fes (orchestre fassi)
--   - Dar Benjelloun Neggafa    : Fes        -> Casablanca
--   - Negafa Dar Benjelloun     : Fes        -> Casablanca
--   - Majda Benjelloun          : Fes        (deja correct, conserve)
--
-- A executer apres : migration_vendors_v5.sql
-- ============================================================

USE ayora_db;

-- 1. Adil Otmani : Casablanca -> Fes
UPDATE vendors
SET city = 'Fes',
    description = REPLACE(description, 'Casablanca', 'Fes'),
    address = 'Fes - Maroc'
WHERE name LIKE '%Adil Otmani%';

-- 2. Dar Benjelloun Neggafa (id 2) : Fes -> Casablanca
UPDATE vendors
SET city = 'Casablanca',
    description = 'Maison de prestige pour la neggafa a Casablanca. Tenues royales, caftans haute couture, savoir-faire traditionnel.',
    address = 'Casablanca - Anfa'
WHERE id = 2 OR (name = 'Dar Benjelloun Neggafa');

-- 3. Negafa Dar Benjelloun (id 39, ancienne entree wedding planner) : Fes -> Casablanca
UPDATE vendors
SET city = 'Casablanca',
    description = REPLACE(description, 'Fes', 'Casablanca'),
    address = 'Casablanca - Anfa'
WHERE id = 39 OR name LIKE 'Negafa Dar Benjelloun%';

-- 4. Majda Benjelloun : confirme Fes (no-op si deja correct)
UPDATE vendors
SET city = 'Fes',
    address = COALESCE(NULLIF(address, ''), 'Fes - Ville Nouvelle')
WHERE name LIKE '%Majda Benjelloun%';

-- ============================================================
-- VERIFICATIONS
-- ============================================================

SELECT id, name, city, category_id, gamme
FROM vendors
WHERE name LIKE '%Otmani%' OR name LIKE '%Benjelloun%' OR name LIKE '%Majda%'
ORDER BY id;
