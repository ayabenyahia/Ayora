# Script de démonstration admin — 2 minutes pour la soutenance

Parcours optimisé pour montrer au jury que l'espace admin est un vrai centre de pilotage.

Compte de démonstration : `admin@ayora.ma`, mot de passe `[fourni en annexe technique]`.

## 0:00 → 0:10 — Connexion

1. Ouvrir `https://ayora.ma/login.html`.
2. Email admin + mot de passe → bouton *Connexion*.
3. Le serveur valide les identifiants (hash PBKDF2) et place `role=ADMIN` en session.
4. Redirection automatique vers `admin.html`.
