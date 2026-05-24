package com.ayora.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpSession;

/**
 * The pipeline that turns a raw user message into a structured AYORA
 * response.
 *
 * <pre>
 *   user message
 *      ↓
 *   detectLanguage()        ← lightweight heuristic, used to hint reply language
 *   AssistantSafetyGuard.preCheck()  ← detect risky asks, harden system prompt
 *   AssistantContextBuilder.build()  ← trusted profile/budget/vendor block
 *   AssistantConversationStore       ← recent turns
 *   AssistantPromptBuilder           ← persona + rules
 *      ↓
 *   provider chain (Gemma endpoint → Gemini → Fallback)
 *      ↓
 *   parse trailing ```action and ```prompts blocks (if any)
 *   AssistantSafetyGuard.postCheck() ← rewrite if hallucination detected
 *      ↓
 *   AssistantConversationStore.appendUser/Assistant
 *      ↓
 *   AssistantResult
 * </pre>
 */
public final class AssistantOrchestrator {

	private static final Logger LOG = Logger.getLogger(AssistantOrchestrator.class.getName());

	private final LlmProviderFactory providerFactory;
	private final AssistantContextBuilder contextBuilder;
	private final AssistantConversationStore conversationStore;
	private final AssistantSafetyGuard safetyGuard;
	private final AssistantPromptBuilder promptBuilder;

	public AssistantOrchestrator(
		LlmProviderFactory providerFactory,
		AssistantContextBuilder contextBuilder,
		AssistantConversationStore conversationStore,
		AssistantSafetyGuard safetyGuard,
		AssistantPromptBuilder promptBuilder
	) {
		this.providerFactory   = providerFactory;
		this.contextBuilder    = contextBuilder;
		this.conversationStore = conversationStore;
		this.safetyGuard       = safetyGuard;
		this.promptBuilder     = promptBuilder;
	}

	// =====================================================================
	// Result DTO
	// =====================================================================

	public static final class AssistantResult {
		public String answer;
		public String languageStyle;     // "french" / "darija_ar" / "darija_latin" / "mixed"
		public boolean grounded;         // true when we had real trusted context
		public boolean requiresConfirmation; // true when a proposed action exists
		public ProposedAction proposedAction;
		public List<String> suggestedPrompts = new ArrayList<String>();
		public List<String> contextTypesUsed = new ArrayList<String>();
		public String providerName;
		public boolean fallback;         // came from FallbackProvider
		public String fallbackReason;    // null | "NO_PROVIDER_CONFIGURED" | "TRANSIENT_ERROR:<HTTP_429|HTTP_500|NETWORK|PARSE_ERROR|EMPTY|OTHER>"
		public String violationCode;     // safety violation rewrite, if any
		public boolean responseValid = true; // false when the visible answer was rejected by the validator
	}

	public static final class ProposedAction {
		public String type;
		public String payloadJson;       // raw JSON object (we keep it as a string)
		public ProposedAction(String type, String payloadJson) {
			this.type = type;
			this.payloadJson = payloadJson;
		}
	}

	// =====================================================================
	// Public entry points
	// =====================================================================

