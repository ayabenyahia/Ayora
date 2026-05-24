package com.ayora.assistant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.ayora.util.JsonUtil;

/**
 * Real Google Gemini provider.
 *
 * <h3>Stabilisation mode</h3>
 * While {@code AYORA_CHAT_ACTIONS_ENABLED=false} (default) the chat
 * pipeline runs in TEXT-ONLY mode:
 * <ul>
 *   <li>Gemini is given a MINIMAL response schema with no action fields.</li>
 *   <li>The model is explicitly forbidden to emit any action schema or
 *       internal markers ({@code OPEN_VENDORS_PAGE}, {@code Third priority},
 *       {@code French register}, …).</li>
 *   <li>Reasoning tokens are disabled ({@code thinkingBudget=0},
 *       {@code includeThoughts=false}) so the model cannot leak
 *       chain-of-thought into the visible answer.</li>
 *   <li>Any {@code parts[*].thought=true} returned by the API is dropped at
 *       extraction time as belt-and-suspenders.</li>
 * </ul>
 *
 * <h3>Activation</h3>
 * <pre>
 *   AYORA_CLOUD_API_KEY  OR  GEMINI_API_KEY     (required)
 *   AYORA_CLOUD_MODEL    OR  AYORA_GEMINI_MODEL (optional, default "gemini-2.5-flash")
 *   AYORA_CHAT_ACTIONS_ENABLED                  (optional, default "false")
 * </pre>
 *
 * <h3>Privacy</h3>
 * Only receives what {@link AssistantContextBuilder} provides: first name,
 * wedding preferences, vendor names already displayed or matched by name,
 * conversation transcript. Never sees passwords, email, phone, or
 * internal database ids.
 */
public final class GeminiCloudProvider implements AyoraLlmProvider {

	private static final String DEFAULT_MODEL    = "gemini-2.5-flash";
	private static final String ENDPOINT_FORMAT  = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
	private static final Duration TIMEOUT        = Duration.ofSeconds(30);

	// Cross-model auto-fallback was REMOVED on operator request: she wants
	// solid AI rather than silently-degraded answers. The configured model
	// is now the only one tried; a quota error surfaces explicitly.

	private final HttpClient http;
	private final String apiKey;
	private final String model;
	private final boolean actionsEnabled;

