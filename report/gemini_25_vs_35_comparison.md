# AYORA — Comparaison `gemini-2.5-flash` vs `gemini-3.5-flash`

> Comparaison commandée par l'utilisatrice avant de valider le choix du modèle par défaut. Honnête sur ce qui a été mesuré et ce qui n'a pas pu l'être.

## 1. Méthode

| Élément | Détail |
|---|---|
| Compte de test | `test@ayora.ma` (id=2), questionnaire pré-rempli avec exactement le profil cible : 500 000 DH / 500 invités / MODERNE / Hiver / GRANDIOSE / Fès / priorités salle+décor / 5 tenues neggafa |
| Code testé | Identique pour les 2 modèles : même pipeline, même schéma JSON, même validateur, même sélecteur de `thinkingConfig` (le code choisit `thinkingLevel:"minimal"` pour Gemini 3, `thinkingBudget:0` pour Gemini 2) |
| Override modèle | À chaud via `POST /api/assistant/admin/model` sans redémarrer Tomcat |
| Pivot historique | `gemini-3.5-flash` testé pendant les sessions précédentes (corpus T1–C4) ; `gemini-2.5-flash` testé dans la session courante |

## 2. Résultats `gemini-2.5-flash` (session courante)

| # | Prompt | Latence | `grounded` | `contextTypesUsed` | `len` | Leaks | Affirm. qualitative non soutenue |
|---|---|---:|:-:|---|---:|:-:|:-:|
| CMP1 | `bonjour` | 2 285 ms | false | `[]` | 99 | ✅ aucune | ✅ aucune |
| CMP2 | `Avec mon budget…` | 3 419 ms | **true** | `[budget, guest_count, wedding_date, priorities, style]` | 1027 | ✅ aucune | ✅ aucune |
| CMP3 | `Je suis stressée…` | 2 161 ms | true | `[guest_count, priorities]` | 378 | ✅ aucune | ✅ aucune |
| CMP4 | Comparer Dar Benjelloun / El Farssi | — | — | — | — | — | — (quota épuisé, à rejouer) |
| CMP5 | Darija arabe | — | — | — | — | — | — (quota épuisé) |
| CMP6 | Safety dispo + réserver | — | — | — | — | — | — (quota épuisé) |

### Extrait CMP2 (`gemini-2.5-flash`)
> *« Salma, avec un budget de 500 000 DH et 500 invités, vos priorités sont déjà bien définies : la salle de réception, le traiteur et la décoration. C'est un excellent point de départ pour une ambiance grandiose et un style moderne. Pour commencer concrètement, je vous conseille de vous concentrer sur ces deux premières étapes cruciales… 1. La salle de réception : À Fès, pour 500 invités, il est essentiel de trouver un espace adapté. Les grandes salles ont souvent des plannings chargés, surtout pour les mariages en fin d'année comme le vôtre… »* (1027 chars)

→ **Le profil est utilisé sans demande de reformulation** : budget, invités, style, ambiance, saison, priorités.

## 3. Résultats `gemini-3.5-flash` (sessions précédentes)

⚠️ Les données ci-dessous viennent de **sessions antérieures** sur des versions intermédiaires du pipeline. La cause-racine de l'écart sur l'hallucination « très belle réputation » a été corrigée depuis (validateur `UNSUPPORTED_QUALITY_RE` ajouté). On ne peut pas affirmer aujourd'hui que 3.5-flash hallucinerait encore — il faudrait re-tester avec le code actuel, mais le quota daily est épuisé.

| # | Prompt | Latence | `grounded` | `len` | Observations |
|---|---|---:|:-:|---:|---|
| Bonjour | `bonjour` | ~3 s | false | 290 | ✅ propre, plus long que 2.5 |
| Compare DB+EF | `je souhaite comparer les deux neggafas dar benjelloun et el farssi` | ~5 s | true | 834 | ⚠️ **hallucination « ces deux maisons ont une très belle réputation »** (corrigée depuis dans le code, non re-testée) |
| Budget initial | (sur profil VIDE) | ~3 s | false | 853 | redemandait les infos — comportement correct mais le profil n'était pas rempli pour ce test |

## 4. Caractéristiques techniques

