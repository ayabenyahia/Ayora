/* ============================================================
   AYORA - Utilitaires UI partages
   ------------------------------------------------------------
   - Toast notifications (remplace alert() natif)
   - Banniere cookies / RGPD
   - Modal de confirmation
   API publique : window.AyoraUI
   ============================================================ */

(function(global) {

	// ---- TOAST NOTIFICATIONS ------------------------------------
	var toastContainer = null;

	function ensureToastContainer() {
		if (toastContainer) return toastContainer;
		toastContainer = document.createElement('div');
		toastContainer.className = 'toast-container';
		document.body.appendChild(toastContainer);
		return toastContainer;
	}

	function icon(type) {
		switch (type) {
			case 'success': return '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3D8B5E" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="9,12 11,14 15,10"/></svg>';
			case 'error':   return '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#8B1A2B" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="9" y1="9" x2="15" y2="15"/><line x1="15" y1="9" x2="9" y2="15"/></svg>';
			case 'warning': return '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#C67200" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>';
			default:        return '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#D4A853" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>';
		}
	}

	/**
	 * Affiche un toast.
	 * @param {string} message
	 * @param {string} type - 'success' | 'error' | 'warning' | 'info' (defaut)
	 * @param {number} duration - millisecondes (defaut 4000)
	 */
	function toast(message, type, duration) {
		type = type || 'info';
		duration = duration || 4000;
		var box = ensureToastContainer();
		var el = document.createElement('div');
		el.className = 'toast toast-' + type;
		el.innerHTML =
			'<div class="toast-icon">' + icon(type) + '</div>' +
			'<div class="toast-content">' + escapeHtml(message) + '</div>' +
			'<button type="button" class="toast-close" aria-label="Fermer">×</button>';
		var closeBtn = el.querySelector('.toast-close');
		var timer;
		var remove = function() {
			if (timer) clearTimeout(timer);
			el.classList.add('toast-out');
			setTimeout(function() {
				if (el.parentNode) el.parentNode.removeChild(el);
			}, 300);
		};
		closeBtn.addEventListener('click', remove);
		timer = setTimeout(remove, duration);
		box.appendChild(el);
	}

	function escapeHtml(s) {
		return (s == null ? '' : String(s))
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;')
			.replace(/'/g, '&#39;')
			.replace(/`/g, '&#96;');
	}
	// Expose en global pour que toutes les pages puissent l'utiliser
	// sans redefinir leur propre version (defense XSS uniforme).
	if (typeof window !== 'undefined') window.escapeHtml = escapeHtml;

	// ---- COOKIES BANNER --------------------------------------------
	var COOKIES_KEY = 'ayora_cookies_accepted';

	function initCookiesBanner() {
		// N'affiche pas si deja accepte/refuse
		if (localStorage.getItem(COOKIES_KEY)) return;
		// N'affiche pas sur les pages legales (pour ne pas gêner la lecture)
		if (/(cgu|confidentialite|mentions|contact|404)\.html/i.test(location.pathname)) return;

		var banner = document.createElement('div');
		banner.className = 'cookies-banner';
		banner.innerHTML =
			'<div class="cookies-text">' +
				'Ayora utilise des cookies strictement nécessaires au fonctionnement du service (session, préférences). ' +
				'Aucun suivi publicitaire. <a href="confidentialite.html" target="_blank">En savoir plus</a>' +
			'</div>' +
			'<div class="cookies-actions">' +
				'<button type="button" class="btn-cookies-decline">Refuser</button>' +
				'<button type="button" class="btn-cookies-accept">J\'accepte</button>' +
			'</div>';
		document.body.appendChild(banner);

		banner.querySelector('.btn-cookies-accept').addEventListener('click', function() {
			localStorage.setItem(COOKIES_KEY, 'accepted');
			banner.remove();
		});
		banner.querySelector('.btn-cookies-decline').addEventListener('click', function() {
			localStorage.setItem(COOKIES_KEY, 'declined');
			banner.remove();
		});
	}

	// ---- CONFIRM MODAL --------------------------------------------
	/**
	 * Remplace window.confirm() avec un style coherent Ayora.
	 * @param {object} opts - { title, message, confirmLabel, cancelLabel, danger, onConfirm }
	 */
	function confirm(opts) {
		opts = opts || {};
		var backdrop = document.createElement('div');
		backdrop.style.cssText = 'position:fixed;inset:0;background:rgba(42,30,22,0.55);z-index:9998;display:flex;align-items:center;justify-content:center;padding:20px;animation:fadeIn 0.2s;';
		backdrop.innerHTML =
			'<div style="background:#fff;border-radius:14px;padding:28px;max-width:420px;width:100%;box-shadow:0 30px 60px rgba(0,0,0,0.25);font-family:inherit;">' +
				'<h3 style="font-family:\'Cormorant Garamond\',serif;font-size:24px;color:' + (opts.danger ? '#8B1A2B' : '#4a3a2e') + ';margin:0 0 12px;">' + escapeHtml(opts.title || 'Confirmation') + '</h3>' +
				'<p style="color:#6a5a4e;font-size:14px;line-height:1.5;margin:0 0 22px;">' + escapeHtml(opts.message || 'Confirmer cette action ?') + '</p>' +
				'<div style="display:flex;gap:10px;justify-content:flex-end;">' +
					'<button type="button" class="btn btn-secondary" data-action="cancel">' + escapeHtml(opts.cancelLabel || 'Annuler') + '</button>' +
					'<button type="button" class="btn ' + (opts.danger ? 'btn-danger' : 'btn-primary') + '" data-action="ok">' + escapeHtml(opts.confirmLabel || 'Confirmer') + '</button>' +
				'</div>' +
			'</div>';
		document.body.appendChild(backdrop);

		var close = function() { if (backdrop.parentNode) backdrop.parentNode.removeChild(backdrop); };
		backdrop.addEventListener('click', function(e) { if (e.target === backdrop) close(); });
		backdrop.querySelector('[data-action="cancel"]').addEventListener('click', close);
		backdrop.querySelector('[data-action="ok"]').addEventListener('click', function() {
			close();
			if (typeof opts.onConfirm === 'function') opts.onConfirm();
		});
	}

	// ---- HAMBURGER MENU (mobile) ---------------------------------
	// Sur mobile < 768px, .menu-bar nav est masquee et un bouton hamburger
	// apparait. Click = ouverture d'un drawer slide-in qui re-affiche les
	// liens en colonne. Aucune modification HTML necessaire : ce script
	// injecte le bouton et le drawer.
	function initHamburger() {
		var bar = document.querySelector('.menu-bar');
		if (!bar) return;
		if (bar.querySelector('.menu-burger')) return; // deja initialise

		var nav = bar.querySelector('nav');
		if (!nav) return;

		// Bouton hamburger insere avant le nav
		var burger = document.createElement('button');
		burger.type = 'button';
		burger.className = 'menu-burger';
		burger.setAttribute('aria-label', 'Ouvrir le menu');
		burger.setAttribute('aria-expanded', 'false');
		burger.innerHTML = '<span></span><span></span><span></span>';
		bar.appendChild(burger);

		// Overlay sombre derriere le drawer
		var overlay = document.createElement('div');
		overlay.className = 'menu-overlay';
		document.body.appendChild(overlay);

		function open() {
			nav.classList.add('is-open');
			overlay.classList.add('is-open');
			burger.classList.add('is-open');
			burger.setAttribute('aria-expanded', 'true');
			document.body.style.overflow = 'hidden';
		}
		function close() {
			nav.classList.remove('is-open');
			overlay.classList.remove('is-open');
			burger.classList.remove('is-open');
			burger.setAttribute('aria-expanded', 'false');
			document.body.style.overflow = '';
		}

		burger.addEventListener('click', function() {
			if (nav.classList.contains('is-open')) close(); else open();
		});
		overlay.addEventListener('click', close);

		// Fermer auto au click sur un lien
		var links = nav.querySelectorAll('span, a');
		for (var i = 0; i < links.length; i++) links[i].addEventListener('click', close);

		// ESC pour fermer
		document.addEventListener('keydown', function(e) {
			if (e.key === 'Escape' && nav.classList.contains('is-open')) close();
		});
	}

	// ---- SUBMIT BUTTON PROTECTION --------------------------------
	// Empeche le double-click sur un bouton qui declenche une requete.
	// Usage : AyoraUI.lockButton(btn) avant fetch, AyoraUI.unlockButton(btn) au .then/.catch.
	function lockButton(btn) {
		if (!btn) return;
		if (btn.dataset.ayoraLabel == null) btn.dataset.ayoraLabel = btn.innerHTML;
		btn.disabled = true;
		btn.setAttribute('aria-busy', 'true');
		btn.innerHTML = '<span class="ayora-btn-spinner" aria-hidden="true"></span> En cours...';
	}
	function unlockButton(btn) {
		if (!btn) return;
		btn.disabled = false;
		btn.removeAttribute('aria-busy');
		if (btn.dataset.ayoraLabel != null) {
			btn.innerHTML = btn.dataset.ayoraLabel;
			delete btn.dataset.ayoraLabel;
		}
	}

	// Auto-init cookies banner + hamburger au chargement
	function autoInit() {
		initCookiesBanner();
		initHamburger();
	}
	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', autoInit);
	} else {
		autoInit();
	}

	// API publique
	global.AyoraUI = {
		toast: toast,
		success: function(msg, d) { toast(msg, 'success', d); },
		error:   function(msg, d) { toast(msg, 'error', d); },
		warning: function(msg, d) { toast(msg, 'warning', d); },
		info:    function(msg, d) { toast(msg, 'info', d); },
		confirm: confirm,
		lockButton: lockButton,
		unlockButton: unlockButton,
		initHamburger: initHamburger
	};

})(window);
