# Dictionnaire de données — espace administrateur AYORA

Référence des tables MySQL exploitées par les endpoints `/api/admin/*` et affichées dans le dashboard.

Chaque section liste : nom de la colonne, type, contraintes, sémantique, et où elle apparaît dans l'UI admin.

## `users`

| Colonne | Type | Contraintes | UI admin |
|---|---|---|---|
| `id` | INT | PK auto | colonne caché, utilisée pour le drawer |
| `first_name` | VARCHAR(80) | NOT NULL | tableau Utilisateurs, drawer |
| `last_name` | VARCHAR(80) | NOT NULL | tableau Utilisateurs, drawer |
| `email` | VARCHAR(160) | UNIQUE NOT NULL | tableau, recherche |
| `phone` | VARCHAR(40) | nullable | tableau, drawer |
| `city` | VARCHAR(80) | nullable | tableau, drawer |
| `role` | ENUM('CLIENT','PRESTATAIRE','ADMIN') | NOT NULL | badge rôle |
| `subscription_type` | ENUM('FREE','PRO','PREMIUM') | NOT NULL | badge plan |
| `is_active` | TINYINT | NOT NULL DEFAULT 1 | badge Actif/Suspendu |
| `questionnaire_completed` | TINYINT | NOT NULL DEFAULT 0 | badge Complété/À compléter |
| `vendor_id` | INT | FK vendors.id nullable | lien fiche prestataire |
| `created_at` | DATETIME | DEFAULT NOW | colonne *Inscrit* |

## `vendors`

| Colonne | Type | Contraintes | UI admin |
|---|---|---|---|
| `id` | INT | PK auto | drawer |
| `name` | VARCHAR(160) | NOT NULL | tableau, drawer |
| `category_id` | INT | FK categories.id | badge catégorie |
| `city` | VARCHAR(80) | nullable | tableau |
| `description` | TEXT | nullable | drawer, complétude |
| `prix_min` / `prix_max` | DECIMAL(10,2) | nullable | colonne *À partir de* |
| `gamme` | ENUM('ECONOMIQUE','MOYEN','PREMIUM') | NOT NULL | badge gamme |
| `phone` / `email` / `instagram` | VARCHAR | nullable | drawer, complétude |
| `address` / `tags` | VARCHAR/TEXT | nullable | drawer |
| `rating` | DECIMAL(3,2) | nullable | colonne *Note* |
| `nb_avis` | INT | DEFAULT 0 | drawer |
| `is_active` | TINYINT | DEFAULT 1 | badge Actif/Inactif |

## `demandes_devis`

| Colonne | Type | Sémantique |
|---|---|---|
| `id` | INT PK | identifiant |
| `client_id` | FK users.id | client demandeur |
| `vendor_id` | FK vendors.id | prestataire ciblé |
| `budget_min` / `budget_max` | DECIMAL | fourchette budgétaire en DHS |
| `message` | TEXT | message du client |
| `date_mariage` | DATE | date prévue |
| `nb_invites` | INT | volume invités |
| `statut` | ENUM('EN_ATTENTE','ACCEPTE','REFUSE') | statut admin |
| `reponse_prestataire` | TEXT | optionnel |
| `created_at` | DATETIME | tri timeline |

## `rendez_vous`

| Colonne | Type | Sémantique |
|---|---|---|
| `id` | INT PK | identifiant |
| `client_id` | FK users.id | client |
| `vendor_id` | FK vendors.id | prestataire |
| `date_rdv` | DATE | jour |
| `heure_rdv` | VARCHAR(8) | heure HH:MM |
| `lieu` | VARCHAR(200) | lieu de RDV |
| `note` | TEXT | note libre |
| `statut` | ENUM('EN_ATTENTE','CONFIRME','ANNULE') | statut admin |
| `created_at` | DATETIME | tri timeline |

## `questionnaire_answers`

| Colonne | Type | Sémantique |
|---|---|---|
| `user_id` | FK users.id | propriétaire |
| `step` | INT | étape du questionnaire |
| `key` | VARCHAR | clé de réponse |
| `value` | TEXT | valeur |
| `created_at` | DATETIME | dernière mise à jour |

Utilisé pour calculer `questionnaire.complete` / `questionnaire.incomplete` dans `/api/admin/stats`.
