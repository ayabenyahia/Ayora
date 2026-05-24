package com.ayora.assistant;

/**
 * Minimal offline fallback.
 *
 * <p>Used only when no real LLM provider is configured or the configured
 * provider fails. The message is intentionally honest about the
 * limitation — the orchestrator must not present this as the
 * "intelligent assistant". Logs show
 * {@code "[AYORA] AI provider unavailable — fallback mode active"}.
 *
 * <p>Per spec section 3 / 15 / 17: the fallback is NOT a hardcoded
 * response library. The full template engine that previously lived in
 * {@code AssistantService} is retired — keeping it would re-create the
 * "smart-looking but actually dumb" experience.
 */
public final class FallbackProvider implements AyoraLlmProvider {

	/**
	 * Reason the orchestrator dropped to the fallback. The orchestrator
	 * sets this just before calling {@link #generate}. The message picked
	 * here is honest about what happened.
	 *
	 * <ul>
	 *   <li>{@code NO_PROVIDER_CONFIGURED} — no API key found. Message:
	 *       "L'assistance AYORA n'est pas activée…"</li>
	 *   <li>{@code TRANSIENT_ERROR} — a real provider is configured but
	 *       failed (quota / network / parsing / etc.). Message:
	 *       "AYORA rencontre momentanément une difficulté…"</li>
	 * </ul>
	 */
	public enum Reason { NO_PROVIDER_CONFIGURED, TRANSIENT_ERROR }

	public static volatile Reason lastReason = Reason.NO_PROVIDER_CONFIGURED;

	@Override public String getProviderName() { return "offline-fallback"; }

	@Override public boolean isAvailable() { return true; }

	@Override public AssistantModelResponse generate(AssistantPromptRequest req) {
		String lang = req == null ? "french" : req.languageHint;
		Reason r = lastReason == null ? Reason.NO_PROVIDER_CONFIGURED : lastReason;
		String body = pickMessage(r, lang);
		return new AssistantModelResponse(body, getProviderName(), 0, true);
	}

	private static String pickMessage(Reason r, String lang) {
		if (r == Reason.TRANSIENT_ERROR) {
			switch (lang) {
				case "darija_ar":
					return "AYORA كتعاني من صعوبة مؤقتة باش تحضر ليك النصيحة. عاودي تجربي من بعد شوية من فضلك.";
				case "darija_latin":
					return "AYORA katt3ani mn so3oba mwaq9ata bach t7adir lik nasi7a. 3awdi jarbi mn b3d chwiya 3afak.";
				case "mixed":
					return "AYORA rencontre momentanément une difficulté pour préparer votre conseil. Veuillez réessayer dans quelques instants.";
				case "french":
				default:
					return "AYORA rencontre momentanément une difficulté pour préparer votre conseil. Veuillez réessayer dans quelques instants.";
			}
		}
		// NO_PROVIDER_CONFIGURED — the literal truth: no AI key set up.
		switch (lang) {
			case "darija_ar":
				return "ما قدرتش نحضر ليك جواب ذكي دابا — الخدمة الذكية ديال AYORA ماشي مفعلة فهاد المحطة. عاود تجربي منين تكون مفعلة.";
			case "darija_latin":
				return "Ma n9derch n7adir lik jawab dki daba — l service intelligent dyal AYORA machi mfa3al hna. 3awd jarbi mnin ykoun mfa33al.";
			case "mixed":
				return "Je ne peux pas préparer de conseil intelligent pour le moment — l'assistance AYORA n'est pas activée sur cette instance. Réessaie quand elle sera configurée.";
			case "french":
			default:
				return "Je ne peux pas préparer de conseil intelligent pour le moment — l'assistance AYORA n'est pas activée sur cette instance. Réessaie quand un fournisseur d'IA sera configuré.";
		}
	}
}