	public AssistantResult handle(HttpSession session, int userId, String userMessage, String currentPage) {
		AssistantResult out = new AssistantResult();
		String trimmed = userMessage == null ? "" : userMessage.trim();
		String lang = detectLanguage(trimmed);
		out.languageStyle = lang;

		// 1. Pre-check (does not replace the LLM, only tightens its prompt).
		AssistantSafetyGuard.PreCheckSignals pre = safetyGuard.preCheck(trimmed);

		// 2. Build prompt: system + trusted context (+ safety reminder if any).
		String systemPrompt = promptBuilder.systemPrompt();
		String trusted      = contextBuilder.build(userId, currentPage, trimmed);
		if (pre.any()) trusted += pre.reminderBlock();

		// 3. Pull recent history.
		List<AssistantPromptRequest.Turn> history = conversationStore.tail(session, AssistantConversationStore.MAX_TURNS);

		// 4. Build request.
		AssistantPromptRequest req = new AssistantPromptRequest(
			systemPrompt, trusted, history, trimmed, lang, 0.7, 2048);

		// 5. Provider chain.
		AssistantModelResponse modelResp = callProviderChain(req, lang);
		out.providerName = modelResp.providerName;
		out.fallback     = modelResp.fallback;
		if (modelResp.fallback) {
			FallbackProvider.Reason reason = FallbackProvider.lastReason;
			if (reason == FallbackProvider.Reason.TRANSIENT_ERROR) {
				String code = lastProviderErrorCode == null ? "OTHER" : lastProviderErrorCode;
				out.fallbackReason = "TRANSIENT_ERROR:" + code;
			} else {
				out.fallbackReason = "NO_PROVIDER_CONFIGURED";
			}
		} else {
			// Real provider succeeded — clear any stale recorded error.
			lastProviderError = null;
			lastProviderErrorCode = null;
		}

		// 6. Parse the structured response.
		ParsedModelOutput parsed = parseModelOutput(modelResp.text, modelResp.fallback);

		// 7. validateUserVisibleAnswer — bottom-line gate on what the bride sees.
		//    Rejects: null/empty, leak markers, raw JSON braces, action schemas,
		//    and qualitative claims (reputation, "best", "available") when the
		//    trusted context contains no field that could ground them.
		String validation = validateUserVisibleAnswer(parsed.answer, trusted);
		if (!modelResp.fallback && validation != null) {
			LOG.log(Level.WARNING, "[AYORA] answer rejected by validator: " + validation
				+ " — regenerating once with hardened prompt");
			String harden = "\n\nSTRICT REMINDER — your previous reply was rejected by the validator "
				+ "because it contained forbidden content (" + validation + "). "
				+ "Respond again. Output ONLY plain prose in the answer field, in the user's language. "
				+ "No English meta-commentary. No action identifiers. No priority labels. "
				+ "No quoted JSON. No mention of registers or styles. "
				+ "No qualitative claims (réputée, meilleure, excellente, disponible, prestige…) "
				+ "unless that quality is literally stated in VERIFIED_VENDORS.";
			AssistantPromptRequest req2 = new AssistantPromptRequest(
				systemPrompt + harden, trusted, history, trimmed, lang, 0.4, 2048);
			AssistantModelResponse retry = callProviderChain(req2, lang);
			ParsedModelOutput parsed2 = parseModelOutput(retry.text, retry.fallback);
			String validation2 = validateUserVisibleAnswer(parsed2.answer, trusted);
			if (validation2 == null) {
				parsed = parsed2;
				out.providerName = retry.providerName;
				out.fallback     = retry.fallback;
			} else {
				// Still bad — replace with a clean error message. No leak ever
				// reaches the browser.
				parsed.answer = neutralFailureMessage(lang);
				parsed.usedBusinessContext = false;
				parsed.contextTypesUsed = new ArrayList<String>();
				parsed.suggestedPrompts = new ArrayList<String>();
				out.violationCode = "INTERNAL_LEAK";
				out.responseValid = false;
			}
		}

		// 8. Safety post-check (booking / availability / real-price hallucinations).
		AssistantSafetyGuard.PostCheckResult post = safetyGuard.postCheck(parsed.answer, lang);
		out.answer = post.sanitizedText;
		if (post.violated) {
			out.violationCode = post.violationCode;
			parsed.usedBusinessContext = false;
			parsed.contextTypesUsed = new ArrayList<String>();
			parsed.suggestedPrompts = new ArrayList<String>();
			out.responseValid = false;
		}

		// 9. Structured fields → response DTO.
		//    Stabilisation mode: NO action is ever surfaced to the frontend.
		out.proposedAction       = null;
		out.requiresConfirmation = false;
		out.suggestedPrompts     = parsed.suggestedPrompts;
		out.contextTypesUsed     = parsed.contextTypesUsed;
		// "grounded" is a strict AND of three signals.
		boolean contextHadRealData =
			   trusted.contains("Wedding date:")
			|| trusted.contains("Total budget:")
			|| trusted.contains("Guest count:")
			|| trusted.contains("VERIFIED_VENDORS");
		out.grounded = (parsed.usedBusinessContext
			&& contextHadRealData
			&& parsed.contextTypesUsed != null
			&& !parsed.contextTypesUsed.isEmpty());

		// 10. Persist turn.
		if (session != null) {
			conversationStore.appendUser(session, trimmed);
			conversationStore.appendAssistant(session, out.answer);
		}

		return out;
	}

