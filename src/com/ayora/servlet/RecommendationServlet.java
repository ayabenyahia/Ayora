package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.RecommendationDao;
import com.ayora.dao.QuestionnaireDao;
import com.ayora.model.Recommendation;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.service.RecommendationService;
import com.ayora.util.JsonUtil;

@WebServlet("/api/recommendations/*")
public class RecommendationServlet extends HttpServlet {

	private RecommendationDao recommendationDao;
	private QuestionnaireDao questionnaireDao;
	private RecommendationService recommendationService;

	@Override
	public void init() throws ServletException {
		recommendationDao = new RecommendationDao();
		questionnaireDao = new QuestionnaireDao();
		recommendationService = new RecommendationService();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		String categoryFilter = request.getParameter("category");

		List<Recommendation> recommendations = recommendationDao.findByUserId(userId);

		StringBuilder json = new StringBuilder("[");
		int count = 0;
		for (int i = 0; i < recommendations.size(); i++) {
			Recommendation rec = recommendations.get(i);
			if (categoryFilter != null && !categoryFilter.isEmpty()) {
				if (!rec.getVendorCategory().equalsIgnoreCase(categoryFilter)) {
					continue;
				}
			}
			if (count > 0) json.append(",");
			json.append("{\"id\":" + rec.getId());
			json.append(",\"vendorId\":" + rec.getVendorId());
			json.append(",\"vendorName\":\"" + JsonUtil.escapeJson(rec.getVendorName()) + "\"");
			json.append(",\"category\":\"" + JsonUtil.escapeJson(rec.getVendorCategory()) + "\"");
			json.append(",\"gamme\":\"" + JsonUtil.escapeJson(rec.getVendorGamme()) + "\"");
			json.append(",\"prixMin\":" + rec.getVendorPrixMin());
			json.append(",\"score\":" + rec.getScore());
			json.append(",\"raison\":\"" + JsonUtil.escapeJson(rec.getRaison()) + "\"");
			json.append("}");
			count++;
		}
		json.append("]");

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
}
