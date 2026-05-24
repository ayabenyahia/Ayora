package com.ayora.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.ayora.model.UserProfile;
import com.ayora.util.AyoraRecommendationEngine;
import com.ayora.util.JsonUtil;

@WebServlet("/api/recommendations/*")
public class RecommendationServlet extends HttpServlet {
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
		QuestionnaireAnswer answers = metier.getQuestionnaire(userId);
		if (answers == null) {
			JsonUtil.sendJson(response, "{\"completed\":false,\"profile\":null,\"blocks\":{},\"all\":[]}");
			return;
		}

		UserProfile profile = metier.buildUserProfile(answers);
		profile.setUserId(userId);
		List<Recommendation> all = metier.computeRecommendations(userId, answers);

		// Filtres
		String fCategory = request.getParameter("category");
		String fGamme = request.getParameter("gamme");
		String fTag = request.getParameter("tag");
		double fMinScore = parseDouble(request.getParameter("minScore"), 0);
		double fMaxPrice = parseDouble(request.getParameter("maxPrice"), 0);

		List<Recommendation> filtered = applyFilters(all, fCategory, fGamme, fTag, fMinScore, fMaxPrice);

		Map<String, List<Recommendation>> blocks = metier.buildRecommendationBlocks(filtered, profile);
		Map<String, List<Recommendation>> topPerCategory = metier.buildTopRecommendationsPerCategory(filtered, profile);

		Set<Integer> pickedIds = metier.getPickedVendorIds(userId);

		StringBuilder json = new StringBuilder();
		json.append("{\"completed\":true,");
		json.append("\"profile\":").append(profileToJson(profile));
		json.append(",\"counts\":{");
		json.append("\"total\":").append(all.size());
		json.append(",\"filtered\":").append(filtered.size());
		json.append(",\"picked\":").append(pickedIds.size());
		json.append("}");
		json.append(",\"pickedVendorIds\":").append(intSetToJson(pickedIds));
		json.append(",\"blocks\":");
		json.append(blocksToJson(blocks));
		json.append(",\"topPerCategory\":");
		json.append(blocksToJson(topPerCategory));
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
			QuestionnaireAnswer answers = metier.getQuestionnaire(userId);
			if (answers == null) {
				JsonUtil.sendError(response, 400, "Questionnaire non rempli");
				return;
			}
			List<Recommendation> recommendations = metier.generateRecommendations(userId, answers);
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
	// JSON BUILDERS (presentation : reste dans le servlet)
	// ============================================================

