# AYORA — Audit produit & plan de transformation UX

> Inventaire complet de l'existant + carte des écrans cibles + plan d'implémentation priorisé pour la transformation produit demandée.

## 1. Existant — frontend

### Pages HTML disponibles (19)

| Page | Rôle actuel | Statut transformation |
|---|---|---|
| `index.html` | Landing publique (hero, features bento, pricing) | ✅ déjà refait dans l'itération précédente |
| `login.html` / `register.html` | Auth | Conserver, harmoniser visuellement |
| `dashboard.html` | Page d'accueil après login | **À refondre** — centre du couple |
| `questionnaire.html` | Onboarding profil mariage | Conserver — devient l'étape « Notre mariage » |
| `mychoices.html` | Picks de la mariée | Conserver, sera intégré dans « Nos décisions » |
| `recommendations.html` | Recommandations IA | Conserver |
| `vendors.html` | Catalogue prestataires | Conserver, améliorer filtres |
| `guests.html` | Gestion invités | Conserver, refondre UX |
| `invitations.html` | Envoi invitations | Conserver |
| `assistant.html` | Page chat plein écran AYORA | Conserver (pipeline déjà stabilisé) |
| `settings.html` | Paramètres compte | Conserver |
| `premium.html` | Page upsell premium | Conserver |
| `admin.html` / `vendor-portal.html` | Back-office | Hors scope |
| `cgu.html`, `mentions.html`, `confidentialite.html`, `contact.html`, `404.html` | Pages légales | Conserver tel quel |

### Pages à créer (transformation)

| Nouvelle page | Rôle | Priorité |
|---|---|---|
| `our-wedding.html` | « Notre mariage » — vision, traditions, priorités du couple | **P0** |
| `budget.html` | « Notre budget » — catégories, engagement, alertes | **P0** |
| `planning.html` | « Notre planning » — timeline jusqu'au jour J | **P0** |
| `compare.html` | « Comparateur » — face-à-face 2-3 prestataires | **P1** |
| `documents.html` | « Documents & traditions » — checklist Maroc + traditions sélectionnées | **P0** différenciant |
| `day-j.html` | « Jour J » — timeline horaire + responsables | P2 reporté |
| `after-wedding.html` | « Après mariage » — clôture sereine | P2 reporté |

## 2. Existant — backend Java

### Servlets (12)

| Servlet | Endpoints | Statut |
|---|---|---|
| `AuthServlet` | `/api/auth/{login,register,logout,me,profile,password,devis-list,rdv-list,…}` | Conserver |
| `QuestionnaireServlet` | `/api/questionnaire` GET/POST + `/lieu` | Conserver, ajouter PATCH des nouvelles données « notre mariage » |
| `VendorServlet` | `/api/vendors` | Conserver |
| `RecommendationServlet` | `/api/recommendations` | Conserver |
| `GuestServlet` | `/api/guests` | Conserver, étendre statuts |
| `InvitationServlet` | `/api/invitations` | Conserver |
| `UserPickServlet` | `/api/picks` | Conserver |
| `SubscriptionServlet` | `/api/subscription` | Conserver |
| `AssistantServlet` | `/api/assistant/{chat,reset,health,suggestion,admin/model}` | **Ne pas toucher** — pipeline stabilisé |
| `AdminServlet`, `VendorPortalServlet` | Back-office | Hors scope |

### Modèles Java (12)

`User`, `UserProfile`, `Vendor`, `VendorCategory`, `QuestionnaireAnswer`, `Recommendation`, `Guest`, `Invitation`, `UserPick`, `Subscription`, `Devis`, `RendezVous`.

## 3. Existant — base MySQL

### Tables disponibles (12)

```
vendor_categories      17 catégories
vendors                ~76 prestataires
users                  ~14 utilisateurs (Hind id=15)
questionnaire_answers  profil mariage (budget, invités, style, traditions partielles)
guests                 invités du couple
invitations            invitations envoyées
subscriptions          plan FREE/PREMIUM/PRO
recommendations        recommendations calculées
demandes_devis         devis demandés
rendez_vous            RDV vendor
user_picks             shortlist mariée (favoris)
_migrations            tracking migrations
```

### Tables à ajouter (transformation)

| Table | Champs principaux | Phase |
|---|---|---|
| `budget_categories` | id, user_id, name, planned_amount, committed_amount, paid_amount, status, notes | **P0** |
| `tasks` | id, user_id, title, description, category, due_date, priority, completed, completed_at | **P0** (planning) |
| `wedding_traditions` | id, user_id, tradition_key, selected, notes | P1 (peut être stocké en TEXT dans questionnaire_answers) |
| `documents_checklist` | id, user_id, doc_key, status, notes | P1 (peut être stocké côté frontend en localStorage initialement) |
| `day_j_timeline` | id, user_id, time, title, responsible_name, notes | P2 reporté |
| `partner_profile` | id, user_id, first_name, role_in_couple | P2 reporté |

**Décision pragmatique** : pour cette session je crée seulement `budget_categories` et `tasks` en MySQL. Les traditions et documents stockés dans des champs JSON/TEXT de `questionnaire_answers` (`traditions_selected`, `priorities_selected`) déjà partiellement présents — extension par ajout de colonnes simples.

## 4. Architecture cible — navigation

