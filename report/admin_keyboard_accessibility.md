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

## Focus visible et contraste

- Tous les `input` et `select` des filtres affichent un anneau de focus burgundy doux : `box-shadow: 0 0 0 3px rgba(139,26,43,.08)`.
- Les boutons d'action critique (Supprimer, Suspendre) ont un contraste WCAG AA minimum sur fond ivoire.
- Les badges de rôle/plan/statut respectent le ratio 4.5:1 entre texte et arrière-plan.

## Palette et daltonisme

Les états utilisent **doublement** couleur + libellé (jamais couleur seule) :

- HIGH (rouge soft) + libellé *Urgent*
- MEDIUM (ambre soft) + libellé *Important*
- LOW (gris) + libellé *À traiter*
- Actif (sauge soft) + libellé *Actif*
- Suspendu (rouge soft) + libellé *Suspendu*

Les barres de santé combinent largeur + couleur pour rester lisibles en simulation deuteranopia/protanopia.
