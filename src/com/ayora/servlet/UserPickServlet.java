package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.UserPickDao;
import com.ayora.dao.VendorDao;
import com.ayora.model.UserPick;
import com.ayora.util.JsonUtil;

/**
 * API des choix utilisateur (page "Mes Choix").
 *
 * Routes :
 *   GET    /api/picks                 -> liste des choix de la mariee
 *   POST   /api/picks                 -> retient un prestataire
 *   DELETE /api/picks/{vendorId}      -> retire ce choix
 */
@WebServlet("/api/picks/*")
public class UserPickServlet extends HttpServlet {

	private UserPickDao pickDao;
	private VendorDao vendorDao;

	@Override
	public void init() throws ServletException {
		pickDao = new UserPickDao();
		vendorDao = new VendorDao();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}
		int userId = (int) session.getAttribute("userId");
		List<UserPick> picks = pickDao.findByUserId(userId);

		StringBuilder sb = new StringBuilder("{\"picks\":[");
		for (int i = 0; i < picks.size(); i++) {
			if (i > 0) sb.append(",");
			sb.append(toJson(picks.get(i)));
		}
		sb.append("]}");
		JsonUtil.sendJson(response, sb.toString());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}
		int userId = (int) session.getAttribute("userId");
		String body = JsonUtil.readRequestBody(request);
		int vendorId = JsonUtil.getIntValue(body, "vendorId");
		if (vendorId <= 0) {
			JsonUtil.sendError(response, 400, "vendorId requis");
			return;
		}
		com.ayora.model.Vendor v = vendorDao.findById(vendorId);
		if (v == null) {
			JsonUtil.sendError(response, 404, "Prestataire introuvable");
			return;
		}
		boolean ok = pickDao.pick(userId, vendorId, v.getCategoryId());
		if (!ok) {
			JsonUtil.sendError(response, 500, "Echec de la sauvegarde du choix");
			return;
		}
		JsonUtil.sendJson(response,
			"{\"success\":true,\"vendorId\":" + vendorId
			+ ",\"category\":\"" + JsonUtil.escapeJson(v.getCategoryName()) + "\""
			+ ",\"message\":\"" + JsonUtil.escapeJson(v.getName()) + " retenu pour " + JsonUtil.escapeJson(v.getCategoryName()) + "\"}");
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}
		int userId = (int) session.getAttribute("userId");
		String path = request.getPathInfo();
		if (path == null || path.length() < 2) {
			JsonUtil.sendError(response, 400, "vendorId manquant dans l'URL");
			return;
		}
		try {
			int vendorId = Integer.parseInt(path.substring(1));
			boolean ok = pickDao.unpick(userId, vendorId);
			JsonUtil.sendJson(response, "{\"success\":" + ok + "}");
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "vendorId invalide");
		}
	}

	private String toJson(UserPick p) {
		return "{"
			+ "\"vendorId\":" + p.getVendorId()
			+ ",\"vendorName\":\"" + JsonUtil.escapeJson(safe(p.getVendorName())) + "\""
			+ ",\"category\":\"" + JsonUtil.escapeJson(safe(p.getVendorCategory())) + "\""
			+ ",\"categoryId\":" + p.getCategoryId()
			+ ",\"gamme\":\"" + JsonUtil.escapeJson(safe(p.getVendorGamme())) + "\""
			+ ",\"prixMin\":" + p.getVendorPrixMin()
			+ ",\"city\":\"" + JsonUtil.escapeJson(safe(p.getVendorCity())) + "\""
			+ ",\"phone\":\"" + JsonUtil.escapeJson(safe(p.getVendorPhone())) + "\""
			+ ",\"instagram\":\"" + JsonUtil.escapeJson(safe(p.getVendorInstagram())) + "\""
			+ ",\"description\":\"" + JsonUtil.escapeJson(safe(p.getVendorDescription())) + "\""
			+ ",\"pickedAt\":\"" + JsonUtil.escapeJson(safe(p.getPickedAt())) + "\""
			+ "}";
	}

	private String safe(String s) { return s != null ? s : ""; }
}
