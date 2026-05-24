package com.ayora.assistant;

/**
 * Plain text + provenance returned by {@link AyoraLlmProvider#generate}.
 *
 * <p>Holding the provider name and latency lets the orchestrator log
 * which backend served each turn — useful when several providers are
 * configured (Gemma endpoint primary, Gemini fallback, etc.).
 */
public final class AssistantModelResponse {

	public final String text;
	public final String providerName;
	public final long   latencyMs;

	/** True when this response came from the offline fallback provider. */
	public final boolean fallback;

	public AssistantModelResponse(String text, String providerName, long latencyMs, boolean fallback) {
		this.text = text == null ? "" : text;
		this.providerName = providerName == null ? "unknown" : providerName;
		this.latencyMs = latencyMs;
		this.fallback = fallback;
	}
}
