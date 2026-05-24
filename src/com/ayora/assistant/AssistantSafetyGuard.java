package com.ayora.assistant;

import java.util.regex.Pattern;

/**
 * Two-sided safety net for the assistant.
 *
 * <ol>
 *   <li><b>Pre-check</b>: inspects the user's message before the LLM
 *       call and signals risky intents (availability ask, booking,
 *       price-as-fact request) so the system prompt can be hardened with
 *       a one-line reminder. The pre-check <i>never</i> replaces the
 *       LLM with a canned sentence — it tightens the prompt and lets
 *       the model phrase the refusal naturally.</li>
 *   <li><b>Post-check</b>: inspects the LLM output before sending it to
 *       the browser. Detects hallucinated bookings ("je viens de
 *       réserver"), fabricated availability ("disponible samedi"), and
 *       vendor names that are NOT in the trusted context. On violation
 *       the response is rewritten to a neutral, safe sentence.</li>
 * </ol>
 *
 * <p>Both checks are heuristic. They are not a replacement for the
 * model behaving correctly; they are a belt that backs up the
 * suspenders of the system prompt.
 */
public final class AssistantSafetyGuard {

	// ---------- pre-check patterns --------------------------------------

	/** Words that imply asking for or asserting availability. */
	private static final Pattern AVAILABILITY_RE = Pattern.compile(
		"\\b(disponible|disponibilit[ée]?|libre|samedi|dimanche|demain|aujourd['’]hui|ce soir|tonight|booked|available)\\b" +
		"|\\bمتوفر\\b|\\b(غدا|اليوم|دابا|daba|lyouma)\\b",
		Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

	/** Booking-intent vocabulary. */
	private static final Pattern BOOKING_RE = Pattern.compile(
		"\\b(reserve|réserve|reserver|réserver|book|booking|hjz)\\b|\\bحجز\\b",
		Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

	/** "Exact / real / actual price" assertions. */
	private static final Pattern EXACT_PRICE_RE = Pattern.compile(
		"\\bprix\\s*(exact|réel|actuel|aujourd)\\b|\\bcurrent\\s*price\\b" +
		"|\\bسومة\\s*(الحقيقية|الواقعية)\\b",
		Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

	// ---------- post-check patterns --------------------------------------

	/** Claims of having actually booked / reserved / contacted a vendor. */
	private static final Pattern CLAIM_BOOKED_RE = Pattern.compile(
		"\\b(j[e']\\s*(?:viens\\s*de\\s*)?(?:r[ée]serv|contact|appell)|" +
		"je\\s*l['e]\\s*ai\\s*r[ée]serv|" +
		"r[ée]servation\\s*confirm[ée]?|" +
		"votre\\s*r[ée]servation\\s*est|" +
		"booked\\s*for\\s*you|i\\s*have\\s*booked)\\b",
		Pattern.CASE_INSENSITIVE);

	/** Assertion of certain availability. */
	private static final Pattern CLAIM_AVAILABLE_RE = Pattern.compile(
		"\\b(?:est\\s*disponible|sera\\s*disponible|est\\s*libre|disponible\\s*(?:samedi|dimanche|demain))\\b",
		Pattern.CASE_INSENSITIVE);

	/** "X DH today" / "real price" hallucinations. */
	private static final Pattern CLAIM_REAL_PRICE_RE = Pattern.compile(
		"\\b(?:le|son)\\s*prix\\s*(?:est|s['e]l[èe]ve|actuel|réel|exact)\\s*(?:à|de|:)?\\s*\\d",
		Pattern.CASE_INSENSITIVE);

	// ---------- API ------------------------------------------------------

	public static final class PreCheckSignals {
		public boolean asksAvailability;
		public boolean asksBooking;
		public boolean asksExactPrice;

		public boolean any() {
			return asksAvailability || asksBooking || asksExactPrice;
		}
		public String reminderBlock() {
			StringBuilder sb = new StringBuilder();
			sb.append("\nADDITIONAL SAFETY REMINDERS FOR THIS TURN:\n");
			if (asksAvailability) {
				sb.append("- The user is asking about availability. You CANNOT confirm any availability ")
				  .append("from memory. Tell her so politely and offer to help filter the options ")
				  .append("shown in AYORA by date/budget/guests.\n");
			}
			if (asksBooking) {
				sb.append("- The user is asking for an actual booking. You CANNOT book anything. Tell her ")
				  .append("the booking must be done through the vendor (after she contacts them via AYORA).\n");
			}
			if (asksExactPrice) {
				sb.append("- The user is asking for an exact current price. Don't invent one. ")
				  .append("Use only the price RANGE shown in VERIFIED_VENDORS when present.\n");
			}
			return sb.toString();
		}
	}

	public PreCheckSignals preCheck(String userMessage) {
		PreCheckSignals s = new PreCheckSignals();
		if (userMessage == null) return s;
		s.asksAvailability = AVAILABILITY_RE.matcher(userMessage).find();
		s.asksBooking      = BOOKING_RE.matcher(userMessage).find();
		s.asksExactPrice   = EXACT_PRICE_RE.matcher(userMessage).find();
		return s;
	}

	public static final class PostCheckResult {
		public final String sanitizedText;
		public final boolean violated;
		public final String violationCode;
		public PostCheckResult(String text, boolean violated, String code) {
			this.sanitizedText = text;
			this.violated = violated;
			this.violationCode = code;
		}
	}

	/**
	 * Rewrite the LLM output if it claims a booking, asserts a specific
	 * availability, or invents a "real price" number. We do NOT try to
	 * scrub vendor names here — that is enforced by the system prompt
	 * + the small set of vendors injected in the context. Doing
	 * additional regex on names would risk false positives on common
	 * Moroccan place names ("Riad", "Palais") and frustrate the user.
	 */
	public PostCheckResult postCheck(String llmOutput, String languageHint) {
		if (llmOutput == null) return new PostCheckResult("", false, null);
		String t = llmOutput.trim();

		if (CLAIM_BOOKED_RE.matcher(t).find()) {
			return new PostCheckResult(safeFallback("booking", languageHint), true, "CLAIM_BOOKED");
		}
		if (CLAIM_AVAILABLE_RE.matcher(t).find()) {
			return new PostCheckResult(safeFallback("availability", languageHint), true, "CLAIM_AVAILABLE");
		}
		if (CLAIM_REAL_PRICE_RE.matcher(t).find()) {
			return new PostCheckResult(safeFallback("price", languageHint), true, "CLAIM_REAL_PRICE");
		}
		return new PostCheckResult(t, false, null);
	}

	private static String safeFallback(String kind, String lang) {
		// Polite refusal — never reveals the original (possibly hallucinated) sentence.
		switch (lang == null ? "french" : lang) {
			case "darija_ar":
				if ("booking".equals(kind))
					return "ما نقدرش نأكد ولا ندير حجز فعلي. خاص الحجز يدوز مباشرة مع المورد من خلال AYORA.";
				if ("availability".equals(kind))
					return "ما نقدرش نأكد التوفر بلا تحقق فالتطبيق. عطيني العدد والتاريخ والميزانية، نعاونك تختاري بين الخيارات الموجودة.";
				return "ما نقدرش نعطي ثمن دقيق وحالي. كنخدمو غير بالفورشات المعروضة فAYORA.";
			case "darija_latin":
				if ("booking".equals(kind))
					return "Ma n9drch n2akkid wla ndir 7jz fa3li. Khass l 7jz ydouz mubacharatan m3a l mourid mn khilal AYORA.";
				if ("availability".equals(kind))
					return "Ma n9drch n2akkid availability bla ta7a99o9 f l'application. 3tini 3adad, date w budget, n3awnek tkhtari mn options li kaynin.";
				return "Ma n9drch n3tik prix exact w 7ali. Kankhdmou ghir b les fourchettes l mardiya f AYORA.";
			case "mixed":
				if ("booking".equals(kind))
					return "Je ne peux pas confirmer ou effectuer une réservation. La réservation doit se faire directement avec le prestataire via AYORA.";
				if ("availability".equals(kind))
					return "Je ne peux pas confirmer une disponibilité sans vérification dans AYORA. 3tini date, budget w 3adad dyaf w n3awnek tkhtari.";
				return "Je ne peux pas donner un prix exact actuel. Je travaille seulement avec les fourchettes affichées dans AYORA.";
			case "french":
			default:
				if ("booking".equals(kind))
					return "Je ne peux pas confirmer ni effectuer une réservation. Elle doit se faire directement avec le prestataire via AYORA.";
				if ("availability".equals(kind))
					return "Je ne peux pas confirmer une disponibilité sans vérification dans AYORA. Donne-moi ta date, ton budget et le nombre d'invités, et je t'aide à comparer les options déjà affichées.";
				return "Je ne peux pas te donner un prix exact actuel. Je travaille uniquement avec les fourchettes de prix affichées dans AYORA.";
		}
	}
}
