# Ayora — Procédure de lancement

## 1. Importer la base de données dans phpMyAdmin / MySQL

1. Démarrer MySQL (8.x recommandé, 5.7+ accepté).
2. Ouvrir `http://localhost/phpmyadmin` (ou MySQL Workbench).
3. Onglet **Import** → choisir `sql/ayora_db_full.sql` → **Go**.
4. Vérifier que la base `ayora_db` apparaît avec **10 tables** :
   `users`, `vendors`, `vendor_categories`, `questionnaire_answers`,
   `guests`, `invitations`, `subscriptions`, `recommendations`,
   `demandes_devis`, `rendez_vous`.

> Pour MySQL 5.1 (très ancien), utiliser `sql/ayora_db_mysql5.sql` à la place.

## 2. Configurer Eclipse

1. **Window → Preferences → Java → Installed JREs** : ajouter un **JDK 17**
   (ex : `C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot`).
2. **Window → Preferences → Server → Runtime Environments** :
   - **Add...** → Apache Tomcat v10.0
   - Pointer sur ton install Tomcat (ex : `C:\Users\pc\Documents\apache-tomcat-10.0.27`)
   - JRE : sélectionner le JDK 17.
3. **File → Import → Existing Projects into Workspace** : choisir le dossier `ayora`.
4. **Project → Clean...** pour recompiler.

## 3. Démarrer Tomcat depuis Eclipse

1. Vue **Servers** → clic droit → **New → Server → Apache Tomcat v10.0**.
2. Clic droit sur le serveur → **Add and Remove...** → ajouter `ayora`.
3. Clic droit → **Start**.

## 4. Tester dans le navigateur

Ouvrir : `http://localhost:8080/ayora/`

## Routes principales

```
GET  /ayora/api/vendors              → liste prestataires
GET  /ayora/api/vendors/categories   → liste catégories
GET  /ayora/api/vendors/{id}         → détail prestataire
GET  /ayora/api/vendors/search?q=    → recherche
POST /ayora/api/auth/login           → {email, password}
POST /ayora/api/auth/register        → {email, password, firstName, lastName, phone}
POST /ayora/api/auth/logout
GET  /ayora/api/auth/me              → utilisateur courant
GET  /ayora/api/questionnaire        → réponses utilisateur
POST /ayora/api/questionnaire        → soumission
GET  /ayora/api/recommendations
GET  /ayora/api/guests
POST /ayora/api/guests
GET  /ayora/api/invitations
POST /ayora/api/invitations
GET  /ayora/api/subscription
POST /ayora/api/subscription/upgrade
```

## Comptes de test

| Rôle | Email | Mot de passe |
|---|---|---|
| ADMIN | admin@ayora.ma | admin123 |
| CLIENT | test@ayora.ma | test123 |