	// =====================================================================
	// validateUserVisibleAnswer — the last barrier before display.
	// =====================================================================

	private static final java.util.regex.Pattern FORBIDDEN_RE = java.util.regex.Pattern.compile(
		"(?i)" +
		"OPEN_VENDORS_PAGE|OPEN_COMPARATOR|OPEN_QUESTIONNAIRE|" +
		"ADD_CHECKLIST_TASK|ADJUST_BUDGET_CATEGORY|UPDATE_BUDGET|" +
		"\\bAction proposal\\b|" +
		"\\b(?:First|Second|Third|Fourth)\\s+priority\\b|" +
		"\\bFrench register\\b|\\bdarija register\\b|" +
		"\\bdiscreet use\\b|" +
		"\\bLet'?s check\\b|\\bLet'?s think\\b|" +
		"\\btool call\\b|\\bresponse schema\\b|\\bsystem prompt\\b|" +
		"\\bdeveloper instruction\\b|\\bincludeThoughts\\b|" +
		"\\bthought:\\b|\\bchain[- ]of[- ]thought\\b");

	/** Unsupported qualitative claims about vendors. */
	private static final java.util.regex.Pattern UNSUPPORTED_QUALITY_RE = java.util.regex.Pattern.compile(
		"(?i)\\b(?:" +
			"r[ée]put[ée]e?s?|" +
			"tr[èe]s\\s+belle\\s+r[ée]putation|" +
			"belle\\s+r[ée]putation|" +
			"la\\s+meilleure|le\\s+meilleur|les?\\s+meilleur[es]?|" +
			"tr[èe]s\\s+appr[ée]ci[ée]e?s?|" +
			"excellent\\s+choix|" +
			"institution\\s+(?:incontournable|fassie|reconnue)|" +
			"r[ée]f[ée]rence\\s+(?:absolue|incontournable)|" +
			"prestige\\s+exceptionnel|" +
			"tarif\\s+garanti|" +
			"disponibilit[ée]?\\s+confirm[ée]e?|" +
			"est\\s+disponible|sera\\s+disponible" +
		")\\b");

	/**
	 * Return {@code null} if the answer is acceptable, else a short reason
	 * code (used in logs and in the retry prompt). The reason code never
	 * leaks to the browser.
	 *
	 * <p>{@code trustedContext} is the block actually injected into the
	 * Gemini prompt — used to decide whether a qualitative claim is
	 * grounded (e.g. "réputée" is allowed only if the description in
	 * VERIFIED_VENDORS literally contains that word).
	 */
	static String validateUserVisibleAnswer(String answer, String trustedContext) {
		if (answer == null) return "NULL_ANSWER";
		String s = answer.trim();
		if (s.isEmpty()) return "EMPTY_ANSWER";
		// Pure JSON object spilled into the answer
		if (s.startsWith("{") && s.indexOf("\"answer\"") >= 0) return "RAW_JSON_IN_ANSWER";
		// Bare schema keys
		if (s.indexOf("\"languageStyle\"") >= 0
			|| s.indexOf("\"usedBusinessContext\"") >= 0
			|| s.indexOf("\"contextTypesUsed\"") >= 0) return "SCHEMA_KEYS_IN_ANSWER";
		// Forbidden internal markers
		java.util.regex.Matcher m = FORBIDDEN_RE.matcher(s);
		if (m.find()) return "LEAK_MARKER:" + m.group();
		// Code-fenced JSON
		if (s.contains("```json") || s.contains("```action")) return "FENCED_BLOCK_IN_ANSWER";
		// Unsupported qualitative claim: only allowed if the same phrase is
		// echoed in the trusted context (e.g. inside a vendor description).
		java.util.regex.Matcher mq = UNSUPPORTED_QUALITY_RE.matcher(s);
		while (mq.find()) {
			String hit = mq.group();
			String ctxLower = trustedContext == null ? "" : trustedContext.toLowerCase();
			if (!ctxLower.contains(hit.toLowerCase())) {
				return "UNSUPPORTED_QUALITY_CLAIM:" + hit;
			}
		}
		return null;
	}

