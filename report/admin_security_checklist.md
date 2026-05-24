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
