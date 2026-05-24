# Module IA Ayora — Assistante conversationnelle AYORA

> Document de référence pour le rapport de PFA. Décrit le module **AYORA AI Assistant**, son architecture, ses garde-fous, et son chemin d'évolution vers le modèle Gemma 4 E2B-it fine-tuné.

## 1. Rôle dans le produit

L'**Assistante AYORA** est le cœur conversationnel de l'application. Elle aide la future mariée à :

* comprendre comment répartir son budget ;
* organiser un planning par jalons selon sa date de mariage ;
* préparer une checklist (avec confirmation explicite avant tout ajout) ;
* comparer des prestataires **uniquement** ceux remontés par la base AYORA ;
* gérer le stress, demander des informations manquantes, prioriser ;
* converser dans la langue qui lui est naturelle : français, darija arabe, darija latine (Arabizi), ou mélange français/darija.

Elle est accessible à deux niveaux :

1. **Page complète `assistant.html`** — expérience conversationnelle centrale avec aperçu du profil mariage et suggestions cliquables.
2. **Bouton flottant contextuel** — présent sur Dashboard, Recommandations, Mes choix et Prestataires. Il ouvre un panneau latéral et envoie au backend la page d'origine pour des réponses adaptées.

## 2. Architecture

```
Browser
   │
   ├── assistant.html (page chat premium)
   ├── floating widget (drawer) sur les pages métier
   │
   ▼
   js/assistant.js          ─── rendu des bulles, langue/RTL, actions
   js/assistant-floating.js ─── widget réutilisable
   │
   ▼ fetch /ayora/api/assistant/chat
   │
AssistantServlet
   │
   ├── Session HTTP : récupère userId
   ├── AppWiring.getMetier()    : charge le questionnaire + profil
   ├── AppWiring.getAssistant() : appelle AssistantService
   │
   ▼
AssistantService
   ├── detectLanguage(text)  : FR / DAR_AR / DAR_LATIN / MIXED
   ├── detectIntent(text)    : BUDGET / PLANNING / SAFETY_GROUNDING / …
   ├── apply guards          : refus d'invention de prestataires/prix/dispo
   ├── generateAnswer(req)   : réponse multilingue ancrée sur le profil
   ▼
JSON { answer, languageStyle, intent, grounded, suggestedActions, demo }
```

Le profil mariage (budget, invités, date, ville, style, priorités) est lu **côté serveur** depuis la session — le client n'a jamais à embarquer ces données dans la requête. C'est important pour la confidentialité et pour empêcher toute manipulation côté navigateur.

## 3. Mode démonstration (état actuel)

`AssistantService.DEMO_MODE = true`.

Tant que le modèle Gemma fine-tuné n'est pas branché, le service produit des réponses **déterministes** à partir de templates multilingues écrits à la main. Ces templates :

