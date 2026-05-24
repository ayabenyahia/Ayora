# Ayora — Comptes de test

Tous ces comptes sont créés automatiquement par les migrations SQL.

## Comptes côté client

| Plan | Email | Mot de passe | Caractéristiques |
|---|---|---|---|
| **FREE** | `test@ayora.ma` | `test123` | 10 invitations, 3 modèles |
| **PRO** | `pro@ayora.ma` | `pro123` | 50 invitations, 9 modèles |
| **PREMIUM** | `amine@ayora.ma` | `amine123` | Illimité, 15 modèles incl. vidéo |
| **PREMIUM** | `ayasofia@ayora.ma` | `Aya@2006` | Illimité, 15 modèles incl. vidéo |

## Compte administrateur

| Email | Mot de passe | Rôle |
|---|---|---|
| `admin@ayora.ma` | `admin123` | ADMIN PREMIUM |

## Scénarios à tester

### Scénario FREE → bloqué sur les modèles Pro/Premium
1. Login `test@ayora.ma` / `test123`
2. Aller sur `/invitations.html`
3. Filtrer "Pro" → cadenas + texte "Disponible avec PRO"
4. Cliquer un modèle Pro → redirection vers `premium.html`

### Scénario PRO → 9 modèles débloqués, vidéos verrouillées
1. Login `pro@ayora.ma` / `pro123`
2. `/invitations.html` → 9 modèles utilisables
3. Filtrer "Premium" → cadenas + redirection

### Scénario PREMIUM → tout débloqué + invitations vidéo
1. Login `amine@ayora.ma` / `amine123`
2. `/invitations.html` → 15 modèles dont 3 vidéo
3. Choisir Cinema Wedding → modal demande lien vidéo
4. Coller un lien YouTube → "Confirmer & envoyer"
5. L'invité reçoit un email avec gros bouton "▶ Voir l'invitation vidéo"

### Scénario ADMIN
1. Login `admin@ayora.ma` / `admin123` → redirige vers `/admin.html`
2. Voir stats users / vendors / devis / RDV
3. Liste de tous les utilisateurs et vendors

## Réinitialisation

```sql
-- Pour remettre amine et ayasofia avec questionnaire à remplir :
UPDATE users SET questionnaire_completed = FALSE
  WHERE email IN ('amine@ayora.ma','ayasofia@ayora.ma');
DELETE FROM questionnaire_answers WHERE user_id IN
  (SELECT id FROM users WHERE email IN ('amine@ayora.ma','ayasofia@ayora.ma'));
```
