# Clavier & accessibilité — espace administrateur AYORA

Bonnes pratiques implémentées et raccourcis disponibles dans l'espace admin.

## Raccourci ESC

La touche `Escape` :

- ferme le drawer de fiche utilisateur / prestataire ;
- ferme la modale de confirmation (suppression, suspension) ;
- ferme le dropdown du profil administrateur si ouvert.

Implémentation : `document.addEventListener('keydown', ...)` dans `admin.html`.