* utilisent les **vraies données** du profil (budget réel, nombre d'invités réel, date réelle) ;
* ne contiennent **aucun nom de prestataire ni prix présenté comme actuel** ;
* refusent toute demande de réservation ou de disponibilité (cf. § Garde-fous).

L'interface affiche un badge **Mode démo** discret en haut de la conversation pour la transparence.

## 4. Détection de langue

Heuristique déterministe (pas un modèle), suffisante pour router vers le bon template :

| Règle | Langue retournée |
|---|---|
| > 60 % de caractères arabes | `DARIJA_AR` |
| Caractères arabes + caractères latins | `MIXED` |
| Tokens Arabizi (`bghit`, `chno`, `9bel`, `m3a`, `dyal`…) | `DARIJA_LATIN` |
| Autrement | `FRENCH` |

La même règle est implémentée côté frontend (`clientSideLangHint`) pour le pré-affichage RTL, mais la langue de référence est toujours celle renvoyée par le serveur.

## 5. Détection d'intention

Classifieur **token-based**, ordre important. La règle de sécurité passe avant tout :

```
SAFETY_GROUNDING ← détection de "demain", "disponible", "réserve", "prix exact", "غدا", "daba"…
COMPARISON      ← "comparer", "vs", "ولا", "wla"
STRESS          ← "stress", "perdue", "حايرة", "ضايعة"
BUDGET          ← "budget", "ميزانية", "dh", "9addach"
CHECKLIST       ← "checklist", "liste", "9aima"
PLANNING        ← "planning", "mois", "9bel"
VENUE / CATERING / NEGAFA / PHOTOGRAPHY / DECORATION / MUSIC
GUEST_MANAGEMENT
TRADITIONS
ONBOARDING
GENERAL (fallback)
```

## 6. Garde-fous anti-hallucination

Implémentés directement dans `AssistantService`, **indépendants du modèle utilisé** :

1. **Aucun nom de prestataire** n'est jamais produit par le modèle. Les noms ne peuvent venir que de `retrievedVendors` injecté depuis la base AYORA par le servlet sur les pages prestataires.
2. **Aucun tarif présenté comme actuel.** Les ordres de grandeur pédagogiques (« ~300 DH/invité ») et les ratios (« 50 % salle + traiteur ») sont permis car ils sont des notions générales, pas des prix commerciaux.
3. **Aucune garantie de disponibilité.** Toute demande contenant « demain », « samedi », « disponible », « متوفر », « daba » déclenche systématiquement le branchement `SAFETY_GROUNDING` qui répond par un message expliquant qu'il faut consulter les données réelles d'AYORA.
4. **Aucune réservation.** Toute demande contenant « réserve », « hjz », « حجز » est traitée comme SAFETY_GROUNDING.
5. **Aucune modification automatique** de budget ou de checklist. Quand l'intention est `BUDGET` ou `CHECKLIST`, `requiresConfirmation = true` est renvoyé dans la réponse, et le frontend doit demander confirmation à l'utilisatrice avant tout side-effect.
6. **Comparaison vide → refus.** Si l'intention est `COMPARISON` et que `retrievedVendors` est vide, le service refuse de comparer plutôt que d'inventer.

## 7. Endpoints HTTP

| Méthode | Route | Rôle |
|---|---|---|
| `POST` | `/api/assistant/chat` | Soumettre un message. Body : `{ message, currentPage?, languageHint? }`. Renvoie la réponse structurée. |
| `GET` | `/api/assistant/suggestion` | Conseil du jour pour la carte dashboard. Déterministe à partir du nombre de jours restants. |
| `GET` | `/api/assistant/health` | Statut + flag `demoMode`. Utile pour la démo et le monitoring. |

Toutes les routes refusent l'anonyme avec `401` (cohérent avec les autres servlets Ayora).

## 8. Chemin vers Gemma fine-tuné

Le service `AssistantService` est volontairement conçu comme une **boundary** unique. Le jour où le modèle Gemma 4 E2B-it fine-tuné est disponible :

1. Créer une classe `GemmaAssistantService` qui implémente la même surface publique que `AssistantService` (méthode `generateAnswer(AssistantRequest)`).
2. À l'intérieur, sérialiser le contexte (`weddingProfile`, `retrievedVendors`, `currentPage`) en payload JSON et appeler l'endpoint d'inférence du modèle.
3. **Conserver les garde-fous** (`SAFETY_GROUNDING`, `requiresConfirmation`) en post-traitement, même si le modèle a déjà été entraîné pour les respecter — ceinture + bretelles.
4. Échanger l'instance dans `AppWiring.ASSISTANT = new GemmaAssistantService(...)`. Aucun changement requis dans `AssistantServlet`, `assistant.js` ou `assistant.html`.

Le format de payload attendu par le futur endpoint Gemma est documenté dans la spec du module IA (cf. brief PFA section 13).

## 9. Fichiers du module

| Fichier | Rôle |
|---|---|
| `src/com/ayora/metier/AssistantService.java` | Détection de langue + intent, génération de réponses, garde-fous. Tient en un seul fichier pour rester lisible et défendable. |
| `src/com/ayora/servlet/AssistantServlet.java` | Endpoint HTTP. Récupère le profil mariage côté session, attache éventuellement les top recommandations comme contexte de grounding. |
| `src/com/ayora/config/AppWiring.java` | Wire `AssistantService` comme instance unique. |
| `WebContent/css/assistant.css` | Styling chat premium (bulles, indicateur de frappe, badge démo, bouton flottant, drawer). |
| `WebContent/js/assistant.js` | Client : envoi de messages, rendu des bulles avec actions, gestion RTL. |
| `WebContent/js/assistant-floating.js` | Widget réutilisable (FAB + drawer) à inclure sur n'importe quelle page. |
| `WebContent/assistant.html` | Page chat principale. |
| `WebContent/dashboard.html` | Carte assistante + conseil du jour + bouton flottant ajoutés. |
| `WebContent/recommendations.html` `mychoices.html` `vendors.html` | Nav entry + bouton flottant contextuel ajoutés. |

## 10. Points défendables devant le jury

* **Architecture propre** : `AssistantService` est la seule classe métier ajoutée, avec une boundary explicite pour le futur Gemma. Aucune dette technique introduite.
* **Honnêteté académique** : le badge « Mode démo » et le drapeau `demo: true` dans chaque réponse rappellent que le modèle n'est pas encore là. Aucun mensonge.
* **Sécurité IA** : 6 garde-fous documentés, démontrables sur scénario réel (« donne-moi le numéro d'un traiteur pas cher » → refus motivé).
* **Multilinguisme** : 4 styles linguistiques supportés (FR, darija arabe, darija latin, mélange), avec RTL automatique. Aligné avec le dataset AYORA construit en parallèle pour le fine-tuning Gemma.
* **Réutilisation** : le widget flottant est **un seul fichier** qui sait s'auto-installer sur n'importe quelle page via un attribut `data-ayora-page`. Bonne ingénierie.
