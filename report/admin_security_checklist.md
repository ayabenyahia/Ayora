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
