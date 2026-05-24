# Checklist sécurité — espace administrateur AYORA

Contrôles techniques vérifiés pour l'espace admin. Aucun secret n'est versionné.

## Authentification côté serveur

- [x] Tous les endpoints `/api/admin/*` appellent `checkAdmin(req,res)` en premier.
- [x] Si `session.getAttribute("userId") == null` → réponse `401 Non authentifié`.
- [x] Vérifié : `curl /api/admin/stats` sans cookie renvoie `{"error":"Non authentifie"}`.
