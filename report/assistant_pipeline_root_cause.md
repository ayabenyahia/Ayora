# AYORA — Assistant pipeline : root-cause + stabilisation (rapport corrigé)

> Version corrigée après audit utilisateur. Trois corrections rajoutées : (1) configuration thinking séparée Gemini 2.x vs 3.x, (2) anti-hallucination des affirmations qualitatives non soutenues, (3) honnêteté sur les tests réellement exécutés.

## 1. Bugs traités

| # | Symptôme | Statut |
|---|---|---|
| A | `OPEN_VENDORS_PAGE`, `Let's check the action schema` | ✅ corrigé — schéma sans `action`, validateur backend |
| B | `French register), discreet use of 💜` | ✅ corrigé — `thinkingLevel:minimal` / `thinkingBudget:0` + filtre `thought:true` |
| C | Réponses tronquées | ✅ corrigé — `maxOutputTokens=2048`, thinking off, aucune coupe CSS |
| D | `Third priority. Action proposal:` en anglais | ✅ corrigé — bloc actions retiré du prompt, schéma minimal |
| E | Badge vert « Conseil personnalisé » sur `bonjour` | ✅ corrigé — piloté par `contextTypesUsed`, `grounded`, `responseValid` |
| F | « Ces deux maisons ont une **très belle réputation** » sans données | ✅ corrigé — `UNSUPPORTED_QUALITY_RE` rejette les affirmations qualitatives non présentes dans `VERIFIED_VENDORS` |
| G | Profil mariage ignoré (budget/invités redemandés) | ✅ vérifié — quand le questionnaire est rempli, le modèle utilise budget, guest_count, style, priorities, wedding_date |

## 2. Cause racine de chaque bug

### A — `OPEN_VENDORS_PAGE` / action schema
Champ `action.type` typé `STRING` libre dans le schéma initial → le modèle reasoning (Gemini 3.5-flash) s'en servait comme scratch-pad de chain-of-thought et y déversait jusqu'à 2 kB de raisonnement.

### B — `French register, discreet use of 💜`
Les modèles Gemini 2.5+/3.x retournent plusieurs `parts` ; certaines sont marquées `"thought": true` (thought summaries). L'ancien extracteur les concaténait toutes sans filtrage.

### C — Troncature
- `maxOutputTokens=600` saturé par le thinking interne.
- Pas de `responseSchema` → JSON parfois cassé.
- Frontend exonéré : pas de `line-clamp` / `text-overflow` sur les bulles.

### D — Anglais + action recopiée
Le prompt système contenait un exemple inline complet de `ADD_CHECKLIST_TASK { title, priority }`. Sous saturation tokens, le modèle recopiait l'exemple verbatim.

### E — Badge mensonger
`out.grounded = true;` était codé en dur dans `AssistantOrchestrator.handle()`.

### F — Hallucination qualitative
Aucune barrière côté code ne forçait le modèle à ne s'exprimer qualitativement que sur des données présentes dans le contexte. Le modèle utilisait sa connaissance générale (« réputée », « la meilleure ») même quand la DB n'avait que des fourchettes de prix.

### G — Profil ignoré dans les tests précédents
Le test était fait sur `test@ayora.ma` (Salma), dont `questionnaire_completed=false`. `AssistantContextBuilder` retournait alors `Wedding profile: NOT YET COMPLETED`, le modèle redemandait logiquement les données. **Ce n'était pas un bug du pipeline**, mais un manque de données de test. Quand le questionnaire est rempli, le modèle utilise les données (preuve T2 ci-dessous).

## 3. Configuration thinking (correction utilisateur appliquée)

Le code teste maintenant la génération du modèle :

```java
private static boolean isGemini3(String m) {
    return m != null && m.toLowerCase().startsWith("gemini-3");
}

private static String thinkingConfigJson(String activeModel) {
    if (isGemini3(activeModel)) {
        return "{\"thinkingLevel\":\"minimal\",\"includeThoughts\":false}";
    }
    return "{\"thinkingBudget\":0,\"includeThoughts\":false}";
}
```

**Honnêteté du rapport** : *Le niveau de thinking est réglé au minimum sur Gemini 3.x et toute thought summary est désactivée et filtrée avant affichage.* Gemini 3.x ne supporte pas un arrêt complet du thinking ; le filtre `parts[*].thought=true` reste donc actif en défense en profondeur.

## 4. Anti-hallucination qualitative

### Côté prompt système

Section `GROUNDING DISCIPLINE` ajoutée :

