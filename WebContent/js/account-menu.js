/* ============================================================
   AYORA — Account menu.

   Drop-in script that enhances the legacy ".user-info" block on every
   private page into a premium dropdown menu (avatar + name + Profil /
   Paramètres / Sécurité / Déconnexion).

   Safety: if this script fails to load OR throws, the original
   ".user-info" remains untouched and the existing logout() span still
   works. Nothing is removed from the DOM — the legacy elements are
   hidden via the ".user-info.acc-enhanced" CSS rule.
   ============================================================ */

(function() {
	'use strict';

	function ready(fn) {
		if (document.readyState === 'loading') {
			document.addEventListener('DOMContentLoaded', fn);
		} else {
			fn();
		}
	}

	function el(tag, cls, html) {
		var e = document.createElement(tag);
		if (cls) e.className = cls;
		if (html !== undefined) e.innerHTML = html;
		return e;
	}

	function getInitials(firstName, lastName, email) {
		var src = (firstName || '') + ' ' + (lastName || '');
		src = src.trim();
		if (!src) src = email || '';
		if (!src) return 'A';
		var parts = src.split(/\s+/);
		if (parts.length >= 2) {
			return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
		}
		return src.substring(0, 2).toUpperCase();
	}

	function escapeHtml(s) {
		return String(s == null ? '' : s)
			.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	}

	function buildMenu(userInfoEl, user) {
		// Find or fall back to localStorage user.
		var u = user || (function() {
			try { return JSON.parse(localStorage.getItem('user') || 'null'); }
			catch (e) { return null; }
		})();
		if (!u) return;

		var firstName = u.firstName || '';
		var lastName  = u.lastName  || '';
		var email     = u.email     || '';
		var plan      = (u.subscriptionType || 'FREE').toUpperCase();
		var initials  = getInitials(firstName, lastName, email);
		var displayName = (firstName + (lastName ? ' ' + lastName.charAt(0) + '.' : '')).trim() || email;

		// Avoid double-enhancement (e.g. if the script is included twice).
		if (userInfoEl.classList.contains('acc-enhanced')) return;
		userInfoEl.classList.add('acc-enhanced');

		var iconUser = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>';
		var iconCog  = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9 1.65 1.65 0 0 0 4.27 7.18l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>';
		var iconShield = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>';
		var iconOut  = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>';
		var iconCaret = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="acc-menu-caret"><polyline points="6 9 12 15 18 9"/></svg>';

		var planClass = plan === 'PREMIUM' ? 'badge-premium' : (plan === 'PRO' ? 'badge-pro' : 'badge-free');

		var wrapper = el('div', 'acc-menu');
		wrapper.innerHTML =
			'<button type="button" class="acc-menu-trigger" aria-haspopup="true" aria-expanded="false">' +
				'<span class="acc-menu-avatar">' + escapeHtml(initials) + '</span>' +
				'<span class="acc-menu-name">' + escapeHtml(displayName) + '</span>' +
				iconCaret +
			'</button>' +
			'<div class="acc-menu-panel" role="menu" aria-label="Menu utilisateur">' +
				'<div class="acc-menu-header">' +
					'<div class="av">' + escapeHtml(initials) + '</div>' +
					'<div class="meta">' +
						'<span class="nm">' + escapeHtml((firstName + ' ' + lastName).trim() || email) + '</span>' +
						'<span class="em">' + escapeHtml(email) + '</span>' +
					'</div>' +
				'</div>' +
				'<a class="acc-menu-item" href="settings.html#profile" role="menuitem">' +
					iconUser + 'Mon profil' +
				'</a>' +
				'<a class="acc-menu-item" href="settings.html" role="menuitem">' +
					iconCog + 'Paramètres' +
					'<span class="acc-menu-badge ' + planClass + '">' + escapeHtml(plan) + '</span>' +
				'</a>' +
				'<a class="acc-menu-item" href="settings.html#security" role="menuitem">' +
					iconShield + 'Sécurité' +
				'</a>' +
				'<div class="acc-menu-sep"></div>' +
				'<button type="button" class="acc-menu-item danger" role="menuitem" data-action="logout">' +
					iconOut + 'Se déconnecter' +
				'</button>' +
			'</div>';

		userInfoEl.appendChild(wrapper);

		var trigger = wrapper.querySelector('.acc-menu-trigger');
		var panel   = wrapper.querySelector('.acc-menu-panel');
		var logoutBtn = wrapper.querySelector('[data-action="logout"]');

		function open() {
			wrapper.classList.add('is-open');
			trigger.setAttribute('aria-expanded', 'true');
		}
		function close() {
			wrapper.classList.remove('is-open');
			trigger.setAttribute('aria-expanded', 'false');
		}
		function toggle(ev) {
			if (ev) ev.stopPropagation();
			if (wrapper.classList.contains('is-open')) close(); else open();
		}

		trigger.addEventListener('click', toggle);
		document.addEventListener('click', function(ev) {
			if (!wrapper.contains(ev.target)) close();
		});
		document.addEventListener('keydown', function(ev) {
			if (ev.key === 'Escape') close();
		});
		logoutBtn.addEventListener('click', function() {
			close();
			if (typeof window.logout === 'function') {
				window.logout();
			} else {
				location.href = 'login.html';
			}
		});
	}

	function init() {
		var bar = document.querySelector('.user-info');
		if (!bar) return;
		// `applyAccountHeader` runs synchronously on most pages right after
		// checkAuth() and writes the firstName + badge into the legacy spans.
		// We let it finish via a microtask so the localStorage user is also
		// populated when we read it.
		setTimeout(function() {
			try {
				var stored = null;
				try { stored = JSON.parse(localStorage.getItem('user') || 'null'); }
				catch (e) {}
				buildMenu(bar, stored);
			} catch (e) {
				// Silently leave the legacy header in place — never crash the page.
				console.warn('[account-menu] could not build dropdown:', e);
			}
		}, 0);
	}

	ready(init);
})();
