# Ayora — User Stories

Backlog complet aligné avec les milestones v1, v2.0, v2.1 et **v2.2**.

## Légende
- 🟢 livré (closed)
- 🟡 en cours (in progress)
- ⚪ à faire (open)

---

## Epic 1 — Compte & authentification

- 🟢 **US-01** En tant que future mariée, je veux créer un compte avec email/mot de passe pour sauvegarder mon projet entre sessions.
- 🟢 **US-02** En tant qu'utilisatrice connectée, je veux que ma session expire proprement et soit restaurée en revenant via le formulaire de login.
- 🟢 **US-03** En tant qu'admin, je veux voir tous les comptes et changer leur plan d'abonnement.

## Epic 2 — Plans et tarification

- 🟢 **US-10** En tant que visiteur, je veux comparer les plans Free / Pro / Premium sur la home avec un alignement strict des cartes.
- 🟢 **US-11** En tant que mariée Free, je veux voir des bandeaux d'upgrade contextuels (Pro et Premium) sur le dashboard.
- 🟢 **US-12** En tant qu'utilisateur sur `premium.html`, je veux choisir mon plan (Pro ou Premium) avant de payer.
- 🟢 **US-13** En tant qu'admin, je veux marquer manuellement un compte comme Premium (cas Zakia).

## Epic 3 — Questionnaire personnalisé

- 🟢 **US-20** En tant que future mariée, je veux remplir un questionnaire en 6 sections avec un stepper visuel pour ne pas être submergée.
- 🟢 **US-21** En tant qu'utilisatrice, je veux que la saison soit déduite automatiquement de la date du mariage.
- 🟢 **US-22** En tant qu'utilisatrice, je veux que mon thème de couleur s'auto-remplisse selon le thème (royal-fassi, oriental, bohème…).
- 🟢 **US-23** En tant qu'utilisatrice qui édite mon questionnaire, je veux retrouver toutes mes réponses pré-remplies (pas tout refaire).
- 🟢 **US-24** En tant que mariée, je veux préciser l'**heure** de la cérémonie pour qu'elle apparaisse sur les invitations. *(v2.1)*
- 🟢 **US-25** En tant que mariée, je veux préciser le **nom du palais/salle** pour qu'il apparaisse sur les invitations (saisi sur la page Invitations après avoir vu les recos). *(v2.1)*
- 🟢 **US-26** En tant qu'utilisatrice, je veux que la section Prestataires ne propose plus de catégories redondantes (coiffure isolée de maquillage, fleuriste isolé de déco, vidéaste isolé de photo). *(v2.1)*
- 🟢 **US-27** En tant qu'utilisatrice, je veux que le **moment** (Journée / Soirée) soit déduit automatiquement de l'heure (≥18h = soirée, 10h–17h30 = journée). *(v2.1)*
- 🟢 **US-28** En tant que mariée, je veux que les **moments forts à capturer** reflètent un mariage marocain (Lebssa fassia, Zaghrouta, Tbarek llah, Tayfor) — pas d'ouverture de bal occidentale. *(v2.1)*
- 🟢 **US-29** En tant que mariée, je veux que les libellés "Nom de la mariée" et "Nom du marié" demandent le **nom complet** (prénom + nom de famille) pour qu'ils apparaissent correctement sur les invitations. *(v2.2)*

## Epic 4 — Moteur de recommandation IA

