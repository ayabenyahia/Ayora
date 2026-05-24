# Ayora — Planification de mariage à Fès

Application web Jakarta EE 5.0 / Tomcat 10 pour organiser un mariage marocain de A à Z :
questionnaire en 6 sections, **moteur de recommandation IA** multi-critères avec tags et raisons
contextualisées, 50+ prestataires vérifiés (Fès, Rabat-Salé-Témara-Kénitra), 15 invitations
digitales pré-remplies dynamiquement (noms du couple, date, heure, lieu), abonnements
Free / Pro (149 DHS) / Premium (299 DHS).

> **v2.3 (mai 2026)** — IA Ayora : moteur de recommandation k-NN pondéré local
> (`src/com/ayora/service/AyoraRecommendationEngine.java`) avec 6 critères pondérés
> (budget 30%, style 25%, ville 15%, invités 15%, luxe 10%, qualité 5%). 16 vrais
> prestataires fassi/casaouis ajoutés depuis leurs comptes Instagram, prix recalibrés
> sur le marché réel Fès 2026. Auto-PREMIUM pour les emails @ayora.ma. Section 6
> du questionnaire enrichie (tolérance hors-ville, mariage mixte/séparé, halal,
> langue, événements). 3 nouveaux templates wow (Holographic Velvet, Aurora Borealis,
> Mihrab d'Or) et 4 templates animés (Royal Black, Emerald, Sunset, Constellation).
> Suppression complète des emojis (SVG lineart partout). **Documentation IA détaillée
> dans [IA_AYORA.md](./IA_AYORA.md)**, voir aussi [CHANGELOG.md](./CHANGELOG.md) et
> [RECOMMENDATIONS.md](./RECOMMENDATIONS.md).

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
├── service/     VendorService/Default (interface + impl style BiblioService), RecommendationService
└── servlet/     @WebServlet : AuthServlet, VendorServlet, InvitationServlet (template-level check), ...

WebContent/
├── *.html       Pages frontend (15 invitations, 6-step questionnaire, 3-tier pricing, ...)
├── css/         styles.css
├── js/          api.js (wrapper fetch)
└── WEB-INF/
    ├── web.xml
    └── lib/     mysql-connector-j

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

## Envoi des invitations

L'envoi reel des emails d'invitation est gere cote frontend.
Le backend (`POST /api/invitations/send/{id}`) verifie l'abonnement,
marque l'invitation comme `ENVOYEE` en BDD et incremente le compteur.

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