	/** Backwards-compat overload used by tests / older call sites. */
	static String validateUserVisibleAnswer(String answer) {
		return validateUserVisibleAnswer(answer, "");
	}

	// =====================================================================
	// Structured-output parsing
	// =====================================================================

	static final class ParsedModelOutput {
		String answer;
		boolean usedBusinessContext;
		List<String> contextTypesUsed = new ArrayList<String>();
		List<String> suggestedPrompts = new ArrayList<String>();
	}

	/**
	 * Parse the structured JSON output the Gemini provider is now forced
	 * to emit. If the provider is the fallback (free text) or the JSON
	 * doesn't parse cleanly, treat the whole text as the answer.
	 */
	static ParsedModelOutput parseModelOutput(String raw, boolean isFallback) {
		ParsedModelOutput p = new ParsedModelOutput();
		if (raw == null) { p.answer = ""; return p; }
		String text = raw.trim();

		// FallbackProvider returns free-form text by design.
		if (isFallback) {
			p.answer = text;
			return p;
		}

		// Real LLM — structured JSON expected.
		if (text.startsWith("{") && text.indexOf("\"answer\"") >= 0) {
			String answer = readJsonString(text, "answer");
			if (answer != null && !answer.trim().isEmpty()) {
				p.answer = answer.trim();
				p.usedBusinessContext = readJsonBoolean(text, "usedBusinessContext", false);
				List<String> ctx = readJsonStringArray(text, "contextTypesUsed");
				if (ctx != null) p.contextTypesUsed = ctx;
				List<String> prompts = readJsonStringArray(text, "suggestedPrompts");
				if (prompts != null) p.suggestedPrompts = prompts;
				return p;
			}
		}

		// Structured parsing failed — DO NOT let JSON braces reach the
		// browser. The validator will reject this and trigger a regenerate.
		p.answer = "";
		return p;
	}

	private static String readJsonString(String json, String key) {
		int keyIdx = json.indexOf("\"" + key + "\"");
		if (keyIdx < 0) return null;
		int colon = json.indexOf(':', keyIdx);
		if (colon < 0) return null;
		int q = json.indexOf('"', colon + 1);
		if (q < 0) return null;
		StringBuilder val = new StringBuilder();
		boolean esc = false;
		for (int i = q + 1; i < json.length(); i++) {
			char c = json.charAt(i);
			if (esc) {
				switch (c) {
					case '"':  val.append('"'); break;
					case '\\': val.append('\\'); break;
					case '/':  val.append('/'); break;
					case 'n':  val.append('\n'); break;
					case 't':  val.append('\t'); break;
					case 'r':  val.append('\r'); break;
					case 'b':  val.append('\b'); break;
					case 'f':  val.append('\f'); break;
					case 'u':
						if (i + 4 < json.length()) {
							try {
								int cp = Integer.parseInt(json.substring(i + 1, i + 5), 16);
								val.append((char) cp);
								i += 4;
							} catch (NumberFormatException e) { val.append('?'); }
						}
						break;
					default: val.append(c);
				}
				esc = false;
			} else if (c == '\\') {
				esc = true;
			} else if (c == '"') {
				return val.toString();
			} else {
				val.append(c);
			}
		}
		return val.toString();
	}

