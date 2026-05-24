/* ============================================================
   AYORA — Assistant client module.

   Used by assistant.html for the full chat page. Also reused by
   assistant-floating.js for the contextual drawer on Budget, Checklist
   and Vendors pages.

   Talks to the backend at POST /api/assistant/chat. The user's wedding
   profile is pulled server-side from the session — the front-end never
   ships sensitive data on the wire.
   ============================================================ */

window.AyoraAssistant = (function() {
	'use strict';

	// Same heuristic as the Java side, used purely to flip the UI to RTL
	// when the assistant chose to answer in Arabic script. The detection
	// of record is the one returned by the server in `languageStyle`.
	function isRtl(text) {
		if (!text) return false;
		var len = text.length, arabic = 0, latin = 0;
		for (var i = 0; i < len; i++) {
			var code = text.charCodeAt(i);
			if (code >= 0x0600 && code <= 0x06FF) arabic++;
			else if ((code >= 65 && code <= 90) || (code >= 97 && code <= 122)) latin++;
		}
		return arabic > latin;
	}

	function clientSideLangHint(text) {
		// Optional hint sent to the backend, never authoritative.
		if (!text) return 'french';
		var arabic = 0, latin = 0;
		for (var i = 0; i < text.length; i++) {
			var c = text.charCodeAt(i);
			if (c >= 0x0600 && c <= 0x06FF) arabic++;
			else if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) latin++;
		}
		if (arabic > 0 && latin === 0) return 'darija_ar';
		if (arabic > 0 && latin > 0) return 'mixed';
		if (/\b(bghit|khass|chno|wash|kifach|m3a|dyal|9bel|daba|7it|3la|3ndi|fes|ngafa)\b/i.test(text)) return 'darija_latin';
		return 'french';
	}

	// ---- DOM helpers ----------------------------------------------------

	function el(tag, cls, html) {
		var e = document.createElement(tag);
		if (cls) e.className = cls;
		if (html !== undefined) e.innerHTML = html;
		return e;
	}

	function escapeHtml(s) {
		return String(s || '')
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;');
	}

	// Render an assistant message bubble from the orchestrator's structured
	// response shape:
	//   { answer, languageStyle, grounded, responseValid,
	//     contextTypesUsed?, suggestedPrompts?, fallback }
	//
	// Stabilisation mode: no action card is ever rendered. The orchestrator
	// never sets requiresConfirmation / proposedAction while
	// AYORA_CHAT_ACTIONS_ENABLED=false (the current default).
	function renderAssistantMessage(streamEl, payload, opts) {
		opts = opts || {};
		var bubble = el('div', 'msg assistant');
		var langLow = (payload.languageStyle || '').toLowerCase();
		var rtl = (langLow === 'darija_ar') || isRtl(payload.answer);
		if (rtl) bubble.classList.add('is-rtl');

		// SAFE rendering: escape, then convert ONLY newlines to <br>. Nothing
		// else is interpreted (no markdown). This guarantees that even if a
		// raw JSON brace ever slipped through, it would render as text — not
		// HTML — and the badge logic below could still gate it.
		var body = escapeHtml(payload.answer || '').replace(/\n/g, '<br>');
		bubble.innerHTML = body;

		// Backend-driven badge logic — never use heuristics on the body text.
		//   - fallback                       → "Service intelligent temporairement indisponible"
		//   - responseValid === false        → no green badge
		//   - contextTypesUsed includes both vendor + budget/style → "Comparaison personnalisée"
		//   - contextTypesUsed non-empty     → "Conseil personnalisé"
		//   - short greeting (<80 chars)     → no badge
		//   - otherwise (long generic reply) → "Conseil général AYORA"
		var ctx = Array.isArray(payload.contextTypesUsed) ? payload.contextTypesUsed : [];
		var hasVendors = ctx.indexOf('verified_vendors') >= 0;
		var hasProfile = ctx.indexOf('budget') >= 0 || ctx.indexOf('guest_count') >= 0
			|| ctx.indexOf('style') >= 0 || ctx.indexOf('priorities') >= 0
			|| ctx.indexOf('wedding_date') >= 0 || ctx.indexOf('checklist') >= 0;
		var bodyTextLen = (payload.answer || '').length;
		var badgeText = null;
		var badgeClass = 'grounded-tag';

		if (payload.fallback === true) {
			// Honest differentiation between the two fallback causes.
			var isTransient = (typeof payload.fallbackReason === 'string'
				&& payload.fallbackReason.indexOf('TRANSIENT_ERROR') === 0);
			if (isTransient) {
				badgeText = rtl ? 'صعوبة مؤقتة' : 'Difficulté momentanée';
			} else {
				badgeText = rtl
					? 'الخدمة الذكية غير متوفرة مؤقتا'
					: 'Service intelligent non configuré';
			}
			badgeClass = 'grounded-tag is-fallback';
		} else if (payload.responseValid === false) {
			badgeText = null;
		} else if (payload.grounded === true && hasVendors && hasProfile) {
			badgeText = rtl ? 'مقارنة شخصية' : 'Comparaison personnalisée';
		} else if (payload.grounded === true && hasVendors) {
			badgeText = rtl ? 'مقارنة شخصية' : 'Comparaison personnalisée';
		} else if (payload.grounded === true && hasProfile) {
			badgeText = rtl ? 'نصيحة شخصية مبنية على ملفك' : 'Conseil personnalisé';
		} else if (bodyTextLen > 0 && bodyTextLen < 80) {
			badgeText = null;
		} else if (bodyTextLen > 0) {
			badgeText = rtl ? 'نصيحة عامة من AYORA' : 'Conseil général AYORA';
			badgeClass = 'grounded-tag is-generic';
		}
		if (badgeText) {
			var tag = el('div', badgeClass,
				'<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" '
				+ 'stroke-linecap="round" stroke-linejoin="round" style="width:11px;height:11px">'
				+ '<path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg> '
				+ badgeText);
			bubble.appendChild(tag);
		}

		streamEl.appendChild(bubble);

		// Suggested follow-up prompts (chips below the message).
		if (payload.suggestedPrompts && payload.suggestedPrompts.length && opts.onSuggestionClick) {
			var chipsRow = el('div', 'msg-suggested-chips');
			for (var j = 0; j < Math.min(payload.suggestedPrompts.length, 3); j++) {
				var s = payload.suggestedPrompts[j];
				var chip = el('button', 'suggested-chip');
				chip.type = 'button';
				chip.textContent = s;
				(function(text) {
					chip.addEventListener('click', function() { opts.onSuggestionClick(text); });
				})(s);
				chipsRow.appendChild(chip);
			}
			streamEl.appendChild(chipsRow);
		}

		streamEl.scrollTop = streamEl.scrollHeight;
	}

	function buildActionCard(action, opts) {
		var card = el('div', 'action-card');
		var label = humanActionLabel(action.type);
		card.innerHTML =
			'<div class="action-card-head">'
			+ '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" '
			+ 'stroke-linecap="round" stroke-linejoin="round" style="width:14px;height:14px">'
			+ '<path d="M12 5v14M5 12h14"/></svg>'
			+ '<span>Action proposée : ' + escapeHtml(label) + '</span></div>'
			+ '<div class="action-card-body">' + escapeHtml(JSON.stringify(action.payload || {})) + '</div>'
			+ '<div class="action-card-actions">'
			+ '<button type="button" class="action-confirm">Confirmer</button>'
			+ '<button type="button" class="action-cancel">Annuler</button>'
			+ '</div>';
		var confirm = card.querySelector('.action-confirm');
		var cancel  = card.querySelector('.action-cancel');
		confirm.addEventListener('click', function() {
			card.classList.add('is-done');
			card.querySelector('.action-card-body').textContent = 'Cette fonctionnalité sera disponible bientôt.';
			confirm.disabled = true; cancel.disabled = true;
			// Placeholder until the corresponding execute-action endpoint lands.
			// The orchestrator already enforces "no execution without confirmation".
			console.log('[AYORA] action confirmed (not yet wired to a service):', action);
		});
		cancel.addEventListener('click', function() {
			card.classList.add('is-cancelled');
			card.querySelector('.action-card-body').textContent = 'Action annulée.';
			confirm.disabled = true; cancel.disabled = true;
		});
		return card;
	}

	function humanActionLabel(type) {
		switch (type) {
			case 'ADD_CHECKLIST_TASK':       return 'Ajouter une tâche à ma checklist';
			case 'ADJUST_BUDGET_CATEGORY':   return 'Ajuster mon budget';
			case 'OPEN_VENDORS_PAGE':        return 'Ouvrir les prestataires';
			case 'OPEN_COMPARATOR':          return 'Comparer ces options';
			case 'OPEN_QUESTIONNAIRE':       return 'Modifier mon questionnaire';
			default: return type || 'Action';
		}
	}

	function renderUserMessage(streamEl, text) {
		var bubble = el('div', 'msg user');
		bubble.textContent = text;
		if (isRtl(text)) bubble.classList.add('is-rtl');
		streamEl.appendChild(bubble);
		streamEl.scrollTop = streamEl.scrollHeight;
	}

	function showTyping(streamEl) {
		var t = el('div', 'typing');
		t.id = '__ayora_typing__';
		t.innerHTML = '<span></span><span></span><span></span>';
		streamEl.appendChild(t);
		streamEl.scrollTop = streamEl.scrollHeight;
		return t;
	}

	function hideTyping(streamEl) {
		var t = streamEl.querySelector('#__ayora_typing__');
		if (t) t.parentNode.removeChild(t);
	}

	function handleAction(ev) {
		var type = ev.currentTarget.dataset.actionType;
		switch (type) {
			case 'open_budget':    location.href = 'mychoices.html'; break;
			case 'open_checklist': location.href = 'dashboard.html#checklist'; break;
			case 'open_vendors':   location.href = 'recommendations.html'; break;
			default:               console.log('[AYORA] Unhandled action', type);
		}
	}

	// ---- Public send ----------------------------------------------------

	function send(opts) {
		// opts: { message, streamEl, currentPage, onDone }
		var msg = (opts.message || '').trim();
		if (!msg) return Promise.resolve(null);

		renderUserMessage(opts.streamEl, msg);
		showTyping(opts.streamEl);

		var payload = {
			message: msg,
			currentPage: opts.currentPage || null,
			languageHint: clientSideLangHint(msg)
		};

		// Bubble back any chip click as a new user submit, so the conversation
		// stays multi-turn instead of replaying the same handler twice.
		var onSuggestionClick = function(text) {
			send({ message: text, streamEl: opts.streamEl, currentPage: opts.currentPage, onDone: opts.onDone });
		};

		return api.post('/api/assistant/chat', payload).then(function(resp) {
			hideTyping(opts.streamEl);
			if (!resp || resp.error) {
				renderAssistantMessage(opts.streamEl, {
					answer: 'Je rencontre un souci de connexion. Réessaie dans un instant.',
					languageStyle: 'french', grounded: false
				}, { onSuggestionClick: onSuggestionClick });
				return null;
			}
			renderAssistantMessage(opts.streamEl, resp, { onSuggestionClick: onSuggestionClick });
			if (typeof opts.onDone === 'function') opts.onDone(resp);
			return resp;
		}).catch(function(err) {
			hideTyping(opts.streamEl);
			console.error('[AYORA] chat error', err);
			renderAssistantMessage(opts.streamEl, {
				answer: 'Désolée, la connexion à AYORA a échoué. Vérifie ton accès et réessaie.',
				languageStyle: 'french', grounded: false
			}, { onSuggestionClick: onSuggestionClick });
			return null;
		});
	}

	function resetConversation(streamEl) {
		return api.post('/api/assistant/reset', {}).then(function() {
			if (streamEl) streamEl.innerHTML = '';
		}).catch(function() { /* silent */ });
	}

	// ---- Greeting -------------------------------------------------------

	function greet(streamEl, userFirstName) {
		var name = userFirstName ? (' ' + userFirstName) : '';
		var greeting = 'Bonjour' + name + ' 🤍 Je suis AYORA, ton assistante de mariage. '
			+ 'Tu peux m\'écrire en français, en darija, ou en mélange — je m\'adapte. '
			+ 'Pose-moi une question, ou choisis une suggestion ci-dessous pour commencer.';
		renderAssistantMessage(streamEl, {
			answer: greeting,
			languageStyle: 'FRENCH',
			grounded: true,
			suggestedActions: []
		});
	}

	// ---- Suggestion-of-the-day for the dashboard ------------------------

	function loadSuggestionOfTheDay(targetEl) {
		api.get('/api/assistant/suggestion').then(function(resp) {
			if (resp && resp.suggestion && targetEl) {
				targetEl.textContent = resp.suggestion;
			}
		}).catch(function() { /* silent — keep static placeholder */ });
	}

	return {
		send: send,
		renderAssistantMessage: renderAssistantMessage,
		renderUserMessage: renderUserMessage,
		showTyping: showTyping,
		hideTyping: hideTyping,
		greet: greet,
		loadSuggestionOfTheDay: loadSuggestionOfTheDay,
		resetConversation: resetConversation
	};
})();
