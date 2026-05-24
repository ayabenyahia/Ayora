package com.ayora.assistant;

import java.util.ArrayList;
import java.util.List;

/**
 * Boot-time picker for the LLM provider chain.
 *
 * <p>The orchestrator asks the factory for the active provider; if it
 * fails at request time the orchestrator transparently falls through to
 * the next provider in {@link #fallbackChain()}.
 *
 * <h3>Environment variables</h3>
 * <pre>
 *   AYORA_AI_PROVIDER    one of "gemma_endpoint", "cloud", "fallback"
 *                        (auto-detect when unset)
 *   AYORA_MODEL_ENDPOINT for the fine-tuned Gemma endpoint
 *   AYORA_MODEL_API_KEY  optional bearer for the Gemma endpoint
 *   AYORA_MODEL_NAME     optional model name override
 *   AYORA_CLOUD_API_KEY  Google AI Studio API key (Gemini)
 *   AYORA_CLOUD_MODEL    optional override, default gemini-2.5-flash
 * </pre>
 *
 * <h3>Auto-detection order</h3>
 * <ol>
 *   <li>{@code AYORA_MODEL_ENDPOINT} set → {@link GemmaEndpointProvider}</li>
 *   <li>{@code AYORA_CLOUD_API_KEY} set → {@link GeminiCloudProvider}</li>
 *   <li>otherwise → {@link FallbackProvider}</li>
 * </ol>
 *
 * <p>Whatever the primary provider, the fallback chain always ends with
 * {@link FallbackProvider} so the assistant never throws to the user.
 */
public final class LlmProviderFactory {

	private final GemmaEndpointProvider gemma = new GemmaEndpointProvider();
	private final GeminiCloudProvider   cloud = new GeminiCloudProvider();
	private final FallbackProvider      fallback = new FallbackProvider();

	private final String explicitChoice;

	public LlmProviderFactory() {
		String v = System.getenv("AYORA_AI_PROVIDER");
		if (v == null) v = System.getProperty("AYORA_AI_PROVIDER");
		this.explicitChoice = v == null ? "" : v.trim().toLowerCase();
	}

	/** Active provider that the orchestrator should try first. */
	public AyoraLlmProvider primary() {
		if ("gemma_endpoint".equals(explicitChoice) && gemma.isAvailable()) return gemma;
		if ("cloud".equals(explicitChoice) && cloud.isAvailable())          return cloud;
		if ("fallback".equals(explicitChoice))                              return fallback;

		// Auto-detect
		if (gemma.isAvailable()) return gemma;
		if (cloud.isAvailable()) return cloud;
		return fallback;
	}

	/**
	 * Ordered list the orchestrator walks until one provider returns a
	 * usable response. Last entry is always the offline fallback.
	 */
	public List<AyoraLlmProvider> fallbackChain() {
		List<AyoraLlmProvider> chain = new ArrayList<AyoraLlmProvider>();
		AyoraLlmProvider p = primary();
		chain.add(p);
		if (p != cloud    && cloud.isAvailable())    chain.add(cloud);
		if (p != gemma    && gemma.isAvailable())    chain.add(gemma);
		if (p != fallback)                           chain.add(fallback);
		return chain;
	}

	/** Diagnostic line shown in {@code /api/assistant/health}. */
	public String describe() {
		StringBuilder sb = new StringBuilder();
		sb.append("primary=").append(primary().getProviderName());
		sb.append(" gemma=").append(gemma.isAvailable());
		sb.append(" cloud=").append(cloud.isAvailable());
		sb.append(" explicit=").append(explicitChoice.isEmpty() ? "(auto)" : explicitChoice);
		return sb.toString();
	}
}