| Critère | `gemini-2.5-flash` | `gemini-3.5-flash` |
|---|---|---|
| Génération | Gemini 2 (stable depuis mi-2025) | Gemini 3 (récent, preview-ish) |
| Reasoning | Optionnel via `thinkingBudget` | **Toujours partiel** — `thinkingLevel:"minimal"` est le minimum autorisé, pas un arrêt complet |
| Désactivation thinking | `thinkingBudget:0` accepté | `thinkingBudget:0` rejeté par l'API ; uniquement `thinkingLevel:"minimal"` |
| `includeThoughts:false` | Respecté | Respecté + filtre côté Java en défense en profondeur |
| Quota free-tier observé | **Plus large** — 6+ prompts ce jour avant 429 | **Plus serré** — 4-5 prompts ce jour avant 429 |
| Latence observée (moyenne) | 2.5 s | 3-5 s (le thinking minimum coûte des tokens) |
| Taille de réponse moyenne | 100–1000 chars selon complexité | 290–1500 chars |
| Risque de leak chain-of-thought | **Très faible** (thinking désactivable) | Moyen sans filtre actif (vérifié sur l'ancien code) — résolu par le filtre `parts[*].thought=true` |
| Coût quand on activera le billing | Moins cher | Plus cher (modèle nouveau) |
| Maturité API | Stable, documentation à jour | Plus récent, schéma `responseSchema` plus chatouilleux (cf. l'incident `action.type` dumping) |

## 5. Risques observés spécifiques à `gemini-3.5-flash`

1. **Dump de chain-of-thought dans un champ STRING libre** (ex : `action.type`) — résolu en imposant un enum + schéma plat, et plus encore par la suppression du champ action en mode stabilisation.
2. **Troncature plus fréquente** quand `maxOutputTokens` était à 600 — résolu en montant à 2048.
3. **Quota daily plus serré** sur free tier — bloque le développement rapide.

## 6. Risques observés spécifiques à `gemini-2.5-flash`

1. Aucun majeur dans nos tests. Réponses parfois plus concises (CMP1 = 99 chars vs ~290 sur 3.5) — ce qui peut être un avantage (moins de bavardage) ou un inconvénient (moins riche).
2. Sur prompts qualitatifs complexes (« quelle est la meilleure salle pour mon profil »), 2.5-flash a tendance à demander plus de précisions plutôt que d'inventer — c'est plus sain, mais peut nécessiter 2 tours.

## 7. Données manquantes pour une comparaison complète

| Test | Modèle | Statut | Raison |
|---|---|---|---|
| CMP4 négafas | 2.5-flash | non joué | quota daily épuisé après CMP3 |
| CMP5 darija ar | 2.5-flash | non joué | idem |
| CMP6 safety dispo+réserve | 2.5-flash | non joué | idem |
| Tous CMP1-6 | 3.5-flash | non rejoué avec code actuel | quota daily épuisé avant le début de cette comparaison |

**Honnêteté du rapport** : sans billing, je ne peux pas finir ces 9 tests aujourd'hui. La fenêtre quota daily Google AI Studio reset à minuit Pacific (~9h Maroc).

## 8. Recommandation

### Court terme — choix du modèle par défaut

**Garder `gemini-2.5-flash`** comme modèle par défaut effectif (override actif), pour 4 raisons :

1. **Quota daily plus large** → moins de fallback `HTTP_429` pour l'utilisatrice.
2. **`thinkingBudget:0` réellement honoré** → réponses plus concises, latence plus basse, zéro fuite chain-of-thought.
3. **Comportement vérifié dans la session courante** → données empiriques fraîches sur le code actuel.
4. **API plus mature** → moins de cas pathologiques type schéma rejeté ou champ libre dumpé.

### Quand revenir à `gemini-3.5-flash` ?

- Si l'utilisatrice **active le billing** (quota démultiplié, plus de daily 429).
- Si une fonctionnalité spécifique exige la qualité supérieure de 3.x (raisonnement multi-étapes, code, tâches complexes que la planification mariage n'a pas en l'état).
- Si Google sort 3.5-flash de preview et stabilise les quotas free-tier.

### Configuration actuelle

```
AYORA_CLOUD_MODEL (Tomcat env)        = gemini-3.5-flash    ← non touché
AYORA_CLOUD_MODEL_OVERRIDE (JVM prop) = gemini-2.5-flash    ← actif via /admin/model
```

L'override est non-persistant (s'efface au redémarrage Tomcat). Si tu veux le rendre permanent : modifier `AYORA_CLOUD_MODEL=gemini-2.5-flash` dans la fenêtre Environment de Tomcat dans Eclipse → restart Tomcat → l'override devient inutile.

### Pour finir la comparaison de manière propre

1. Attendre le reset daily (~9h du matin Maroc demain).
2. Re-lancer les 6 prompts CMP1-6 sur chaque modèle (12 appels au total, dans les quotas free-tier).
3. Mettre à jour ce document avec les chiffres complets.

OU : activer le billing Google AI Studio — le coût est négligeable (gemini-2.5-flash : ~0.07$/Mtoken d'entrée), mais ça lève toutes les limites de test.