	private static boolean readJsonBoolean(String json, String key, boolean dflt) {
		int keyIdx = json.indexOf("\"" + key + "\"");
		if (keyIdx < 0) return dflt;
		int colon = json.indexOf(':', keyIdx);
		if (colon < 0) return dflt;
		int j = colon + 1;
		while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
		if (j + 4 <= json.length() && json.startsWith("true", j))  return true;
		if (j + 5 <= json.length() && json.startsWith("false", j)) return false;
		return dflt;
	}

	private static String readJsonObject(String json, String key) {
		int keyIdx = json.indexOf("\"" + key + "\"");
		if (keyIdx < 0) return null;
		int colon = json.indexOf(':', keyIdx);
		if (colon < 0) return null;
		int j = colon + 1;
		while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
		if (j < json.length() && json.startsWith("null", j)) return "null";
		if (j >= json.length() || json.charAt(j) != '{') return null;
		int end = matchingCloseBrace(json, j);
		if (end < 0) return null;
		return json.substring(j, end + 1);
	}

	private static List<String> readJsonStringArray(String json, String key) {
		int keyIdx = json.indexOf("\"" + key + "\"");
		if (keyIdx < 0) return null;
		int colon = json.indexOf(':', keyIdx);
		if (colon < 0) return null;
		int j = colon + 1;
		while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
		if (j >= json.length() || json.charAt(j) != '[') return null;
		int end = matchingCloseBracket(json, j);
		if (end < 0) return null;
		String body = json.substring(j + 1, end);
		List<String> out = new ArrayList<String>();
		int k = 0;
		while (k < body.length() && out.size() < 5) {
			int q = body.indexOf('"', k);
			if (q < 0) break;
			StringBuilder val = new StringBuilder();
			boolean esc = false;
			int i = q + 1;
			for (; i < body.length(); i++) {
				char c = body.charAt(i);
				if (esc) { val.append(c); esc = false; }
				else if (c == '\\') esc = true;
				else if (c == '"') break;
				else val.append(c);
			}
			String s = val.toString().trim();
			if (!s.isEmpty() && s.length() <= 80) out.add(s);
			k = i + 1;
		}
		return out;
	}

