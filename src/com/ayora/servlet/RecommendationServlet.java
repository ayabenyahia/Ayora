package com.ayora.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.QuestionnaireDao;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.model.UserProfile;
import com.ayora.service.RecommendationService;
import com.ayora.util.JsonUtil;

@WebServlet("/api/recommendations/*")
public class RecommendationServlet extends HttpServlet {

	private QuestionnaireDao questionnaireDao;
	private RecommendationService recommendationService;

	@Override
	public void init() throws ServletException {
		questionnaireDao = new QuestionnaireDao();
		recommendationService = new RecommendationService();
	}

	/**
	 * GET /api/recommendations
	 *
	 * Retourne un payload riche :
	 *   {
	 *     "profile": {...},
	 *     "blocks":  { topPicks: [...], bestValue: [...], mostChic: [...], ... },
	 *     "all":     [...],
	 *     "categories": [{id, name, count}]
	 *   }
	 *
	 * Filtres optionnels (en query params) appliques a "all" :
	 *   - category   : nom_fr de la categorie (string, exact match)
	 *   - gamme      : ECONOMIQUE / MOYEN / PREMIUM
	 *   - minScore   : 0..100
	 *   - maxPrice   : double
	 *   - tag        : tag (texte recherche dans la liste de tags)
	 *
	 * Si le questionnaire n'existe pas : 400 et body indicatif.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		QuestionnaireAnswer answers = questionnaireDao.findByUserId(userId);
		if (answers == null) {
			JsonUtil.sendJson(response, "{\"completed\":false,\"profile\":null,\"blocks\":{},\"all\":[]}");
			return;
		}

		UserProfile profile = recommendationService.buildUserProfile(answers);
		profile.setUserId(userId);
		List<Recommendation> all = recommendationService.computeRecommendations(userId, answers);

		// Filtres
		String fCategory = request.getParameter("category");
		String fGamme = request.getParameter("gamme");
		String fTag = request.getParameter("tag");
		double fMinScore = parseDouble(request.getParameter("minScore"), 0);
		double fMaxPrice = parseDouble(request.getParameter("maxPrice"), 0);

		List<Recommendation> filtered = applyFilters(all, fCategory, fGamme, fTag, fMinScore, fMaxPrice);

		Map<String, List<Recommendation>> blocks = recommendationService.buildBlocks(filtered, profile);

		// Construire JSON
		StringBuilder json = new StringBuilder();
		json.append("{\"completed\":true,");
		json.append("\"profile\":").append(recommendationService.profileToJson(profile));
		json.append(",\"counts\":{");
		json.append("\"total\":").append(all.size());
		json.append(",\"filtered\":").append(filtered.size());
		json.append("}");
		json.append(",\"blocks\":");
		json.append(blocksToJson(blocks));
		json.append(",\"all\":").append(listToJson(filtered));
		json.append(",\"categories\":").append(categoryCountsJson(filtered));
		json.append("}");

		JsonUtil.sendJson(response, json.toString());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		String path = request.getPathInfo();

		if ("/refresh".equals(path)) {
			QuestionnaireAnswer answers = questionnaireDao.findByUserId(userId);
			if (answers == null) {
				JsonUtil.sendError(response, 400, "Questionnaire non rempli");
				return;
			}
			List<Recommendation> recommendations = recommendationService.generateRecommendations(userId, answers);
			JsonUtil.sendJson(response, "{\"success\":true,\"count\":" + recommendations.size() + "}");
		} else {
			JsonUtil.sendError(response, 404, "Route non trouvee");
		}
	}

	// ============================================================
	// FILTRES
	// ============================================================

	private List<Recommendation> applyFilters(List<Recommendation> in, String category, String gamme,
			String tag, double minScore, double maxPrice) {
		List<Recommendation> out = new ArrayList<Recommendation>();
		for (int i = 0; i < in.size(); i++) {
			Recommendation r = in.get(i);
			if (category != null && !category.isEmpty() && !category.equalsIgnoreCase(r.getVendorCategory())) continue;
			if (gamme != null && !gamme.isEmpty() && !gamme.equalsIgnoreCase(r.getVendorGamme())) continue;
			if (minScore > 0 && r.getScore() < minScore) continue;
			if (maxPrice > 0 && r.getVendorPrixMin() > maxPrice) continue;
			if (tag != null && !tag.isEmpty()) {
				boolean found = false;
				List<String> tags = r.getTags();
				if (tags != null) {
					for (int j = 0; j < tags.size(); j++) {
						if (tags.get(j).toLowerCase().contains(tag.toLowerCase())) { found = true; break; }
					}
				}
				if (!found) continue;
			}
			out.add(r);
		}
		return out;
	}

	private double parseDouble(String s, double dflt) {
		if (s == null || s.isEmpty()) return dflt;
		try { return Double.parseDouble(s); } catch (NumberFormatException e) { return dflt; }
	}

	// ============================================================
	// JSON BUILDERS
	// ============================================================

	private String listToJson(List<Recommendation> list) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append(",");
			sb.append(recommendationService.toJson(list.get(i)));
		}
		sb.append("]");
		return sb.toString();
	}

	private String blocksToJson(Map<String, List<Recommendation>> blocks) {
		StringBuilder sb = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, List<Recommendation>> e : blocks.entrySet()) {
			if (!first) sb.append(",");
			sb.append("\"").append(JsonUtil.escapeJson(e.getKey())).append("\":");
			sb.append(listToJson(e.getValue()));
			first = false;
		}
		sb.append("}");
		return sb.toString();
	}

	private String categoryCountsJson(List<Recommendation> list) {
		// Comptage par categorie (LinkedHashMap pour ordre stable)
		java.util.LinkedHashMap<String, int[]> counts = new java.util.LinkedHashMap<String, int[]>();
		for (int i = 0; i < list.size(); i++) {
			Recommendation r = list.get(i);
			String key = r.getVendorCategory();
			if (key == null) continue;
			int[] c = counts.get(key);
			if (c == null) counts.put(key, new int[]{1});
			else c[0]++;
		}
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<String, int[]> e : counts.entrySet()) {
			if (!first) sb.append(",");
			sb.append("{\"name\":\"").append(JsonUtil.escapeJson(e.getKey())).append("\"");
			sb.append(",\"count\":").append(e.getValue()[0]).append("}");
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}
}
