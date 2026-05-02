# Ayora — Changelog

## v2.1 — Refonte recommandations + catégories + invitations dynamiques (2026-05-02)

### Moteur de recommandation IA — Refonte
- Nouveau modèle `UserProfile` synthétisant le questionnaire (style, ambiance, niveauLuxe, budgetTier, guestSize, prioriteCategories, moodKeywords)
- Scoring **multi-critères** : Budget 30 + Luxe 20 + Style 20 + Préférences 20 + Priorité 10 + Popularité 5 + Mood 5 + Culturel 5
- **Tags pertinents** : Coup de cœur, Bon rapport qualité/prix, Choix luxe, Petit budget, Style aligné, Préférence exacte, Authenticité fassie, Catégorie prioritaire, Hors budget, Format intime, Grand événement
- **Raisons contextualisées** citant les réponses du questionnaire (budget, niveau de luxe, style, type de musique, neggafa…)
- 7 **blocs thématiques** : Top picks, Vos priorités, Bon plan, Les plus chic, Petit budget, Sélection premium, À considérer
- Servlet enrichi : profil + blocs + counts + categories + filtres avancés (category, gamme, minScore, maxPrice, tag)
- Page recommandations refondue : sidebar profil sticky + onglets de blocs + cards riches (score ribbon, sub-scores, tags, raison, contacts SVG)

### Refonte des catégories de prestations
- Coiffure → fusionnée dans **Maquillage & Coiffure**
- Fleuriste → fusionnée dans **Décoration & Fleuriste**
- Vidéaste → fusionnée dans **Photographe & Vidéaste**
- Transport et Wedding Planner **supprimés** (services non offerts par Ayora)
- Mounia Ramsis Tounsi → reclassée **Neggafa** (plus Wedding Planner)
- Numéros normalisés au format marocain 10 chiffres `06XX-XX-XX-XX`
- Instagram en lowercase avec `@` préfixé (convention IG)

### Invitations
- 3 modèles vidéo retirés (UX bloquée, peu utilisés)
- 3 nouveaux modèles wow Premium : **Or Liquide** (dégradé doré animé), **Caftan Ivoire** (broderies fassi), **Impérial Bordeaux** (sceau royal)
- Pré-remplissage **dynamique** des aperçus avec les vraies données du couple : noms, date FR, heure (`19h00`), ville, lieu (palais/salle)
- Email d'invitation enrichi : `Salma & Yassine` au lieu du compte utilisateur, ligne date `12 Juin 2026 à 19h00`, lieu `Palais Mokri - Fes`

### Questionnaire
- Champs `heureMariage` (input time) et `lieuMariageNom` ajoutés en section 1
- Section 4 reflète les nouvelles catégories fusionnées
- Pré-remplissage des nouveaux champs sur édition

### Backend / Infra
- `Database` facade alignée sur p01-jdbc + `Examples` runner standalone
- Interface générique `Dao<T,K>` pour formaliser la couche d'accès aux données
- Fix `JsonUtil.unescapeJson` + `inlineJsonOrString` : plus de double-escape sur `notesSpeciales`

### UI
- Carte de filtres avancés retirée de la page recommandations (les onglets suffisent)
- Prix des prestataires : affiché uniquement "À partir de X DHS" (plus de plafond effrayant)
- Tag "Plebiscite" retiré (signification floue)

## v2.0 — Refonte Pro/Premium + nouveaux prestataires

### Abonnements
- Nouveau palier **PRO** (149 DHS/mois) entre Free et Premium
- Page d'accueil : 3 cartes pricing alignées (Gratuit / Pro recommandé / Premium)
- `premium.html` : sélecteur 3 plans + paiement adapté au plan choisi
- Dashboard : 2 bandeaux d'upgrade (Pro + Premium) côte-à-côte
- Compte de test PRO : `pro@ayora.ma` / `pro123`
- Comptes de test PREMIUM : `amine@ayora.ma` / `amine123` et `ayasofia@ayora.ma` / `Aya@2006`

### Invitations
- 15 modèles signés Ayora (3 FREE, 5 PRO, 7 PREMIUM dont 3 vidéo)
- Modèles marocains **Zellige Pearl** (PRO) et **Zellige Royal** (PREMIUM)
- 3 modèles **VIDÉO** Premium : Cinema Wedding, Fassi Royal Video, Black Velvet Video
- Lock overlay sur les modèles non accessibles (cadenas + redirection premium.html)
- Modal "Confirmer & envoyer" avec récap invité/email/modèle
- Champ `videoUrl` pour les invitations vidéo (lien YouTube/Drive/Vimeo intégré dans l'email)
- Backend défense en profondeur : 403 si plan utilisateur < niveau requis du modèle
- Noms du couple dynamiques (lus depuis le questionnaire)

### Questionnaire
- 6 sections en stepper visuel
- Section 1 : noms mariée/marié, date, ville, type
- Section 2 : budget total + flexibilité + 16 priorités multi-choix
- Section 3 : style multi-choix (chic, royal, marocain, oriental, ...)
- Section 4 : services + bloc Neggafa détaillé (tenues, style, amariya)
- Section 5 : ambiance (couleurs, thème, lieu en select, sliders)
- Section 6 : contraintes
- Animation soirée séparée : Issawa / Orchestre / DJ + "Aucun" exclusif
- **Saison automatique** depuis la date du mariage
- Récupération des réponses si la session expire pendant la saisie

### Prestataires
- 60 prestataires en base (vérifiés par sources directes)
- Lot 2 : My Kiko Cake, The Cake House by Chaym, Hnaya Fati, Adil Otmani, Haj Said Berrada, Omar Hanoun, Mehdi Artiste Orchestre, DJ Kacem, DJ Mehdi Bouchal, Issawa Moustafa Meseyah, Le Pavillon Dor
- Prix alignés avec le marché marocain réel 2026
- Encodage UTF-8 nettoyé (plus de `ÔÇö`)
- "Tenguit" → "Tenguif", doublon Festin supprimé
- Mention "déplacement partout au Maroc" sur tous les prestataires

### Email
- Service refactoré pour lire les credentials SMTP depuis les variables d'environnement
- Mode DEMO si `AYORA_MAIL_FROM` non configuré (logs uniquement)
- Template HTML "vidéo" avec gros bouton CTA cliquable
- 4 templates email : classique, moderne, luxe, vidéo

### Base de données
| Migration | Action |
|---|---|
| `migration_pro_tier.sql` | étend les ENUMs `subscription_type` et `plan` avec `PRO` |
| `migration_vendors_real.sql` | upsert lot 1 (24 prestataires) |
| `migration_vendors_fix.sql` | corrections encodage + prix réalistes |
| `migration_vendors_v3.sql` | corrections noms + 11 nouveaux prestataires |
| `migration_video_url.sql` | colonne `invitations.video_url` |
| `migration_questionnaire_enums.sql` | ENUMs questionnaire élargis |

## v1.0 — Mise en place initiale

- Backend Jakarta EE 5.0 / Tomcat 10 / Java 17 (style p02-jee du prof)
- Pattern `DataSource` / `MySQLDataSource` (style p01-jdbc du prof)
- Auth, questionnaire, vendors, invitations, recommendations
- 22 user stories en GitHub Issues + labels Kanban
