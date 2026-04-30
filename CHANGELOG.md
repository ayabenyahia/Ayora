# Ayora — Changelog

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
