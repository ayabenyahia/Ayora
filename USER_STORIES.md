# Ayora — User Stories

Backlog complet aligné avec les milestones v1, v2.0 et v2.1.

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
- 🟢 **US-25** En tant que mariée, je veux préciser le **nom du palais/salle** pour qu'il apparaisse sur les invitations. *(v2.1)*
- 🟢 **US-26** En tant qu'utilisatrice, je veux que la section Prestataires ne propose plus de catégories redondantes (coiffure isolée de maquillage, fleuriste isolé de déco, vidéaste isolé de photo). *(v2.1)*

## Epic 4 — Moteur de recommandation IA

- 🟢 **US-30** En tant que mariée ayant rempli le questionnaire, je veux recevoir des recommandations classées par score de compatibilité.
- 🟢 **US-31** En tant que mariée, je veux comprendre **pourquoi** chaque prestataire m'est recommandé (raison contextualisée). *(v2.1)*
- 🟢 **US-32** En tant que mariée, je veux voir des **tags** pertinents sur chaque carte (Coup de cœur, Bon plan, Authenticité fassie…). *(v2.1)*
- 🟢 **US-33** En tant que mariée, je veux des **blocs thématiques** (Top picks, Bon plan, Premium, À considérer…) pour explorer plus facilement. *(v2.1)*
- 🟢 **US-34** En tant que mariée, je veux voir mon **profil** synthétisé en haut de la page (style, budget, ambiance…) et comprendre comment il influence les recommandations. *(v2.1)*
- 🟢 **US-35** En tant que mariée, je veux **contacter** le prestataire en un clic via téléphone, WhatsApp ou Instagram. *(v2.1)*
- 🟢 **US-36** En tant que mariée, je veux que le prix affiche "À partir de X DHS" sans plafond effrayant. *(v2.1)*
- ⚪ **US-37** En tant que mariée, je veux pouvoir **sauvegarder** un prestataire en favoris pour y revenir.
- ⚪ **US-38** En tant que mariée, je veux pouvoir **demander un devis** au prestataire directement depuis la carte.

## Epic 5 — Prestataires (catalogue)

- 🟢 **US-40** En tant qu'admin, je veux maintenir un catalogue de prestataires par catégorie avec gamme/prix/contact/tags.
- 🟢 **US-41** En tant qu'utilisatrice, je veux que les coordonnées soient au **format marocain réel** (06XX-XX-XX-XX, @handle IG). *(v2.1)*
- 🟢 **US-42** En tant qu'admin, je veux que **Mounia Ramsis** soit classée en Neggafa et non en Wedding Planner. *(v2.1)*
- 🟢 **US-43** En tant qu'admin, je veux retirer les catégories non offertes (Transport, Wedding Planner). *(v2.1)*

## Epic 6 — Invitations digitales

- 🟢 **US-50** En tant que mariée, je veux choisir parmi 15 modèles d'invitations signés Ayora.
- 🟢 **US-51** En tant que mariée, je veux que les modèles Pro et Premium soient verrouillés avec un cadenas et redirigent vers l'upgrade.
- 🟢 **US-52** En tant que mariée, je veux que mes **noms du couple** apparaissent automatiquement sur les invitations. *(v2.0)*
- 🟢 **US-53** En tant que mariée, je veux que la **date, l'heure, la ville et le lieu** s'alignent sur mes réponses du questionnaire et apparaissent sur chaque invitation. *(v2.1)*
- 🟢 **US-54** En tant que mariée, je veux des modèles **majestueux et eye-catching** (Or Liquide animé, Caftan Ivoire, Impérial Bordeaux). *(v2.1)*
- 🟢 **US-55** En tant que mariée, je ne veux **pas** de modèles vidéo (UX bloquée). *(v2.1)*
- 🟢 **US-56** En tant que mariée, je veux confirmer chaque envoi avec un récap (invité, email, modèle, date, lieu) avant l'envoi. *(v2.1)*
- 🟢 **US-57** En tant que mariée, je veux recevoir un email d'invitation enrichi avec les vrais noms du couple, date+heure formatées et lieu complet. *(v2.1)*

## Epic 7 — Invités & RSVP

- 🟢 **US-60** En tant que mariée, je veux ajouter mes invités avec nom/email/téléphone/groupe.
- 🟢 **US-61** En tant qu'invité, je veux recevoir une invitation par email et pouvoir confirmer/décliner.
- 🟢 **US-62** En tant que mariée, je veux voir le statut de chaque invitation (en attente / envoyée / confirmée / déclinée).

## Epic 8 — Conformité prof / pédagogie

- 🟢 **US-70** En tant qu'enseignant, je veux que le code suive le pattern p01-jdbc (DataSource + MySQLDataSource + Database).
- 🟢 **US-71** En tant qu'enseignant, je veux que les servlets suivent le pattern p02-jee (@WebServlet + init() + doGet/doPost).
- 🟢 **US-72** En tant qu'enseignant, je veux voir un fichier `Examples.java` exécutable en standalone pour tester la couche DB.
- 🟢 **US-73** En tant qu'enseignant, je veux voir une interface `Dao<T,K>` documentant l'architecture.

---

## Suivi

- Total user stories : **48**
- Livrées (v1 + v2.0 + v2.1) : **46**
- En backlog : 2 (US-37 favoris, US-38 demande de devis)
