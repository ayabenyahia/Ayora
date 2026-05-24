package com.ayora.assistant;

/**
 * Single LLM boundary for the AYORA assistant.
 *
 * <p>Implementations are responsible for:
 * <ul>
 *   <li>HTTP / IPC to a model backend (Google Gemini, fine-tuned Gemma
 *       endpoint, OpenAI-compatible, local llama.cpp, …);</li>
 *   <li>Translating an {@link AssistantPromptRequest} to that backend's
 *       payload shape;</li>
 *   <li>Returning a plain-text {@link AssistantModelResponse} — safety
 *       and grounding are handled by {@code AssistantSafetyGuard} on
 *       both sides of this call.</li>
 * </ul>
 *
 * <p>Providers must <b>never</b>:
 * <ul>
 *   <li>read or log the user's password, email, phone, or full name;</li>
 *   <li>swallow exceptions silently — bubble them up so the orchestrator
 *       can fall back gracefully;</li>
 *   <li>format the AYORA response shape themselves — they return text.</li>
 * </ul>
 *
 * <p>The exact provider activated at runtime is decided by
 * {@code LlmProviderFactory} via environment variables. The orchestrator
 * code is completely agnostic of which provider is in use — swapping
 * Gemini for the fine-tuned Gemma endpoint later requires no change here.
 */
public interface AyoraLlmProvider {

	/** Identifier surfaced in logs and in {@code /api/assistant/health}. */
	String getProviderName();

	/**
	 * Cheap availability probe. Should NOT call the network — usually
	 * just checks that the required environment variables are set.
	 */
	boolean isAvailable();

	/**
	 * Generate a response. Implementations MAY throw a runtime exception
	 * on transport errors, malformed payloads, etc. — the orchestrator
	 * catches those and falls back to the next provider.
	 */
	AssistantModelResponse generate(AssistantPromptRequest request) throws Exception;
}
