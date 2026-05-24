/* ============================================================
   AYORA - Helpers de formatage (date / devise / texte)
   ------------------------------------------------------------
   Centralise pour cohérence cross-pages. À inclure AVANT
   ayora-ui.js et avant tout script de page.

   API publique : window.AyoraFmt
   ============================================================ */
(function(global) {

	// ---- Devise (toujours en DHS) ----
	function price(n) {
		var v = parseInt(n, 10) || 0;
		if (v >= 1000000) return (v / 1000000).toFixed(1).replace('.0','') + ' M DHS';
		if (v >= 1000)    return Math.round(v / 1000) + 'K DHS';
		return v + ' DHS';
	}

	// Prix avec deux décimales pour les pages d'achat/devis
	function priceFull(n) {
		var v = parseFloat(n) || 0;
		return v.toLocaleString('fr-FR', { minimumFractionDigits: 0, maximumFractionDigits: 0 }) + ' DHS';
	}

	// ---- Dates ----
	var MONTHS = ['janvier','février','mars','avril','mai','juin','juillet','août','septembre','octobre','novembre','décembre'];
	var DAYS = ['dimanche','lundi','mardi','mercredi','jeudi','vendredi','samedi'];

	/** "2026-06-14" -> "14 juin 2026" */
	function date(s) {
		if (!s) return '';
		var d = parseISO(s);
		if (!d) return s;
		return d.getDate() + ' ' + MONTHS[d.getMonth()] + ' ' + d.getFullYear();
	}

	/** "2026-06-14" -> "samedi 14 juin 2026" */
	function dateLong(s) {
		if (!s) return '';
		var d = parseISO(s);
		if (!d) return s;
		return DAYS[d.getDay()] + ' ' + d.getDate() + ' ' + MONTHS[d.getMonth()] + ' ' + d.getFullYear();
	}

	/** "2026-06-14" -> "14/06/2026" (style français court) */
	function dateShort(s) {
		if (!s) return '';
		var d = parseISO(s);
		if (!d) return s;
		var dd = String(d.getDate()).padStart(2, '0');
		var mm = String(d.getMonth() + 1).padStart(2, '0');
		return dd + '/' + mm + '/' + d.getFullYear();
	}

	/** "2026-01-14 18:30:00" -> "14/01/2026 à 18:30" */
	function dateTime(s) {
		if (!s) return '';
		var parts = String(s).split(/[T ]/);
		var d = parseISO(parts[0]);
		if (!d) return s;
		var t = parts[1] ? parts[1].substring(0, 5) : '';
		return dateShort(parts[0]) + (t ? ' à ' + t : '');
	}

	function parseISO(s) {
		if (!s) return null;
		s = String(s).trim();
		// Format "YYYY-MM-DD" ou "YYYY-MM-DD HH:mm:ss"
		var m = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
		if (!m) return null;
		var y = parseInt(m[1], 10);
		var mo = parseInt(m[2], 10) - 1;
		var d = parseInt(m[3], 10);
		return new Date(y, mo, d);
	}

	/** Nombre de jours jusqu'à une date (peut être négatif si passée). */
	function daysUntil(s) {
		var target = parseISO(s);
		if (!target) return null;
		var today = new Date();
		today.setHours(0, 0, 0, 0);
		target.setHours(0, 0, 0, 0);
		var diffMs = target.getTime() - today.getTime();
		return Math.round(diffMs / (1000 * 60 * 60 * 24));
	}

	/** "Plus que 32 jours" / "Dans 1 an et 2 mois" / "C'est aujourd'hui !" */
	function timeUntil(s) {
		var days = daysUntil(s);
		if (days == null) return '';
		if (days < 0) return 'Mariage passé (il y a ' + Math.abs(days) + ' jour' + (Math.abs(days)>1?'s':'') + ')';
		if (days === 0) return "C'est aujourd'hui !";
		if (days === 1) return 'Demain !';
		if (days < 30)  return 'Plus que ' + days + ' jours';
		if (days < 60)  return 'Plus que ' + Math.round(days / 7) + ' semaines';
		if (days < 365) return 'Dans ' + Math.round(days / 30) + ' mois';
		var years = Math.floor(days / 365);
		var months = Math.round((days % 365) / 30);
		if (months === 0) return 'Dans ' + years + ' an' + (years > 1 ? 's' : '');
		return 'Dans ' + years + ' an' + (years > 1 ? 's' : '') + ' et ' + months + ' mois';
	}

	// ---- Texte ----
	/** Première lettre en majuscule, le reste en minuscule. */
	function capitalize(s) {
		if (!s) return '';
		s = String(s).toLowerCase();
		return s.charAt(0).toUpperCase() + s.slice(1);
	}

	/** "ROYAL_FASSI" -> "Royal fassi" — pour afficher proprement les enums backend. */
	function humanEnum(s) {
		if (!s) return '';
		return capitalize(String(s).replace(/_/g, ' ').toLowerCase());
	}

	/** Traduit un enum métier en libellé français lisible. */
	var ENUM_LABELS = {
		// Type de mariage
		'TRADITIONNEL': 'Traditionnel',
		'MODERNE': 'Moderne',
		'MIXTE': 'Mixte',
		'LUXE': 'Luxe',
		'INTIME': 'Intime',
		'SIMPLE': 'Simple',
		// Lieu cérémonie
		'RIAD': 'Riad / Palais',
		'SALLE': 'Salle de fête',
		'JARDIN': 'Jardin / Extérieur',
		'PISCINE': 'Bord de piscine',
		'HOTEL': 'Hôtel / Resort',
		'DOMICILE': 'À domicile',
		// Saison
		'ETE': 'Été',
		'AUTOMNE': 'Automne',
		'HIVER': 'Hiver',
		'PRINTEMPS': 'Printemps',
		// Flexibilité budget
		'STRICT': 'Strict',
		'FLEXIBLE': 'Flexible (+20%)',
		'TRES_FLEXIBLE': 'Très flexible',
		// Niveau luxe
		'ECONOMIQUE': 'Économique',
		'MOYEN': 'Moyen',
		'PREMIUM': 'Premium',
		'ULTRA_LUXE': 'Ultra luxe',
		// Plans
		'FREE': 'Gratuit',
		'PRO': 'Pro',
		// Status devis/rdv
		'EN_ATTENTE': 'En attente',
		'ACCEPTE': 'Accepté',
		'REFUSE': 'Refusé',
		'CONFIRME': 'Confirmé',
		'ANNULE': 'Annulé',
		'ENVOYEE': 'Envoyée',
		'CONFIRMEE': 'Confirmée',
		'DECLINEE': 'Refusée'
	};

	function label(s) {
		if (!s) return '';
		return ENUM_LABELS[String(s).toUpperCase()] || humanEnum(s);
	}

	// ---- Score qualitatif ----
	/** Score numérique 0-100 -> label qualitatif. */
	function scoreLabel(score) {
		var s = parseFloat(score) || 0;
		if (s >= 85) return 'Excellent match';
		if (s >= 70) return 'Très bon match';
		if (s >= 55) return 'Bon match';
		if (s >= 40) return 'À considérer';
		return 'À comparer';
	}

	function scoreClass(score) {
		var s = parseFloat(score) || 0;
		if (s >= 85) return 'excellent';
		if (s >= 70) return 'good';
		if (s >= 55) return 'ok';
		return 'low';
	}

	// API publique
	global.AyoraFmt = {
		price: price,
		priceFull: priceFull,
		date: date,
		dateLong: dateLong,
		dateShort: dateShort,
		dateTime: dateTime,
		daysUntil: daysUntil,
		timeUntil: timeUntil,
		capitalize: capitalize,
		humanEnum: humanEnum,
		label: label,
		scoreLabel: scoreLabel,
		scoreClass: scoreClass
	};

})(window);
