package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.ayora.config.AppWiring;
import com.ayora.dao.VendorDao;
import com.ayora.metier.IAyoraMetier;
import com.ayora.model.Devis;
import com.ayora.model.RendezVous;
import com.ayora.model.User;
import com.ayora.model.Vendor;
import com.ayora.util.JsonUtil;

/**
 * Back-office Ayora : endpoints admin enrichis.
 *
 * Pattern Servlet -> Metier -> DAO -> Database. Tous les acces base
 * passent par IAyoraMetier ; aucun JDBC direct dans ce servlet.
 *
 * Routes :
 *   GET    /api/admin/stats              KPIs etendus
 *   GET    /api/admin/users              liste filtree + paginee
 *   GET    /api/admin/users/{id}         fiche detail
 *   PUT    /api/admin/users/{id}         modification
 *   POST   /api/admin/users/{id}/role    changement role
 *   POST   /api/admin/users/{id}/plan    changement abonnement
 *   POST   /api/admin/users/{id}/active  toggle suspend/activer
 *   DELETE /api/admin/users/{id}         suppression dure
 *
 *   GET    /api/admin/vendors            liste filtree + paginee + score completude
 *   GET    /api/admin/vendors/{id}       fiche detail
 *   PUT    /api/admin/vendors/{id}       modification
 *   POST   /api/admin/vendors/{id}/active toggle actif
 *   DELETE /api/admin/vendors/{id}
 *
 *   GET    /api/admin/devis              liste devis + filtres
 *   PUT    /api/admin/devis/{id}/status  changer statut
 *   GET    /api/admin/rdv                liste RDV + filtres
 *   PUT    /api/admin/rdv/{id}/status
 *
 *   GET    /api/admin/activity           timeline evenements
 *   GET    /api/admin/actions            elements a traiter
 *   GET    /api/admin/analytics          donnees pour graphiques
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAyoraMetier metier;

	@Override
	public void init() throws ServletException {
		this.metier = AppWiring.getMetier();
	}

	// ============================================================
	// SECURITE : tous les endpoints exigent role=ADMIN
	// ============================================================
	private boolean checkAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(res, 401, "Non authentifie");
			return false;
		}
		String role = (String) session.getAttribute("role");
		if (!"ADMIN".equals(role)) {
			JsonUtil.sendError(res, 403, "Acces refuse - role ADMIN requis");
			return false;
		}
		return true;
	}

	// ============================================================
	// ROUTING
	// ============================================================
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (!checkAdmin(req, res)) return;
		String path = req.getPathInfo();
		if (path == null) path = "/";

		try {
			if ("/stats".equals(path))      handleStats(req, res);
			else if ("/users".equals(path)) handleUsersList(req, res);
			else if (path.matches("^/users/\\d+$")) handleUserDetail(req, res, extractId(path));
			else if ("/vendors".equals(path)) handleVendorsList(req, res);
			else if (path.matches("^/vendors/\\d+$")) handleVendorDetail(req, res, extractId(path));
			else if ("/devis".equals(path)) handleDevis(req, res);
			else if ("/rdv".equals(path))   handleRdv(req, res);
			else if ("/activity".equals(path)) handleActivity(req, res);
			else if ("/actions".equals(path))  handleActions(req, res);
			else if ("/analytics".equals(path)) handleAnalytics(req, res);
			else JsonUtil.sendError(res, 404, "Route admin non trouvee : " + path);
		} catch (Exception e) {
			System.out.println("## Erreur admin GET " + path + " : " + e.getMessage());
			JsonUtil.sendError(res, 500, "Erreur serveur : " + e.getMessage());
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (!checkAdmin(req, res)) return;
		String path = req.getPathInfo();
		if (path == null) path = "/";
		try {
			if (path.matches("^/users/\\d+$"))            handleUserUpdate(req, res, extractId(path));
			else if (path.matches("^/vendors/\\d+$"))     handleVendorUpdate(req, res, extractId(path));
			else if (path.matches("^/devis/\\d+/status$")) handleDevisStatus(req, res, extractIdMid(path));
			else if (path.matches("^/rdv/\\d+/status$"))   handleRdvStatus(req, res, extractIdMid(path));
			else JsonUtil.sendError(res, 404, "Route PUT non trouvee : " + path);
		} catch (Exception e) {
			System.out.println("## Erreur admin PUT " + path + " : " + e.getMessage());
			JsonUtil.sendError(res, 500, "Erreur : " + e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (!checkAdmin(req, res)) return;
		String path = req.getPathInfo();
		if (path == null) path = "/";
		try {
			if (path.matches("^/users/\\d+/role$"))    handleUserRole(req, res, extractIdMid(path));
			else if (path.matches("^/users/\\d+/plan$"))   handleUserPlan(req, res, extractIdMid(path));
			else if (path.matches("^/users/\\d+/active$")) handleUserActive(req, res, extractIdMid(path));
			else if (path.matches("^/vendors/\\d+/active$")) handleVendorActive(req, res, extractIdMid(path));
			else JsonUtil.sendError(res, 404, "Route POST non trouvee : " + path);
		} catch (Exception e) {
			System.out.println("## Erreur admin POST " + path + " : " + e.getMessage());
			JsonUtil.sendError(res, 500, "Erreur : " + e.getMessage());
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (!checkAdmin(req, res)) return;
		String path = req.getPathInfo();
		if (path == null) path = "/";
		try {
			if (path.matches("^/users/\\d+$"))   handleUserDelete(req, res, extractId(path));
			else if (path.matches("^/vendors/\\d+$")) handleVendorDelete(req, res, extractId(path));
			else JsonUtil.sendError(res, 404, "Route DELETE non trouvee : " + path);
		} catch (Exception e) {
			System.out.println("## Erreur admin DELETE " + path + " : " + e.getMessage());
			JsonUtil.sendError(res, 500, "Erreur : " + e.getMessage());
		}
	}

	private int extractId(String path)    { return Integer.parseInt(path.substring(path.lastIndexOf('/')+1)); }
	private int extractIdMid(String path) {
		// path = /users/123/role  => extrait 123
		String[] parts = path.split("/");
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].matches("\\d+")) return Integer.parseInt(parts[i]);
		}
		return -1;
	}

	// ============================================================
	// 1. STATS — KPIs etendus
	// ============================================================
	private void handleStats(HttpServletRequest req, HttpServletResponse res) throws IOException {
		int totalUsers      = metier.countUsers();
		int totalClients    = metier.countUsersByRole("CLIENT");
		int totalAdmins     = metier.countUsersByRole("ADMIN");
		int totalPrestaires = metier.countUsersByRole("PRESTATAIRE");
		int usersActifs     = metier.countUsersByActive(true);
		int usersSuspendus  = metier.countUsersByActive(false);
		int planFree        = metier.countUsersByPlan("FREE");
		int planPro         = metier.countUsersByPlan("PRO");
		int planPremium     = metier.countUsersByPlan("PREMIUM");
		int qComplete       = metier.countUsersByQuestionnaire(true);
		int qIncomplete     = metier.countUsersByQuestionnaire(false);

		int totalVendors    = metier.countVendors();
		int vendorsActifs   = metier.countVendorsByActive(true);
		int vendorsInactifs = metier.countVendorsByActive(false);
		int vendorsIncompl  = metier.countVendorsIncomplete();

		int totalDevis = metier.countDevis();
		int devisNew = metier.countDevisByStatut("EN_ATTENTE");
		int devisAccepte = metier.countDevisByStatut("ACCEPTE");
		int totalRdv = metier.countRendezVous();
		int rdvAttente = metier.countRendezVousByStatut("EN_ATTENTE");
		int rdvConfirme = metier.countRendezVousByStatut("CONFIRME");

		double tauxConvPremium = totalUsers > 0 ? (planPremium * 100.0 / totalUsers) : 0;

		StringBuilder json = new StringBuilder("{");
		json.append("\"users\":{");
		json.append("\"total\":").append(totalUsers);
		json.append(",\"clients\":").append(totalClients);
		json.append(",\"admins\":").append(totalAdmins);
		json.append(",\"prestataires\":").append(totalPrestaires);
		json.append(",\"actifs\":").append(usersActifs);
		json.append(",\"suspendus\":").append(usersSuspendus);
		json.append("},");
		json.append("\"plans\":{");
		json.append("\"free\":").append(planFree);
		json.append(",\"pro\":").append(planPro);
		json.append(",\"premium\":").append(planPremium);
		json.append(",\"tauxPremium\":").append(String.format(java.util.Locale.US, "%.1f", tauxConvPremium));
		json.append("},");
		json.append("\"questionnaire\":{");
		json.append("\"complete\":").append(qComplete);
		json.append(",\"incomplete\":").append(qIncomplete);
		json.append("},");
		json.append("\"vendors\":{");
		json.append("\"total\":").append(totalVendors);
		json.append(",\"actifs\":").append(vendorsActifs);
		json.append(",\"inactifs\":").append(vendorsInactifs);
		json.append(",\"incomplets\":").append(vendorsIncompl);
		json.append("},");
		json.append("\"devis\":{");
		json.append("\"total\":").append(totalDevis);
		json.append(",\"nouveaux\":").append(devisNew);
		json.append(",\"acceptes\":").append(devisAccepte);
		json.append("},");
		json.append("\"rdv\":{");
		json.append("\"total\":").append(totalRdv);
		json.append(",\"attente\":").append(rdvAttente);
		json.append(",\"confirmes\":").append(rdvConfirme);
		json.append("}}");

		JsonUtil.sendJson(res, json.toString());
	}

	// ============================================================
	// 2. USERS — liste, detail, modification, actions
	// ============================================================
	private void handleUsersList(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String q     = req.getParameter("q");
		String role  = req.getParameter("role");
		String plan  = req.getParameter("plan");
		String qc    = req.getParameter("questionnaire");
		String act   = req.getParameter("active");
		int page     = parseInt(req.getParameter("page"), 1);
		int perPage  = parseInt(req.getParameter("perPage"), 20);
		Boolean qcB  = ("1".equals(qc)) ? Boolean.TRUE : ("0".equals(qc) ? Boolean.FALSE : null);
		Boolean actB = ("1".equals(act)) ? Boolean.TRUE : ("0".equals(act) ? Boolean.FALSE : null);

		int total = metier.countSearchUsers(q, role, plan, qcB, actB);
		List<User> list = metier.searchUsers(q, role, plan, qcB, actB, (page - 1) * perPage, perPage);

		StringBuilder json = new StringBuilder("{");
		json.append("\"page\":").append(page);
		json.append(",\"perPage\":").append(perPage);
		json.append(",\"total\":").append(total);
		json.append(",\"pages\":").append((int) Math.ceil(total / (double) perPage));
		json.append(",\"items\":[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) json.append(",");
			json.append(userJson(list.get(i)));
		}
		json.append("]}");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleUserDetail(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		User u = metier.getUserById(id);
		if (u == null) { JsonUtil.sendError(res, 404, "Utilisateur non trouve"); return; }

		int nbDevis = metier.countDevisByClient(id);
		int nbRdv   = metier.countRendezVousByClient(id);
		int nbPicks = metier.countPicksByUser(id);
		boolean hasQ = metier.countQuestionnaireByUser(id) > 0;
		int completeness = computeUserCompleteness(u, hasQ);

		StringBuilder json = new StringBuilder("{");
		json.append("\"user\":").append(userJson(u));
		json.append(",\"completeness\":").append(completeness);
		json.append(",\"counts\":{");
		json.append("\"devis\":").append(nbDevis);
		json.append(",\"rdv\":").append(nbRdv);
		json.append(",\"picks\":").append(nbPicks);
		json.append(",\"hasQuestionnaire\":").append(hasQ);
		json.append("}}");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleUserUpdate(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String firstName = JsonUtil.getStringValue(body, "firstName");
		String lastName  = JsonUtil.getStringValue(body, "lastName");
		String email     = JsonUtil.getStringValue(body, "email");
		String phone     = JsonUtil.getStringValue(body, "phone");
		String city      = JsonUtil.getStringValue(body, "city");
		boolean ok = metier.updateUser(id, firstName, lastName, email, phone, city);
		if (ok) JsonUtil.sendSuccess(res, "Utilisateur mis a jour");
		else JsonUtil.sendError(res, 500, "Echec mise a jour");
	}

	private void handleUserRole(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String role = JsonUtil.getStringValue(body, "role");
		if (role == null || !role.matches("CLIENT|ADMIN|PRESTATAIRE")) {
			JsonUtil.sendError(res, 400, "Role invalide"); return;
		}
		boolean ok = metier.updateUserRole(id, role);
		if (ok) JsonUtil.sendSuccess(res, "Role change en " + role);
		else JsonUtil.sendError(res, 500, "Echec");
	}

	private void handleUserPlan(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String plan = JsonUtil.getStringValue(body, "plan");
		if (plan == null || !plan.matches("FREE|PRO|PREMIUM")) {
			JsonUtil.sendError(res, 400, "Plan invalide"); return;
		}
		boolean ok = metier.changeSubscription(id, plan);
		// Synchronise aussi la table subscriptions
		metier.syncSubscriptionPlan(id, plan);
		if (ok) JsonUtil.sendSuccess(res, "Plan change en " + plan);
		else JsonUtil.sendError(res, 500, "Echec");
	}

	private void handleUserActive(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String activeStr = JsonUtil.getStringValue(body, "active");
		boolean active = !"false".equalsIgnoreCase(activeStr) && !"0".equals(activeStr);
		boolean ok = metier.updateUserActive(id, active);
		if (ok) JsonUtil.sendSuccess(res, active ? "Compte reactive" : "Compte suspendu");
		else JsonUtil.sendError(res, 500, "Echec");
	}

	private void handleUserDelete(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		// Empeche de supprimer le dernier admin
		User u = metier.getUserById(id);
		if (u != null && "ADMIN".equals(u.getRole()) && metier.countUsersByRole("ADMIN") <= 1) {
			JsonUtil.sendError(res, 400, "Impossible : c'est le dernier compte admin");
			return;
		}
		boolean ok = metier.deleteUser(id);
		if (ok) JsonUtil.sendSuccess(res, "Compte supprime");
		else JsonUtil.sendError(res, 500, "Echec suppression");
	}

	// ============================================================
	// 3. VENDORS
	// ============================================================
	private void handleVendorsList(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String q       = req.getParameter("q");
		Integer cat    = parseIntOrNull(req.getParameter("cat"));
		String city    = req.getParameter("city");
		String gamme   = req.getParameter("gamme");
		String act     = req.getParameter("active");
		int page       = parseInt(req.getParameter("page"), 1);
		int perPage    = parseInt(req.getParameter("perPage"), 20);
		Boolean actB   = ("1".equals(act)) ? Boolean.TRUE : ("0".equals(act) ? Boolean.FALSE : null);

		int total = metier.countSearchVendors(q, cat, city, gamme, actB);
		List<Vendor> list = metier.searchVendorsAll(q, cat, city, gamme, actB, (page - 1) * perPage, perPage);

		StringBuilder json = new StringBuilder("{");
		json.append("\"page\":").append(page);
		json.append(",\"perPage\":").append(perPage);
		json.append(",\"total\":").append(total);
		json.append(",\"pages\":").append((int) Math.ceil(total / (double) perPage));
		json.append(",\"items\":[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) json.append(",");
			json.append(vendorJson(list.get(i), VendorDao.computeCompleteness(list.get(i))));
		}
		json.append("]}");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleVendorDetail(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		Vendor v = metier.getVendor(id);
		if (v == null) { JsonUtil.sendError(res, 404, "Prestataire non trouve"); return; }
		int compl = VendorDao.computeCompleteness(v);
		int nbDevis = metier.countDevisByVendor(id);
		int nbRdv   = metier.countRendezVousByVendor(id);
		int nbRecos = metier.countRecommendationsByVendor(id);
		int nbPicks = metier.countPicksByVendor(id);

		StringBuilder json = new StringBuilder("{");
		json.append("\"vendor\":").append(vendorJson(v, compl));
		json.append(",\"counts\":{");
		json.append("\"devis\":").append(nbDevis);
		json.append(",\"rdv\":").append(nbRdv);
		json.append(",\"recos\":").append(nbRecos);
		json.append(",\"picks\":").append(nbPicks);
		json.append("}}");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleVendorUpdate(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String b = JsonUtil.readRequestBody(req);
		String name = JsonUtil.getStringValue(b, "name");
		String city = JsonUtil.getStringValue(b, "city");
		String desc = JsonUtil.getStringValue(b, "description");
		String pmin = JsonUtil.getStringValue(b, "prixMin");
		String pmax = JsonUtil.getStringValue(b, "prixMax");
		String gamme = JsonUtil.getStringValue(b, "gamme");
		String phone = JsonUtil.getStringValue(b, "phone");
		String email = JsonUtil.getStringValue(b, "email");
		String ig = JsonUtil.getStringValue(b, "instagram");
		String addr = JsonUtil.getStringValue(b, "address");
		String tags = JsonUtil.getStringValue(b, "tags");
		Double dmin = pmin != null ? Double.parseDouble(pmin) : null;
		Double dmax = pmax != null ? Double.parseDouble(pmax) : null;
		boolean ok = metier.updateVendor(id, name, city, desc, dmin, dmax, gamme, phone, email, ig, addr, tags);
		if (ok) JsonUtil.sendSuccess(res, "Prestataire mis a jour");
		else JsonUtil.sendError(res, 500, "Echec");
	}

	private void handleVendorActive(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String activeStr = JsonUtil.getStringValue(body, "active");
		boolean active = !"false".equalsIgnoreCase(activeStr) && !"0".equals(activeStr);
		boolean ok = metier.updateVendorActive(id, active);
		if (ok) JsonUtil.sendSuccess(res, active ? "Prestataire reactive" : "Prestataire suspendu");
		else JsonUtil.sendError(res, 500, "Echec");
	}

	private void handleVendorDelete(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		boolean ok = metier.deleteVendor(id);
		if (ok) JsonUtil.sendSuccess(res, "Prestataire supprime");
		else JsonUtil.sendError(res, 500, "Echec");
	}

	// ============================================================
	// 4. DEVIS & RDV
	// ============================================================
	private void handleDevis(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String status = req.getParameter("status");
		List<Devis> list = metier.getAllDevis(status);

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) json.append(",");
			Devis d = list.get(i);
			json.append("{\"id\":").append(d.getId())
				.append(",\"clientId\":").append(d.getClientId())
				.append(",\"vendorId\":").append(d.getVendorId())
				.append(",\"clientName\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getClientFirstName()) + " " + JsonUtil.safe(d.getClientLastName()))).append("\"")
				.append(",\"clientEmail\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getClientEmail()))).append("\"")
				.append(",\"vendorName\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getVendorName()))).append("\"")
				.append(",\"vendorCat\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getVendorCategory()))).append("\"")
				.append(",\"budgetMin\":").append(d.getBudgetMin())
				.append(",\"budgetMax\":").append(d.getBudgetMax())
				.append(",\"message\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getMessage()))).append("\"")
				.append(",\"dateMariage\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getDateMariage()))).append("\"")
				.append(",\"nbInvites\":").append(d.getNbInvites())
				.append(",\"statut\":\"").append(JsonUtil.safe(d.getStatut())).append("\"")
				.append(",\"reponse\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(d.getReponsePrestataire()))).append("\"")
				.append(",\"createdAt\":\"").append(JsonUtil.safe(d.getCreatedAt())).append("\"}");
		}
		json.append("]");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleDevisStatus(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String status = JsonUtil.getStringValue(body, "status");
		if (status == null || !status.matches("EN_ATTENTE|ACCEPTE|REFUSE")) {
			JsonUtil.sendError(res, 400, "Statut invalide"); return;
		}
		boolean ok = metier.updateDevisStatut(id, status);
		if (ok) JsonUtil.sendSuccess(res, "Statut change");
		else JsonUtil.sendError(res, 404, "Devis non trouve");
	}

	private void handleRdv(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String status = req.getParameter("status");
		List<RendezVous> list = metier.getAllRendezVous(status);

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) json.append(",");
			RendezVous r = list.get(i);
			json.append("{\"id\":").append(r.getId())
				.append(",\"clientId\":").append(r.getClientId())
				.append(",\"vendorId\":").append(r.getVendorId())
				.append(",\"clientName\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getClientFirstName()) + " " + JsonUtil.safe(r.getClientLastName()))).append("\"")
				.append(",\"clientPhone\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getClientPhone()))).append("\"")
				.append(",\"vendorName\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getVendorName()))).append("\"")
				.append(",\"dateRdv\":\"").append(JsonUtil.safe(r.getDateRdv())).append("\"")
				.append(",\"heureRdv\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getHeureRdv()))).append("\"")
				.append(",\"lieu\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getLieu()))).append("\"")
				.append(",\"note\":\"").append(JsonUtil.escapeJson(JsonUtil.safe(r.getNote()))).append("\"")
				.append(",\"statut\":\"").append(JsonUtil.safe(r.getStatut())).append("\"")
				.append(",\"createdAt\":\"").append(JsonUtil.safe(r.getCreatedAt())).append("\"}");
		}
		json.append("]");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleRdvStatus(HttpServletRequest req, HttpServletResponse res, int id) throws IOException {
		String body = JsonUtil.readRequestBody(req);
		String status = JsonUtil.getStringValue(body, "status");
		if (status == null || !status.matches("EN_ATTENTE|CONFIRME|ANNULE")) {
			JsonUtil.sendError(res, 400, "Statut invalide"); return;
		}
		boolean ok = metier.updateRendezVousStatut(id, status);
		if (ok) JsonUtil.sendSuccess(res, "Statut change");
		else JsonUtil.sendError(res, 404, "RDV non trouve");
	}

	// ============================================================
	// 5. ACTIVITY + ACTIONS + ANALYTICS
	// ============================================================
	private void handleActivity(HttpServletRequest req, HttpServletResponse res) throws IOException {
		int limit = parseInt(req.getParameter("limit"), 25);
		StringBuilder json = new StringBuilder("[");
		boolean first = true;

		for (Map<String, Object> row : metier.recentUsers(limit)) {
			if (!first) json.append(","); first = false;
			json.append("{\"type\":\"USER_REGISTER\"")
				.append(",\"label\":\"Inscription\"")
				.append(",\"who\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name")))).append("\"")
				.append(",\"meta\":\"").append(str(row.get("role"))).append("\"")
				.append(",\"date\":\"").append(str(row.get("created_at"))).append("\"}");
		}
		for (Map<String, Object> row : metier.recentDevis(limit)) {
			if (!first) json.append(","); first = false;
			json.append("{\"type\":\"DEVIS\"")
				.append(",\"label\":\"Demande de devis\"")
				.append(",\"who\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name")))).append("\"")
				.append(",\"meta\":\"").append(JsonUtil.escapeJson(str(row.get("vendor_name")))).append(" / ").append(str(row.get("statut"))).append("\"")
				.append(",\"date\":\"").append(str(row.get("created_at"))).append("\"}");
		}
		for (Map<String, Object> row : metier.recentRdv(limit)) {
			if (!first) json.append(","); first = false;
			json.append("{\"type\":\"RDV\"")
				.append(",\"label\":\"Rendez-vous\"")
				.append(",\"who\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name")))).append("\"")
				.append(",\"meta\":\"").append(JsonUtil.escapeJson(str(row.get("vendor_name")))).append(" / ").append(str(row.get("statut"))).append("\"")
				.append(",\"date\":\"").append(str(row.get("created_at"))).append("\"}");
		}
		for (Map<String, Object> row : metier.recentQuestionnaires(limit)) {
			if (!first) json.append(","); first = false;
			json.append("{\"type\":\"QUESTIONNAIRE\"")
				.append(",\"label\":\"Questionnaire complete\"")
				.append(",\"who\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name")))).append("\"")
				.append(",\"meta\":\"\"")
				.append(",\"date\":\"").append(str(row.get("created_at"))).append("\"}");
		}
		json.append("]");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleActions(HttpServletRequest req, HttpServletResponse res) throws IOException {
		StringBuilder json = new StringBuilder("[");
		boolean first = true;

		for (Map<String, Object> row : metier.pendingDevis(10)) {
			if (!first) json.append(","); first = false;
			json.append("{\"priority\":\"HIGH\",\"type\":\"DEVIS\"")
				.append(",\"id\":").append(intOf(row.get("id")))
				.append(",\"title\":\"Devis en attente\"")
				.append(",\"detail\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name"))))
				.append(" -> ").append(JsonUtil.escapeJson(str(row.get("vendor_name")))).append("\"}");
		}
		for (Map<String, Object> row : metier.pendingRdv(10)) {
			if (!first) json.append(","); first = false;
			json.append("{\"priority\":\"HIGH\",\"type\":\"RDV\"")
				.append(",\"id\":").append(intOf(row.get("id")))
				.append(",\"title\":\"Rendez-vous a confirmer\"")
				.append(",\"detail\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name"))))
				.append(" / ").append(JsonUtil.escapeJson(str(row.get("vendor_name"))))
				.append(" - ").append(str(row.get("date_rdv"))).append("\"}");
		}
		for (Map<String, Object> row : metier.incompleteVendors(10)) {
			if (!first) json.append(","); first = false;
			json.append("{\"priority\":\"MEDIUM\",\"type\":\"VENDOR_INCOMPLETE\"")
				.append(",\"id\":").append(intOf(row.get("id")))
				.append(",\"title\":\"Profil prestataire incomplet\"")
				.append(",\"detail\":\"").append(JsonUtil.escapeJson(str(row.get("name")))).append("\"}");
		}
		for (Map<String, Object> row : metier.clientsWithoutQuestionnaire(10)) {
			if (!first) json.append(","); first = false;
			json.append("{\"priority\":\"LOW\",\"type\":\"USER_NO_QUESTIONNAIRE\"")
				.append(",\"id\":").append(intOf(row.get("id")))
				.append(",\"title\":\"Client sans questionnaire\"")
				.append(",\"detail\":\"").append(JsonUtil.escapeJson(str(row.get("first_name")) + " " + str(row.get("last_name")))).append("\"}");
		}
		json.append("]");
		JsonUtil.sendJson(res, json.toString());
	}

	private void handleAnalytics(HttpServletRequest req, HttpServletResponse res) throws IOException {
		StringBuilder json = new StringBuilder("{");

		// 1) Inscriptions par mois
		json.append("\"signupsByMonth\":[");
		boolean first = true;
		for (Map<String, Object> row : metier.signupsByMonth()) {
			if (!first) json.append(","); first = false;
			json.append("{\"month\":\"").append(str(row.get("month")))
				.append("\",\"count\":").append(intOf(row.get("count"))).append("}");
		}
		json.append("]");

		// 2) Repartition Free/Pro/Premium
		json.append(",\"plansDistribution\":[");
		first = true;
		for (Map<String, Object> row : metier.plansDistribution()) {
			if (!first) json.append(","); first = false;
			json.append("{\"plan\":\"").append(str(row.get("plan")))
				.append("\",\"count\":").append(intOf(row.get("count"))).append("}");
		}
		json.append("]");

		// 3) Vendors par categorie
		json.append(",\"vendorsByCategory\":[");
		Map<String, Integer> map = metier.countVendorsByCategory();
		first = true;
		for (Map.Entry<String, Integer> e : map.entrySet()) {
			if (!first) json.append(","); first = false;
			json.append("{\"category\":\"").append(JsonUtil.escapeJson(e.getKey()))
				.append("\",\"count\":").append(e.getValue()).append("}");
		}
		json.append("]");

		// 4) Questionnaires
		int qComplete = metier.countUsersByQuestionnaire(true);
		int qIncomplete = metier.countUsersByQuestionnaire(false);
		json.append(",\"questionnaire\":{\"complete\":").append(qComplete)
			.append(",\"incomplete\":").append(qIncomplete).append("}");

		// 5) Devis par statut
		json.append(",\"devisByStatus\":[");
		first = true;
		for (Map<String, Object> row : metier.devisByStatus()) {
			if (!first) json.append(","); first = false;
			json.append("{\"status\":\"").append(str(row.get("statut")))
				.append("\",\"count\":").append(intOf(row.get("count"))).append("}");
		}
		json.append("]");

		json.append("}");
		JsonUtil.sendJson(res, json.toString());
	}

	// ============================================================
	// HELPERS
	// ============================================================
	private String userJson(User u) {
		return "{\"id\":" + u.getId()
			+ ",\"firstName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(u.getFirstName())) + "\""
			+ ",\"lastName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(u.getLastName())) + "\""
			+ ",\"email\":\"" + JsonUtil.escapeJson(JsonUtil.safe(u.getEmail())) + "\""
			+ ",\"phone\":\"" + JsonUtil.escapeJson(JsonUtil.safe(u.getPhone())) + "\""
			+ ",\"city\":\"" + JsonUtil.escapeJson(JsonUtil.safe(u.getCity())) + "\""
			+ ",\"role\":\"" + u.getRole() + "\""
			+ ",\"plan\":\"" + u.getSubscriptionType() + "\""
			+ ",\"questionnaireCompleted\":" + u.isQuestionnaireCompleted()
			+ ",\"active\":" + u.isActive()
			+ ",\"vendorId\":" + u.getVendorId()
			+ ",\"createdAt\":\"" + JsonUtil.safe(u.getCreatedAt()) + "\"}";
	}

	private String vendorJson(Vendor v, int completeness) {
		return "{\"id\":" + v.getId()
			+ ",\"name\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getName())) + "\""
			+ ",\"category\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getCategoryName())) + "\""
			+ ",\"categoryId\":" + v.getCategoryId()
			+ ",\"city\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getCity())) + "\""
			+ ",\"description\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getDescription())) + "\""
			+ ",\"prixMin\":" + v.getPrixMin()
			+ ",\"prixMax\":" + v.getPrixMax()
			+ ",\"gamme\":\"" + JsonUtil.safe(v.getGamme()) + "\""
			+ ",\"phone\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getPhone())) + "\""
			+ ",\"email\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getEmail())) + "\""
			+ ",\"instagram\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getInstagram())) + "\""
			+ ",\"address\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getAddress())) + "\""
			+ ",\"tags\":\"" + JsonUtil.escapeJson(JsonUtil.safe(v.getTags())) + "\""
			+ ",\"rating\":" + v.getRating()
			+ ",\"nbAvis\":" + v.getNbAvis()
			+ ",\"active\":" + v.isActive()
			+ ",\"completeness\":" + completeness + "}";
	}

	private int computeUserCompleteness(User u, boolean hasQuestionnaire) {
		int total = 6, score = 0;
		if (u.getFirstName() != null && !u.getFirstName().isEmpty()) score++;
		if (u.getLastName() != null && !u.getLastName().isEmpty()) score++;
		if (u.getEmail() != null && !u.getEmail().isEmpty()) score++;
		if (u.getPhone() != null && !u.getPhone().isEmpty()) score++;
		if (u.getCity() != null && !u.getCity().isEmpty()) score++;
		if (hasQuestionnaire) score++;
		return (int) Math.round(score * 100.0 / total);
	}

	private int parseInt(String s, int dflt) {
		if (s == null) return dflt;
		try { return Integer.parseInt(s); } catch (NumberFormatException e) { return dflt; }
	}

	private Integer parseIntOrNull(String s) {
		if (s == null || s.isEmpty()) return null;
		try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
	}

	private String str(Object o) { return o == null ? "" : o.toString(); }

	private int intOf(Object o) {
		if (o == null) return 0;
		if (o instanceof Number) return ((Number) o).intValue();
		try { return Integer.parseInt(o.toString()); } catch (NumberFormatException e) { return 0; }
	}
}