- 🟢 **US-30** En tant que mariée ayant rempli le questionnaire, je veux recevoir des recommandations classées par score de compatibilité.
- 🟢 **US-31** En tant que mariée, je veux comprendre **pourquoi** chaque prestataire m'est recommandé (raison contextualisée). *(v2.1)*
- 🟢 **US-32** En tant que mariée, je veux voir des **tags** pertinents sur chaque carte (Coup de cœur, Bon plan, Authenticité fassie…). *(v2.1)*
- 🟢 **US-33** En tant que mariée, je veux des **blocs thématiques** (Top picks, Bon plan, Premium, À considérer…) pour explorer plus facilement. *(v2.1)*
- 🟢 **US-34** En tant que mariée, je veux voir mon **profil** synthétisé en haut de la page (style, budget, ambiance…) et comprendre comment il influence les recommandations. *(v2.1)*
- 🟢 **US-35** En tant que mariée, je veux **contacter** le prestataire en un clic via téléphone, WhatsApp ou Instagram. *(v2.1)*
- 🟢 **US-36** En tant que mariée, je veux que le prix affiche "À partir de X DHS" sans plafond effrayant. *(v2.1)*
- 🟢 **US-37** En tant que mariée, je veux que le moteur me propose **3 prestataires maximum par catégorie** (pas une longue liste confuse). *(v2.1)*
- 🟢 **US-38** En tant que mariée, je veux **choisir un prestataire** parmi les recommandations via un bouton "Choisir ce prestataire" (un seul retenu par catégorie, le nouveau choix remplace l'ancien). *(v2.1)*
- 🟢 **US-39** En tant que mariée, je veux qu'un **toast** confirme mon choix avec un lien direct vers "Mes choix". *(v2.1)*
- ⚪ **US-39b** En tant que mariée, je veux pouvoir **sauvegarder** plusieurs prestataires en favoris (différent du choix unique).
- ⚪ **US-39c** En tant que mariée, je veux pouvoir **demander un devis** au prestataire directement depuis la carte.

## Epic 4bis — Mes Choix (sélection finale)

- 🟢 **US-44** En tant que mariée, je veux un onglet **"Mes choix"** dans la navbar (juste après Recommandations) pour retrouver mes prestataires retenus. *(v2.1)*
- 🟢 **US-45** En tant que mariée, je veux mes choix **regroupés par catégorie** (Salle, Traiteur, Photographe…) avec un badge "Choisi". *(v2.1)*
- 🟢 **US-46** En tant que mariée, sur chaque choix je veux voir : nom, gamme, ville, prix, description, et des boutons Appeler / Instagram / Modifier / Retirer. *(v2.1)*
- 🟢 **US-47** En tant que mariée, je veux pouvoir **modifier** un choix : le bouton renvoie vers les recommandations filtrées sur cette catégorie pour comparer les alternatives. *(v2.1)*
- 🟢 **US-48** En tant que mariée, je veux pouvoir **retirer** un choix avec confirmation. *(v2.1)*
- 🟢 **US-49** En tant que mariée, si je n'ai aucun choix, je veux un **état vide** sympa avec un CTA vers les recommandations. *(v2.1)*

## Epic 5 — Prestataires (catalogue)

- 🟢 **US-40** En tant qu'admin, je veux maintenir un catalogue de prestataires par catégorie avec gamme/prix/contact/tags.
- 🟢 **US-41** En tant qu'utilisatrice, je veux que les coordonnées soient au **format marocain réel** (06XX-XX-XX-XX, @handle IG). *(v2.1)*
- 🟢 **US-42** En tant qu'admin, je veux que **Mounia Ramsis** soit classée en Neggafa et non en Wedding Planner. *(v2.1)*
- 🟢 **US-43** En tant qu'admin, je veux retirer les catégories non offertes (Transport, Wedding Planner). *(v2.1)*
- 🟢 **US-43b** En tant qu'utilisatrice, je veux que les catégories obsolètes/supprimées **n'apparaissent pas** dans les filtres frontend. *(v2.1)*
- 🟢 **US-43c** En tant qu'admin, je veux que **Adil Otmani** soit positionné à Fès (orchestre fassi), **Dar Benjelloun Neggafa** sur Casablanca, et confirmer **Majda Benjelloun** à Fès. *(v2.1)*
- 🟢 **US-43d** En tant qu'admin, je veux reclasser **Chocolat de Joie** et **Emeraude by Malika** en Myadi/Tyafer (et non Cake Designer), et **Haj Said Berrada** en Issawa (et non Orchestre). *(v2.1)*
- 🟢 **US-43e** En tant qu'utilisatrice, je veux que la page Prestataires affiche uniquement "À partir de X DHS" (sans plafond "jusqu'à Y"). *(v2.1)*

## Epic 6 — Invitations digitales

- 🟢 **US-50** En tant que mariée, je veux choisir parmi 15 modèles d'invitations signés Ayora.
- 🟢 **US-51** En tant que mariée, je veux que les modèles Pro et Premium soient verrouillés avec un cadenas et redirigent vers l'upgrade.
- 🟢 **US-52** En tant que mariée, je veux que mes **noms du couple** apparaissent automatiquement sur les invitations. *(v2.0)*
- 🟢 **US-53** En tant que mariée, je veux que la **date, l'heure, la ville et le lieu** s'alignent sur mes réponses du questionnaire et apparaissent sur chaque invitation. *(v2.1)*
- 🟢 **US-54** En tant que mariée, je veux des modèles **majestueux et eye-catching** (Or Liquide animé, Caftan Ivoire, Impérial Bordeaux). *(v2.1)*
- 🟢 **US-55** En tant que mariée, je ne veux **pas** de modèles vidéo (UX bloquée). *(v2.1)*
- 🟢 **US-56** En tant que mariée, je veux confirmer chaque envoi avec un récap (invité, email, modèle, date, lieu) avant l'envoi. *(v2.1)*
- 🟢 **US-57** En tant que mariée, je veux recevoir un email d'invitation enrichi avec les vrais noms du couple, date+heure formatées et lieu complet. *(v2.1)*
- 🟢 **US-58** En tant que mariée, je veux saisir le **lieu de la cérémonie** sur la page Invitations (et non pendant le questionnaire), pour que je le choisisse parmi mes recommandations avant de personnaliser mes invitations. *(v2.1)*
- 🟢 **US-59** En tant que mariée, je veux accéder à une bibliothèque de **24 modèles** triés par tier (FREE → PRO → PREMIUM) : Ivory Gold, Minimal Luxury, Soft Calligraphy, Ocean Blush, Lavender Dream pour FREE ; Desert Rose, Garden Elegance, Moroccan Pearl, Zellige Pearl, Palace Cream, Sunset Marrakech, Vintage Postcard, Henna Garden pour PRO ; Royal Black, Emerald Night, Andalusian Touch, Zellige Royal, Or Liquide, Caftan Ivoire, Impérial Bordeaux, Art Déco Onyx, Constellation, Marble Rose Gold pour PREMIUM. *(v2.2)*

## Epic 7 — Invités & RSVP

- 🟢 **US-60** En tant que mariée, je veux ajouter mes invités avec nom/email/téléphone/groupe.
- 🟢 **US-61** En tant qu'invité, je veux recevoir une invitation par email et pouvoir confirmer/décliner.
- 🟢 **US-62** En tant que mariée, je veux voir le statut de chaque invitation (en attente / envoyée / confirmée / déclinée).

## Epic 8 — Tableau de bord (assistant wedding planner)

- 🟢 **US-80** En tant que mariée, je veux un **message de bienvenue personnalisé** avec mon prénom sur le dashboard. *(v2.2)*
- 🟢 **US-81** En tant que mariée, je veux **6 cartes de stats** en haut du dashboard (Recommandations / Mes choix / Invités / Invitations envoyées / Restantes / Budget restant) avec icônes colorées. *(v2.2)*
- 🟢 **US-82** En tant que mariée, je veux une section **"Progression de votre mariage"** avec une barre de progression dorée et 6 étapes vérifiées. *(v2.2)*
- 🟢 **US-83** En tant que mariée, je veux que chaque étape ait **3 états** distincts : Terminé (vert), En cours (or), À faire (gris) — pas juste "fait/pas fait". *(v2.2)*
- 🟢 **US-84** En tant que mariée, je veux un **intervalle de tolérance ±20%** pour les étapes "Invités ajoutés" et "Invitations envoyées", basé sur le `nbInvites` saisi au questionnaire (ex : 250 → [200-300]). *(v2.2)*
- 🟢 **US-85** En tant que mariée, je veux que le **% d'avancement global** soit calculé proportionnellement (in-progress = 0.5, done = 1, todo = 0). *(v2.2)*
- 🟢 **US-86** En tant que mariée, je veux une section **"Vos prochains pas"** avec des CTA contextuels selon mon état réel (questionnaire incomplet → CTA, picks vides → CTA, plan FREE → CTA Pro/Premium…). *(v2.2)*
- 🟢 **US-87** En tant que mariée, je veux voir mes **derniers choix de prestataires** (top 4) avec lien vers Mes choix. *(v2.2)*
- 🟢 **US-88** En tant que mariée, je veux voir un **aperçu des 3 meilleures recommandations** (triées par score) avec lien vers la page complète. *(v2.2)*
- 🟢 **US-89** En tant que mariée, je veux une section **Budget mariage** avec : total, estimé engagé (somme des prixMin des picks), restant. *(v2.2)*
- 🟢 **US-90** En tant que mariée, je veux une section **Invités & invitations** récap en sidebar. *(v2.2)*
- 🟢 **US-91** En tant que mariée, je veux **6 raccourcis rapides** vers les sections principales (Recos, Mes choix, Prestataires, Invités, Invitations, Questionnaire). *(v2.2)*

## Epic 9 — Compte utilisateur (header)

- 🟢 **US-92** En tant qu'utilisatrice, je veux que mon **prénom**, **Déconnexion** et le **badge plan** apparaissent en haut à droite avec une hiérarchie claire (prénom prominent, déconnexion discret, badge en-dessous). *(v2.2)*
- 🟢 **US-93** En tant qu'utilisatrice, je veux que le **badge plan** (Free/Pro/Premium) reste visible **sur toutes les pages** (Tableau de bord, Recommandations, Mes choix, Prestataires, Invités, Invitations, Questionnaire). *(v2.2)*
- 🟢 **US-94** En tant qu'utilisatrice, je veux que le **menu "Mes choix"** soit présent dans la navbar de toutes les pages (juste après Recommandations). *(v2.1)*
- 🟢 **US-95** En tant qu'utilisatrice Premium, je ne veux **pas** voir de bandeau "Passer à Pro" sur le dashboard ; en tant que Pro, je veux voir uniquement le bandeau Premium. *(v2.2)*

## Epic 10 — Conformité prof / pédagogie

- 🟢 **US-70** En tant qu'enseignant, je veux que le code suive le pattern p01-jdbc (DataSource + MySQLDataSource + Database).
- 🟢 **US-71** En tant qu'enseignant, je veux que les servlets suivent le pattern p02-jee (@WebServlet + init() + doGet/doPost).
- 🟢 **US-72** En tant qu'enseignant, je veux voir un fichier `Examples.java` exécutable en standalone pour tester la couche DB.
- 🟢 **US-73** En tant qu'enseignant, je veux voir une interface `Dao<T,K>` documentant l'architecture.
- 🟢 **US-74** En tant qu'enseignant, je veux que les nouveaux endpoints (`/api/picks/*`, `/api/questionnaire/lieu`) suivent strictement le même pattern (PreparedStatement, JsonUtil, session check).  *(v2.1)*
- 🟢 **US-75** En tant qu'enseignant, je veux que la **base SQL** soit migrable progressivement (migration_user_picks.sql, migration_vendors_v4/v5/v6.sql) sans casser les FK. *(v2.1)*

---

## Suivi

| Indicateur | Valeur |
|-----------|--------|
| Total user stories | **74** |
| Livrées (v1 + v2.0 + v2.1 + v2.2) | **72** |
| En backlog | 2 (US-39b favoris, US-39c demande de devis) |
| Epics actifs | 10 |

### Répartition par milestone

- **v1** (initial) : 12 stories (auth, dashboard basique, questionnaire, vendors, invites, RSVP, admin)
- **v2.0** (avril 2026) : 8 stories (plan PRO 149 DHS, 15 templates, dashboard upgrade banners, 60 vendors)
- **v2.1** (mai 2026) : 38 stories (moteur reco IA, fusion catégories, Mes Choix, dashboard reco riche, lieu workflow, contacts réels)
- **v2.2** (mai 2026) : 16 stories (refonte dashboard premium, 3 états progression, intervalle ±20%, 8 nouveaux templates, account header polish, full-name labels)

### Backlog (candidate v2.3)

- **US-39b** Favoris — La mariée peut épingler plusieurs prestataires sans qu'ils soient son "choix retenu"
- **US-39c** Demande de devis — Bouton "Demander un devis" sur chaque carte (avec budget min/max + date + nb invités)