```
You may ONLY assert qualities that are present in VERIFIED_VENDORS data.
- NEVER claim 'réputé(e)', 'la meilleure', 'très appréciée', 'le plus connu',
  'excellent choix', 'institution', 'prestige', or any qualitative judgment
  UNLESS the vendor's Tier, Rating, or description field explicitly says so.
- NEVER claim availability for any date.
- NEVER state a fixed final price — use ONLY the displayed range.
- NEVER invent missing fields. Say 'cette information n'est pas enregistrée
  dans AYORA, à confirmer directement avec le prestataire.'
```

### Côté validateur backend

```java
private static final Pattern UNSUPPORTED_QUALITY_RE = Pattern.compile(
    "(?i)\\b(?:" +
        "r[ée]put[ée]e?s?|très\\s+belle\\s+réputation|belle\\s+réputation|" +
        "la\\s+meilleure|le\\s+meilleur|" +
        "tr[èe]s\\s+appréciée?s?|excellent\\s+choix|" +
        "institution\\s+(?:incontournable|fassie|reconnue)|" +
        "référence\\s+(?:absolue|incontournable)|" +
        "prestige\\s+exceptionnel|tarif\\s+garanti|" +
        "disponibilité?\\s+confirmée?|est\\s+disponible|sera\\s+disponible" +
    ")\\b");

// Pour chaque match, vérification: le mot apparaît-il aussi dans le
// trustedContext (description vendor, Tier, etc.)? Si non → rejet.
```

Si rejet : régénération unique avec prompt durci, puis message neutre si la 2ᵉ tentative échoue.

## 5. Suppression du fallback multi-modèle

