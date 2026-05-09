-- ============================================
-- AYORA - Migration : extension ENUMs questionnaire
-- Pour accepter les valeurs supplementaires utilisees par
-- le formulaire frontend (sections Style et Ambiance).
-- ============================================

USE ayora_db;

-- ambiance : ajout de LUXUEUSE et TRADITIONNELLE
ALTER TABLE questionnaire_answers
    MODIFY COLUMN ambiance ENUM(
        'FESTIVE','INTIME','GRANDIOSE','ROMANTIQUE','FAMILIALE',
        'LUXUEUSE','TRADITIONNELLE'
    );

-- style_mariage : ajout de INTIME (avant : TRADITIONNEL/MODERNE/MIXTE/LUXE/SIMPLE)
ALTER TABLE questionnaire_answers
    MODIFY COLUMN style_mariage ENUM(
        'TRADITIONNEL','MODERNE','MIXTE','LUXE','SIMPLE','INTIME'
    );

-- niveau_luxe : ajout LUXE (defensive)
ALTER TABLE questionnaire_answers
    MODIFY COLUMN niveau_luxe ENUM(
        'ECONOMIQUE','MOYEN','PREMIUM','ULTRA_LUXE','LUXE'
    );

SELECT 'Migration questionnaire ENUMs OK' AS status;
