# IA Ayora — Moteur de recommandation personnalisée

> Document à montrer au professeur. Tout est local, déterministe et explicable.

## Vue d'ensemble

L'IA Ayora prend en entrée le **profil d'une mariée** (issu du questionnaire) et lui propose les **3 meilleurs prestataires par catégorie** (salle, traiteur, neggafa, photographe, …). Elle attribue un **score de compatibilité** entre 0 et 100 % à chaque prestataire, en comparant 6 critères pondérés.

- **Pas de LLM, pas d'API externe, pas de clé d'API.**
- Java pur, code lisible, déterministe.
- Algorithme : moyenne pondérée de scores de similarité par feature, équivalent d'un k-NN avec k = 3 par classe (la classe = la catégorie de service). On parle d'IA *symbolique* / *à base de règles*, pas de machine learning.

## Où se trouve la logique

| Fichier | Rôle |
|---|---|
| **`src/com/ayora/service/AyoraRecommendationEngine.java`** | **Le moteur**. Tout l'algorithme tient ici : construction du profil, scoring, top-3 par catégorie, badges, phrases d'explication. |
| `src/com/ayora/model/UserProfile.java` | Le profil utilisateur dérivé du questionnaire. |
| `src/com/ayora/model/Recommendation.java` | Une recommandation enrichie : score global, 6 sous-scores, badges, phrase d'explication, données du vendor. |
| `src/com/ayora/service/RecommendationService.java` | Couche de coordination : appelle le moteur, persiste les résultats, sérialise en JSON. |
| `src/com/ayora/servlet/RecommendationServlet.java` | Endpoint HTTP `GET /api/recommendations` qui renvoie le payload au frontend. |
| `WebContent/recommendations.html` | UI : affiche les cartes par catégorie + bouton "Voir plus" + sous-scores visibles. |

## Les 6 critères et leurs poids

```
scoreFinal = budgetMatch    × 0.30    (30 %)
           + styleMatch     × 0.25    (25 %)
           + cityMatch      × 0.15    (15 %)
           + guestCountMatch× 0.15    (15 %)
           + luxuryMatch    × 0.10    (10 %)
           + qualityMatch   × 0.05    ( 5 %)        → ramené sur 100
```

Chaque sous-score est dans `[0, 1]`. Le score final est dans `[0, 100]`.

| Critère | Mesure |
|---|---|
| **budgetMatch** | Le prix du prestataire tient-il dans le budget alloué à sa catégorie ? Calibré par catégorie : 25 % du budget pour la salle, 30 % pour le traiteur (par invité), 9 % pour le photographe, etc. |
| **styleMatch** | Compare les `tags` du prestataire à `style + ambiance + thème couleur` du mariage. Ratio de matchs textuels. |
| **cityMatch** | Même ville → 1.0 ; même région → 0.55–1.0 ; ailleurs → 0.20–0.85. La pénalité dépend de la **tolérance hors-ville** indiquée dans le questionnaire (slider 1–5). |
| **guestCountMatch** | Capacité du prestataire (déduite des tags `intime/petit/moyen/grand/très-grand`) vs nombre d'invités. Neutre (1.0) pour les catégories qui ne dépendent pas de la capacité (photo, makeup, …). |
| **luxuryMatch** | Table de correspondance gamme `(ECONOMIQUE/MOYEN/PREMIUM)` ↔ niveau de luxe demandé `(ECONOMIQUE/MOYEN/PREMIUM/ULTRA_LUXE)`. |
| **qualityMatch** | `0.7 × (rating−3)/2 + 0.3 × min(nbAvis/100, 1)`. Combine la note moyenne et le volume d'avis. |

## Pipeline complet

```
Questionnaire (HTML)                                                                  
   │                                                                                  
   ▼                                                                                  
QuestionnaireServlet → INSERT/UPDATE questionnaire_answers                            
   │                                                                                  
   ▼                                                                                  
AyoraRecommendationEngine.buildUserProfile(questionnaireAnswer)                       
   → UserProfile {budget, style, ambiance, ville, nbInvités, niveauLuxe,             
                  priorités, services demandés, tolérance hors-ville,…}              
   │                                                                                  
   ▼                                                                                  
1. Filtrer les vendors aux SEULES catégories cochées dans le questionnaire           
   (si la mariée n'a pas coché "salle", on ne propose AUCUNE salle)                  
2. Pour chaque vendor restant : engine.scoreVendor(vendor, profile)                  
   → calcule les 6 sous-scores → score final → badges → phrase                       
3. engine.topPerCategory(scoredList) → garde les 3 meilleurs par catégorie           
   │                                                                                  
   ▼                                                                                  
RecommendationServlet → JSON                                                          
   │                                                                                  
   ▼                                                                                  
recommendations.html → affichage cartes + sous-scores + badges + raison              
```

