/* ============================================
   AYORA - API Utility
   Communication avec le backend Java
   ============================================ */

var api = {

	baseUrl: '/ayora',

	get: function(url) {
		return fetch(api.baseUrl + url, {
			method: 'GET',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(function(response) {
			return response.json();
		});
	},

	post: function(url, data) {
		return fetch(api.baseUrl + url, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		}).then(function(response) {
			return response.json();
		});
	},

	put: function(url, data) {
		return fetch(api.baseUrl + url, {
			method: 'PUT',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		}).then(function(response) {
			return response.json();
		});
	},

	del: function(url) {
		return fetch(api.baseUrl + url, {
			method: 'DELETE',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(function(response) {
			return response.json();
		});
	}
};

/* ============================================
   Verification de session
   ============================================ */
function checkAuth() {
	var user = localStorage.getItem('user');
	if (!user) {
		location.href = 'login.html';
		return null;
	}
	return JSON.parse(user);
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
