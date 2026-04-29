# Ayora — Planification de mariage à Fès

Application web Jakarta EE 5.0 / Tomcat 10 pour aider à organiser un mariage : questionnaire, prestataires de Fès, invitations digitales, abonnement freemium.

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
├── model/       POJOs : User, Vendor, VendorCategory, Subscription, ...
├── dao/         Accès BD : UserDao, VendorDao, ...
├── service/     EmailService, RecommendationService
└── servlet/     @WebServlet : AuthServlet, VendorServlet, ...

WebContent/
├── *.html       Pages frontend (index, login, dashboard, vendors, ...)
├── css/         styles.css
├── js/          api.js (wrapper fetch)
└── WEB-INF/
    ├── web.xml
    └── lib/     mysql-connector-j, jakarta.mail, jakarta.activation
```

## Démarrage rapide

Voir [`LAUNCH.md`](LAUNCH.md) pour la procédure complète (Eclipse + Tomcat 10 + phpMyAdmin).
Voir [`TESTING.md`](TESTING.md) pour la checklist de tests manuels.

## Comptes de test

- **admin** : `admin@ayora.ma` / `admin123`
- **client** : `test@ayora.ma` / `test123`