	private static int matchingCloseBrace(String s, int start) {
		int depth = 0;
		boolean inStr = false, esc = false;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (inStr) { if (esc) esc = false; else if (c == '\\') esc = true; else if (c == '"') inStr = false; continue; }
			if (c == '"') inStr = true;
			else if (c == '{') depth++;
			else if (c == '}') { depth--; if (depth == 0) return i; }
		}
		return -1;
	}
	private static int matchingCloseBracket(String s, int start) {
		int depth = 0;
		boolean inStr = false, esc = false;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (inStr) { if (esc) esc = false; else if (c == '\\') esc = true; else if (c == '"') inStr = false; continue; }
			if (c == '"') inStr = true;
			else if (c == '[') depth++;
			else if (c == ']') { depth--; if (depth == 0) return i; }
		}
		return -1;
	}

	private static String neutralFailureMessage(String lang) {
		switch (lang == null ? "french" : lang) {
			case "darija_ar":
				return "ما قدرتش نحضر ليك جواب واضح دابا. واش ممكن تعاودي صياغة السؤال ديالك؟";
			case "darija_latin":
				return "Ma 9dertch n7adir lik jawab wadi7 daba. Wach mumkin t3awdi siyagha dyal so2alek?";
			case "mixed":
				return "Je n'ai pas réussi à préparer un conseil clair pour le moment. Peux-tu reformuler ta demande ?";
			default:
				return "Je n'ai pas réussi à préparer un conseil clair pour le moment. Peux-tu reformuler ta demande ?";
		}
	}

	public void resetConversation(HttpSession session) {
		conversationStore.reset(session);
	}

	// =====================================================================
	// Helpers
	// =====================================================================

	public static volatile String lastProviderError = null;
	public static volatile String lastProviderErrorCode = null;   // "HTTP_429" | "NETWORK" | "EMPTY" | "PARSE_ERROR" | "OTHER"
	public static volatile FallbackProvider.Reason lastFallbackReason = FallbackProvider.Reason.NO_PROVIDER_CONFIGURED;

	private AssistantModelResponse callProviderChain(AssistantPromptRequest req, String lang) {
		List<AyoraLlmProvider> chain = providerFactory.fallbackChain();
		boolean realProviderConfigured = false;
		for (int i = 0; i < chain.size(); i++) {
			AyoraLlmProvider p = chain.get(i);
			boolean isReal = !"offline-fallback".equals(p.getProviderName());
			if (isReal) realProviderConfigured = true;

			// Just before the fallback runs, label the cause so its message
			// is honest. If we reach the FallbackProvider after a real one
			// failed, it's a TRANSIENT_ERROR; if no real provider exists at
			// all, it's a configuration issue.
			if (!isReal) {
				FallbackProvider.lastReason = realProviderConfigured
					? FallbackProvider.Reason.TRANSIENT_ERROR
					: FallbackProvider.Reason.NO_PROVIDER_CONFIGURED;
				lastFallbackReason = FallbackProvider.lastReason;
			}

			try {
				AssistantModelResponse r = p.generate(req);
				if (r != null && r.text != null && !r.text.trim().isEmpty()) return r;
				if (isReal) {
					lastProviderError = "[" + p.getProviderName() + "] empty answer";
					lastProviderErrorCode = "EMPTY";
				}
				LOG.log(Level.WARNING, "[AYORA] empty answer from " + p.getProviderName() + ", trying next");
			} catch (Exception e) {
				if (isReal) {
					String msg = e.getMessage();
					lastProviderError = "[" + p.getProviderName() + "] " + e.getClass().getSimpleName()
						+ ": " + (msg == null ? "(no message)" : msg.substring(0, Math.min(800, msg.length())));
					lastProviderErrorCode = classifyErrorCode(e, msg);
				}
				LOG.log(Level.WARNING, "[AYORA] provider " + p.getProviderName()
					+ " failed: " + e.getClass().getSimpleName() + " — " + (e.getMessage() == null ? "" : e.getMessage()));
			}
		}
		// All providers failed — should not happen because FallbackProvider can't fail.
		return new FallbackProvider().generate(new AssistantPromptRequest(
			"", "", new ArrayList<AssistantPromptRequest.Turn>(), "", lang, 0.7, 200));
	}

	/** Classify a provider exception into a short stable code. */
	static String classifyErrorCode(Exception e, String msg) {
		String low = (msg == null ? "" : msg.toLowerCase());
		if (low.contains("http 429")) return "HTTP_429";
		if (low.contains("http 400")) return "HTTP_400";
		if (low.contains("http 401") || low.contains("http 403")) return "HTTP_AUTH";
		if (low.contains("http 404")) return "HTTP_404";
		if (low.contains("http 500") || low.contains("http 502")
			|| low.contains("http 503") || low.contains("http 504")) return "HTTP_5XX";
		if (low.contains("empty candidate")) return "EMPTY";
		String cn = e.getClass().getSimpleName().toLowerCase();
		if (cn.contains("timeout") || cn.contains("connect") || cn.contains("io")) return "NETWORK";
		return "OTHER";
	}

	/**
	 * Lightweight language detector — same heuristic as the previous
	 * AssistantService, kept here so the orchestrator is self-contained.
	 */
	public static String detectLanguage(String text) {
		if (text == null || text.isEmpty()) return "french";
		int arabic = 0, latin = 0;
		for (int i = 0; i < text.length(); i++) {
			int c = text.charAt(i);
			if (c >= 0x0600 && c <= 0x06FF) arabic++;
			else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) latin++;
		}
		int total = arabic + latin;
		if (total == 0) return "french";
		double ar = arabic / (double) total;
		if (ar > 0.6) return "darija_ar";
		if (ar > 0.15 && latin > 0) return "mixed";
		String lower = text.toLowerCase();
		String[] arabizi = {"bghit","khass","chno","wash","kifach","m3a","dyal","9bel","daba","3la","7it","3andi","fes","ngafa","ana"};
		for (String t : arabizi) if (lower.matches(".*\\b" + t + "\\b.*")) return "darija_latin";
		return "french";
	}

}
