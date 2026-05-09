# Ayora — Kanban (état au 2026-05-05)

```
┌──────────────────┬─────────────────┬─────────────────┬───────────────────────┐
│   📋 Backlog     │  🟡 In Progress  │   👀 Review     │  ✅ Done              │
├──────────────────┼─────────────────┼─────────────────┼───────────────────────┤
│ US-39b Favoris   │  (rien)         │  (rien)         │ Tout v1 + v2.0        │
│ US-39c Devis     │                 │                 │ + tout v2.1 + v2.2    │
└──────────────────┴─────────────────┴─────────────────┴───────────────────────┘
```

## Done — v2.2 (livré 2026-05-05)

### Refonte du tableau de bord (assistant wedding planner)
- ✅ Hero burgundy avec **message de bienvenue personnalisé** + pill "Avancement global X%"
- ✅ **6 cartes de stats** dynamiques (Recos / Mes choix / Invités / Invitations envoyées / Restantes / Budget restant) avec icônes thématiques
- ✅ Section "Progression de votre mariage" avec **barre de progression dorée** + 6 étapes
- ✅ **3 états par étape** : Terminé (vert) / En cours (or) / À faire (gris) — couleurs différenciées
- ✅ **Intervalle de tolérance ±20%** sur "Invités ajoutés" et "Invitations envoyées" (ex : 250 → [200-300])
- ✅ Section "Vos prochains pas" avec CTA contextuels
- ✅ Section "Vos derniers choix" + "Vos meilleures recommandations"
- ✅ Sections "Budget mariage" + "Invités & invitations" + "6 raccourcis rapides"

### Zone compte (header) — toutes les pages
- ✅ Hiérarchie visuelle : prénom prominent + Déconnexion discret + **badge plan persistant**
- ✅ Badge Free/Pro/Premium **visible sur toutes les pages** (dashboard, recommandations, mes choix, prestataires, invités, invitations, premium, questionnaire)
- ✅ Helpers `applyAccountHeader()` + `syncPlanBadge()` dans `js/api.js`
- ✅ Plan-aware sur dashboard : Premium → aucune bannière / Pro → Premium uniquement / Free → les deux

### Invitations (8 nouveaux modèles → 24 total)
- ✅ Lot 1 (5 modèles) : **Ocean Blush** (FREE), **Sunset Marrakech** (PRO), **Vintage Postcard** (PRO), **Art Déco Onyx** (PREMIUM), **Constellation** (PREMIUM)
- ✅ Lot 2 (3 modèles) : **Lavender Dream** (FREE), **Henna Garden** (PRO), **Marble Rose Gold** (PREMIUM)
- ✅ Catalogue **réordonné** : 5 FREE → 8 PRO → 11 PREMIUM (FREE en premier, PREMIUM à la fin)

### Questionnaire
- ✅ Labels "Nom complet de la mariée" / "Nom complet du marié" (au lieu de juste "Nom de…")
- ✅ Auto-détection du **moment** (Journée/Soirée) selon l'heure (≥18h = soirée)
- ✅ **Moments forts à capturer** typiquement marocains (Lebssa fassia, Zaghrouta, Tbarek llah, Tayfor)
- ✅ Lieu de cérémonie déplacé vers la page Invitations (saisi après avoir vu les recos)
- ✅ Navbar enrichie + badge plan visible



## Done — v2.1 (livré 2026-05-02)

### Moteur de recommandation
- ✅ Refonte `RecommendationService` — scoring multi-critères (8 dimensions)
- ✅ Nouveau modèle `UserProfile` (synthèse du questionnaire)
- ✅ Génération de tags pertinents (Coup de cœur, Bon plan, Authenticité fassie…)
- ✅ Raisons contextualisées qui citent les réponses du questionnaire
- ✅ Buildblocks : 7 blocs thématiques (Top picks, Bon plan, Chic, Éco, Premium, Priorités, Alternatives)
- ✅ JSON serializers `toJson(reco)` + `profileToJson(profile)`

