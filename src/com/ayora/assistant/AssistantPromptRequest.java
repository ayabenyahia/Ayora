package com.ayora.assistant;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider-agnostic prompt envelope.
 *
 * <p>The orchestrator fills this DTO and hands it to the active provider.
 * It carries everything the model needs to produce a grounded answer:
 * system instructions, prior turns, the user's new message, and a flat
 * trusted-context string built by {@code AssistantContextBuilder} (so
 * that the provider doesn't have to know about WeddingProfile, Budget,
 * Vendor, etc.).
 *
 * <p>The system prompt and trusted context together form the model's
 * "ground truth" — nothing else may inject facts about the user.
 */
public final class AssistantPromptRequest {

	/** Full system prompt — AYORA persona + behaviour rules + safety. */
	public final String systemPrompt;

	/** Flat, machine-trustable summary of profile / budget / checklist / vendors. */
	public final String trustedContextBlock;

	/** Prior turns of the conversation (oldest first). May be empty. */
	public final List<Turn> history;

	/** The new user message. */
	public final String userMessage;

	/** Detected language style of the user message ("french" / "darija_ar" / "darija_latin" / "mixed"). */
	public final String languageHint;

	/** Soft generation tuning knobs. Providers map these to their native names. */
	public final double temperature;
	public final int maxOutputTokens;

	public AssistantPromptRequest(
		String systemPrompt,
		String trustedContextBlock,
		List<Turn> history,
		String userMessage,
		String languageHint,
		double temperature,
		int maxOutputTokens
	) {
		this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
		this.trustedContextBlock = trustedContextBlock == null ? "" : trustedContextBlock;
		this.history = history == null ? new ArrayList<Turn>() : history;
		this.userMessage = userMessage == null ? "" : userMessage;
		this.languageHint = languageHint == null ? "french" : languageHint;
		this.temperature = temperature;
		this.maxOutputTokens = maxOutputTokens;
	}

	/** One past turn. Role is "user" or "assistant". */
	public static final class Turn {
		public final String role;
		public final String text;
		public Turn(String role, String text) {
			this.role = role; this.text = text;
		}
	}
}
