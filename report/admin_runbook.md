# Runbook administrateur AYORA

Ce runbook décrit les procédures opérationnelles standard pour l'administrateur de la plateforme AYORA.

## Responsabilités

- Vérifier la qualité des profils prestataires.
- Modérer les comptes clients.
- Suivre les demandes de devis et rendez-vous.
- Maintenir la cohérence des abonnements.
- Superviser la santé globale de la plateforme.

## Connexion administrateur

1. Ouvrir `https://ayora.ma/login.html`.
2. Saisir l'email administrateur et le mot de passe (PBKDF2).
3. Le serveur place `role=ADMIN` en session via `AuthServlet.login()`.
4. Le front redirige vers `admin.html` — toute autre destination déclenche un retour sur `dashboard.html`.

## Triage des priorités

Le bloc *Priorités administratives* du dashboard liste 4 types d'éléments à traiter :

- Profils prestataires incomplets (priorité MEDIUM)
- Clients sans questionnaire (priorité LOW)
- Devis en attente (priorité HIGH)
- Rendez-vous en attente (priorité HIGH)

Cliquer un chip filtre la liste par catégorie. Le bouton *Examiner* ouvre la fiche détaillée correspondante.

## Validation d'un profil prestataire incomplet

1. Filtrer les priorités sur *Prestataires* ou cliquer le KPI *Prestataires actifs*.
2. Cliquer *Examiner* → le drawer affiche la fiche complète + score de complétude.
3. Compléter les champs manquants (description, téléphone, Instagram, etc.).
4. Cliquer *Enregistrer* → appel `PUT /api/admin/vendors/{id}`.
5. Le score de complétude se met à jour automatiquement.

## Suspension d'un compte client

1. Ouvrir l'onglet *Utilisateurs*.
2. Filtrer par rôle CLIENT et statut Actif.
3. Cliquer *Détail* sur la ligne ciblée.
4. Cliquer *Suspendre* → modale de confirmation.
5. Appel `POST /api/admin/users/{id}/active` avec `active=false`.
6. Le compte n'apparaît plus dans les listes actives mais reste en base.

## Changement de plan d'abonnement

1. Ouvrir la fiche utilisateur via le drawer.
2. Sélectionner le nouveau plan (`FREE`, `PRO`, `PREMIUM`).
3. Cliquer *Enregistrer* → enchaînement de 3 appels :
   - `PUT /api/admin/users/{id}` (identité)
   - `POST /api/admin/users/{id}/role` (rôle)
   - `POST /api/admin/users/{id}/plan` (plan)
4. La table `subscriptions` est synchronisée côté serveur.
