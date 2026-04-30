# Ayora — Checklist de tests manuels

À exécuter après chaque modification importante avant la démo.

## 1. Authentification

- [ ] Inscription d'un nouveau client via `/register.html`
- [ ] Connexion `test@ayora.ma` / `test123` → redirection dashboard
- [ ] Connexion `pro@ayora.ma` / `pro123` → badge PRO en haut à droite
- [ ] Connexion `amine@ayora.ma` / `amine123` → badge PREMIUM doré
- [ ] Connexion `ayasofia@ayora.ma` / `Aya@2006` → badge PREMIUM
- [ ] Connexion `admin@ayora.ma` / `admin123` → redirection admin.html
- [ ] Mauvais mot de passe → message d'erreur clair
- [ ] Déconnexion : la session doit être invalidée

## 2. Questionnaire (6 sections en stepper)

- [ ] Section 1 : noms mariée/marié, date, ville, type — date saisie active la section suivante
- [ ] Section 2 : budget total + 16 priorités multi-choix
- [ ] Section 3 : **saison auto-calculée** depuis la date (juin-sept = été, etc.)
- [ ] Section 4 : services + bloc Neggafa détaillé visible (tenues, style, amariya)
- [ ] Section 5 : lieu cérémonie en select (Salle/Riad/Jardin/Piscine/Hôtel)
- [ ] Section 6 : contraintes
- [ ] Soumission → redirection dashboard
- [ ] Si session expirée pendant le remplissage → redirection login + restauration auto

## 3. Pricing & abonnements

- [ ] Page d'accueil : 3 cartes alignées (Gratuit / Pro recommandé / Premium)
- [ ] Bouton "Passer Pro" sur la card Pro → premium.html?plan=pro
- [ ] Bouton "Commencer Premium" → premium.html?plan=premium
- [ ] Dashboard FREE : 2 bandeaux d'upgrade côte-à-côte
- [ ] premium.html : sélecteur de plan affiche le bon tarif (149/299) selon le bouton cliqué

## 4. Prestataires

- [ ] `/vendors.html` charge 60 prestataires depuis `/api/vendors`
- [ ] Filtre par catégorie fonctionne (17 catégories)
- [ ] Recherche par mot-clé (`/api/vendors/search?q=...`)
- [ ] Aucun caractère `ÔÇö` dans les descriptions
- [ ] Prix réalistes (Youssef Wahbi 40k-80k, Al Jawda 450 DHS/personne, salles ≤ 40k)
- [ ] Téléphone + Instagram visibles sur chaque carte
- [ ] Le Pavillon Dor, Palais Benjelloun, Haj Said Berrada apparaissent

## 5. Invitations (15 modèles)

### Compte FREE (`test@ayora.ma`)
- [ ] 3 modèles utilisables (Ivory Gold, Minimal Luxury, Soft Calligraphy)
- [ ] 12 modèles avec cadenas + "Disponible avec PRO/PREMIUM"
- [ ] Clic sur modèle verrouillé → redirection vers premium.html

### Compte PRO (`pro@ayora.ma`)
- [ ] 9 modèles utilisables (Free + Pro y compris **Zellige Pearl** marocain)
- [ ] 6 modèles Premium verrouillés
- [ ] 50 invitations max / mois

### Compte PREMIUM (`amine@ayora.ma` ou `ayasofia@ayora.ma`)
- [ ] 15 modèles utilisables — aucun cadenas
- [ ] 3 modèles **VIDÉO** : Cinema Wedding, Fassi Royal Video, Black Velvet Video
- [ ] Clic sur modèle vidéo → modal demande lien vidéo
- [ ] Coller un lien YouTube/Drive → "Confirmer & envoyer"
- [ ] L'invité reçoit un email avec gros bouton "▶ Voir l'invitation vidéo"
- [ ] Invitations illimitées (compteur affiche ∞)
- [ ] Modèles **Zellige Royal** et **Andalusian Touch** marocains débloqués

### Personnalisation
- [ ] Les noms du couple (mariée + marié) saisis dans le questionnaire apparaissent sur tous les modèles
- [ ] Modal de création affiche un récap (invité, email, modèle)

## 6. Backend (template-level check)

- [ ] FREE qui POST `/api/invitations` avec `templateName: "royal-black"` → 403
- [ ] PRO qui POST `/api/invitations` avec `templateName: "video-cinema"` → 403
- [ ] PREMIUM qui POST avec n'importe quel template → 200
- [ ] Console Tomcat : `Connection established successfully` au démarrage
- [ ] EmailService en mode DEMO si `AYORA_MAIL_FROM` non configuré (logs)

## 7. Admin

- [ ] `/admin.html` accessible uniquement avec compte ADMIN
- [ ] Stats : nb users, nb vendors, nb devis, nb RDV
- [ ] Liste des users et vendors complète

## 8. Base de données

- [ ] La base `ayora_db` est bien chargée avec 10 tables (+ video_url sur invitations)
- [ ] 17 catégories et **60 prestataires**
- [ ] ENUMs questionnaire élargies (LUXUEUSE, TRADITIONNELLE, INTIME)
- [ ] Comptes test présents : test, pro, amine, ayasofia, admin
