package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.config.AppWiring;
import com.ayora.metier.IAyoraMetier;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.util.JsonUtil;

@WebServlet("/api/questionnaire/*")
public class QuestionnaireServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAyoraMetier metier;

	@Override
	public void init() throws ServletException {
		this.metier = AppWiring.getMetier();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		QuestionnaireAnswer answer = metier.getQuestionnaire(userId);

		if (answer == null) {
			JsonUtil.sendJson(response, "{\"completed\":false}");
			return;
		}

		String json = buildAnswerJson(answer);
		JsonUtil.sendJson(response, json);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		String body = JsonUtil.readRequestBody(request);

		// Path /lieu : update partiel du nom du lieu (saisi sur la page invitations
		// apres avoir vu les recommandations).
		String path = request.getPathInfo();
		if ("/lieu".equals(path)) {
			handleLieuUpdate(userId, body, response);
			return;
		}

		QuestionnaireAnswer answer = parseAnswer(body, userId);

		boolean success = metier.saveQuestionnaire(answer);

		if (!success) {
			JsonUtil.sendError(response, 500, "Erreur lors de la sauvegarde du questionnaire");
			return;
		}

		// Marquer le questionnaire comme complete
		metier.markQuestionnaireCompleted(userId, true);

		// Generer les recommandations
		List<Recommendation> recommendations = metier.generateRecommendations(userId, answer);

		// Construire la reponse
		StringBuilder json = new StringBuilder();
		json.append("{\"success\":true,\"message\":\"Questionnaire sauvegarde\",");
		json.append("\"recommendationsCount\":" + recommendations.size() + ",");
		json.append("\"topRecommendations\":[");

		int limit = Math.min(5, recommendations.size());
		for (int i = 0; i < limit; i++) {
			Recommendation rec = recommendations.get(i);
			if (i > 0) json.append(",");
			json.append("{\"vendorName\":\"" + JsonUtil.escapeJson(rec.getVendorName()) + "\",");
			json.append("\"category\":\"" + JsonUtil.escapeJson(rec.getVendorCategory()) + "\",");
			json.append("\"score\":" + rec.getScore() + ",");
			json.append("\"raison\":\"" + JsonUtil.escapeJson(rec.getRaison()) + "\"}");
		}
		json.append("]}");

		JsonUtil.sendJson(response, json.toString());
	}

	private QuestionnaireAnswer parseAnswer(String body, int userId) {
		QuestionnaireAnswer a = new QuestionnaireAnswer();
		a.setUserId(userId);
		a.setBudgetTotal(JsonUtil.getDoubleValue(body, "budgetTotal"));
		a.setBudgetFlexibility(JsonUtil.getStringValue(body, "budgetFlexibility"));
		a.setNbInvites(JsonUtil.getIntValue(body, "nbInvites"));
		a.setNbInvitesFemmes(JsonUtil.getIntValue(body, "nbInvitesFemmes"));
		a.setNbInvitesHommes(JsonUtil.getIntValue(body, "nbInvitesHommes"));
		a.setDateMariage(JsonUtil.getStringValue(body, "dateMariage"));
		a.setSaisonPreferee(JsonUtil.getStringValue(body, "saisonPreferee"));
		a.setLieuCeremonie(JsonUtil.getStringValue(body, "lieuCeremonie"));
		a.setStyleMariage(JsonUtil.getStringValue(body, "styleMariage"));
		a.setAmbiance(JsonUtil.getStringValue(body, "ambiance"));
		a.setThemeCouleur(JsonUtil.getStringValue(body, "themeCouleur"));
		a.setNiveauLuxe(JsonUtil.getStringValue(body, "niveauLuxe"));
		a.setPrioriteSalle(JsonUtil.getIntValue(body, "prioriteSalle"));
		a.setPrioriteTraiteur(JsonUtil.getIntValue(body, "prioriteTraiteur"));
		a.setPrioritePhoto(JsonUtil.getIntValue(body, "prioritePhoto"));
		a.setPrioriteMusique(JsonUtil.getIntValue(body, "prioriteMusique"));
		a.setPrioriteDecoration(JsonUtil.getIntValue(body, "prioriteDecoration"));
		a.setPrioriteNeggafa(JsonUtil.getIntValue(body, "prioriteNeggafa"));
		a.setPrioriteMakeup(JsonUtil.getIntValue(body, "prioriteMakeup"));
		a.setTypeCuisine(JsonUtil.getStringValue(body, "typeCuisine"));
		a.setTypeMusique(JsonUtil.getStringValue(body, "typeMusique"));
		a.setPrefPhoto(JsonUtil.getStringValue(body, "prefPhoto"));
		a.setPrefDecoration(JsonUtil.getStringValue(body, "prefDecoration"));
		a.setNbTenuesNeggafa(JsonUtil.getIntValue(body, "nbTenuesNeggafa"));
		a.setStyleNeggafa(JsonUtil.getStringValue(body, "styleNeggafa"));
		a.setPostesEconomie(JsonUtil.getStringValue(body, "postesEconomie"));
		a.setNotesSpeciales(JsonUtil.getStringValue(body, "notesSpeciales"));

		// Valeurs par defaut si 0
		if (a.getPrioriteSalle() == 0) a.setPrioriteSalle(3);
		if (a.getPrioriteTraiteur() == 0) a.setPrioriteTraiteur(3);
		if (a.getPrioritePhoto() == 0) a.setPrioritePhoto(3);
		if (a.getPrioriteMusique() == 0) a.setPrioriteMusique(3);
		if (a.getPrioriteDecoration() == 0) a.setPrioriteDecoration(3);
		if (a.getPrioriteNeggafa() == 0) a.setPrioriteNeggafa(3);
		if (a.getPrioriteMakeup() == 0) a.setPrioriteMakeup(3);
		if (a.getNbTenuesNeggafa() == 0) a.setNbTenuesNeggafa(3);

		return a;
	}

	private String buildAnswerJson(QuestionnaireAnswer a) {
		return "{\"completed\":true,"
				+ "\"budgetTotal\":" + a.getBudgetTotal() + ","
				+ "\"budgetFlexibility\":\"" + nullSafe(a.getBudgetFlexibility()) + "\","
				+ "\"nbInvites\":" + a.getNbInvites() + ","
				+ "\"nbInvitesFemmes\":" + a.getNbInvitesFemmes() + ","
				+ "\"nbInvitesHommes\":" + a.getNbInvitesHommes() + ","
				+ "\"dateMariage\":\"" + nullSafe(a.getDateMariage()) + "\","
				+ "\"saisonPreferee\":\"" + nullSafe(a.getSaisonPreferee()) + "\","
				+ "\"lieuCeremonie\":\"" + nullSafe(a.getLieuCeremonie()) + "\","
				+ "\"styleMariage\":\"" + nullSafe(a.getStyleMariage()) + "\","
				+ "\"ambiance\":\"" + nullSafe(a.getAmbiance()) + "\","
				+ "\"themeCouleur\":\"" + nullSafe(a.getThemeCouleur()) + "\","
				+ "\"niveauLuxe\":\"" + nullSafe(a.getNiveauLuxe()) + "\","
				+ "\"prioriteSalle\":" + a.getPrioriteSalle() + ","
				+ "\"prioriteTraiteur\":" + a.getPrioriteTraiteur() + ","
				+ "\"prioritePhoto\":" + a.getPrioritePhoto() + ","
				+ "\"prioriteMusique\":" + a.getPrioriteMusique() + ","
				+ "\"prioriteDecoration\":" + a.getPrioriteDecoration() + ","
				+ "\"prioriteNeggafa\":" + a.getPrioriteNeggafa() + ","
				+ "\"prioriteMakeup\":" + a.getPrioriteMakeup() + ","
				+ "\"typeCuisine\":\"" + nullSafe(a.getTypeCuisine()) + "\","
				+ "\"typeMusique\":\"" + nullSafe(a.getTypeMusique()) + "\","
				+ "\"prefPhoto\":\"" + nullSafe(a.getPrefPhoto()) + "\","
				+ "\"prefDecoration\":\"" + nullSafe(a.getPrefDecoration()) + "\","
				+ "\"nbTenuesNeggafa\":" + a.getNbTenuesNeggafa() + ","
				+ "\"styleNeggafa\":\"" + nullSafe(a.getStyleNeggafa()) + "\","
				+ "\"postesEconomie\":\"" + JsonUtil.escapeJson(a.getPostesEconomie()) + "\","
				+ "\"notesSpeciales\":" + inlineJsonOrString(a.getNotesSpeciales())
				+ "}";
	}

	/**
	 * Si notesSpeciales contient deja du JSON valide (commence par { ou [), on
	 * l'inclut tel quel comme objet/array (pas de string entouree de guillemets).
	 * Sinon on l'echape comme une string normale. Cela evite le double-escape
	 * qui faisait planter le JSON.parse cote frontend lors du pre-remplissage.
	 */
	private String inlineJsonOrString(String value) {
		if (value == null || value.isEmpty()) return "null";
		String trimmed = value.trim();
		if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
			return trimmed; // deja JSON valide, on l'inclut tel quel
		}
		return "\"" + JsonUtil.escapeJson(value) + "\"";
	}

	private String nullSafe(String s) {
		return s != null ? s : "";
	}

	/**
	 * Update partiel : modifie uniquement lieuMariageNom dans notesSpeciales.
	 * Appele depuis invitations.html avant l'envoi des invitations, une fois
	 * que la mariee a choisi sa salle parmi les recommandations.
	 *
	 * Body attendu : {"lieuMariageNom": "Palais Mokri"}
	 */
	private void handleLieuUpdate(int userId, String body, HttpServletResponse response) throws IOException {
		String lieuNom = JsonUtil.getStringValue(body, "lieuMariageNom");
		if (lieuNom == null) lieuNom = "";

		QuestionnaireAnswer existing = metier.getQuestionnaire(userId);
		if (existing == null) {
			JsonUtil.sendError(response, 400, "Remplissez d'abord le questionnaire");
			return;
		}

		String notes = existing.getNotesSpeciales();
		String updated = upsertJsonField(notes, "lieuMariageNom", lieuNom);
		existing.setNotesSpeciales(updated);

		boolean ok = metier.saveQuestionnaire(existing);
		if (!ok) {
			JsonUtil.sendError(response, 500, "Echec mise a jour du lieu");
			return;
		}
		JsonUtil.sendJson(response,
			"{\"success\":true,\"lieuMariageNom\":\"" + JsonUtil.escapeJson(lieuNom) + "\"}");
	}

	/**
	 * Met a jour une cle dans un JSON simple (objet plat). Si la cle existe,
	 * remplace sa valeur. Sinon, ajoute la paire avant l'accolade fermante.
	 * Si la chaine n'est pas un objet JSON, en cree un avec juste cette cle.
	 */
	private String upsertJsonField(String json, String key, String value) {
		String escapedValue = JsonUtil.escapeJson(value);
		String pair = "\"" + key + "\":\"" + escapedValue + "\"";

		if (json == null || json.trim().isEmpty()
				|| !json.trim().startsWith("{") || !json.trim().endsWith("}")) {
			return "{" + pair + "}";
		}

		String trimmed = json.trim();
		String search = "\"" + key + "\"";
		int idx = trimmed.indexOf(search);
		if (idx < 0) {
			// Cle absente : on l'ajoute avant la }
			String inner = trimmed.substring(1, trimmed.length() - 1).trim();
			if (inner.isEmpty()) return "{" + pair + "}";
			return "{" + inner + "," + pair + "}";
		}
		// Cle presente : on remplace sa valeur (string)
		int colon = trimmed.indexOf(":", idx + search.length());
		if (colon < 0) return trimmed;
		int valStart = trimmed.indexOf("\"", colon + 1);
		if (valStart < 0) return trimmed;
		int valEnd = trimmed.indexOf("\"", valStart + 1);
		while (valEnd > 0 && trimmed.charAt(valEnd - 1) == '\\') {
			valEnd = trimmed.indexOf("\"", valEnd + 1);
		}
		if (valEnd < 0) return trimmed;
		return trimmed.substring(0, valStart + 1) + escapedValue + trimmed.substring(valEnd);
	}
}
