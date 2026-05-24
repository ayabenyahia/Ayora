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
import com.ayora.model.UserPick;
import com.ayora.model.Vendor;
import com.ayora.util.JsonUtil;

/**
 * API des choix utilisateur (page "Mes Choix").
 *
 * Routes :
 *   GET    /api/picks                 -> liste des choix de la mariee, groupes par categorie
 *   POST   /api/picks                 -> body {vendorId} : retient ce prestataire
 *                                          (remplace eventuel choix precedent dans la meme categorie)
 *   DELETE /api/picks/{vendorId}      -> retire ce choix
 */
@WebServlet("/api/picks/*")
public class UserPickServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAyoraMetier metier;

	@Override
	public void init() throws ServletException {
		this.metier = AppWiring.getMetier();
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
		List<UserPick> picks = metier.getPicksByUser(userId);

		// Enrichit chaque pick avec les champs media via VendorDao (lecture
		// defensive : null si la migration media n'a pas ete jouee). N+1
		// borne par le nombre de picks (max ~1 par categorie), donc
		// negligeable. Le UI utilise photoUrl/galleryUrls/reelUrl pour le
		// rendu de la card.
		for (int i = 0; i < picks.size(); i++) {
			UserPick p = picks.get(i);
			Vendor v = metier.getVendor(p.getVendorId());
			if (v != null) {
				p.setVendorPhotoUrl(v.getPhotoUrl());
				p.setVendorGalleryUrls(v.getGalleryUrls());
				p.setVendorReelUrl(v.getReelUrl());
			}
		}

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
		Vendor v = metier.getVendor(vendorId);
		if (v == null) {
			JsonUtil.sendError(response, 404, "Prestataire introuvable");
			return;
		}
		boolean ok = metier.pickVendor(userId, vendorId, v.getCategoryId());
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
			boolean ok = metier.unpickVendor(userId, vendorId);
			JsonUtil.sendJson(response, "{\"success\":" + ok + "}");
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "vendorId invalide");
		}
	}

	private String toJson(UserPick p) {
		return "{"
			+ "\"vendorId\":" + p.getVendorId()
			+ ",\"vendorName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorName())) + "\""
			+ ",\"category\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorCategory())) + "\""
			+ ",\"categoryId\":" + p.getCategoryId()
			+ ",\"gamme\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorGamme())) + "\""
			+ ",\"prixMin\":" + p.getVendorPrixMin()
			+ ",\"city\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorCity())) + "\""
			+ ",\"phone\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorPhone())) + "\""
			+ ",\"instagram\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorInstagram())) + "\""
			+ ",\"description\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorDescription())) + "\""
			+ ",\"rating\":" + p.getVendorRating()
			+ ",\"nbAvis\":" + p.getVendorNbAvis()
			+ ",\"photoUrl\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorPhotoUrl())) + "\""
			+ ",\"galleryUrls\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorGalleryUrls())) + "\""
			+ ",\"reelUrl\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getVendorReelUrl())) + "\""
			+ ",\"pickedAt\":\"" + JsonUtil.escapeJson(JsonUtil.safe(p.getPickedAt())) + "\""
			+ "}";
	}

}
