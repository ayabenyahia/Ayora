/* ============================================================
   AYORA — Floating contextual assistant.

   Drop this script on any page that wants a contextual chat
   button. Add a <script> tag with data-ayora-page="<page>"
   and the widget will inject its FAB + drawer automatically.

   Example:
     <link rel="stylesheet" href="css/assistant.css">
     <script src="js/assistant.js"></script>
     <script src="js/assistant-floating.js" data-ayora-page="budget"></script>
   ============================================================ */

(function() {
	'use strict';

	var SCRIPT = document.currentScript;
	var PAGE = SCRIPT ? (SCRIPT.dataset.ayoraPage || 'unknown') : 'unknown';

	function el(tag, cls, html) {
		var e = document.createElement(tag);
		if (cls) e.className = cls;
		if (html !== undefined) e.innerHTML = html;
		return e;
	}

	function buildWidget() {
		// Floating action button
		var fab = el('button', 'ayora-fab');
		fab.type = 'button';
		fab.setAttribute('aria-label', 'Ouvrir AYORA, ton assistante de mariage');
		fab.innerHTML = '<span class="fab-glyph">A</span>';

		// Backdrop
		var backdrop = el('div', 'ayora-drawer-backdrop');

		// Drawer
		var drawer = el('aside', 'ayora-drawer');
		drawer.innerHTML =
			'<header class="ayora-drawer-header">' +
				'<div class="av">A</div>' +
				'<h4>Assistante AYORA</h4>' +
				'<span class="ctx-pill">' + escapePageLabel(PAGE) + '</span>' +
				'<button type="button" class="ayora-drawer-reset" aria-label="Nouvelle conversation" title="Nouvelle conversation">' +
					'<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
						'<polyline points="1 4 1 10 7 10"/>' +
						'<path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>' +
					'</svg>' +
				'</button>' +
				'<button type="button" class="ayora-drawer-close" aria-label="Fermer">' +
					'<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
						'<line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>' +
					'</svg>' +
				'</button>' +
			'</header>' +
			'<div class="assistant-stream" id="__ayora_drawer_stream__"></div>' +
			'<form class="assistant-composer" id="__ayora_drawer_form__">' +
				'<div class="composer-suggest" id="__ayora_drawer_chips__"></div>' +
				'<div class="composer-row">' +
					'<textarea id="__ayora_drawer_input__" rows="1" ' +
						'placeholder="Pose ta question…"></textarea>' +
					'<button type="submit" class="composer-send" aria-label="Envoyer">' +
						'<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
							'<line x1="22" y1="2" x2="11" y2="13"/>' +
							'<polygon points="22 2 15 22 11 13 2 9 22 2"/>' +
						'</svg>' +
					'</button>' +
				'</div>' +
				'<div class="composer-hint">' +
					'<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">' +
						'<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><line x1="12" y1="8" x2="12.01" y2="8"/>' +
					'</svg>' +
					'AYORA ne propose jamais un prestataire ou un prix sans données réelles.' +
				'</div>' +
			'</form>';

		document.body.appendChild(backdrop);
		document.body.appendChild(drawer);
		document.body.appendChild(fab);

		var stream = drawer.querySelector('#__ayora_drawer_stream__');
		var input = drawer.querySelector('#__ayora_drawer_input__');
		var form = drawer.querySelector('#__ayora_drawer_form__');
		var closeBtn = drawer.querySelector('.ayora-drawer-close');
		var resetBtn = drawer.querySelector('.ayora-drawer-reset');
		var chipsBox = drawer.querySelector('#__ayora_drawer_chips__');

		injectChips(chipsBox, PAGE, function(question) {
			input.value = question;
			submit();
		});

		var hasGreeted = false;
		function open(customGreeting) {
			drawer.classList.add('open');
			backdrop.classList.add('open');
			if (!hasGreeted) {
				AyoraAssistant.renderAssistantMessage(stream, {
					answer: customGreeting || contextualGreeting(PAGE),
					languageStyle: 'french',
					grounded: false,
					responseValid: true,
					contextTypesUsed: []
				});
				hasGreeted = true;
			}
			setTimeout(function() { input.focus(); }, 250);
		}

		function close() {
			drawer.classList.remove('open');
			backdrop.classList.remove('open');
		}

		fab.addEventListener('click', function() { open(); });
		backdrop.addEventListener('click', close);
		closeBtn.addEventListener('click', close);
		resetBtn.addEventListener('click', function() {
			if (!stream.querySelector('.msg')) return; // nothing to clear
			AyoraAssistant.resetConversation(stream).then(function() {
				hasGreeted = false;
				// Re-greet immediately so the drawer doesn't look empty.
				AyoraAssistant.renderAssistantMessage(stream, {
					answer: contextualGreeting(PAGE),
					languageStyle: 'french',
					grounded: false,
					responseValid: true,
					contextTypesUsed: []
				}, {});
				hasGreeted = true;
			});
		});

		// Expose a small global API so other components on the page (the
		// recommendations hero, the agent card, the "Avis AYORA" button on
		// each vendor card) can open the drawer and seed it with context.
		window.AyoraAgent = {
			open: function() { open(); },
			openWithMessage: function(question) {
				open();
				if (question && typeof question === 'string') {
					setTimeout(function() {
						AyoraAssistant.send({
							message: question,
							streamEl: stream,
							currentPage: PAGE
						});
					}, 280);
				}
			},
			openWithVendor: function(vendor) {
				// Build a contextual question grounded only on data already
				// visible in the vendor card — never invent facts.
				if (!vendor || !vendor.name) {
					open();
					return;
				}
				var bits = [];
				if (vendor.category) bits.push(vendor.category);
				if (vendor.priceMin || vendor.priceMax) {
					var p1 = vendor.priceMin ? vendor.priceMin.toLocaleString('fr-MA') : '?';
					var p2 = vendor.priceMax ? vendor.priceMax.toLocaleString('fr-MA') : '?';
					bits.push('fourchette ' + p1 + ' à ' + p2 + ' DH');
				}
				if (vendor.gamme) bits.push('gamme ' + vendor.gamme.toLowerCase());
				var ctxLine = bits.length ? ' (' + bits.join(', ') + ')' : '';
				var greeting = 'Tu regardes ' + vendor.name + ctxLine
					+ '. Je peux t\'aider à vérifier si cette option correspond à ton budget, '
					+ 'au nombre d\'invités prévus et au style de réception que tu cherches. '
					+ 'Avant de décider, vérifie ce qui est inclus dans l\'offre.';
				open(greeting);
			}
		};

		function submit() {
			var msg = input.value.trim();
			if (!msg) return;
			input.value = '';
			autoResize(input);
			AyoraAssistant.send({
				message: msg,
				streamEl: stream,
				currentPage: PAGE
			});
		}

		form.addEventListener('submit', function(ev) {
			ev.preventDefault();
			submit();
		});

		input.addEventListener('keydown', function(ev) {
			if (ev.key === 'Enter' && !ev.shiftKey) {
				ev.preventDefault();
				submit();
			}
		});

		input.addEventListener('input', function() { autoResize(input); });
	}

	function autoResize(textarea) {
		textarea.style.height = 'auto';
		textarea.style.height = Math.min(textarea.scrollHeight, 140) + 'px';
	}

	function escapePageLabel(page) {
		var map = {
			'budget': 'Budget',
			'checklist': 'Checklist',
			'vendors': 'Prestataires',
			'comparator': 'Comparateur',
			'dashboard': 'Tableau de bord'
		};
		return map[page] || 'Conversation';
	}

	function contextualGreeting(page) {
		switch (page) {
			case 'budget':
				return 'Je vois ta page Budget 🤍 Je peux t\'aider à répartir, à identifier où tu pourrais économiser, '
					+ 'ou à comprendre une ligne qui te paraît élevée. Aucune modification ne sera faite sans ta confirmation.';
			case 'checklist':
				return 'Bienvenue sur ta checklist 🤍 Je peux te suggérer les prochaines tâches selon la date de ton mariage, '
					+ 'mais je te demanderai toujours de confirmer avant d\'ajouter quoi que ce soit.';
			case 'vendors':
				return 'Je peux comparer les prestataires que la base AYORA t\'a remontés, expliquer leurs différences, '
					+ 'et te suggérer des questions à poser. Je ne propose jamais un nom hors de cette liste.';
			case 'comparator':
				return 'Sélectionne 2 ou 3 prestataires et je t\'aiderai à les comparer côte à côte — uniquement sur les données fournies par AYORA.';
			default:
				return 'Je suis là 🤍 Pose-moi une question sur ton mariage.';
		}
	}

	function injectChips(box, page, onClick) {
		var chipsByPage = {
			'budget': [
				'Comment répartir mon budget ?',
				'Où je peux économiser ?',
				'بغيت تقسيمة بالدرهم'
			],
			'checklist': [
				'Que préparer 3 mois avant ?',
				'Quelles tâches sont urgentes ?',
				'Génère-moi un planning'
			],
			'vendors': [
				'Compare ces salles',
				'Quelles questions poser ?',
				'كاين فالميزانية ديالي ؟'
			],
			'comparator': [
				'Quelle option pour mon budget ?',
				'Avantages et compromis',
				'Que vérifier avant de signer ?'
			],
			'dashboard': [
				'Par où commencer ?',
				'Je suis stressée',
				'بغيت ندير عرس بميزانية 60000 درهم'
			]
		};
		var list = chipsByPage[page] || chipsByPage['dashboard'];
		for (var i = 0; i < list.length; i++) {
			(function(text) {
				var chip = el('button', 'assistant-chip');
				chip.type = 'button';
				chip.textContent = text;
				chip.addEventListener('click', function() { onClick(text); });
				box.appendChild(chip);
			})(list[i]);
		}
	}

	// Wait for DOM ready
	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', buildWidget);
	} else {
		buildWidget();
	}
})();
