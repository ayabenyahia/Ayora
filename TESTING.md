# Ayora — Checklist de tests manuels

À exécuter après chaque modification importante avant la démo.

## 1. Authentification

- [ ] Inscription d'un nouveau client via `/register.html`
  - Email pas déjà utilisé → succès + auto-login
  - Email déjà utilisé → message "Cet email est deja utilise"
- [ ] Connexion via `/login.html`
  - `test@ayora.ma` / `test123` → redirection dashboard
  - `admin@ayora.ma` / `admin123` → redirection admin
  - mauvais mot de passe → message d'erreur
- [ ] Déconnexion : la session doit être invalidée

## 2. Questionnaire

- [ ] Premier login client → redirection vers `/questionnaire.html`
- [ ] Soumission complète → flag `questionnaireCompleted=true`
- [ ] Re-login → redirection directe vers dashboard

## 3. Prestataires

- [ ] `/vendors.html` charge la liste depuis `/api/vendors`
- [ ] Filtre par catégorie fonctionne
- [ ] Recherche par mot-clé fonctionne (`/api/vendors/search?q=...`)
- [ ] Détail d'un prestataire affiche prix, gamme, rating, téléphone

## 4. Invités et invitations

- [ ] Ajout d'un invité dans `/guests.html`
- [ ] Création d'une invitation depuis un invité
- [ ] Compteur freemium : bloquer après 10 invitations en plan FREE

## 5. Admin

- [ ] `/admin.html` accessible uniquement avec compte ADMIN
- [ ] Stats : nb users, nb vendors, nb devis, nb RDV
- [ ] Liste des users et vendors

## 6. API REST

- [ ] `GET /api/vendors` → tableau JSON
- [ ] `GET /api/vendors/categories` → 17 catégories
- [ ] `GET /api/auth/me` non authentifié → 401
- [ ] `GET /api/auth/me` authentifié → JSON utilisateur

## 7. Backend

- [ ] Console Tomcat affiche `Connection established successfully` au premier appel BD
- [ ] Pas de stacktrace dans la console pendant les tests
- [ ] Pas d'erreur 500 inattendue

## 8. Base de données

- [ ] La base `ayora_db` est bien chargée avec 10 tables
- [ ] Au moins 17 catégories et 20 prestataires en seed
- [ ] Comptes admin et test présents

> Cette checklist couvre les user stories US-01 à US-22.
> Voir le Kanban GitHub pour le statut détaillé de chaque US.
