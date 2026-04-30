# Ayora — Planification de mariage à Fès

Application web Jakarta EE 5.0 / Tomcat 10 pour organiser un mariage marocain de A à Z :
questionnaire, prestataires vérifiés à Fès, invitations digitales (15 modèles dont vidéo Premium),
abonnement Free / Pro / Premium.

## Stack

- **Java 17** (LTS)
- **Jakarta EE 5.0** (`jakarta.servlet.*`)
- **Apache Tomcat 10.0.x**
- **MySQL 8.x** (compatible 5.7+)
- **HTML / CSS / Vanilla JS** (pas de framework côté front)

## Architecture (style p01-jdbc + p02-jee du cours)

```
src/com/ayora/
├── util/        DataSource, MySQLDataSource, DatabaseConnection, JsonUtil
├── model/       POJOs : User, Vendor, VendorCategory, Subscription, Invitation, ...
├── dao/         Accès BD : UserDao, VendorDao, InvitationDao (avec video_url), ...
├── service/     EmailService (SMTP via env vars + 4 templates), RecommendationService
└── servlet/     @WebServlet : AuthServlet, VendorServlet, InvitationServlet (template-level check), ...

WebContent/
├── *.html       Pages frontend (15 invitations, 6-step questionnaire, 3-tier pricing, ...)
├── css/         styles.css
├── js/          api.js (wrapper fetch)
└── WEB-INF/
    ├── web.xml
    └── lib/     mysql-connector-j, jakarta.mail, jakarta.activation

sql/
├── ayora_db_full.sql              schema initial complet
├── migration_pro_tier.sql         palier PRO ajouté
├── migration_vendors_real.sql     24 vendors verifiés (lot 1)
├── migration_vendors_fix.sql      encodage + prix réalistes
├── migration_vendors_v3.sql       11 nouveaux vendors + corrections
├── migration_video_url.sql        colonne invitations.video_url
└── migration_questionnaire_enums.sql  ENUMs élargis (LUXUEUSE, INTIME, ...)
```

## Démarrage rapide

1. Voir [`LAUNCH.md`](LAUNCH.md) — Eclipse + Tomcat 10 + phpMyAdmin
2. Importer `sql/ayora_db_full.sql` puis toutes les migrations dans l'ordre
3. Voir [`TEST_ACCOUNTS.md`](TEST_ACCOUNTS.md) pour les 5 comptes de démo
4. Voir [`TESTING.md`](TESTING.md) pour la checklist de tests manuels
5. Voir [`CHANGELOG.md`](CHANGELOG.md) pour l'historique des versions

## Comptes de test

| Plan | Email | Mot de passe |
|---|---|---|
| FREE | `test@ayora.ma` | `test123` |
| **PRO** | `pro@ayora.ma` | `pro123` |
| **PREMIUM** | `amine@ayora.ma` | `amine123` |
| **PREMIUM** | `ayasofia@ayora.ma` | `Aya@2006` |
| ADMIN | `admin@ayora.ma` | `admin123` |

## Configuration SMTP (envoi des invitations)

Voir [`.env.example`](.env.example) pour les variables d'environnement.
Sans configuration → l'EmailService fonctionne en mode **DEMO** (logs uniquement).

## Routes API

```
GET  /api/vendors                       liste 60 prestataires
GET  /api/vendors/categories            17 catégories
GET  /api/vendors/{id}                  détail
POST /api/auth/login {email, password}
GET  /api/auth/me                       utilisateur courant
GET  /api/questionnaire                 réponses du questionnaire
POST /api/questionnaire {...}           sauvegarde
GET  /api/recommendations               recos personnalisées
POST /api/invitations {guestId, templateName, videoUrl?}
POST /api/invitations/send/{id}         envoi par email
GET  /api/invitations                   mes invitations + statut abonnement
POST /api/subscription/upgrade {plan}   PRO ou PREMIUM
```