	private String intSetToJson(Set<Integer> ids) {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (Integer id : ids) {
			if (!first) sb.append(",");
			sb.append(id);
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}

	private String listToJson(List<Recommendation> list) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append(",");
			sb.append(toJson(list.get(i)));
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

	private String toJson(Recommendation r) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"vendorId\":").append(r.getVendorId());
		sb.append(",\"vendorName\":\"").append(JsonUtil.escapeJson(r.getVendorName())).append("\"");
		sb.append(",\"category\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorCategory()))).append("\"");
		sb.append(",\"categoryId\":").append(r.getVendorCategoryId());
		sb.append(",\"gamme\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorGamme()))).append("\"");
		sb.append(",\"prixMin\":").append(r.getVendorPrixMin());
		sb.append(",\"prixMax\":").append(r.getVendorPrixMax());
		sb.append(",\"city\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorCity()))).append("\"");
		sb.append(",\"phone\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorPhone()))).append("\"");
		sb.append(",\"instagram\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorInstagram()))).append("\"");
		sb.append(",\"rating\":").append(r.getVendorRating());
		sb.append(",\"nbAvis\":").append(r.getVendorNbAvis());
		sb.append(",\"photoUrl\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorPhotoUrl()))).append("\"");
		sb.append(",\"galleryUrls\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorGalleryUrls()))).append("\"");
		sb.append(",\"reelUrl\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorReelUrl()))).append("\"");
		sb.append(",\"score\":").append(r.getScore());
		sb.append(",\"scoreBudget\":").append(r.getScoreBudget());
		sb.append(",\"scoreStyle\":").append(r.getScoreStyle());
		sb.append(",\"scoreCity\":").append(r.getScoreCity());
		sb.append(",\"scoreGuestCount\":").append(r.getScoreGuestCount());
		sb.append(",\"scoreLuxe\":").append(r.getScoreLuxe());
		sb.append(",\"scoreQuality\":").append(r.getScoreQuality());
		sb.append(",\"scorePopularite\":").append(r.getScorePopularite());
		sb.append(",\"raison\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getRaison()))).append("\"");
		sb.append(",\"tags\":[");
		List<String> tags = r.getTags();
		if (tags != null) {
			for (int i = 0; i < tags.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append("\"").append(JsonUtil.escapeJson(tags.get(i))).append("\"");
			}
		}
		sb.append("]");
		sb.append(",\"matchHighlights\":[");
		List<String> mh = r.getMatchHighlights();
		if (mh != null) {
			for (int i = 0; i < mh.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append("\"").append(JsonUtil.escapeJson(mh.get(i))).append("\"");
			}
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	private String profileToJson(UserProfile p) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"style\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getStyle()))).append("\"");
		sb.append(",\"ambiance\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getAmbiance()))).append("\"");
		sb.append(",\"niveauLuxe\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getNiveauLuxe()))).append("\"");
		sb.append(",\"themeCouleur\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getThemeCouleur()))).append("\"");
		sb.append(",\"saison\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getSaison()))).append("\"");
		sb.append(",\"budgetTotal\":").append(p.getBudgetTotal());
		sb.append(",\"budgetPerGuest\":").append(p.getBudgetPerGuest());
		sb.append(",\"budgetTier\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getBudgetTier()))).append("\"");
		sb.append(",\"budgetFlexibility\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getBudgetFlexibility()))).append("\"");
		sb.append(",\"nbInvites\":").append(p.getNbInvites());
		sb.append(",\"guestSize\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getGuestSize()))).append("\"");
		sb.append(",\"typeMusique\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getTypeMusique()))).append("\"");
		sb.append(",\"typeCuisine\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getTypeCuisine()))).append("\"");
		sb.append(",\"prefPhoto\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getPrefPhoto()))).append("\"");
		sb.append(",\"prefDecoration\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getPrefDecoration()))).append("\"");
		sb.append(",\"styleNeggafa\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getStyleNeggafa()))).append("\"");
		sb.append(",\"nbTenuesNeggafa\":").append(p.getNbTenuesNeggafa());
		sb.append(",\"lieuType\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getLieuType()))).append("\"");
		sb.append(",\"userCity\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getUserCity()))).append("\"");
		sb.append(",\"halalStrict\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(p.getHalalStrict()))).append("\"");
		sb.append(",\"topCategories\":[");
		List<Integer> top = p.getTopCategoryIds();
		if (top != null) {
			for (int i = 0; i < top.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append(top.get(i));
			}
		}
		sb.append("]");
		// Poids du moteur IA (transparence pour le frontend)
		sb.append(",\"aiWeights\":{");
		sb.append("\"budget\":").append(AyoraRecommendationEngine.WEIGHT_BUDGET);
		sb.append(",\"style\":").append(AyoraRecommendationEngine.WEIGHT_STYLE);
		sb.append(",\"city\":").append(AyoraRecommendationEngine.WEIGHT_CITY);
		sb.append(",\"guests\":").append(AyoraRecommendationEngine.WEIGHT_GUESTS);
		sb.append(",\"luxury\":").append(AyoraRecommendationEngine.WEIGHT_LUXURY);
		sb.append(",\"quality\":").append(AyoraRecommendationEngine.WEIGHT_QUALITY);
		sb.append("}");
		sb.append("}");
		return sb.toString();
	}
}
