# Ayora — Kanban (état au 2026-05-02)

```
┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
│   📋 Backlog    │  🟡 In Progress  │   👀 Review     │  ✅ Done        │
├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
│ US-37 Favoris   │  (rien)         │  (rien)         │ Tout v1 + v2.0  │
│ US-38 Devis     │                 │                 │ + tout v2.1     │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┘
```

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

## Backlog (v2.2 candidates)

- ⚪ **US-37** Favoris — La mariée peut épingler des prestataires pour y revenir
- ⚪ **US-38** Demande de devis — Bouton "Demander un devis" sur chaque carte

---

## Métriques

| Indicateur            | Valeur |
|-----------------------|--------|
| User stories total    | 48     |
| Livrées               | 46     |
| Backlog               | 2      |
| Couverture v2.1       | 100%   |
| Catégories actives    | 12     |
| Modèles d'invitations | 15     |
| Prestataires actifs   | ~50    |
