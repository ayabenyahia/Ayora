/* ============================================================
   AYORA - Vendor Card Component (shared)
   ------------------------------------------------------------
   Compose le markup HTML d'une carte prestataire. Reutilise sur
   recommendations.html (cartes IA) et mychoices.html (picks).

   API publique :
     AyoraCard.render(vendor, options)
         -> string HTML d'un <article class="reco-card v2">
     AyoraCard.coverHtml(vendor)
         -> string HTML du visuel de couverture (image ou placeholder)
     AyoraCard.openDetail(vendor)
         -> ouvre le modal gallerie + reel + details
     AyoraCard.parseGallery(vendor)
         -> liste d'URLs (galerie + cover en fallback)
     AyoraCard.categorySlug(categoryName)
         -> slug normalise pour les placeholders

   Le composant n'introduit aucune dependance externe. Le markup est
   produit en string concatenation pour rester compatible avec les
   patterns existants (innerHTML).
   ============================================================ */

(function(global) {

	// ---- Catalogue de placeholders par categorie. -------------------
	// Chaque entree definit un gradient sobre + une icone SVG en
	// filigrane (motif marocain : etoile a 8 branches, arche, calice,
	// etc.). Aucun fichier image n'est requis : tout est genere a la
	// volee en SVG inline. Pour brancher de vrais visuels plus tard,
	// il suffit d'ajouter un fichier dans WebContent/images/placeholders
	// et de remplacer la fonction placeholderUrl().
	// Palette sobre, monogramme de luxe : tons crème / ivoire / champagne /
	// nude / blush / doré doux. Différenciation TRÈS subtile par catégorie.
	// Pas de pictogramme en fond : le monogramme suffit. La clé "icon" est
	// conservée par compatibilité (categorySlug peut la lire) mais n'est plus
	// utilisée par la cover. La clé "dark" est supprimée : tout est clair.
	var PLACEHOLDERS = {
		negafa:     { from: '#FDF7F2', to: '#F2DFCF', label: 'Neggafa' },        // blush nude
		makeup:     { from: '#FDF6F4', to: '#F4DDD7', label: 'Maquillage' },     // rose poudré
		photo:      { from: '#FBF7EF', to: '#ECDFC9', label: 'Photographe' },    // champagne doux
		cake:       { from: '#FDF8F0', to: '#F2E1D2', label: 'Gateau' },         // crème chaude
		issawa:     { from: '#FBF6EC', to: '#E8D5B0', label: 'Issawa' },         // champagne doré
		orchestre:  { from: '#FAF4EF', to: '#E3CFC2', label: 'Orchestre' },      // beige rose
		decoration: { from: '#FCFAF6', to: '#EFE5D6', label: 'Decoration' },     // ivoire
		salle:      { from: '#FCF8F1', to: '#EAD8BD', label: 'Salle' },          // crème dorée
		traiteur:   { from: '#FDF8F0', to: '#F0DECD', label: 'Traiteur' },       // crème chaude
		myadi:      { from: '#FCF6EC', to: '#E9D3B5', label: 'Myadi' },          // champagne
		dj:         { from: '#FBF4EF', to: '#DDC0A8', label: 'DJ' },             // taupe doux
		hennaya:    { from: '#FBF6EC', to: '#E7D2A7', label: 'Hennaya' },        // doré crème
		fleuriste:  { from: '#FCF8F4', to: '#F0DCD1', label: 'Fleuriste' },      // blush
		transport:  { from: '#FBFAF7', to: '#E9DFCF', label: 'Transport' },      // crème neutre
		_default:   { from: '#FAF6F0', to: '#EBDDCB', label: '' }                // champagne neutre
	};

	// Icones SVG en filigrane (path-only, currentColor). Trace 1.5px
	// fin pour rester elegant. Toutes calees sur viewBox 64x64.
	var ICONS = {
		caftan:  '<path d="M32 6L20 14v8l-6 4v22l4 8h28l4-8V26l-6-4v-8L32 6z" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M32 14v44M22 26h20" fill="none" stroke="currentColor" stroke-width="1"/>',
		sparkle: '<path d="M32 8l3 13 13 3-13 3-3 13-3-13-13-3 13-3z" fill="none" stroke="currentColor" stroke-width="1.5"/><circle cx="50" cy="18" r="2" fill="currentColor"/><circle cx="14" cy="46" r="2" fill="currentColor"/>',
		camera:  '<rect x="10" y="20" width="44" height="30" rx="3" fill="none" stroke="currentColor" stroke-width="1.5"/><circle cx="32" cy="35" r="9" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M22 20l4-6h12l4 6" fill="none" stroke="currentColor" stroke-width="1.5"/>',
		cake:    '<path d="M14 50V36c0-3 18-3 18 0v14M32 50V32c0-3 18-3 18 0v18" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M10 50h44" stroke="currentColor" stroke-width="1.5"/><path d="M23 32v-6M41 28v-6" stroke="currentColor" stroke-width="1.5"/>',
		drum:    '<ellipse cx="32" cy="20" rx="18" ry="6" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M14 20v24a18 6 0 0 0 36 0V20" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M20 26l-4 18M44 26l4 18" stroke="currentColor" stroke-width="1"/>',
		lute:    '<path d="M32 8c-4 0-4 6 0 6s4-6 0-6zM32 14v8M22 22h20l-4 24a8 8 0 0 1-16 0z" fill="none" stroke="currentColor" stroke-width="1.5"/><circle cx="32" cy="36" r="4" fill="none" stroke="currentColor" stroke-width="1"/>',
		arch:    '<path d="M14 56V28a18 18 0 0 1 36 0v28" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M22 56V36a10 10 0 0 1 20 0v20" fill="none" stroke="currentColor" stroke-width="1"/><path d="M32 28v28" stroke="currentColor" stroke-width="0.8"/>',
		tagine:  '<path d="M14 44h36" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M16 44a16 4 0 0 1 32 0" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M32 44V18l-8 12 8-4 8 4-8-12z" fill="none" stroke="currentColor" stroke-width="1.5"/>',
		lantern: '<path d="M32 6v6M26 12h12M22 16h20l-3 30a4 4 0 0 1-4 4H29a4 4 0 0 1-4-4z" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M28 22v22M32 22v22M36 22v22" stroke="currentColor" stroke-width="0.8"/>',
		wave:    '<path d="M6 32q6-12 12 0t12 0t12 0t12 0t12 0" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M6 42q6-8 12 0t12 0t12 0t12 0t12 0" fill="none" stroke="currentColor" stroke-width="1"/>',
		henna:   '<path d="M32 10c-8 6-8 14 0 20s8-14 0-20zM32 30v22M22 38c4-2 8-2 10 4M42 38c-4-2-8-2-10 4M18 50c8 2 12 2 14-2M46 50c-8 2-12 2-14-2" fill="none" stroke="currentColor" stroke-width="1.3"/>',
		flower:  '<circle cx="32" cy="32" r="6" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M32 8c-6 8-6 16 0 18s6-10 0-18zM32 56c6-8 6-16 0-18s-6 10 0 18zM8 32c8 6 16 6 18 0s-10-6-18 0zM56 32c-8 6-16 6-18 0s10-6 18 0z" fill="none" stroke="currentColor" stroke-width="1.3"/>',
		star:    '<path d="M32 8l6 18h18l-14 11 5 19-15-11-15 11 5-19-14-11h18z" fill="none" stroke="currentColor" stroke-width="1.5"/>'
	};

	function categorySlug(categoryName) {
		if (!categoryName) return '_default';
		var n = categoryName.toLowerCase()
			.replace(/[éèêë]/g, 'e')
			.replace(/[àâä]/g, 'a')
			.replace(/[îï]/g, 'i')
			.replace(/[ôö]/g, 'o')
			.replace(/[ûü]/g, 'u')
			.replace(/[ç]/g, 'c');
		if (/neggafa|negafa/.test(n))   return 'negafa';
		if (/maquillage|makeup|coif/.test(n)) return 'makeup';
		if (/photo|video/.test(n))      return 'photo';
		if (/cake|gateau|patiss/.test(n)) return 'cake';
		if (/issawa|dakka/.test(n))     return 'issawa';
		if (/orchestre|chanteur|musi/.test(n)) return 'orchestre';
		if (/decoration|fleurist/.test(n)) return /fleur/.test(n) ? 'fleuriste' : 'decoration';
		if (/salle|fete|riad/.test(n))  return 'salle';
		if (/traiteur|cater/.test(n))   return 'traiteur';
		if (/myadi|tyafr/.test(n))      return 'myadi';
		if (/^dj$|dj /.test(n))         return 'dj';
		if (/henna|henne/.test(n))      return 'hennaya';
		if (/transport/.test(n))        return 'transport';
		return '_default';
	}

	function placeholderHtml(slug, vendorName) {
		var spec = PLACEHOLDERS[slug] || PLACEHOLDERS._default;
		var iconPath = ICONS[spec.icon] || ICONS.star;
		var textColor = spec.dark ? 'rgba(255,255,255,0.18)' : 'rgba(139,26,43,0.15)';
		var initials = (vendorName || '').trim().split(/\s+/).slice(0, 2)
			.map(function(w){ return w.charAt(0).toUpperCase(); }).join('');
		var labelColor = spec.dark ? 'rgba(255,255,255,0.55)' : 'rgba(92,14,27,0.45)';
		return '<div class="vc-cover placeholder" style="background:linear-gradient(135deg,' +
			spec.from + ' 0%,' + spec.to + ' 100%);">' +
			'<svg viewBox="0 0 64 64" class="vc-cover-icon" aria-hidden="true" ' +
			'style="color:' + textColor + ';">' + iconPath + '</svg>' +
			(initials ? '<span class="vc-cover-initials" style="color:' + labelColor + ';">' +
				escapeHtml(initials) + '</span>' : '') +
			'</div>';
	}

	function coverHtml(vendor) {
		// MAJ UX : la carte n'affiche plus la photo de couverture (qui
		// chargeait inutilement des images lourdes en grille). On affiche
		// systematiquement un "logo Ayora" elegant avec les initiales du
		// prestataire au centre + un filigrane SVG par categorie. Les
		// photos restent disponibles dans le modal "Voir la galerie".
		var slug = categorySlug(vendor.category || vendor.vendorCategory);
		return cardLogoHtml(slug, vendor.vendorName || vendor.name);
	}

	function cardLogoHtml(slug, vendorName) {
		// Monogramme de luxe : initiales centrees, encadrees de deux filets
		// dores tres fins (au-dessus et en-dessous), avec en bas un libelle
		// de categorie discret. Aucun pictogramme decoratif en fond.
		var spec = PLACEHOLDERS[slug] || PLACEHOLDERS._default;
		var initials = (vendorName || '').trim().split(/\s+/).slice(0, 2)
			.map(function(w){ return w.charAt(0).toUpperCase(); }).join('') || '·';
		return '<div class="vc-cover logo" style="background:linear-gradient(135deg,' +
			spec.from + ' 0%,' + spec.to + ' 100%);">' +
			'<div class="vc-logo-frame">' +
				'<div class="vc-logo-monogram">' + escapeHtml(initials) + '</div>' +
				'<div class="vc-logo-label">' + escapeHtml(spec.label || 'Ayora') + '</div>' +
			'</div>' +
			'</div>';
	}

	function parseGallery(vendor) {
		var raw = (vendor.galleryUrls || '').trim();
		var urls = [];
		if (raw) {
			urls = raw.split('|').map(function(s){ return s.trim(); }).filter(function(s){ return s.length > 0; });
		}
		if (vendor.photoUrl) urls.unshift(vendor.photoUrl);
		// Dedupe en preservant l'ordre.
		var seen = {};
		return urls.filter(function(u){ if (seen[u]) return false; seen[u] = 1; return true; });
	}

	// ---- Helpers visuels et formattage --------------------------------

	function formatPriceShort(n) {
		var v = parseInt(n, 10) || 0;
		if (v >= 1000000) return (v/1000000).toFixed(1).replace('.0','') + 'M DHS';
		if (v >= 1000)    return (v/1000).toFixed(0) + 'K DHS';
		return v + ' DHS';
	}

	function escapeHtml(s) {
		return (s == null ? '' : String(s))
			.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;').replace(/'/g, '&#39;')
			.replace(/`/g, '&#96;');
	}
	function escapeAttr(s) { return escapeHtml(s); }
	function escapeJs(s)   { return (s == null ? '' : String(s)).replace(/\\/g,'\\\\').replace(/'/g,"\\'"); }

	function igHandle(raw) {
		if (!raw) return '';
		return raw.replace(/^@/, '').replace(/^https?:\/\/(www\.)?instagram\.com\//i, '').replace(/\/.*$/, '').trim();
	}

	function gammeClass(g) {
		if (g === 'ECONOMIQUE') return 'eco';
		if (g === 'MOYEN')      return 'moyen';
		if (g === 'PREMIUM')    return 'premium';
		return '';
	}
	function gammeLabel(g) {
		if (g === 'ECONOMIQUE') return 'Economique';
		if (g === 'MOYEN')      return 'Moyenne';
		if (g === 'PREMIUM')    return 'Premium';
		return g || '';
	}

	// ---- Rendu principal de la carte ---------------------------------

	function render(vendor, options) {
		options = options || {};
		var v = vendor;
		var name = v.vendorName || v.name || '';
		var category = v.category || v.vendorCategory || '';
		var city = v.city || v.vendorCity || '';
		var price = v.prixMin || v.vendorPrixMin || 0;
		var rating = v.rating || v.vendorRating || 0;
		var nbAvis = v.nbAvis || v.vendorNbAvis || 0;
		var gamme = v.gamme || v.vendorGamme || '';
		var instagram = igHandle(v.instagram || v.vendorInstagram);

		// Score (uniquement sur cartes IA, pas sur les picks).
		var scoreBadge = '';
		if (typeof v.score === 'number' && options.showScore !== false) {
			var sc = Math.round(v.score);
			var scoreCls = sc >= 85 ? 'excellent' : sc >= 70 ? 'good' : sc >= 55 ? 'ok' : 'low';
			// Etiquette qualitative : utilise AyoraFmt.scoreLabel si dispo, sinon fallback.
			var qualLabel = (window.AyoraFmt && AyoraFmt.scoreLabel)
				? AyoraFmt.scoreLabel(sc)
				: (sc >= 85 ? 'Excellent match' : sc >= 70 ? 'Très bon match' : sc >= 55 ? 'Bon match' : 'À comparer');
			scoreBadge = '<div class="vc-score ' + scoreCls + '" title="' + qualLabel + '">' +
				'<span class="vc-score-pct">' + sc + '%</span>' +
				'<span class="vc-score-lbl">' + qualLabel + '</span>' +
				'</div>';
		}

		var premiumBadge = gamme === 'PREMIUM'
			? '<div class="vc-premium" title="Prestataire Premium">Premium</div>'
			: '';

		// Tags : on en garde max 3, en priorisant les tags "remarquables"
		// (coup de coeur, luxe, eco, priorite).
		var tags = (v.tags || []).slice();
		tags.sort(function(a, b) {
			var pa = tagPriority(a), pb = tagPriority(b);
			return pb - pa;
		});
		var tagsHtml = '';
		var topTags = tags.slice(0, 3);
		for (var i = 0; i < topTags.length; i++) {
			tagsHtml += '<span class="vc-tag ' + tagClass(topTags[i]) + '">' +
				escapeHtml(topTags[i]) + '</span>';
		}

		// "Match highlights" : pastilles concretes qui montrent quelles
		// reponses du questionnaire ont guide cette recommandation. On les
		// affiche AVANT les tags genriques pour que le client voie
		// immediatement le lien IA -> questionnaire.
		var matches = (v.matchHighlights || []).slice(0, 4);
		var matchesHtml = '';
		if (matches.length > 0) {
			matchesHtml = '<div class="vc-matches" title="Liens detectes avec vos reponses au questionnaire">';
			for (var m = 0; m < matches.length; m++) {
				var cls = /hors budget/i.test(matches[m]) ? 'vc-match-warn' : 'vc-match-ok';
				matchesHtml += '<span class="vc-match ' + cls + '">' + escapeHtml(matches[m]) + '</span>';
			}
			matchesHtml += '</div>';
		}

		var ratingHtml = rating > 0
			? '<span class="vc-rating" title="' + nbAvis + ' avis">' +
				'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2l2.6 6 6.4.6-4.8 4.4 1.4 6.4L12 16.6 6.4 19.4 7.8 13 3 8.6l6.4-.6z"/></svg>' +
				rating.toFixed(1) +
				'</span>'
			: '';

		// Bouton "Voir la fiche" : ouvre le modal complet (galerie photos
		// + description + scores IA + contacts). event.stopPropagation()
		// evite que le clic remonte au <article> qui ouvre deja la fiche
		// (double-trigger).
		var detailBtn = '<button type="button" class="vc-btn vc-btn-detail" ' +
			'onclick="event.stopPropagation(); AyoraCard.openDetailFromAttr(this.closest(\'.vc-card\'))">Voir la fiche</button>';

		// Bouton Instagram (lien direct, nouvel onglet). stopPropagation
		// pour eviter d'ouvrir la fiche en meme temps que le profil Insta.
		var igBtn = instagram
			? '<a class="vc-btn vc-btn-ig" href="https://instagram.com/' + escapeAttr(instagram) +
				'" target="_blank" rel="noopener noreferrer" title="@' + escapeAttr(instagram) + '" ' +
				'onclick="event.stopPropagation()">' +
				'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2.2c3.2 0 3.6 0 4.8.1.9 0 1.6.2 2.2.4a4.5 4.5 0 0 1 2.3 2.3c.2.6.4 1.3.4 2.2.1 1.2.1 1.6.1 4.8s0 3.6-.1 4.8c0 .9-.2 1.6-.4 2.2a4.5 4.5 0 0 1-2.3 2.3c-.6.2-1.3.4-2.2.4-1.2.1-1.6.1-4.8.1s-3.6 0-4.8-.1c-.9 0-1.6-.2-2.2-.4a4.5 4.5 0 0 1-2.3-2.3c-.2-.6-.4-1.3-.4-2.2C2.2 15.6 2.2 15.2 2.2 12s0-3.6.1-4.8c0-.9.2-1.6.4-2.2A4.5 4.5 0 0 1 5 2.7c.6-.2 1.3-.4 2.2-.4C8.4 2.2 8.8 2.2 12 2.2zm0 1.8c-3.1 0-3.5 0-4.7.1-.9 0-1.4.2-1.7.3-.4.2-.7.4-1 .7s-.5.6-.7 1c-.1.3-.3.8-.3 1.7-.1 1.2-.1 1.6-.1 4.7s0 3.5.1 4.7c0 .9.2 1.4.3 1.7.2.4.4.7.7 1s.6.5 1 .7c.3.1.8.3 1.7.3 1.2.1 1.6.1 4.7.1s3.5 0 4.7-.1c.9 0 1.4-.2 1.7-.3.4-.2.7-.4 1-.7s.5-.6.7-1c.1-.3.3-.8.3-1.7.1-1.2.1-1.6.1-4.7s0-3.5-.1-4.7c0-.9-.2-1.4-.3-1.7a2.7 2.7 0 0 0-.7-1c-.3-.3-.6-.5-1-.7-.3-.1-.8-.3-1.7-.3-1.2-.1-1.6-.1-4.7-.1zm0 3.1a4.9 4.9 0 1 1 0 9.8 4.9 4.9 0 0 1 0-9.8zm0 8a3.1 3.1 0 1 0 0-6.2 3.1 3.1 0 0 0 0 6.2zm6.2-8.2a1.1 1.1 0 1 1-2.3 0 1.1 1.1 0 0 1 2.3 0z"/></svg>' +
				'<span>Instagram</span>' +
				'</a>'
			: '';

		// Bouton secondaire (Choisir / Retirer) optionnel, fourni par la
		// page hote via options.actionHtml. Wrappe dans un div avec
		// stopPropagation pour eviter que ces clics ouvrent la fiche.
		var actionHtml = options.actionHtml || '';

		// Pastille "deja choisi" optionnelle.
		var pickedBadge = options.picked ? '<div class="vc-picked">Retenu</div>' : '';
		var pickedClass = options.picked ? ' is-picked' : '';

		// Optional "Avis AYORA" button — opt-in via options.showAyoraOpinion.
		// Only the host page (recommendations.html) requests this button; the
		// vendor name/category/price are passed through window.AyoraAgent so
		// the assistant can ground its answer on visible card data only.
		var ayoraBtn = '';
		if (options.showAyoraOpinion && (typeof window !== 'undefined')) {
			var ctx = {
				name: name,
				category: category,
				gamme: gamme,
				priceMin: vendor.prixMin || vendor.vendorPrixMin || 0,
				priceMax: vendor.prixMax || vendor.vendorPrixMax || 0,
				city: city
			};
			var ctxJson = escapeAttr(JSON.stringify(ctx));
			ayoraBtn = '<button type="button" class="vc-ayora-opinion" data-ctx=\'' + ctxJson + '\' '
				+ 'onclick="event.stopPropagation(); if(window.AyoraAgent){try{AyoraAgent.openWithVendor(JSON.parse(this.dataset.ctx));}catch(e){AyoraAgent.open();}}">'
				+ '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" '
				+ 'stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>'
				+ 'Avis AYORA</button>';
		}

		// data-vendor sur l'<article> : la carte entiere est cliquable et
		// ouvre la fiche complete (meme action que le bouton "Voir la fiche").
		// Les boutons internes utilisent event.stopPropagation() pour ne
		// pas double-trigger.
		var vendorJson = escapeAttr(JSON.stringify(slimVendor(v)));
		return '<article class="vc-card' + pickedClass + '" data-vendor=\'' + vendorJson + '\' ' +
			'onclick="AyoraCard.openDetailFromAttr(this)" ' +
			'role="button" tabindex="0">' +
			coverHtml(v) +
			scoreBadge +
			premiumBadge +
			pickedBadge +
			'<div class="vc-body">' +
				'<div class="vc-cat">' + escapeHtml(category) + '</div>' +
				'<h3 class="vc-name">' + escapeHtml(name) + '</h3>' +
				'<div class="vc-meta">' +
					(city ? '<span class="vc-city">' +
						'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 22s7-7.6 7-13a7 7 0 1 0-14 0c0 5.4 7 13 7 13z" fill="none" stroke="currentColor" stroke-width="1.6"/><circle cx="12" cy="9" r="2.5" fill="none" stroke="currentColor" stroke-width="1.6"/></svg>' +
						escapeHtml(city) + '</span>' : '') +
					(rating > 0 ? ratingHtml : '') +
					(gamme ? '<span class="vc-gamme gamme-' + gammeClass(gamme) + '">' +
						escapeHtml(gammeLabel(gamme)) + '</span>' : '') +
				'</div>' +
				matchesHtml +
				(tagsHtml ? '<div class="vc-tags">' + tagsHtml + '</div>' : '') +
				'<div class="vc-price">A partir de <b>' + formatPriceShort(price) + '</b></div>' +
				'<div class="vc-actions" onclick="event.stopPropagation()">' +
					detailBtn +
					igBtn +
				'</div>' +
				(ayoraBtn ? '<div class="vc-ayora-row" onclick="event.stopPropagation()">' + ayoraBtn + '</div>' : '') +
				(actionHtml ? '<div class="vc-cta" onclick="event.stopPropagation()">' + actionHtml + '</div>' : '') +
			'</div>' +
		'</article>';
	}

	function tagPriority(t) {
		if (/coup de coeur/i.test(t)) return 10;
		if (/priorit/i.test(t))       return 9;
		if (/luxe|premium|chic/i.test(t)) return 8;
		if (/budget|qualite|prix|petit/i.test(t)) return 7;
		if (/hors budget/i.test(t))   return 6;
		return 1;
	}
	function tagClass(t) {
		if (/coup de coeur/i.test(t)) return 'tag-coup';
		if (/luxe|premium|chic/i.test(t)) return 'tag-luxe';
		if (/budget|qualite|prix|petit/i.test(t)) return 'tag-eco';
		if (/hors budget/i.test(t))   return 'tag-alert';
		if (/priorit/i.test(t))       return 'tag-priority';
		return '';
	}

	function slimVendor(v) {
		// Conserve uniquement ce qui est utile au modal pour ne pas
		// alourdir le data-attribute (les sous-scores et tags y sont
		// inclus pour permettre une vue "details" enrichie).
		return {
			vendorId: v.vendorId || v.id || 0,
			vendorName: v.vendorName || v.name || '',
			category: v.category || v.vendorCategory || '',
			city: v.city || v.vendorCity || '',
			gamme: v.gamme || v.vendorGamme || '',
			prixMin: v.prixMin || v.vendorPrixMin || 0,
			prixMax: v.prixMax || v.vendorPrixMax || 0,
			rating: v.rating || v.vendorRating || 0,
			nbAvis: v.nbAvis || v.vendorNbAvis || 0,
			instagram: v.instagram || v.vendorInstagram || '',
			phone: v.phone || v.vendorPhone || '',
			description: v.description || v.vendorDescription || '',
			photoUrl: v.photoUrl || '',
			galleryUrls: v.galleryUrls || '',
			reelUrl: v.reelUrl || '',
			raison: v.raison || '',
			tags: v.tags || [],
			matchHighlights: v.matchHighlights || [],
			scoreBudget: v.scoreBudget, scoreStyle: v.scoreStyle, scoreCity: v.scoreCity,
			scoreGuestCount: v.scoreGuestCount, scoreLuxe: v.scoreLuxe, scoreQuality: v.scoreQuality
		};
	}

	// ---- Modal galerie + reel + details ------------------------------

	function ensureModal() {
		var existing = document.getElementById('ayora-vendor-modal');
		if (existing) return existing;
		var m = document.createElement('div');
		m.id = 'ayora-vendor-modal';
		m.className = 'vc-modal';
		m.setAttribute('aria-hidden', 'true');
		m.setAttribute('role', 'dialog');
		m.innerHTML =
			'<div class="vc-modal-backdrop" onclick="AyoraCard.closeDetail()"></div>' +
			'<div class="vc-modal-panel" role="document">' +
				'<button type="button" class="vc-modal-close" aria-label="Fermer" ' +
					'onclick="AyoraCard.closeDetail()">&times;</button>' +
				'<div class="vc-modal-body"></div>' +
			'</div>';
		document.body.appendChild(m);
		// Raccourcis clavier : Echap (fermer), Fleches gauche/droite (naviguer)
		document.addEventListener('keydown', function(e) {
			var modal = document.getElementById('ayora-vendor-modal');
			if (!modal || !modal.classList.contains('is-open')) return;
			if (e.key === 'Escape') closeDetail();
			else if (e.key === 'ArrowLeft')  galNav(-1);
			else if (e.key === 'ArrowRight') galNav(1);
		});
		return m;
	}

	// Etat global du carrousel galerie (1 modal a la fois).
	var GAL_STATE = { urls: [], index: 0 };

	function openDetail(vendor) {
		var m = ensureModal();
		var body = m.querySelector('.vc-modal-body');
		body.innerHTML = detailBodyHtml(vendor);
		m.classList.add('is-open');
		m.setAttribute('aria-hidden', 'false');
		document.body.style.overflow = 'hidden';

		// Initialise le carrousel : index courant + URLs
		GAL_STATE.urls = parseGallery(vendor);
		GAL_STATE.index = 0;

		// Clic sur miniature -> change image principale
		var thumbs = body.querySelectorAll('.vc-gal-thumb');
		for (var i = 0; i < thumbs.length; i++) {
			(function(idx){
				thumbs[idx].addEventListener('click', function(){
					galGoTo(idx);
				});
			})(i);
		}
	}

	// Navigation carrousel : delta = -1 (prev) ou +1 (next)
	function galNav(delta) {
		if (!GAL_STATE.urls || GAL_STATE.urls.length <= 1) return;
		var n = GAL_STATE.urls.length;
		var newIdx = (GAL_STATE.index + delta + n) % n;
		galGoTo(newIdx);
	}

	function galGoTo(idx) {
		if (!GAL_STATE.urls || idx < 0 || idx >= GAL_STATE.urls.length) return;
		GAL_STATE.index = idx;
		var m = document.getElementById('ayora-vendor-modal');
		if (!m) return;
		var mainImg = m.querySelector('.vc-gal-main img');
		var counter = m.querySelector('.vc-gal-counter');
		if (mainImg) mainImg.src = GAL_STATE.urls[idx];
		if (counter) counter.textContent = (idx + 1) + ' / ' + GAL_STATE.urls.length;
		// Active la miniature correspondante + scroll into view
		var thumbs = m.querySelectorAll('.vc-gal-thumb');
		for (var i = 0; i < thumbs.length; i++) {
			if (i === idx) {
				thumbs[i].classList.add('active');
				thumbs[i].scrollIntoView({ block: 'nearest', inline: 'center', behavior: 'smooth' });
			} else {
				thumbs[i].classList.remove('active');
			}
		}
	}

	function openDetailFromAttr(btn) {
		try {
			var v = JSON.parse(btn.getAttribute('data-vendor') || '{}');
			openDetail(v);
		} catch (e) {
			console.error('Erreur ouverture details', e);
		}
	}

	function closeDetail() {
		var m = document.getElementById('ayora-vendor-modal');
		if (!m) return;
		m.classList.remove('is-open');
		m.setAttribute('aria-hidden', 'true');
		document.body.style.overflow = '';
	}

	function detailBodyHtml(v) {
		var name = v.vendorName || '';
		var category = v.category || '';
		var city = v.city || '';
		var gamme = v.gamme || '';
		var slug = categorySlug(category);
		var gallery = parseGallery(v);
		var mainImg = gallery[0] || '';

		// Galerie : carrousel navigable avec fleches prev/next + compteur
		var galleryHtml = '';
		if (gallery.length > 0) {
			var thumbs = '';
			for (var i = 0; i < gallery.length; i++) {
				thumbs += '<button type="button" class="vc-gal-thumb' + (i === 0 ? ' active' : '') +
					'" data-url="' + escapeAttr(gallery[i]) + '">' +
					'<img src="' + escapeAttr(gallery[i]) + '" alt="" loading="lazy" ' +
					'onerror="this.parentNode.style.display=\'none\'"/></button>';
			}
			// Fleches navigation : seulement si plusieurs photos
			var navArrows = '';
			var counter = '';
			if (gallery.length > 1) {
				navArrows =
					'<button type="button" class="vc-gal-nav vc-gal-prev" aria-label="Photo precedente" ' +
						'onclick="AyoraCard.galNav(-1)">' +
						'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>' +
					'</button>' +
					'<button type="button" class="vc-gal-nav vc-gal-next" aria-label="Photo suivante" ' +
						'onclick="AyoraCard.galNav(1)">' +
						'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 6l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>' +
					'</button>';
				counter = '<div class="vc-gal-counter">1 / ' + gallery.length + '</div>';
			}
			galleryHtml =
				'<div class="vc-gal">' +
					'<div class="vc-gal-main">' +
						'<img src="' + escapeAttr(mainImg) + '" alt="' + escapeAttr(name) +
							'" onerror="this.parentNode.innerHTML=AyoraCard._placeholderInline(\'' + slug + '\',\'' + escapeJs(name) + '\')"/>' +
						navArrows +
						counter +
					'</div>' +
					(gallery.length > 1 ? '<div class="vc-gal-thumbs">' + thumbs + '</div>' : '') +
				'</div>';
		} else {
			// Pas de photos : message propre plutot qu'un placeholder vide.
			galleryHtml =
				'<div class="vc-gal">' +
					'<div class="vc-gal-empty">' +
						'<svg viewBox="0 0 64 64" aria-hidden="true">' +
							'<rect x="8" y="14" width="48" height="36" rx="4" fill="none" stroke="currentColor" stroke-width="1.6"/>' +
							'<circle cx="22" cy="26" r="3" fill="currentColor"/>' +
							'<path d="M8 44l14-12 12 10 8-6 14 12" fill="none" stroke="currentColor" stroke-width="1.6"/>' +
						'</svg>' +
						'<div class="vc-gal-empty-title">Galerie bientot disponible</div>' +
						'<div class="vc-gal-empty-sub">Le prestataire n\'a pas encore publie ses photos.</div>' +
					'</div>' +
				'</div>';
		}

		// Reel Instagram : on n'embed pas (Instagram requiert un script
		// tiers + parfois un blocage CORS / login). On affiche une preview
		// statique avec un CTA "Voir le reel sur Instagram" qui s'ouvre
		// en nouvel onglet.
		var reelHtml = '';
		if (v.reelUrl) {
			reelHtml =
				'<div class="vc-reel">' +
					'<div class="vc-reel-head">Reel Instagram</div>' +
					'<div class="vc-reel-card">' +
						'<svg viewBox="0 0 64 64" class="vc-reel-icon" aria-hidden="true">' +
							'<path d="M12 8h40a4 4 0 0 1 4 4v40a4 4 0 0 1-4 4H12a4 4 0 0 1-4-4V12a4 4 0 0 1 4-4z" fill="none" stroke="currentColor" stroke-width="2"/>' +
							'<path d="M26 22v20l16-10z" fill="currentColor"/>' +
						'</svg>' +
						'<p>Decouvrez l\'ambiance en video sur le compte du prestataire.</p>' +
						'<a class="vc-btn vc-btn-ig vc-btn-lg" href="' + escapeAttr(v.reelUrl) +
							'" target="_blank" rel="noopener noreferrer">Voir le reel sur Instagram</a>' +
					'</div>' +
				'</div>';
		}

		// Description
		var descHtml = v.description
			? '<div class="vc-desc"><h4>A propos</h4><p>' + escapeHtml(v.description) + '</p></div>'
			: '';

		// Raison IA + match highlights (le lien concret avec le questionnaire).
		var raisonHtml = '';
		var mh = (v.matchHighlights || []);
		if (v.raison || mh.length > 0) {
			raisonHtml = '<div class="vc-reason"><h4>Pourquoi Ayora le recommande</h4>';
			if (mh.length > 0) {
				raisonHtml += '<div class="vc-matches" style="margin:0 0 10px;">';
				for (var mi = 0; mi < mh.length; mi++) {
					var mcls = /hors budget/i.test(mh[mi]) ? 'vc-match-warn' : 'vc-match-ok';
					raisonHtml += '<span class="vc-match ' + mcls + '">' + escapeHtml(mh[mi]) + '</span>';
				}
				raisonHtml += '</div>';
			}
			if (v.raison) raisonHtml += '<p>' + escapeHtml(v.raison) + '</p>';
			raisonHtml += '</div>';
		}

		var subsHtml = '';
		if (typeof v.scoreBudget === 'number') {
			var keys = [
				['scoreBudget','Budget','30%'],
				['scoreStyle','Style','25%'],
				['scoreCity','Ville','15%'],
				['scoreGuestCount','Invites','15%'],
				['scoreLuxe','Luxe','10%'],
				['scoreQuality','Qualite','5%']
			];
			var bars = '';
			for (var k = 0; k < keys.length; k++) {
				var val = Math.round(v[keys[k][0]] || 0);
				bars += '<div class="vc-sub"><span class="vc-sub-lbl">' + keys[k][1] +
					'</span><div class="vc-sub-bar"><div style="width:' + val + '%;"></div></div>' +
					'<span class="vc-sub-val">' + val + '</span></div>';
			}
			subsHtml = '<div class="vc-subs"><h4>Scoring IA Ayora</h4>' + bars + '</div>';
		}

		// Contact bar
		var contact = '<div class="vc-contact">';
		if (v.phone) {
			var clean = v.phone.replace(/[^\d+]/g, '');
			var wa = clean.replace(/^0/, '212');
			contact += '<a class="vc-btn" href="tel:' + escapeAttr(clean) + '">Appeler</a>';
			contact += '<a class="vc-btn vc-btn-wa" href="https://wa.me/' + escapeAttr(wa) +
				'" target="_blank" rel="noopener">WhatsApp</a>';
		}
		if (v.instagram) {
			var ig = igHandle(v.instagram);
			contact += '<a class="vc-btn vc-btn-ig" href="https://instagram.com/' + escapeAttr(ig) +
				'" target="_blank" rel="noopener">@' + escapeHtml(ig) + '</a>';
		}
		contact += '</div>';

		var price = v.prixMin > 0
			? 'A partir de <b>' + formatPriceShort(v.prixMin) + '</b>' +
				(v.prixMax > v.prixMin ? ' &middot; jusqu\'a ' + formatPriceShort(v.prixMax) : '')
			: '';

		return galleryHtml +
			'<div class="vc-modal-info">' +
				'<div class="vc-modal-cat">' + escapeHtml(category) + '</div>' +
				'<h2 class="vc-modal-name">' + escapeHtml(name) + '</h2>' +
				'<div class="vc-modal-meta">' +
					(city ? '<span>' + escapeHtml(city) + '</span>' : '') +
					(v.rating > 0 ? '<span>' + v.rating.toFixed(1) + ' (' + (v.nbAvis||0) + ' avis)</span>' : '') +
					(gamme ? '<span class="vc-gamme gamme-' + gammeClass(gamme) + '">' +
						escapeHtml(gammeLabel(gamme)) + '</span>' : '') +
				'</div>' +
				(price ? '<div class="vc-modal-price">' + price + '</div>' : '') +
				contact +
				descHtml +
				raisonHtml +
				subsHtml +
				reelHtml +
			'</div>';
	}

	// ---- Handlers exposes -------------------------------------------

	function onImgError(imgEl, slug, name) {
		var cover = imgEl.closest('.vc-cover');
		if (!cover) return;
		cover.outerHTML = placeholderHtml(slug, name);
	}

	function placeholderInline(slug, name) { return placeholderHtml(slug, name); }

	// API publique
	global.AyoraCard = {
		render: render,
		coverHtml: coverHtml,
		openDetail: openDetail,
		openDetailFromAttr: openDetailFromAttr,
		closeDetail: closeDetail,
		parseGallery: parseGallery,
		categorySlug: categorySlug,
		galNav: galNav,
		galGoTo: galGoTo,
		_onImgError: onImgError,
		_placeholderInline: placeholderInline
	};

})(window);
