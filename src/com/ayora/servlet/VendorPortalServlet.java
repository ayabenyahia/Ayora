package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.VendorDao;
import com.ayora.dao.RecommendationDao;
import com.ayora.model.Vendor;
import com.ayora.model.Recommendation;
import com.ayora.util.JsonUtil;

@WebServlet("/api/vendor-portal/*")
public class VendorPortalServlet extends HttpServlet {

	private VendorDao vendorDao;
	private RecommendationDao recommendationDao;

	@Override
	public void init() throws ServletException {
		vendorDao = new VendorDao();
		recommendationDao = new RecommendationDao();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		// Verifier le role PRESTATAIRE
		String role = (String) session.getAttribute("role");
		if (!"PRESTATAIRE".equals(role)) {
			JsonUtil.sendError(response, 403, "Acces refuse - Prestataire requis");
			return;
		}

		String path = request.getPathInfo();
		if (path == null) path = "/";

		int userId = (int) session.getAttribute("userId");

		if ("/dashboard".equals(path)) {
			handleDashboard(request, response, userId);
		} else {
			JsonUtil.sendError(response, 404, "Route prestataire non trouvee");
		}
	}

	private void handleDashboard(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
		// Recuperer le vendor_id depuis la session ou le user
		com.ayora.dao.UserDao userDao = new com.ayora.dao.UserDao();
		com.ayora.model.User user = userDao.findById(userId);

		if (user == null || user.getVendorId() <= 0) {
			JsonUtil.sendJson(response, "{\"vendor\":null,\"message\":\"Aucun prestataire lie a ce compte\"}");
			return;
		}

		int vendorId = user.getVendorId();
		Vendor vendor = vendorDao.findById(vendorId);

		if (vendor == null) {
			JsonUtil.sendJson(response, "{\"vendor\":null,\"message\":\"Prestataire non trouve\"}");
			return;
		}

		// Recuperer les recommandations pour ce vendor
		List<Recommendation> recs = recommendationDao.findByVendorId(vendorId);
		int totalRecs = recs.size();
		int viewedRecs = 0;
		for (Recommendation r : recs) {
			if (r.isViewed()) viewedRecs++;
		}

		StringBuilder json = new StringBuilder();
		json.append("{\"vendor\":{");
		json.append("\"id\":" + vendor.getId());
		json.append(",\"name\":\"" + JsonUtil.escapeJson(vendor.getName()) + "\"");
		json.append(",\"categoryName\":\"" + JsonUtil.escapeJson(vendor.getCategoryName()) + "\"");
		json.append(",\"gamme\":\"" + vendor.getGamme() + "\"");
		json.append(",\"prixMin\":" + vendor.getPrixMin());
		json.append(",\"prixMax\":" + vendor.getPrixMax());
		json.append(",\"rating\":" + vendor.getRating());
		json.append(",\"nbAvis\":" + vendor.getNbAvis());
		json.append(",\"phone\":\"" + JsonUtil.escapeJson(vendor.getPhone() != null ? vendor.getPhone() : "") + "\"");
		json.append(",\"email\":\"" + JsonUtil.escapeJson(vendor.getEmail() != null ? vendor.getEmail() : "") + "\"");
		json.append("},");
		json.append("\"stats\":{");
		json.append("\"totalRecommandations\":" + totalRecs);
		json.append(",\"recommandationsVues\":" + viewedRecs);
		json.append("},");

		// Liste des recommandations (prospects)
		json.append("\"prospects\":[");
		for (int i = 0; i < recs.size(); i++) {
			Recommendation r = recs.get(i);
			if (i > 0) json.append(",");
			json.append("{\"id\":" + r.getId());
			json.append(",\"userId\":" + r.getUserId());
			json.append(",\"score\":" + r.getScore());
			json.append(",\"raison\":\"" + JsonUtil.escapeJson(r.getRaison() != null ? r.getRaison() : "") + "\"");
			json.append(",\"isViewed\":" + r.isViewed());
			json.append("}");
		}
		json.append("]}");

		JsonUtil.sendJson(response, json.toString());
	}
}
