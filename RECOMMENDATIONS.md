# Moteur de recommandation Ayora

Ce document décrit l'architecture du moteur de recommandation après la refonte v2.1.

## Architecture (Service Layer pattern)

```
QuestionnaireAnswer (form data)
        |
        v  buildUserProfile()
UserProfile (abstraction métier)
        |
        v  scoreVendor()         (pour chaque vendor)
Recommendation { score, sub-scores, tags, raison }
        |
        v  buildBlocks()
{ topPicks, prioritePicks, bestValue, mostChic, economic, premium, alternatives }
```

Tout passe par `RecommendationService` (un seul point d'entrée). La servlet `RecommendationServlet` se contente de wrapper le service, lire la session, parser les query params et sérialiser la sortie en JSON.

## Critères de scoring

| Critère       | Pts | Source                                                    |
|---------------|-----|-----------------------------------------------------------|
| Budget        | 30  | Prix vs budget alloué par catégorie + flexibilité         |
| Luxe / Gamme  | 20  | gamme du vendor vs niveau de luxe demandé                 |
| Style         | 20  | match des tags vendor avec style + ambiance + thème       |
| Préférences   | 20  | préférences spécifiques par catégorie (musique, photo…)   |
| Priorité      | 10  | priorité utilisateur sur la catégorie du vendor (1-5)     |
| Popularité    | +5  | rating × volume d'avis                                    |
| Mood          | +5  | mots-clés section 6 du questionnaire                      |
| Culturel      | +5  | bonus fassi/heritage si profil traditionnel               |

Score final ramené à [0, 100], arrondi à 0.1 près.

## Estimation du budget par catégorie

Pourcentages calibrés sur un mariage marocain à Fès (post-fusion v4) :

| Catégorie               | %        |
|-------------------------|----------|
| Salle                   | 25%      |
| Traiteur                | 30% / nb invités (par personne) |
| Neggafa                 | 10%      |
| Photographe & Vidéaste  | 9%       |
| Décoration & Fleuriste  | 10%      |
| Maquillage & Coiffure   | 5%       |
| Orchestre               | 6%       |
| DJ / Issawa             | 4%       |
| Cake / Myadi            | 3%       |
| Hennaya                 | 1%       |

Ajustement ±20% selon la priorité utilisateur de la catégorie.

## Tags

Les tags sont générés à partir des sub-scores et des caractéristiques du vendor :

- **Coup de cœur** — score >= 85 et sub-scores Budget >= 80% et Luxe >= 80%
- **Bon rapport qualité/prix** — sub-scores Budget >= 85% et Luxe >= 70% et Préf >= 70%
- **Choix luxe** — sub-score Luxe >= 95% et gamme PREMIUM
- **Petit budget** — gamme ECONOMIQUE et sub-score Budget >= 90%
- **Style aligné** — sub-score Style >= 85%
- **Préférence exacte** — sub-score Préférences >= 95%
- **Très bien noté** — popularité >= 80% et nb_avis >= 50
- **Authenticité fassie** — tags fassi/heritage et style TRADITIONNEL
- **Esprit moderne** — tags moderne/tendance et style MODERNE
- **Drone inclus** — vendor Photo avec tag "drone"
- **Catégorie prioritaire** — la première catégorie dans `topCategoryIds`
- **Hors budget** — sub-score Budget < 40%
- **Format intime** — guestSize INTIME et tags intime/petit
- **Grand événement** — guestSize TRES_GRAND et tags grand/complet

## Blocs

| Bloc            | Critère                                                      | Limite |
|-----------------|--------------------------------------------------------------|--------|
| topPicks        | score >= 70                                                  | 12     |
| prioritePicks   | catégorie ∈ top 2 prioritaires de l'utilisateur              | 8      |
| bestValue       | sub-budget >= 80% et score >= 55 et gamme eco/moyen          | 8      |
| mostChic        | gamme PREMIUM et score >= 55                                 | 8      |
| economic        | gamme ECONOMIQUE et score >= 50                              | 8      |
| premium         | gamme PREMIUM et score >= 70                                 | 6      |
| alternatives    | score 35-55 (pour comparaison)                               | 6      |

## Filtres avancés (query params sur GET /api/recommendations)

- `category` — nom_fr exact d'une catégorie
- `gamme` — ECONOMIQUE / MOYEN / PREMIUM
- `minScore` — 0..100
- `maxPrice` — DHS, filtre `prix_min <= maxPrice`
- `tag` — recherche substring dans la liste de tags

## Pattern conformité prof

- Service Layer avec un seul point d'entrée (`RecommendationService`)
- Tri par sélection (algorithme du cours)
- JDBC pur via `RecommendationDao` (PreparedStatement, pas de Hibernate)
- POJO avec constructeurs/getters/setters/toString
- Pas de framework lourd : Java 17 + Jakarta Servlet 5
