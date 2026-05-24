# Checklist sécurité — espace administrateur AYORA

Contrôles techniques vérifiés pour l'espace admin. Aucun secret n'est versionné.

## Authentification côté serveur

- [x] Tous les endpoints `/api/admin/*` appellent `checkAdmin(req,res)` en premier.
- [x] Si `session.getAttribute("userId") == null` → réponse `401 Non authentifié`.
- [x] Vérifié : `curl /api/admin/stats` sans cookie renvoie `{"error":"Non authentifie"}`.

## Guard de rôle ADMIN

- [x] `session.getAttribute("role")` doit être exactement `"ADMIN"`.
- [x] Sinon → `403 Accès refusé - rôle ADMIN requis`.
- [x] Côté front, `admin.html` redirige vers `dashboard.html` si `user.role !== 'ADMIN'` (ligne 471 de l'ancienne version, conservée).
- [x] Un utilisateur CLIENT ne peut PAS voir ni appeler les endpoints admin.

## Codes HTTP

| Situation | Code | Body |
|---|---|---|
| Pas de session | 401 | `{"error":"Non authentifie"}` |
| Session sans ADMIN | 403 | `{"error":"Acces refuse - role ADMIN requis"}` |
| Route inconnue | 404 | `{"error":"Route admin non trouvee : ..."}` |
| Validation échouée | 400 | `{"error":"Plan invalide"}` etc. |
| Erreur serveur | 500 | message sans stack trace |

## Garde du dernier compte admin

Dans `AdminServlet.handleUserDelete()` :

```java
if (u != null && "ADMIN".equals(u.getRole()) && metier.countUsersByRole("ADMIN") <= 1) {
    JsonUtil.sendError(res, 400, "Impossible : c'est le dernier compte admin");
    return;
}
```

Empêche le verrouillage total de la plateforme par suppression accidentelle du dernier admin.

## Aucun mot de passe ou hash exposé

- [x] La méthode `userJson()` du servlet ne sérialise PAS `password_hash`.
- [x] Le drawer admin ne lit ni n'écrit le hash — modification de mot de passe = flux séparé via `AuthServlet`.
- [x] Aucune réponse `/api/admin/*` ne contient de champ commençant par `password*`.

## Validation stricte des valeurs sensibles

Toutes les enum sont validées par regex côté serveur :

```java
if (role == null || !role.matches("CLIENT|ADMIN|PRESTATAIRE")) { 400 }
if (plan == null || !plan.matches("FREE|PRO|PREMIUM"))         { 400 }
if (status == null || !status.matches("EN_ATTENTE|ACCEPTE|REFUSE")) { 400 }
```

Empêche injection de valeurs hors-périmètre via la requête.

## Protection contre l'injection SQL

- [x] Tous les DAO utilisent `PreparedStatement` via le wrapper `Database.queryList(sql, mapper, args...)`.
- [x] Aucune concaténation directe de paramètres utilisateur dans une requête.
- [x] Les filtres `q`, `role`, `plan` etc. passent par des `?` placeholders.

## Gestion de session

- [x] Cookie `JSESSIONID` standard servlet, `HttpOnly` activé par Tomcat.
- [x] La déconnexion (`POST /api/auth/logout`) invalide la session côté serveur ET supprime `localStorage.user` côté client.
- [x] Aucune information sensible n'est stockée dans `localStorage` (seulement nom/prénom/role pour personnalisation UI).

## Logs d'erreurs propres

- [x] Les exceptions admin sont catchées dans chaque `doGet/doPost/doPut/doDelete`.
- [x] Le serveur log côté console (`System.out.println("## Erreur admin ...")`) mais ne renvoie au client qu'un message générique sans stack trace.
- [x] Pas de divulgation d'informations internes (chemins fichiers, versions DB, etc.).

## Revue finale

Tous les points ci-dessus ont été vérifiés sur la version déployée. La checklist est revue à chaque modification d'un endpoint admin.
