# Audit — Refonte espace administrateur AYORA

Date : 2026-05-24
Périmètre : **uniquement** `admin.html` + son JS inline + un nouveau `admin-exec.css`.
Hors-périmètre confirmé : Gemini, pipeline assistant, pages couple, recommandations, services backend.

---

## 1. Fichiers concernés

### Front-end
| Fichier | État | Action |
|---|---|---|
| `WebContent/admin.html` (1185 lignes) | tout en inline : style + body + JS + footer | refonte totale |
| `WebContent/css/admin-exec.css` | n'existe pas | **création** |
| `WebContent/css/styles.css` | partagé avec couple ; contient `.menu-bar` | **non modifié** |
| `WebContent/js/api.js` | wrapper REST utilisé partout | **non modifié** |
| `WebContent/js/ayora-ui.js` | toasts globaux | **non modifié** |

### Back-end (déjà fonctionnel — aucune modification)
| Fichier | Rôle |
|---|---|
| `src/com/ayora/servlet/AdminServlet.java` | 12 routes admin, sécurité `role=ADMIN` via session |
| `src/com/ayora/dao/AdminStatsDao.java` | timeline, analytics, actions à traiter |
| `src/com/ayora/dao/VendorDao.java` | `computeCompleteness(vendor)` |
| `src/com/ayora/metier/AyoraMetier.java` | exposition des compteurs et listes |

---

## 2. Endpoints admin disponibles (vérifiés)

| Verbe | Route | Données réelles ? |
|---|---|---|
| GET | `/api/admin/stats` | OUI — counts users/vendors/devis/rdv/plans/questionnaire |
| GET | `/api/admin/users` | OUI — paginé + filtres q/role/plan/questionnaire/active |
| GET | `/api/admin/users/{id}` | OUI — fiche + score complétude + counts devis/rdv/picks |
| PUT | `/api/admin/users/{id}` | OUI |
| POST | `/api/admin/users/{id}/role\|plan\|active` | OUI |
| DELETE | `/api/admin/users/{id}` | OUI (avec garde « dernier admin ») |
| GET | `/api/admin/vendors` | OUI — paginé + filtres + completeness |
| GET | `/api/admin/vendors/{id}` | OUI |
| PUT | `/api/admin/vendors/{id}` | OUI |
| POST | `/api/admin/vendors/{id}/active` | OUI |
| DELETE | `/api/admin/vendors/{id}` | OUI |
| GET | `/api/admin/devis?status=` | OUI |
| PUT | `/api/admin/devis/{id}/status` | OUI |
| GET | `/api/admin/rdv?status=` | OUI |
| PUT | `/api/admin/rdv/{id}/status` | OUI |
| GET | `/api/admin/activity?limit=` | OUI — agrège inscriptions, devis, rdv, questionnaires |
| GET | `/api/admin/actions` | OUI — devis en attente + rdv en attente + vendors incomplets + clients sans questionnaire |
| GET | `/api/admin/analytics` | OUI — signups par mois (12 mois), répartition plans, vendors par catégorie, questionnaire complete/incomplete, devis par statut |

Constat : **aucun endpoint manquant**. Le back est nettement plus complet que ce que le front exploite.

---

## 3. Tables MySQL réellement lues

- `users` : id, first_name, last_name, email, phone, city, role (CLIENT/PRESTATAIRE/ADMIN), subscription_type (FREE/PRO/PREMIUM), is_active, questionnaire_completed, created_at, vendor_id
- `vendors` : id, name, category_id, city, description, prix_min, prix_max, gamme, phone, email, instagram, address, tags, rating, nb_avis, is_active
- `categories` : id, name
- `demandes_devis` : id, client_id, vendor_id, budget_min/max, message, date_mariage, nb_invites, statut (EN_ATTENTE/ACCEPTE/REFUSE), reponse_prestataire, created_at
- `rendez_vous` : id, client_id, vendor_id, date_rdv, heure_rdv, lieu, note, statut (EN_ATTENTE/CONFIRME/ANNULE), created_at
- `questionnaire_answers` : user_id, created_at
- `user_picks` : user_id, vendor_id
- `recommendations` : user_id, vendor_id
- `subscriptions` : user_id, plan (synchro lors du changement de plan)

---

## 4. Sécurité admin — vérifications

| Contrôle | Résultat |
|---|---|
| `AdminServlet.checkAdmin(req,res)` invoqué dans tous les `doGet/doPost/doPut/doDelete` | ✅ |
| Session absente → 401 « Non authentifié » | ✅ vérifié `curl /api/admin/stats` → `{"error":"Non authentifie"}` |
| Role ≠ ADMIN → 403 « Accès refusé - rôle ADMIN requis » | ✅ |
| Front : `admin.html` redirige vers `dashboard.html` si role ≠ ADMIN (ligne 471) | ✅ |
| Garde contre suppression du dernier admin | ✅ ligne 337-341 |
| Aucun champ mot de passe / hash exposé dans `userJson()` | ✅ |
| Validation des enum côté serveur (role, plan, statuts) | ✅ regex `matches("CLIENT\|ADMIN\|PRESTATAIRE")` etc. |

---

## 5. Constat sur la page actuelle

### Problèmes UX identifiés
1. **Largeur sous-exploitée** : `max-width: 1380px` OK mais grille KPI sur 6 colonnes étroites → l'œil ne sait pas où regarder.
2. **12 mini-KPIs au même niveau** → aucune hiérarchie. L'admin ne voit pas immédiatement les priorités.
3. **« Actions à traiter »** : simple liste scrollable étroite (`max-height: 320px`) au-dessus des KPIs → mauvaise UX, l'urgence n'est pas mise en valeur.
4. **Graphiques riquiqui** : `chart-bars` à 140px de haut, donut à 110px → illisibles.
5. **Menu-bar partagée avec espace couple** : montre un badge `FREE/PRO/PREMIUM` pour l'admin, ce qui n'a aucun sens.
6. **Hero générique** : pas de message dynamique d'état, pas de CTA fonctionnels.
7. **Pas de section « santé plateforme »** avec barres de progression de complétude.
8. **Activité récente** cachée derrière un onglet alors qu'elle pourrait s'afficher en bas du dashboard.