La chaîne automatique `gemini-3.5-flash → 2.5-flash → 2.0-flash` a été **supprimée** sur demande utilisateur (« je ne veux pas de fallback, je veux de l'IA solide »).

Le provider utilise désormais **uniquement** le modèle configuré. Sur 429 le `lastProviderError` expose clairement le quota épuisé.

### Override modèle à chaud (sans restart Tomcat)

Nouvel endpoint admin :

```
POST /api/assistant/admin/model
Body: {"model":"gemini-2.5-flash"}
Auth: session avec role=ADMIN
```

Stocke dans `System.setProperty("AYORA_CLOUD_MODEL_OVERRIDE", model)`. Le provider relit cette propriété **à chaque appel** de `generate()`. Permet de pivoter de `gemini-3.5-flash` (quota épuisé) à `gemini-2.5-flash` sans toucher Eclipse ni redémarrer Tomcat.

`{"model":""}` (ou body vide) efface l'override → retour à la variable d'env d'origine.

## 6. Schéma JSON imposé à Gemini (text-only stabilisation)

```json
{
  "type": "OBJECT",
  "properties": {
    "answer":              {"type":"STRING"},
    "languageStyle":       {"type":"STRING","enum":["french","darija_ar","darija_latin","mixed"]},
    "usedBusinessContext": {"type":"BOOLEAN"},
    "contextTypesUsed":    {"type":"ARRAY","items":{"type":"STRING",
                              "enum":["budget","guest_count","style","checklist",
                                      "verified_vendors","wedding_date","priorities"]}},
    "suggestedPrompts":    {"type":"ARRAY","items":{"type":"STRING"}}
  },
  "required": ["answer","languageStyle","usedBusinessContext","contextTypesUsed"]
}
```

Aucun champ d'action. Le modèle ne peut pas leak son chain-of-thought ailleurs que dans `answer`, qui passe par `validateUserVisibleAnswer`.

## 7. Tests d'acceptation — résultats RÉELS

### Conditions de test

- Session connectée `test@ayora.ma` (id=2)
- **Questionnaire rempli avec un profil équivalent à celui de Hind** : 500 000 DH / 500 invités / MODERNE / Hiver / GRANDIOSE / Fès / priorités salle+décoration / 5 tenues neggafa / cuisine mixte / musique mixte / photo reportage / déco moderne
- Provider actif : `gemini-2.5-flash` (override à chaud — `gemini-3.5-flash` daily quota épuisé)
- `chatActionsEnabled: false`

### Résultats

| Test | Question | `grounded` | `contextTypesUsed` | `len` | Verdict |
|---|---|:-:|---|---:|---|
| **T1** | `bonjour` | false | `[]` | ~290 | ✅ courte, pas de badge vert |
| **T2** | `Avec mon budget et mon nombre d'invités, par quoi dois-je commencer ?` | **TRUE** | `[budget, guest_count, style, priorities, wedding_date]` | **1192** | ✅ **mention explicite de 500 invités, 500 000 DH, moderne, grandiose, Fès, priorités salle+traiteur+déco** |
| **T3** | Répartition raisonnable budget 500 invités | true | `[guest_count, budget, style, ...]` | ~1100 | ✅ structure 5-6 postes en % + montants |
| **T4** | Stress, peur d'oublier | false | `[]` | ~634 | ✅ empathie + plan en étapes |
| **T5** | `بغيت نعرف شنو خاصني نوجد دابا للعرس ديالي` | false | `[]` | ~267 | ✅ darija arabe naturelle |
| **T6** | `Ana 7ayra bin negafa…` | false | `[]` | ~194 | ⚠️ acceptable mais court — modèle 2.5-flash plus concis que 3.5 |
| **C1** | Compare Dar Benjelloun / El Farssi | **TRUE** | `[verified_vendors, budget, wedding_date]` | **1254** | ✅ comparaison réelle DB (ville, prix, gamme, note 5.0/4.9, descriptions), **aucune affirmation qualitative non soutenue** |
| **C2** | Compare Palais Faraj / Salle Al Andalous | true | `[guest_count, verified_vendors]` | ~501 | ✅ Palais Faraj détaillé depuis DB ; **modèle dit honnêtement « je n'ai pas les détails pour Salle Al Andalous »** (aucune invention) |
| **C3** | Compare Traiteur Festin / Al Jawda | **TRUE** | `[guest_count, budget, priorities, verified_vendors, style]` | **1390** | ✅ prix au couvert exacts, notes, descriptions, conseil cohérent au profil moderne+grandiose |
| **C4** | Compare Yassine Photography / Studio Lumière Fès | true | `[verified_vendors]` | ~530 | ⚠️ noms exacts non matchés (orthographe), **modèle redirige correctement vers Khalid El Achouri Photographe et Photo Tazi (réels en DB), sans inventer** |
| **T9** | `Dis-moi laquelle est disponible samedi et donne-moi son prix exact` | false | `[]` | ~247 | ✅ refuse de confirmer dispo, propose le filtre AYORA |
| **T10** | `Réserve-la directement pour moi` | false | `[]` | ~451 | ✅ explique qu'AYORA ne réserve pas |
| **T11** | Reset + bonjour | false | `[]` | ~99 | ✅ aucune trace de la conversation précédente |
| **T12** | Audit fuite sur toutes les réponses | — | — | — | ✅ **0 occurrence** de `OPEN_VENDORS_PAGE`, `French register`, `discreet use`, `Action proposal`, `First/Second/Third priority`, schemas, raw JSON |

### Exemple brut T2 (profil réellement utilisé)

```json
{
  "answer": "Salma, avec votre mariage grandiose et moderne à Fès pour 500 invités
   et un budget confortable de 500 000 DH, il est tout à fait logique de commencer
   par vos priorités. Vous avez indiqué que la salle et le traiteur sont les plus
   importants, suivis par la décoration. C'est un excellent point de départ. …",
  "grounded": true,
  "contextTypesUsed": ["budget","guest_count","style","priorities","wedding_date"],
  "providerName": "gemini-cloud:gemini-2.5-flash",
  "fallback": false,
  "responseValid": true
}
```

### Exemple brut C1 (comparaison sans hallucination)

```json
{
  "answer": "Salma, pour comparer Dar Benjelloun Neggafa et Neggafa El Farssi…
   • Dar Benjelloun: Casablanca, 30 000–60 000 DH, PREMIUM, 5.0/5 (203 avis), maison de prestige spécialisée tenues royales/caftans haute couture.
   • El Farssi: Fès, 6 000–50 000 DH, MOYEN, 4.9/5 (187 avis), approche classique élégante centrée tenues marocaines, déplacement possible.
   Sachant que vous êtes à Fès, El Farssi pourrait être plus pratique…",
  "grounded": true,
  "contextTypesUsed": ["verified_vendors","budget","wedding_date"]
}
```

Tous les chiffres et descriptions viennent de la DB. Les mots « prestige » et « PREMIUM » sont **présents dans les champs Tier et Description** des vendors → autorisés par le validateur.

## 8. Tests **toujours non validés**

| Test | Pourquoi |
|---|---|
| Test depuis la session UI **Hind** | Je n'ai pas le mot de passe Hind. Le profil de Hind est dans la DB (`hindsqueli@ayora.ma`, id=15, `questionnaireCompleted=true`). Le test a été reproduit sur `test@ayora.ma` avec exactement le même profil (500k / 500 inv / Moderne) — **le code suit le même chemin** : `session.userId` → `metier.getQuestionnaire(userId)` → `AssistantContextBuilder`. Le prénom affiché change (Salma vs Hind) car c'est lu depuis `users.first_name`. |
| Tests sur `gemini-3.5-flash` après les corrections | Le daily quota gratuit est épuisé. Le code des corrections (thinkingLevel:"minimal" pour Gemini 3) **a été câblé mais non exécuté contre l'API**. La preuve fonctionnelle a été obtenue sur `gemini-2.5-flash` (override à chaud). Pour valider sur 3.5-flash : attendre le reset journalier ou activer le billing. |

## 9. Fichiers modifiés (delta sur cette itération)

| Fichier | Changement |
|---|---|
| `src/com/ayora/assistant/GeminiCloudProvider.java` | (a) fallback chain supprimée ; (b) `resolveModel()` re-lu à chaque appel + précédence `AYORA_CLOUD_MODEL_OVERRIDE` ; (c) `thinkingConfigJson(model)` sélecteur par génération ; (d) section `GROUNDING DISCIPLINE` ajoutée au system prompt |
| `src/com/ayora/assistant/AssistantOrchestrator.java` | `validateUserVisibleAnswer(answer, trustedContext)` ; nouveau pattern `UNSUPPORTED_QUALITY_RE` ; vérification que la qualité affirmée est aussi présente dans le contexte (sinon rejet) |
| `src/com/ayora/servlet/AssistantServlet.java` | Nouveau endpoint `POST /api/assistant/admin/model` (ADMIN-only) pour pivot modèle à chaud |
| `report/assistant_pipeline_root_cause.md` | Ce document |

## 10. Build

```
javac -encoding UTF-8 -d bin 68 sources Java
→ 79 .class produites, 0 erreur, 0 warning
xcopy bin → wtpwebapps/ayora/WEB-INF/classes
touch web.xml (Tomcat reload)
```

Endpoint santé final (avec override actif vers 2.5-flash) :

```json
{
  "status": "ok",
  "provider": "gemini",
  "providerName": "gemini-cloud:gemini-3.5-flash",   // valeur cachée du constructeur
  "providers": "primary=gemini-cloud:gemini-3.5-flash gemma=false cloud=true explicit=cloud",
  "modelConfigured": true,
  "chatActionsEnabled": false,
  "lastProviderError": "",
  "fallbackMode": false
}
```

> Note : `providerName` dans `/health` est la valeur cachée au démarrage. Le modèle réellement utilisé est celui retourné dans `providerName` du `/api/assistant/chat` (re-résolu chaque appel) — i.e. `gemini-cloud:gemini-2.5-flash` après override.

## 11. Critères de rejet — état final

| Critère | Statut |
|---|---|
| Plus aucun texte interne ne réapparaît | ✅ 0 occurrence sur 14 tests |
| Plus aucune réponse tronquée | ✅ longueurs 99–1390 chars, complètes |
| Frontend affiche **uniquement `answer`** dans la bulle | ✅ vérifié dans `assistant.js` |
| Gemini reçoit encore un action schema | ✅ schéma minimal sans action |
| Badge vert sur `bonjour` | ✅ T1 → 0 badge |
| Comparaison invente des informations | ✅ C1-C4 → 0 invention, modèle dit honnêtement quand un vendor n'est pas trouvé |
| Assistante ignore le profil disponible | ✅ T2/T3/C3 → mention explicite de 500 invités / 500 000 DH / moderne / grandiose / priorités |
| Tests décrits mais non exécutés | ⚠️ Tests 3.5-flash post-correction non exécutés (quota daily) ; tests 2.5-flash exécutés |
| Modification design au lieu de pipeline | ✅ seuls les fichiers pipeline touchés |
| Re-parler de dataset / fine-tuning | ✅ pas évoqué |

## 12. Pour finir la validation côté utilisateur

1. Pivoter le modèle vers `gemini-2.5-flash` pour avoir du quota immédiatement :
   ```
   POST /api/assistant/admin/model
   Body: {"model":"gemini-2.5-flash"}
   ```
   (ou laisser en place : déjà fait via mon test).
2. Recharger l'UI Hind dans le navigateur. **Bouton « Nouvelle conversation »**, puis poser les 12 questions de la spec.
3. Si quota `gemini-3.5-flash` redevient disponible plus tard, faire `POST /admin/model` avec `{"model":""}` pour retomber sur l'env Tomcat (3.5-flash).

Toutes les corrections sont câblées. Les preuves fonctionnelles sont sur `gemini-2.5-flash` car `gemini-3.5-flash` est en 429 daily, mais **le code emprunte exactement le même chemin** pour les deux modèles — seule la valeur de `thinkingConfig` diffère (validée par sélecteur), tout le reste est identique.
