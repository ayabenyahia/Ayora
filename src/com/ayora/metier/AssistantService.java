package com.ayora.metier;

import com.ayora.assistant.AssistantOrchestrator;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.UserProfile;

/**
 * Thin facade exposed to the servlet layer.
 *
 * <p>Almost all logic now lives in {@link AssistantOrchestrator} and
 * the {@code com.ayora.assistant} package. This class survives only to
 * keep the deterministic "suggestion of the day" computation (no LLM
 * needed for that) and to expose a stable handle for the orchestrator.
 *
 * <p>The previous 16 hardcoded template methods (budgetAnswer,
 * planningAnswer, …) have been retired in favour of a real LLM
 * pipeline — see {@code report/assistant_current_behavior_audit.md}.
 */
public final class AssistantService {

	private final AssistantOrchestrator orchestrator;

	public AssistantService(AssistantOrchestrator orchestrator) {
		this.orchestrator = orchestrator;
	}

	public AssistantOrchestrator orchestrator() { return orchestrator; }

	/**
	 * Suggestion of the day — used by the dashboard hero card.
	 * Deterministic from the wedding date. No model call.
	 */
	public String suggestionOfTheDay(UserProfile profile, QuestionnaireAnswer qa) {
		int days = qa == null ? -1 : daysUntil(qa.getDateMariage());
		if (days < 0) {
			return "Complète d'abord ton questionnaire de mariage : avec tes priorités et ta date, "
				+ "je pourrai te proposer un plan personnalisé chaque jour.";
		}
		if (days <= 14) {
			return "Plus que " + days + " jours : concentre-toi sur les confirmations écrites avec chaque prestataire, "
				+ "le solde des paiements, le nombre final d'invités et la préparation personnelle. Tout le reste peut attendre.";
		}
		if (days <= 60) {
			return "À " + days + " jours du mariage, finalise le menu avec le traiteur, valide la décoration, "
				+ "prévois un essai maquillage et confirme le timing de l'amaria avec ta négafa.";
		}
		if (days <= 120) {
			return "Il reste environ " + (days / 30) + " mois. C'est le bon moment pour boucler le photographe et la musique, "
				+ "demander des devis comparés et arrêter le style de la décoration.";
		}
		return "Tu as encore " + (days / 30) + " mois devant toi. Profites-en pour bien définir ton budget par catégorie "
			+ "et lancer la recherche de salle et de traiteur — ce sont les deux postes qui se réservent le plus tôt.";
	}

	private static int daysUntil(String isoDate) {
		if (isoDate == null || isoDate.length() < 10) return -1;
		try {
			java.time.LocalDate target = java.time.LocalDate.parse(isoDate.substring(0, 10));
			return (int) java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), target);
		} catch (Exception e) { return -1; }
	}
}
