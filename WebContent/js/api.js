/* ============================================
   AYORA - API Utility
   Communication avec le backend Java
   ============================================ */

var api = (function() {
	var BASE = '/ayora';
	var TIMEOUT_MS = 15000;

	// Toast helper qui marche que AyoraUI soit charge ou non.
	function toastErr(msg) {
		if (window.AyoraUI && AyoraUI.error) AyoraUI.error(msg);
		else console.error('[api] ' + msg);
	}

	function send(method, url, data) {
		var controller = (typeof AbortController !== 'undefined') ? new AbortController() : null;
		var opts = {
			method: method,
			credentials: 'include',
			headers: { 'Content-Type': 'application/json' }
		};
		if (data !== undefined) opts.body = JSON.stringify(data);
		if (controller) opts.signal = controller.signal;
		var timer = controller ? setTimeout(function() { controller.abort(); }, TIMEOUT_MS) : null;

		return fetch(BASE + url, opts)
			.then(function(response) {
				if (timer) clearTimeout(timer);
				// 401 sur une page protegee : on coupe propre.
				if (response.status === 401 && url !== '/api/auth/login' && url !== '/api/auth/me') {
					localStorage.removeItem('user');
					if (location.pathname.indexOf('login.html') === -1
						&& location.pathname.indexOf('register.html') === -1
						&& location.pathname.indexOf('index.html') === -1) {
						location.href = 'login.html';
					}
				}
				// 429 : rate-limit
				if (response.status === 429) {
					return response.json().catch(function(){ return {}; }).then(function(j) {
						toastErr(j.error || 'Trop de tentatives, reessayez plus tard.');
						throw new Error('rate-limited');
					});
				}
				// Parse JSON propre meme sur erreur (sinon renvoyer texte)
				return response.json().catch(function() { return { _nonJson: true, status: response.status }; });
			})
			.catch(function(err) {
				if (timer) clearTimeout(timer);
				if (err && err.name === 'AbortError') {
					toastErr('Le serveur met trop de temps a repondre. Reessayez.');
					throw err;
				}
				if (err && err.message === 'rate-limited') throw err;
				if (!navigator.onLine) toastErr('Pas de connexion internet.');
				else toastErr('Erreur reseau, reessayez.');
				throw err;
			});
	}

	return {
		baseUrl: BASE,
		get:  function(url)        { return send('GET',    url);       },
		post: function(url, data)  { return send('POST',   url, data); },
		put:  function(url, data)  { return send('PUT',    url, data); },
		del:  function(url)        { return send('DELETE', url);       }
	};
})();

/* ============================================
   Verification de session
   ============================================ */
function checkAuth() {
	var user = localStorage.getItem('user');
	if (!user) {
		location.href = 'login.html';
		return null;
	}
	var u = JSON.parse(user);
	// Garde-fou : l'admin ne doit JAMAIS naviguer sur les pages utilisateur
	// classiques (dashboard, recommendations, mychoices, questionnaire, ...).
	// On le redirige automatiquement vers son back-office admin.html.
	// Exceptions : admin.html lui-meme + login.html + index.html.
	if (u && u.role === 'ADMIN') {
		var page = location.pathname.split('/').pop().toLowerCase();
		var allowed = { 'admin.html':1, 'login.html':1, 'index.html':1, '':1 };
		if (!allowed[page]) {
			location.href = 'admin.html';
			return null;
		}
	}
	return u;
}

function checkRole(requiredRole) {
	var user = checkAuth();
	if (!user) return null;
	if (user.role !== requiredRole) {
		if (user.role === 'ADMIN') location.href = 'admin.html';
		else if (user.role === 'PRESTATAIRE') location.href = 'vendor-portal.html';
		else location.href = 'dashboard.html';
		return null;
	}
	return user;
}

function logout() {
	api.post('/api/auth/logout', {}).then(function() {
		localStorage.removeItem('user');
		location.href = 'login.html';
	}).catch(function() {
		localStorage.removeItem('user');
		location.href = 'login.html';
	});
}

/* ============================================
   Zone compte (navbar) - badge plan visible partout
   Appelle automatiquement /api/auth/me pour avoir le plan a jour
   et synchronise le DOM #userBadge sur toutes les pages.
   ============================================ */
function applyAccountHeader(user) {
	if (!user) return;
	var nameEl = document.getElementById('userName');
	if (nameEl && !nameEl.textContent) nameEl.textContent = user.firstName || 'Utilisateur';
	syncPlanBadge(user.subscriptionType);
	// Verifie aupres du serveur (au cas ou le plan a change)
	api.get('/api/auth/me').then(function(d) {
		if (d && d.user && d.user.subscriptionType) {
			syncPlanBadge(d.user.subscriptionType);
			// Mise a jour du localStorage pour cohérence
			try {
				var u = JSON.parse(localStorage.getItem('user') || '{}');
				u.subscriptionType = d.user.subscriptionType;
				localStorage.setItem('user', JSON.stringify(u));
			} catch(e) {}
		}
	}).catch(function(){});
}

function syncPlanBadge(plan) {
	var el = document.getElementById('userBadge');
	if (!el) return;
	var p = (plan || 'FREE').toUpperCase();
	el.textContent = p;
	el.className = 'badge-' + p.toLowerCase();
}

/* ============================================
   Fonctions utilitaires
   ============================================ */
function formatPrice(price) {
	if (!price || price <= 0) return '-';
	return price.toLocaleString('fr-MA') + ' DHS';
}

function getGammeLabel(gamme) {
	if (gamme === 'ECONOMIQUE') return 'Economique';
	if (gamme === 'MOYEN') return 'Moyen';
	if (gamme === 'PREMIUM') return 'Premium';
	return gamme;
}

function getGammeClass(gamme) {
	if (gamme === 'ECONOMIQUE') return 'gamme-economique';
	if (gamme === 'MOYEN') return 'gamme-moyen';
	if (gamme === 'PREMIUM') return 'gamme-premium';
	return '';
}

function getScoreClass(score) {
	if (score >= 70) return 'score-high';
	if (score >= 40) return 'score-medium';
	return 'score-low';
}

function getGroupeLabel(groupe) {
	var labels = {
		'FAMILLE_MARIEE': 'Famille de la mariee',
		'FAMILLE_MARIE': 'Famille du marie',
		'AMIS_MARIEE': 'Amis de la mariee',
		'AMIS_MARIE': 'Amis du marie',
		'COLLEGUES': 'Collegues',
		'AUTRES': 'Autres'
	};
	return labels[groupe] || groupe;
}

function getStatutLabel(statut) {
	var labels = {
		'EN_ATTENTE': 'En attente',
		'ENVOYEE': 'Envoyee',
		'CONFIRMEE': 'Confirmee',
		'DECLINEE': 'Declinee'
	};
	return labels[statut] || statut;
}