	public GeminiCloudProvider() {
		String k = readEnv("AYORA_CLOUD_API_KEY");
		if (k == null || k.isEmpty()) k = readEnv("GEMINI_API_KEY");
		this.apiKey = k;
		String act = readEnv("AYORA_CHAT_ACTIONS_ENABLED");
		this.actionsEnabled = "true".equalsIgnoreCase(act) || "1".equals(act) || "yes".equalsIgnoreCase(act);
		this.http  = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(8))
			.build();
		// model is resolved at call time so a JVM property override
		// can pivot the active model without restarting Tomcat. The
		// constructor still pre-resolves it for the health endpoint.
		this.model = resolveModel();
	}

	/**
	 * Resolve the active model name at every call.
	 * Precedence (first wins):
	 *   1. JVM system property {@code AYORA_CLOUD_MODEL_OVERRIDE} (set at
	 *      runtime, no restart needed once the JVM allows it).
	 *   2. Env {@code AYORA_CLOUD_MODEL} (Eclipse Tomcat Environment tab).
	 *   3. Env {@code AYORA_GEMINI_MODEL} (alias).
	 *   4. {@link #DEFAULT_MODEL}.
	 */
	private static String resolveModel() {
		String over = System.getProperty("AYORA_CLOUD_MODEL_OVERRIDE");
		if (over != null && !over.trim().isEmpty()) return over.trim();
		String m = readEnv("AYORA_CLOUD_MODEL");
		if (m == null || m.isEmpty()) m = readEnv("AYORA_GEMINI_MODEL");
		return (m == null || m.isEmpty()) ? DEFAULT_MODEL : m;
	}

	/**
	 * Resolve the ordered list of models to try.
	 * Precedence (first wins):
	 *   1. JVM property {@code AYORA_CLOUD_MODELS_OVERRIDE} — comma-separated.
	 *   2. Env {@code AYORA_GEMINI_MODELS} — comma-separated.
	 *   3. Single-model legacy resolution via {@link #resolveModel()}, plus
	 *      the alternate Flash model as automatic backup so the bride never
	 *      sees a quota error while one Flash bucket is still alive.
	 *
	 * <p>The chain always tries the primary model first. If it returns 429
	 * or a transient 5xx, the next model is tried. Auth/parameter errors
	 * (400/401/403/404) are not retried — they would fail on every model
	 * for the same reason.
	 */
	public static java.util.List<String> resolveModelChain() {
		java.util.List<String> out = new java.util.ArrayList<String>();
		String over = System.getProperty("AYORA_CLOUD_MODELS_OVERRIDE");
		String env = readEnv("AYORA_GEMINI_MODELS");
		String raw = (over != null && !over.trim().isEmpty()) ? over
		           : (env != null && !env.isEmpty()) ? env : "";
		if (!raw.isEmpty()) {
			for (String s : raw.split(",")) {
				String t = s.trim();
				if (!t.isEmpty() && !out.contains(t)) out.add(t);
			}
			if (!out.isEmpty()) return out;
		}
		// Default chain (operator: "travailler avec les deux pour meilleur
		// résultat"): primary configured model first, then the alternate
		// Flash generations as automatic fallback. Each generation has its
		// own quota bucket on Google AI Studio free tier, so the chain
		// keeps the bride covered while one bucket is exhausted.
		String primary = resolveModel();
		out.add(primary);
		if (!primary.equals("gemini-2.5-flash")) out.add("gemini-2.5-flash");
		if (!primary.equals("gemini-3.5-flash")) out.add("gemini-3.5-flash");
		if (!primary.equals("gemini-2.0-flash")) out.add("gemini-2.0-flash");
		return out;
	}

	private static String readEnv(String key) {
		String v = System.getenv(key);
		if (v == null) v = System.getProperty(key);
		return v == null ? "" : v.trim();
	}

	@Override public String getProviderName() { return "gemini-cloud:" + model; }

	@Override public boolean isAvailable() { return apiKey != null && !apiKey.isEmpty(); }

	/** Exposed for the servlet's health endpoint. */
	public boolean isActionsEnabled() { return actionsEnabled; }

	@Override public AssistantModelResponse generate(AssistantPromptRequest request) throws Exception {
		java.util.List<String> chain = resolveModelChain();
		Exception lastErr = null;
		String lastFailedModel = chain.isEmpty() ? "gemini" : chain.get(0);

		for (int i = 0; i < chain.size(); i++) {
			String activeModel = chain.get(i);
			long start = System.currentTimeMillis();
			String body = buildRequestBody(request, activeModel);

			HttpRequest httpReq = HttpRequest.newBuilder()
				.uri(URI.create(String.format(ENDPOINT_FORMAT, activeModel)))
				.timeout(TIMEOUT)
				.header("Content-Type", "application/json")
				.header("x-goog-api-key", apiKey)
				.POST(HttpRequest.BodyPublishers.ofString(body, java.nio.charset.StandardCharsets.UTF_8))
				.build();

			HttpResponse<String> resp;
			try {
				resp = http.send(httpReq, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
			} catch (Exception e) {
				// Network / timeout — try next model.
				lastErr = e;
				lastFailedModel = activeModel;
				continue;
			}
			long latency = System.currentTimeMillis() - start;
			int code = resp.statusCode();

			if (code >= 200 && code < 300) {
				String text = extractCandidateText(resp.body());
				if (text == null || text.trim().isEmpty()) {
					lastErr = new RuntimeException("Gemini empty candidate body on " + activeModel
						+ " — " + truncate(resp.body(), 200));
					lastFailedModel = activeModel;
					continue;
				}
				// Label the response with the model that actually served it
				// AND whether we had to fall back inside the chain.
				String label = "gemini-cloud:" + activeModel + (i > 0 ? " (after " + lastFailedModel + " unavailable)" : "");
				return new AssistantModelResponse(text.trim(), label, latency, false);
			}

			// 429 (quota) or transient 5xx → try next model in the chain.
			if (code == 429 || code == 500 || code == 502 || code == 503 || code == 504) {
				lastErr = new RuntimeException("Gemini HTTP " + code + " on " + activeModel
					+ " — " + truncate(resp.body(), 200));
				lastFailedModel = activeModel;
				continue;
			}
			// 4xx other than 429 = request error; would fail on every model.
			throw new RuntimeException("Gemini HTTP " + code + " on " + activeModel
				+ " — " + truncate(resp.body(), 240));
		}
		throw lastErr != null ? lastErr
			: new RuntimeException("Gemini: no models available in the chain");
	}

	// ---------------------------------------------------------------------
	// Thinking config selector
	// ---------------------------------------------------------------------

	/**
	 * Gemini 2.x (2.0 / 2.5 / 2.5-flash-lite / etc.) accepts
	 * {@code thinkingBudget: 0} to disable reasoning entirely.
	 *
	 * <p>Gemini 3.x (3, 3.5-flash, 3-pro, etc.) does NOT accept a budget
	 * of 0; the documented control is {@code thinkingLevel}, valid values
	 * "minimal" or "high". "minimal" is the lowest possible and the closest
	 * to "thinking off" that the API allows on Gemini 3.
	 */
	private static boolean isGemini3(String m) {
		return m != null && m.toLowerCase().startsWith("gemini-3");
	}

	/** Returns the JSON fragment to drop into generationConfig.thinkingConfig. */
	private static String thinkingConfigJson(String activeModel) {
		if (isGemini3(activeModel)) {
			return "{\"thinkingLevel\":\"minimal\",\"includeThoughts\":false}";
		}
		return "{\"thinkingBudget\":0,\"includeThoughts\":false}";
	}

	// ---------------------------------------------------------------------
	// Payload — text-only stabilisation schema
	// ---------------------------------------------------------------------

	/**
	 * Build the generateContent payload in TEXT-ONLY stabilisation mode.
	 * The schema contains NO action field — the model cannot propose
	 * structured navigation and therefore cannot leak action schemas into
	 * the answer.
	 */
	private String buildRequestBody(AssistantPromptRequest req) { return buildRequestBody(req, this.model); }
	private String buildRequestBody(AssistantPromptRequest req, String activeModel) {
		StringBuilder sys = new StringBuilder();
		sys.append(req.systemPrompt);
		if (req.trustedContextBlock != null && !req.trustedContextBlock.isEmpty()) {
			sys.append("\n\n").append(req.trustedContextBlock);
		}
		// Append a strict, explicit forbid-list. These rules are not part of
		// the long-term persona prompt; they are stabilisation rails.
		sys.append("\n\n=== OUTPUT CONTRACT (STRICT, NON-NEGOTIABLE) ===\n");
		sys.append("Return exactly one JSON object with these keys: answer, languageStyle, usedBusinessContext, contextTypesUsed, suggestedPrompts.\n");
		sys.append("- answer: the final user-facing reply, in plain natural prose, in the user's language. Nothing else.\n");
		sys.append("- languageStyle: one of \"french\", \"darija_ar\", \"darija_latin\", \"mixed\".\n");
		sys.append("- usedBusinessContext: true ONLY if you actually used real budget, guest count, style, checklist task, or VERIFIED_VENDORS data in the answer. Otherwise false.\n");
		sys.append("- contextTypesUsed: subset of [\"budget\",\"guest_count\",\"style\",\"checklist\",\"verified_vendors\",\"wedding_date\",\"priorities\"] reflecting which trusted-context fields you actually used. [] if none.\n");
		sys.append("- suggestedPrompts: up to 3 short follow-up chips in the user's language, ≤6 words each. Empty array allowed.\n");
		sys.append("\nFORBIDDEN inside answer (instant violation):\n");
		sys.append("- Any English meta-commentary about register, tone, heart symbol, style, or how you crafted the reply.\n");
		sys.append("- Any internal planning marker: 'First priority', 'Second priority', 'Third priority', 'Action proposal', 'Let's check', 'tool call', 'schema', 'response schema', 'system prompt', 'developer instruction', 'thought:', 'chain-of-thought', 'includeThoughts'.\n");
		sys.append("- Any action identifier: 'OPEN_VENDORS_PAGE', 'OPEN_COMPARATOR', 'OPEN_QUESTIONNAIRE', 'ADD_CHECKLIST_TASK', 'ADJUST_BUDGET_CATEGORY', 'UPDATE_BUDGET'.\n");
		sys.append("- Any explanation that you are an AI, a model, a chatbot, or that you are following instructions.\n");
		sys.append("- Any reference to Gemini, Gemma, LLM, prompts or the schema itself.\n");
		sys.append("- Any leading echo of the trusted context (no 'Based on your profile…' opener).\n");
		sys.append("\n=== GROUNDING DISCIPLINE (NEVER VIOLATE) ===\n");
		sys.append("You may ONLY assert qualities that are present in VERIFIED_VENDORS data. Specifically:\n");
		sys.append("- NEVER claim a vendor is 'réputé(e)', 'la meilleure', 'très appréciée', 'le plus connu', 'excellent choix', 'institution', 'haut de gamme', 'prestige', or any similar qualitative judgment UNLESS the vendor's Tier, Rating, or description field in the context explicitly says so.\n");
		sys.append("- NEVER claim availability ('disponible', 'libre', 'a des disponibilités') for any date — the application has no realtime calendar.\n");
		sys.append("- NEVER state a fixed final price. Use ONLY the displayed price range from VERIFIED_VENDORS.\n");
		sys.append("- NEVER invent missing fields. If a service list, location detail, photo count, or guest capacity is not provided in the context, you say so and offer to verify.\n");
		sys.append("- Comparing vendors: discuss ONLY price range, category, city, tier, rating (if provided), described services (if provided). For everything else: 'cette information n'est pas enregistrée dans AYORA, à confirmer directement avec le prestataire.'\n");
		sys.append("- NEVER use the user's profile data ornamentally — when budget / guests / style are in the context, USE them concretely in the reply (numbers, math, recommendation tied to the value). Do not ask the user to give you the data again.\n");
		sys.append("\nSTABILISATION MODE: structured actions are temporarily DISABLED. Never suggest 'I can add this to your checklist', 'I will open the vendors page', etc. Instead respond conversationally: 'Souhaitez-vous que nous regardions ensemble cette étape ?' (or the equivalent in the user's language).\n");
		sys.append("\nIf you cannot produce a useful reply, set answer to a short, polite acknowledgement in the user's language. NEVER return an empty answer. NEVER return raw JSON braces inside answer.\n");

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"system_instruction\":{\"parts\":[{\"text\":\"")
			.append(JsonUtil.escapeJson(sys.toString())).append("\"}]},");

		json.append("\"contents\":[");
		boolean first = true;
		if (req.history != null) {
			for (int i = 0; i < req.history.size(); i++) {
				AssistantPromptRequest.Turn t = req.history.get(i);
				if (t == null || t.text == null || t.text.isEmpty()) continue;
				if (!first) json.append(",");
				String role = "assistant".equalsIgnoreCase(t.role) ? "model" : "user";
				json.append("{\"role\":\"").append(role).append("\",\"parts\":[{\"text\":\"")
					.append(JsonUtil.escapeJson(t.text)).append("\"}]}");
				first = false;
			}
		}
		if (!first) json.append(",");
		json.append("{\"role\":\"user\",\"parts\":[{\"text\":\"")
			.append(JsonUtil.escapeJson(req.userMessage)).append("\"}]}");
		json.append("],");

		// generationConfig — thinking OFF, structured JSON ON, generous budget.
		json.append("\"generationConfig\":{");
		json.append("\"temperature\":").append(req.temperature);
		json.append(",\"maxOutputTokens\":2048");
		json.append(",\"topP\":0.95");
		json.append(",\"thinkingConfig\":").append(thinkingConfigJson(activeModel));
		json.append(",\"responseMimeType\":\"application/json\"");
		json.append(",\"responseSchema\":");
		json.append(buildTextOnlyResponseSchema());
		json.append("}");

		json.append("}");
		return json.toString();
	}

	/**
	 * Minimal stabilisation schema — NO action field. Even if a future
	 * model dumps reasoning somewhere, there is no schema slot for it
	 * besides the validated {@code answer} string.
	 */
	private static String buildTextOnlyResponseSchema() {
		return "{"
			+ "\"type\":\"OBJECT\","
			+ "\"properties\":{"
			+ "\"answer\":{\"type\":\"STRING\"},"
			+ "\"languageStyle\":{\"type\":\"STRING\",\"enum\":[\"french\",\"darija_ar\",\"darija_latin\",\"mixed\"]},"
			+ "\"usedBusinessContext\":{\"type\":\"BOOLEAN\"},"
			+ "\"contextTypesUsed\":{\"type\":\"ARRAY\",\"items\":{\"type\":\"STRING\",\"enum\":["
				+ "\"budget\",\"guest_count\",\"style\",\"checklist\",\"verified_vendors\",\"wedding_date\",\"priorities\""
				+ "]}},"
			+ "\"suggestedPrompts\":{\"type\":\"ARRAY\",\"items\":{\"type\":\"STRING\"}}"
			+ "},"
			+ "\"required\":[\"answer\",\"languageStyle\",\"usedBusinessContext\",\"contextTypesUsed\"],"
			+ "\"propertyOrdering\":[\"answer\",\"languageStyle\",\"usedBusinessContext\",\"contextTypesUsed\",\"suggestedPrompts\"]"
			+ "}";
	}

	/**
	 * Walk {@code candidates[0].content.parts[]} and concatenate the
	 * {@code text} of parts that are NOT marked {@code thought:true}.
	 */
	static String extractCandidateText(String json) {
		if (json == null) return null;

		int candIdx = json.indexOf("\"candidates\"");
		if (candIdx < 0) return null;

		int contentIdx = json.indexOf("\"content\"", candIdx);
		if (contentIdx < 0) return null;

		int partsIdx = json.indexOf("\"parts\"", contentIdx);
		if (partsIdx < 0) return null;

		int partsArrStart = json.indexOf('[', partsIdx);
		if (partsArrStart < 0) return null;
		int partsArrEnd = matchingCloseBracket(json, partsArrStart);
		if (partsArrEnd < 0) partsArrEnd = json.length();

		StringBuilder out = new StringBuilder();
		int cursor = partsArrStart + 1;
		while (cursor < partsArrEnd) {
			int partStart = json.indexOf('{', cursor);
			if (partStart < 0 || partStart > partsArrEnd) break;
			int partEnd = matchingCloseBrace(json, partStart);
			if (partEnd < 0 || partEnd > partsArrEnd) break;

			String partBody = json.substring(partStart, partEnd + 1);

			if (containsThoughtFlag(partBody)) {
				cursor = partEnd + 1;
				continue;
			}

			String text = extractStringField(partBody, "text");
			if (text != null && !text.isEmpty()) {
				out.append(text);
			}
			cursor = partEnd + 1;
		}
		return out.toString();
	}

	private static boolean containsThoughtFlag(String objBody) {
		int idx = objBody.indexOf("\"thought\"");
		if (idx < 0) return false;
		int colon = objBody.indexOf(':', idx);
		if (colon < 0) return false;
		int j = colon + 1;
		while (j < objBody.length() && Character.isWhitespace(objBody.charAt(j))) j++;
		return j + 4 <= objBody.length() && objBody.startsWith("true", j);
	}

	private static int matchingCloseBracket(String s, int start) {
		int depth = 0;
		boolean inStr = false, escape = false;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (inStr) {
				if (escape)      escape = false;
				else if (c == '\\') escape = true;
				else if (c == '"')  inStr = false;
				continue;
			}
			if (c == '"')      inStr = true;
			else if (c == '[') depth++;
			else if (c == ']') { depth--; if (depth == 0) return i; }
		}
		return -1;
	}

	private static int matchingCloseBrace(String s, int start) {
		int depth = 0;
		boolean inStr = false, escape = false;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (inStr) {
				if (escape)      escape = false;
				else if (c == '\\') escape = true;
				else if (c == '"')  inStr = false;
				continue;
			}
			if (c == '"')      inStr = true;
			else if (c == '{') depth++;
			else if (c == '}') { depth--; if (depth == 0) return i; }
		}
		return -1;
	}

	private static String extractStringField(String objBody, String name) {
		int keyIdx = objBody.indexOf("\"" + name + "\"");
		if (keyIdx < 0) return null;
		int colon = objBody.indexOf(':', keyIdx);
		if (colon < 0) return null;
		int firstQuote = objBody.indexOf('"', colon + 1);
		if (firstQuote < 0) return null;
		StringBuilder val = new StringBuilder();
		boolean escape = false;
		int i = firstQuote + 1;
		for (; i < objBody.length(); i++) {
			char c = objBody.charAt(i);
			if (escape) {
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
						if (i + 4 < objBody.length()) {
							try {
								int cp = Integer.parseInt(objBody.substring(i + 1, i + 5), 16);
								val.append((char) cp);
								i += 4;
							} catch (NumberFormatException e) { val.append('?'); }
						}
						break;
					default: val.append(c);
				}
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else if (c == '"') {
				return val.toString();
			} else {
				val.append(c);
			}
		}
		return val.toString();
	}

	private static String truncate(String s, int n) {
		if (s == null) return "";
		return s.length() <= n ? s : s.substring(0, n) + "…";
	}
}