## Exemple d'explication automatique

> *« Excellente correspondance : ce prestataire est recommandé car il correspond à votre budget, à votre style chic traditionnel, à votre ville et à votre nombre d'invités. »*

La phrase est générée mécaniquement à partir des 6 sous-scores : si `scoreBudget ≥ 70` → ajoute *"à votre budget"*, si `scoreStyle ≥ 70` → ajoute *"à votre style chic traditionnel"*, etc. Aucune génération de texte par LLM.

## Pourquoi 3 par catégorie ?

La mariée choisit **un seul** prestataire par catégorie. Lui en proposer 3 lui donne **le meilleur match + 2 alternatives proches**, sans la noyer sous 50 options. C'est l'analogue d'un k-NN avec **k = 3 par classe**, où la "classe" est la catégorie de service.

Le bouton **"Voir plus"** sur chaque catégorie permet d'élargir à toutes les recommandations triées par score, sans drawer ni modale (les cartes apparaissent en place dans la grille).

## Filtrage par services demandés

À la **section 4 du questionnaire**, la mariée coche les services qu'elle veut réserver (salle, traiteur, neggafa, photographe, makeup, décoration, gâteau, hennaya, myadi, animations…).

Le moteur **ne propose que des prestataires dans ces catégories**. Exemple : si la mariée organise son mariage **à la maison** (`lieuCeremonie = DOMICILE`), le moteur retire automatiquement la catégorie SALLE et **aucune salle ne sera recommandée**, même si elle a oublié de la décocher.

## Tolérance hors-ville

Le slider **"Ouverture hors-ville" (1 à 5)** dans la section 6 module la pénalité du `cityMatch` :

| Tolérance | Même ville | Même région | Autre ville |
|---|---|---|---|
| 1 (strict) | 1.00 | 0.56 | 0.21 |
| 2 | 1.00 | 0.67 | 0.37 |
| 3 (par défaut) | 1.00 | 0.78 | 0.53 |
| 4 | 1.00 | 0.89 | 0.69 |
| 5 (ouvert) | 1.00 | 1.00 | 0.85 |

Une mariée qui accepte un prestataire de Casablanca ou Marrakech mettra **5** ; une mariée qui veut **strictement Fès** mettra **1**.

## Comment expliquer ça au professeur

> *"L'IA Ayora compare le profil de la mariée à chaque prestataire sur 6 critères normalisés. Chaque critère donne un score entre 0 et 1. On en fait une moyenne pondérée — les poids reflètent l'importance métier (le budget pèse 30 %, la qualité 5 %). On obtient un score final entre 0 et 100. On garde ensuite les 3 meilleurs prestataires par catégorie de service.*
>
> *Tout est calculé en local, en Java pur, sans aucune API externe ni LLM. Le code est déterministe : pour le même profil et les mêmes prestataires, on obtient toujours le même score. Et surtout, c'est entièrement explicable : tous les sous-scores sont affichés sur la card, donc je peux justifier au pixel près pourquoi un prestataire est recommandé ou non."*

## Données alimentant l'IA (DB)

- Table `vendors` : nom, ville, gamme, prix_min/max, tags, rating, nb_avis, instagram, phone.
- Table `vendor_categories` : 12 catégories actives.
- Table `questionnaire_answers` : réponses brutes ; `notes_speciales` contient un JSON enrichi (services demandés, tolérance hors-ville, mariage mixte/séparé, langue, sensibilité halal, …).

## Évolutions possibles

- Apprentissage à partir des `picks` réels (collaborative filtering simple).
- Pondération adaptative selon la priorité que la mariée a mise sur chaque catégorie.
- Mise à jour du `qualityMatch` avec un score calculé à partir des avis textuels (NLP basique).

Aucune de ces évolutions n'est nécessaire pour la version actuelle.