```
[Tableau de bord]   ← accueil après login, centre opérationnel
[Notre mariage]     ← vision, traditions, priorités (édition profil couple)
[Budget]            ← suivi multi-catégories
[Invités]           ← gestion invités (existant amélioré)
[Planning]          ← timeline / checklist temporelle
[Prestataires]      ← catalogue + filtres
[Comparateur]       ← face-à-face réel
[Documents]         ← démarches Maroc + traditions
[AYORA]             ← chat
[Profil ▾]          ← menu utilisateur (settings, déconnexion)
```

10 entrées dont 1 menu déroulant. Navigation desktop horizontale, mobile en hamburger ou tab bar.

## 5. Carte des écrans (vue produit complète)

```
PUBLIC
├── index.html             landing
├── login.html / register.html

CONNECTÉ
├── dashboard.html         ★ refondu — centre couple
├── our-wedding.html       ★ NOUVEAU — Notre mariage (vision + traditions + priorités)
├── budget.html            ★ NOUVEAU — Notre budget
├── guests.html            existant amélioré
├── planning.html          ★ NOUVEAU — checklist temporelle
├── vendors.html           catalogue (existant)
├── compare.html           ★ NOUVEAU — comparateur 2-3 prestataires
├── documents.html         ★ NOUVEAU — démarches + traditions
├── recommendations.html   existant
├── mychoices.html         existant (intégré dans dashboard "Nos décisions")
├── invitations.html       existant
├── assistant.html         existant (pipeline stabilisé)
├── settings.html          existant
├── premium.html           existant

PHASE SUIVANTE (préparées, pas implémentées)
├── day-j.html             timeline horaire jour J
├── after-wedding.html     clôture après mariage
├── contracts.html         contrats, devis, acomptes
```

## 6. Vertical slice à démontrer (5 minutes jury)

```
1. Login (15 s)
   → Tableau de bord : Hind, 500 invités, 500 000 DH, mariage à Fès
2. Notre mariage (45 s)
   → Vision + traditions sélectionnées + priorités du couple
3. Budget (60 s)
   → Catégories avec engagement réel, alerte de dépassement
4. Invités (45 s)
   → Total + groupes familiaux + impact d'une évolution
5. Comparateur (60 s)
   → Comparer Dar Benjelloun vs El Farssi (vraies données DB)
6. Documents & traditions (45 s)
   → Différenciateur Maroc : checklist adoul + traditions choisies
7. Planning (30 s)
   → À faire cette semaine
8. AYORA (30 s, si quota dispo)
   → Question budget, réponse contextuelle
```

## 7. Plan d'implémentation — priorisation honnête

### Phase 1 (cette itération, garanti) — socle premium

- **Audit produit** ✅ (ce document)
- **Nav unifiée** : top bar partagée alignée sur la nouvelle architecture
- **Dashboard refondu** : carte d'accueil couple + 4 cartes essentielles (Budget, Invités, Planning, Décisions) + carte AYORA
- **Notre mariage** : page complète vision + traditions + priorités
- **Documents & traditions** : page différenciante (checklist informative + traditions sélectionnées)

### Phase 2 (cette itération, si temps) — fonctionnel

- **Budget** : table SQL + page avec catégories, allocations, statuts
- **Planning** : table SQL `tasks` + page timeline auto-générée selon date du mariage
- **Comparateur** : page avec sélection 2-3 vendors et tableau face-à-face

### Phase 3 (préparée, marquée « phase suivante »)

- Jour J, après-mariage, contrats, plan de table

## 8. Règles strictes pour cette transformation

- ✅ Ne pas toucher au pipeline Gemini (déjà stabilisé)
- ✅ Réutiliser tables et servlets existants quand possible
- ✅ Pas de faux boutons — toute fonctionnalité visible doit faire quelque chose de réel
- ✅ Données venues de MySQL uniquement (pas d'invention)
- ✅ Disclaimers administratifs sur documents (« informatif, vérifier auprès de l'Adoul »)
- ✅ Aucun jargon technique visible (provider, modèle, API, …)
- ✅ Pas d'emojis excessifs, palette serif élégante, fond ivoire

## 9. Fichiers à créer / modifier (inventaire prévisionnel)

**Backend**
- `src/com/ayora/dao/BudgetDao.java` — NEW
- `src/com/ayora/dao/TaskDao.java` — NEW
- `src/com/ayora/model/BudgetCategory.java`, `Task.java` — NEW
- `src/com/ayora/servlet/BudgetServlet.java`, `TaskServlet.java` — NEW
- `src/com/ayora/metier/IAyoraMetier.java`, `AyoraMetier.java` — étendre
- `src/com/ayora/config/AppWiring.java` — wirer les nouveaux DAO

**Frontend**
- `WebContent/our-wedding.html` — NEW
- `WebContent/budget.html` — NEW
- `WebContent/planning.html` — NEW
- `WebContent/compare.html` — NEW
- `WebContent/documents.html` — NEW
- `WebContent/dashboard.html` — refondre
- `WebContent/css/styles.css` — extensions (navbar, cartes, états vides)
- `WebContent/js/ayora-ui.js` — extension (nav active, formatters)

**SQL**
- `sql/migration_budget_categories.sql` — NEW
- `sql/migration_tasks.sql` — NEW

## 10. Limites assumées de cette session

- Pas de système d'invitation du partenaire (sera marqué « extension prévue »)
- Pas de paiement en ligne
- Pas de plan de table interactif
- Pas de mode Jour J (timeline horaire fonctionnelle)
- Pas d'après-mariage actif
- Le comparateur si fait sera basique (tableau 2-3 colonnes statique, pas DnD)

Tout cela est **assumé et marqué dans le code et dans le README final**, pas caché derrière de faux boutons.
