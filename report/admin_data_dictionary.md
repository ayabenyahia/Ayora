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