### Ce qui fonctionne (à conserver)
- Le drawer de détail user/vendor (édition + actions backend) → garder
- Tables paginées + filtres → garder, juste relooker
- Export CSV → garder
- JS api.js intégré → garder

---

## 6. Plan exact des modifications

### 6.1 Création
- `WebContent/css/admin-exec.css` — design system exécutif :
  - palette burgundy/champagne/ivoire (réutilise `--burgundy`, `--gold` de styles.css)
  - composants : `ax-shell`, `ax-topbar`, `ax-hero`, `ax-kpi-grid`, `ax-priorities`, `ax-health`, `ax-analytics`, `ax-activity`
  - largeur 1440px desktop / fluid
  - typo Cormorant Garamond pour titres + Inter pour data

### 6.2 Refonte `admin.html` — structure cible
```
┌─ ax-topbar (header admin spécifique, sans badge plan)
│  Logo | Administration | Tableau | Utilisateurs | Prestataires | Devis & RDV | Activité
│  --------------------------- | Alertes badge | Avatar Admin ▾
├─ ax-hero (centre de pilotage)
│  ESPACE ADMINISTRATEUR
│  Centre de pilotage AYORA
│  « 13 éléments nécessitent votre attention… » (dynamique)
│  [Traiter les alertes] [Gérer les prestataires] [Consulter l'activité]
├─ ax-kpi-grid (4 grandes cartes)
│  Utilisateurs | Prestataires | Abonnements | Demandes
├─ ax-secondary-row (5 indicateurs secondaires plus discrets)
│  Quest. complétés | Quest. incomplets | Comptes suspendus | Prestataires inactifs | Devis acceptés
├─ ax-split (2 colonnes)
│  ┌─ Priorités administratives (filtres : tout / prestataires / utilisateurs / questionnaires / devis / rdv)
│  └─ Santé de la plateforme (barres progression)
├─ ax-analytics (3 cartes larges)
│  Inscriptions 12 mois | Répartition plans (donut) | Prestataires par catégorie (bars)
├─ ax-activity (timeline en bas)
└─ tabs (Utilisateurs / Prestataires / Devis & RDV) — conserver section existante
```

### 6.3 Conservation
- Tous les `id` de drawer/modals/toast pour ne pas casser le JS d'édition existant
- Toutes les fonctions JS (`openUserDetail`, `saveVendor`, `exportUsersCSV`, etc.)
- Le footer existant

### 6.4 Suppression / corrections
- Plus de `#userBadge` plan FREE/PRO/PREMIUM sur l'admin
- Plus d'emoji 📥 dans les boutons export (header user demande sobriété)
- Bug ligne 1095 et 1134 : `usrQc` et `vndCity` n'existent pas → à corriger pour que l'export marche

---

## 7. Données réellement exploitées par carte (mapping)

| Carte | Source | Champs |
|---|---|---|
| KPI Utilisateurs | `/api/admin/stats` | `users.total`, `users.clients`, `users.suspendus` |
| KPI Prestataires | `/api/admin/stats` | `vendors.actifs`, `vendors.incomplets`, `vendors.inactifs` |
| KPI Abonnements | `/api/admin/stats` | `plans.premium`, `plans.free`, `plans.tauxPremium` |
| KPI Demandes | `/api/admin/stats` | `devis.nouveaux`, `rdv.attente` (+`devis.total` info) |
| Priorités | `/api/admin/actions` | priority + type + title + detail |
| Santé plateforme | `/api/admin/stats` | ratios completes/totaux |
| Inscriptions chart | `/api/admin/analytics` `signupsByMonth` | month + count |
| Plans donut | `/api/admin/analytics` `plansDistribution` | plan + count |
| Vendors par cat | `/api/admin/analytics` `vendorsByCategory` | category + count |
| Activité | `/api/admin/activity?limit=8` | type + label + who + meta + date |

---

## 8. Limites identifiées / non-fait

| Sujet | Pourquoi non fait | Documenté |
|---|---|---|
| « Dernière connexion » dans le hero | Pas de champ `last_login_at` dans table `users` | hero indique seulement le rôle |
| Onglet « Abonnements » dédié | La table `subscriptions` existe mais n'a pas d'endpoint de liste paginée → réutilise filtre plan dans `/api/admin/users` | filtre plan |
| Recherche globale header | Pas d'endpoint search-all → réutilise les recherches scopées par onglet | non implémenté |
| Graphique « devis par statut » | Disponible côté backend mais quatrième chart surchargerait → mis dans le KPI Demandes | OK |

---

## 9. Scripts SQL ajoutés ou modifiés

**Aucun.** Le schéma actuel suffit. La refonte est purement UI/UX.

---

## 10. Critères de succès

- [ ] La page exploite 1280-1440px en desktop sans grand vide
- [ ] Les 4 KPI principaux sont immédiatement lisibles
- [ ] La section priorités a des filtres fonctionnels
- [ ] La santé plateforme affiche des ratios réels
- [ ] Les graphiques sont grands et lisibles
- [ ] Aucun chiffre inventé
- [ ] L'API admin reste protégée (401/403)
- [ ] Tous les boutons existants (drawer édition, export CSV) continuent de fonctionner
- [ ] Pas de badge `Premium` sur l'admin
- [ ] Pipeline Gemini intact
