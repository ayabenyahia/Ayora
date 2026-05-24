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
