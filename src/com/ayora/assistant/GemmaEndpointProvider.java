package com.ayora.assistant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import com.ayora.util.JsonUtil;

/**
 * Provider for the AYORA-fine-tuned Gemma 4 E2B-it endpoint.
 *
 * <p>This is the <b>academic deliverable target</b> of the PFA: once the
 * fine-tuning notebook produces a checkpoint and the checkpoint is served
 * behind an HTTP endpoint, this provider becomes the primary one (set
 * {@code AYORA_AI_PROVIDER=gemma_endpoint}).
 *
 * <p>The wire format expected here is the OpenAI-compatible chat
 * completions shape, which is what most off-the-shelf inference servers
 * for Gemma expose (vLLM, llama.cpp, text-generation-inference, …):
 * <pre>
 * POST {AYORA_MODEL_ENDPOINT}/v1/chat/completions
 * Authorization: Bearer {AYORA_MODEL_API_KEY}     # optional
 * Content-Type: application/json
 *
 * { "model": "ayora-gemma-4-e2b-it",
 *   "messages": [
 *     {"role": "system", "content": "<system + trusted context>"},
 *     {"role": "user",   "content": "..."},
 *     {"role": "assistant", "content": "..."},
 *     ...
 *   ],
 *   "temperature": 0.7, "max_tokens": 600 }
 *
 * Response:
 * { "choices": [{ "message": {"role": "assistant", "content": "..."} }] }
 * </pre>
 *
 * <p>If the eventual hosting choice uses a different shape, only this
 * file needs to change — the orchestrator and the prompt builder stay
 * identical.
 */
public final class GemmaEndpointProvider implements AyoraLlmProvider {

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private final HttpClient http;
	private final String endpoint;     // base URL, without trailing slash
	private final String apiKey;       // may be empty for self-hosted endpoints
	private final String modelName;

	public GemmaEndpointProvider() {
		String ep = readEnv("AYORA_MODEL_ENDPOINT");
		this.endpoint = stripTrailingSlash(ep);
		this.apiKey   = readEnv("AYORA_MODEL_API_KEY");
		String name   = readEnv("AYORA_MODEL_NAME");
		this.modelName = name.isEmpty() ? "ayora-gemma-4-e2b-it" : name;
		this.http = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(8))
			.build();
	}

	private static String readEnv(String key) {
		String v = System.getenv(key);
		if (v == null) v = System.getProperty(key);
		return v == null ? "" : v.trim();
	}

	private static String stripTrailingSlash(String s) {
		if (s == null) return "";
		while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
		return s;
	}

	@Override public String getProviderName() { return "gemma-endpoint:" + modelName; }

	@Override public boolean isAvailable() { return endpoint != null && !endpoint.isEmpty(); }

	@Override public AssistantModelResponse generate(AssistantPromptRequest req) throws Exception {
		long start = System.currentTimeMillis();
		String body = buildBody(req);

		HttpRequest.Builder b = HttpRequest.newBuilder()
			.uri(URI.create(endpoint + "/v1/chat/completions"))
			.timeout(TIMEOUT)
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body, java.nio.charset.StandardCharsets.UTF_8));
		if (apiKey != null && !apiKey.isEmpty()) b.header("Authorization", "Bearer " + apiKey);

		HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
		long latency = System.currentTimeMillis() - start;

		if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
			throw new RuntimeException("Gemma endpoint HTTP " + resp.statusCode() + " — "
				+ (resp.body() == null ? "" : resp.body().substring(0, Math.min(240, resp.body().length()))));
		}

		String text = extractAssistantText(resp.body());
		if (text == null || text.isEmpty()) {
			throw new RuntimeException("Gemma endpoint empty completion");
		}
		return new AssistantModelResponse(text.trim(), getProviderName(), latency, false);
	}

	private String buildBody(AssistantPromptRequest req) {
		StringBuilder sys = new StringBuilder(req.systemPrompt);
		if (req.trustedContextBlock != null && !req.trustedContextBlock.isEmpty()) {
			sys.append("\n\n").append(req.trustedContextBlock);
		}
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"model\":\"").append(JsonUtil.escapeJson(modelName)).append("\",");
		json.append("\"messages\":[");
		json.append("{\"role\":\"system\",\"content\":\"")
			.append(JsonUtil.escapeJson(sys.toString())).append("\"}");
		List<AssistantPromptRequest.Turn> hist = req.history;
		if (hist != null) {
			for (AssistantPromptRequest.Turn t : hist) {
				if (t == null || t.text == null || t.text.isEmpty()) continue;
				String role = "assistant".equalsIgnoreCase(t.role) ? "assistant" : "user";
				json.append(",{\"role\":\"").append(role).append("\",\"content\":\"")
					.append(JsonUtil.escapeJson(t.text)).append("\"}");
			}
		}
		json.append(",{\"role\":\"user\",\"content\":\"")
			.append(JsonUtil.escapeJson(req.userMessage)).append("\"}");
		json.append("],");
		json.append("\"temperature\":").append(req.temperature);
		json.append(",\"max_tokens\":").append(req.maxOutputTokens);
		json.append("}");
		return json.toString();
	}

	private static String extractAssistantText(String json) {
		if (json == null) return null;
		int choices = json.indexOf("\"choices\"");
		if (choices < 0) return null;
		int messageIdx = json.indexOf("\"message\"", choices);
		if (messageIdx < 0) return null;
		int contentKey = json.indexOf("\"content\"", messageIdx);
		if (contentKey < 0) return null;
		int colon = json.indexOf(":", contentKey);
		int firstQuote = json.indexOf('"', colon + 1);
		if (firstQuote < 0) return null;
		int i = firstQuote + 1;
		StringBuilder out = new StringBuilder();
		boolean escape = false;
		while (i < json.length()) {
			char c = json.charAt(i);
			if (escape) {
				switch (c) {
					case '"': out.append('"'); break;
					case '\\': out.append('\\'); break;
					case 'n':  out.append('\n'); break;
					case 't':  out.append('\t'); break;
					case 'r':  out.append('\r'); break;
					case '/':  out.append('/'); break;
					default:   out.append(c);
				}
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else if (c == '"') {
				break;
			} else {
				out.append(c);
			}
			i++;
		}
		return out.toString();
	}
}
