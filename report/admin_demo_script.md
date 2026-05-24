# Script de démonstration admin — 2 minutes pour la soutenance

Parcours optimisé pour montrer au jury que l'espace admin est un vrai centre de pilotage.

Compte de démonstration : `admin@ayora.ma`, mot de passe `[fourni en annexe technique]`.

## 0:00 → 0:10 — Connexion

1. Ouvrir `https://ayora.ma/login.html`.
2. Email admin + mot de passe → bouton *Connexion*.
3. Le serveur valide les identifiants (hash PBKDF2) et place `role=ADMIN` en session.
4. Redirection automatique vers `admin.html`.

## 0:10 → 0:25 — Centre de pilotage

*"L'administrateur arrive sur un hero qui résume immédiatement l'état de la plateforme."*

Mettre en avant :

- Le titre serif *Centre de pilotage AYORA*
- Le bloc d'état dynamique : *N éléments nécessitent votre attention…*
- Les 3 CTAs : *Traiter les alertes*, *Gérer les prestataires*, *Consulter l'activité*

## 0:25 → 0:40 — 4 KPI principaux

*"Quatre grandes cartes hiérarchisent l'information stratégique."*

1. **Utilisateurs** — total + clients + prestataires
2. **Prestataires actifs** — actifs + incomplets + inactifs
3. **Plans Premium** — premium + free + pro (avec taux de conversion)
4. **Demandes en attente** — devis + rendez-vous

Chaque carte est cliquable et ouvre l'onglet correspondant avec le filtre pré-appliqué.

## 0:40 → 0:55 — Priorités administratives

*"Le bloc priorités centralise tout ce qui demande une intervention."*

1. Montrer les chips de filtrage : Toutes / Prestataires / Utilisateurs / Devis / Rendez-vous.
2. Cliquer un chip → la liste se restreint instantanément.
3. Cliquer *Examiner* sur un profil prestataire incomplet → le drawer s'ouvre à droite avec la fiche complète.
4. Pointer les champs manquants (description, téléphone, Instagram).

## 0:55 → 1:10 — Santé de la plateforme

*"Les barres de progression donnent une vue d'ensemble qualitative."*

Mettre en avant :

- *Profils prestataires complets* (X / Y)
- *Questionnaires clients terminés*
- *Comptes actifs*
- *Demandes à traiter* (vert quand 0)

Insister : aucune donnée inventée — tout est calculé en temps réel depuis MySQL.

## 1:10 → 1:25 — Analytics

*"Trois cartes analytics lisibles, pas de vanity metrics."*

1. **Inscriptions sur 12 mois** — bar chart `signupsByMonth` (12 derniers mois).
2. **Répartition des plans** — donut Free / Pro / Premium avec total au centre.
3. **Prestataires par catégorie** — barres horizontales triées par effectif.

Toutes les données viennent de `/api/admin/analytics`.

## 1:25 → 1:40 — Gestion des utilisateurs

*"Tableau complet avec filtres multi-critères et édition inline."*

1. Filtres : rôle, plan, questionnaire, statut, recherche libre.
2. Cliquer *Détail* sur une ligne → drawer d'édition.
3. Démontrer le changement de plan FREE → PREMIUM (3 appels REST enchaînés).
4. Toast de succès, dashboard rafraîchi.
