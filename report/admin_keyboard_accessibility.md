# Clavier & accessibilité — espace administrateur AYORA

Bonnes pratiques implémentées et raccourcis disponibles dans l'espace admin.

## Raccourci ESC

La touche `Escape` :

- ferme le drawer de fiche utilisateur / prestataire ;
- ferme la modale de confirmation (suppression, suspension) ;
- ferme le dropdown du profil administrateur si ouvert.

Implémentation : `document.addEventListener('keydown', ...)` dans `admin.html`.

## ARIA du dropdown profil

Le bouton `#axUserBtn` porte :

- `aria-haspopup="true"`
- `aria-expanded` mis à jour à chaque ouverture/fermeture

Le menu `#axUserMenu` porte `role="menu"`. Chaque entrée est cliquable au clic souris et au focus clavier.
