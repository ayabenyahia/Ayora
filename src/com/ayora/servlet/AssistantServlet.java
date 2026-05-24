package com.ayora.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.ayora.assistant.AssistantOrchestrator;
import com.ayora.assistant.AssistantOrchestrator.AssistantResult;
import com.ayora.assistant.LlmProviderFactory;
import com.ayora.config.AppWiring;
import com.ayora.metier.AssistantService;
import com.ayora.metier.IAyoraMetier;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.UserProfile;
import com.ayora.util.JsonUtil;

/**
 * AssistantServlet — HTTP boundary for the AYORA conversational assistant.
 *
 * <ul>
 *   <li>{@code POST /api/assistant/chat}       — single user turn, returns structured reply
 *   <li>{@code POST /api/assistant/reset}      — clear the in-session conversation history
 *   <li>{@code GET  /api/assistant/suggestion} — deterministic dashboard hero line
 *   <li>{@code GET  /api/assistant/health}     — which provider is active right now
 * </ul>
 *
 * <p>All routes require an authenticated session. The user id is read from
 * {@code session.userId} only — the front-end cannot impersonate another user
 * by passing an id in the body.
 */
@WebServlet("/api/assistant/*")
public class AssistantServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAyoraMetier metier;
	private AssistantService assistant;
	private AssistantOrchestrator orchestrator;
	private LlmProviderFactory providerFactory;

	@Override
	public void init() throws ServletException {
		this.metier         = AppWiring.getMetier();
		this.assistant      = AppWiring.getAssistant();
		this.orchestrator   = AppWiring.getAssistantOrchestrator();
		this.providerFactory = AppWiring.getLlmProviderFactory();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if ("/chat".equals(path))       handleChat(request, response);
		else if ("/reset".equals(path)) handleReset(request, response);
		else if ("/admin/model".equals(path)) handleSetModel(request, response);
		else                            JsonUtil.sendError(response, 404, "Route non trouvee");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if ("/suggestion".equals(path)) handleSuggestion(request, response);
		else if ("/health".equals(path)) handleHealth(response);
		else                            JsonUtil.sendError(response, 404, "Route non trouvee");
	}

	// =====================================================================
	// Handlers
	// =====================================================================

	private void handleChat(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie"); return;
		}
		int userId = (int) session.getAttribute("userId");

		String body = JsonUtil.readRequestBody(request);
		String message = JsonUtil.getStringValue(body, "message");
		if (message == null || message.trim().isEmpty()) {
			JsonUtil.sendError(response, 400, "Message vide"); return;
		}
		String currentPage = JsonUtil.getStringValue(body, "currentPage");

		AssistantResult result = orchestrator.handle(session, userId, message, currentPage);
		JsonUtil.sendJson(response, toJson(result));
	}

	/**
	 * Live override of the Gemini model. Sets the JVM system property
	 * {@code AYORA_CLOUD_MODEL_OVERRIDE} which the provider re-reads on
	 * every call. Useful when the configured model hits its daily quota
	 * and operating Tomcat env vars would require a restart.
	 *
	 * <p>Requires an authenticated session with role ADMIN. Body:
	 * <pre>{"model":"gemini-2.5-flash"}</pre> or
	 * <pre>{"model":""}</pre> to clear the override.
	 */
	private void handleSetModel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie"); return;
		}
		String role = (String) session.getAttribute("role");
		if (!"ADMIN".equals(role)) {
			JsonUtil.sendError(response, 403, "Reserve aux administrateurs"); return;
		}
		String body = JsonUtil.readRequestBody(request);
		String m = JsonUtil.getStringValue(body, "model");
		if (m == null) m = "";
		if (m.isEmpty()) {
			System.clearProperty("AYORA_CLOUD_MODEL_OVERRIDE");
			JsonUtil.sendJson(response, "{\"success\":true,\"override\":\"\"}");
			return;
		}
		// Basic safety — model name is only used to build the URL path.
		if (!m.matches("[A-Za-z0-9._-]{1,60}")) {
			JsonUtil.sendError(response, 400, "Nom de modele invalide"); return;
		}
		System.setProperty("AYORA_CLOUD_MODEL_OVERRIDE", m);
		JsonUtil.sendJson(response, "{\"success\":true,\"override\":\"" + JsonUtil.escapeJson(m) + "\"}");
	}

	private void handleReset(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie"); return;
		}
		orchestrator.resetConversation(session);
		JsonUtil.sendJson(response, "{\"success\":true}");
	}

	private void handleSuggestion(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie"); return;
		}
		int userId = (int) session.getAttribute("userId");
		QuestionnaireAnswer qa = metier.getQuestionnaire(userId);
		UserProfile profile = qa != null ? metier.buildUserProfile(qa) : null;
		String suggestion = assistant.suggestionOfTheDay(profile, qa);
		String json = "{\"suggestion\":\"" + JsonUtil.escapeJson(suggestion) + "\"}";
		JsonUtil.sendJson(response, json);
	}

	private void handleHealth(HttpServletResponse response) throws IOException {
		String desc = providerFactory.describe();
		String primaryName = providerFactory.primary().getProviderName();
		boolean primaryIsFallback = "offline-fallback".equals(primaryName);
		String selectedKind;
		if (primaryName.startsWith("gemini")) selectedKind = "gemini";
		else if (primaryName.startsWith("gemma")) selectedKind = "gemma";
		else selectedKind = "fallback";

		// apiKeyPresent: never expose the value, only a boolean.
		String key = System.getenv("AYORA_CLOUD_API_KEY");
		if (key == null || key.isEmpty()) key = System.getenv("GEMINI_API_KEY");
		if (key == null) key = System.getProperty("AYORA_CLOUD_API_KEY");
		if (key == null || key.isEmpty()) key = System.getProperty("GEMINI_API_KEY");
		boolean apiKeyPresent = (key != null && !key.isEmpty());
		String apiKeyVarSeen = "";
		if (System.getenv("AYORA_CLOUD_API_KEY") != null && !System.getenv("AYORA_CLOUD_API_KEY").isEmpty())
			apiKeyVarSeen = "AYORA_CLOUD_API_KEY";
		else if (System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isEmpty())
			apiKeyVarSeen = "GEMINI_API_KEY";

		String envModel = System.getenv("AYORA_CLOUD_MODEL");
		if (envModel == null || envModel.isEmpty()) envModel = System.getenv("AYORA_GEMINI_MODEL");
		String override = System.getProperty("AYORA_CLOUD_MODEL_OVERRIDE");
		String activeModel;
		if (override != null && !override.isEmpty()) activeModel = override;
		else if (envModel != null && !envModel.isEmpty()) activeModel = envModel;
		else activeModel = "gemini-2.5-flash"; // mirror provider default

		String envProvider = System.getenv("AYORA_AI_PROVIDER");
		if (envProvider == null) envProvider = "";

		String actEnv = System.getenv("AYORA_CHAT_ACTIONS_ENABLED");
		if (actEnv == null) actEnv = System.getProperty("AYORA_CHAT_ACTIONS_ENABLED");
		boolean actionsEnabled = "true".equalsIgnoreCase(actEnv) || "1".equals(actEnv) || "yes".equalsIgnoreCase(actEnv);

		String lastErrMsg  = com.ayora.assistant.AssistantOrchestrator.lastProviderError;
		String lastErrCode = com.ayora.assistant.AssistantOrchestrator.lastProviderErrorCode;
		com.ayora.assistant.FallbackProvider.Reason fbReason =
			com.ayora.assistant.AssistantOrchestrator.lastFallbackReason;

		// Honest semantics: providerAvailable is what the orchestrator
		// observed on the most recent call. modelConfigured is whether the
		// operator actually configured a real provider.
		boolean modelConfigured = apiKeyPresent;
		boolean providerAvailable = !primaryIsFallback && (lastErrCode == null || lastErrCode.isEmpty());
		String fallbackReason;
		if (!modelConfigured) {
			fallbackReason = "NO_PROVIDER_CONFIGURED";
		} else if (lastErrCode != null && !lastErrCode.isEmpty()) {
			fallbackReason = "TRANSIENT_ERROR:" + lastErrCode;
		} else {
			fallbackReason = "";
		}

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"status\":\"ok\"");
		json.append(",\"configuredProvider\":\"").append(modelConfigured ? "gemini" : "fallback").append("\"");
		json.append(",\"selectedProvider\":\"").append(JsonUtil.escapeJson(selectedKind)).append("\"");
		json.append(",\"providerName\":\"gemini-cloud:").append(JsonUtil.escapeJson(activeModel)).append("\"");
		json.append(",\"providers\":\"").append(JsonUtil.escapeJson(desc)).append("\"");
		json.append(",\"apiKeyPresent\":").append(apiKeyPresent);
		json.append(",\"apiKeyEnvVarSeen\":\"").append(JsonUtil.escapeJson(apiKeyVarSeen)).append("\"");
		json.append(",\"modelConfigured\":").append(modelConfigured);
		json.append(",\"modelName\":\"").append(JsonUtil.escapeJson(activeModel)).append("\"");
		json.append(",\"modelEnv\":\"").append(JsonUtil.escapeJson(envModel == null ? "" : envModel)).append("\"");
		json.append(",\"modelOverride\":\"").append(JsonUtil.escapeJson(override == null ? "" : override)).append("\"");
		// Full ordered chain — first tried, then fallback in order.
		java.util.List<String> chain = com.ayora.assistant.GeminiCloudProvider.resolveModelChain();
		json.append(",\"modelChain\":[");
		for (int i = 0; i < chain.size(); i++) {
			if (i > 0) json.append(",");
			json.append("\"").append(JsonUtil.escapeJson(chain.get(i))).append("\"");
		}
		json.append("]");
		json.append(",\"providerChoiceEnv\":\"").append(JsonUtil.escapeJson(envProvider)).append("\"");
		json.append(",\"providerAvailable\":").append(providerAvailable);
		json.append(",\"chatActionsEnabled\":").append(actionsEnabled);
		json.append(",\"fallbackMode\":").append(!providerAvailable);
		json.append(",\"fallbackReason\":\"").append(JsonUtil.escapeJson(fallbackReason)).append("\"");
		json.append(",\"lastProviderErrorCode\":\"").append(JsonUtil.escapeJson(lastErrCode == null ? "" : lastErrCode)).append("\"");
		json.append(",\"lastProviderErrorMessage\":\"").append(JsonUtil.escapeJson(lastErrMsg == null ? "" : lastErrMsg)).append("\"");
		json.append("}");
		JsonUtil.sendJson(response, json.toString());
	}

	// =====================================================================
	// JSON serialisation of the assistant result
	// =====================================================================

	private String toJson(AssistantResult r) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"answer\":\"").append(JsonUtil.escapeJson(r.answer)).append("\"");
		sb.append(",\"languageStyle\":\"").append(r.languageStyle).append("\"");
		sb.append(",\"grounded\":").append(r.grounded);
		sb.append(",\"responseValid\":").append(r.responseValid);
		sb.append(",\"providerName\":\"").append(JsonUtil.escapeJson(r.providerName == null ? "" : r.providerName)).append("\"");
		sb.append(",\"fallback\":").append(r.fallback);
		if (r.fallbackReason != null) {
			sb.append(",\"fallbackReason\":\"").append(JsonUtil.escapeJson(r.fallbackReason)).append("\"");
		}
		if (r.violationCode != null) {
			sb.append(",\"safetyRewriteCode\":\"").append(JsonUtil.escapeJson(r.violationCode)).append("\"");
		}
		sb.append(",\"contextTypesUsed\":[");
		if (r.contextTypesUsed != null) {
			for (int i = 0; i < r.contextTypesUsed.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append("\"").append(JsonUtil.escapeJson(r.contextTypesUsed.get(i))).append("\"");
			}
		}
		sb.append("]");
		sb.append(",\"suggestedPrompts\":[");
		if (r.suggestedPrompts != null) {
			for (int i = 0; i < r.suggestedPrompts.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append("\"").append(JsonUtil.escapeJson(r.suggestedPrompts.get(i))).append("\"");
			}
		}
		sb.append("]}");
		return sb.toString();
	}
}