### API
- ✅ `RecommendationServlet` retourne profile + blocks + counts + categories
- ✅ Filtres avancés en query params (category, gamme, minScore, maxPrice, tag)
- ✅ Fix `JsonUtil.unescapeJson` + `inlineJsonOrString` (plus de double-escape)

### Frontend recommandations
- ✅ Layout 2 colonnes : profil sticky + blocs onglets
- ✅ Cards riches (score ribbon, sub-scores barres, tags pills, raison)
- ✅ Icônes contact unifiées (téléphone / WhatsApp / Instagram en SVG)
- ✅ Suppression "jusqu'à X DHS" (uniquement "À partir de")
- ✅ Suppression carte filtres avancés (les onglets suffisent)
- ✅ Suppression tag "Plebiscite" (signification floue)

### Catégories de prestations
- ✅ Coiffure → Maquillage & Coiffure
- ✅ Fleuriste → Décoration & Fleuriste
- ✅ Vidéaste → Photographe & Vidéaste
- ✅ Transport supprimé
- ✅ Wedding Planner supprimé
- ✅ Mounia Ramsis → Neggafa (Instagram + phone vérifiés)
- ✅ Numéros normalisés au format MA 10 chiffres
- ✅ Instagram en lowercase + `@`

### Questionnaire
- ✅ Section 1 : champ `heureMariage` (input time)
- ✅ Section 1 : champ `lieuMariageNom` (palais/salle)
- ✅ Section 4 : chips reflètent les nouvelles fusions
- ✅ Pré-remplissage des nouveaux champs sur édition

### Invitations
- ✅ Suppression des 3 modèles vidéo
- ✅ Nouveau modèle **Or Liquide** (dégradé doré animé)
- ✅ Nouveau modèle **Caftan Ivoire** (broderies fassi)
- ✅ Nouveau modèle **Impérial Bordeaux** (sceau royal)
- ✅ Pré-remplissage dynamique des aperçus avec données du couple
- ✅ Email enrichi avec date+heure et lieu complets
- ✅ `InvitationServlet` template catalog mis à jour

### Documentation
- ✅ CHANGELOG v2.1
- ✅ RECOMMENDATIONS.md (architecture du moteur)
- ✅ USER_STORIES.md (48 stories, 8 epics)
- ✅ KANBAN.md (ce fichier)

---

## Done — v2.0 (livré il y a quelques jours)

- ✅ Plan PRO 149 DHS entre Free et Premium
- ✅ 15 modèles d'invitations (3 FREE, 5 PRO, 7 PREMIUM dont 3 vidéo)
- ✅ Modèles Zellige Pearl + Zellige Royal
- ✅ Lock overlay sur templates non accessibles
- ✅ Modal "Confirmer & envoyer" avec récap
- ✅ Questionnaire en 6 sections + stepper
- ✅ Saison automatique depuis la date
- ✅ 60 prestataires réels (lots 1 + 2)
- ✅ Compte Premium pour Zakia (samouhzakia2@gmail.com)

## Done — v1 (initial)

- ✅ Auth email/password + sessions
- ✅ Dashboard
- ✅ Questionnaire
- ✅ Catalogue prestataires
- ✅ Invitations basiques
- ✅ Module invités + RSVP
- ✅ Admin pour gérer comptes/plans

---

## Backlog (v2.3 candidates)

- ⚪ **US-39b** Favoris — La mariée peut épingler plusieurs prestataires (sans en faire son "choix retenu")
- ⚪ **US-39c** Demande de devis — Bouton "Demander un devis" sur chaque carte (avec budget min/max + date + nb invités)

---

## Métriques

| Indicateur            | Valeur |
|-----------------------|--------|
| User stories total    | **74** |
| Livrées               | **72** |
| Backlog               | 2      |
| Couverture v2.2       | 100%   |
| Epics actifs          | 10     |
| Catégories actives    | 12     |
| Modèles d'invitations | **24** (5 FREE • 8 PRO • 11 PREMIUM) |
| Prestataires actifs   | ~50    |
| États par étape progress | 3 (todo / in-progress / done) |
| Tolérance invitations | ±20% sur la cible questionnaire |
