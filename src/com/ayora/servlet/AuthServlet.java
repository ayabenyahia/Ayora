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
import com.ayora.model.Devis;
import com.ayora.model.RendezVous;
import com.ayora.model.Subscription;
import com.ayora.model.User;
import com.ayora.model.Vendor;
import com.ayora.util.JsonUtil;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Pattern Servlet -> Metier -> DAO -> Database.
	private IAyoraMetier metier;

	@Override
	public void init() throws ServletException {
		this.metier = AppWiring.getMetier();
	}

	// ============================================================
	// ROUTING
	// ============================================================

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if ("/login".equals(path)) { handleLogin(request, response); }
		else if ("/register".equals(path)) { handleRegister(request, response); }
		else if ("/logout".equals(path)) { handleLogout(request, response); }
		else if ("/devis-create".equals(path)) { handleDevisCreate(request, response); }
		else if ("/devis-update".equals(path)) { handleDevisUpdate(request, response); }
		else if ("/rdv-create".equals(path)) { handleRdvCreate(request, response); }
		else if ("/rdv-update".equals(path)) { handleRdvUpdate(request, response); }
		else { JsonUtil.sendError(response, 404, "Route non trouvee"); }
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if ("/me".equals(path)) { handleMe(request, response); }
		else if ("/admin-stats".equals(path)) { handleAdminStats(request, response); }
		else if ("/admin-users".equals(path)) { handleAdminUsers(request, response); }
		else if ("/admin-vendors".equals(path)) { handleAdminVendors(request, response); }
		else if ("/devis-list".equals(path)) { handleDevisList(request, response); }
		else if ("/rdv-list".equals(path)) { handleRdvList(request, response); }
		else if ("/vendor-dashboard".equals(path)) { handleVendorDashboard(request, response); }
		else { JsonUtil.sendError(response, 404, "Route non trouvee"); }
	}

	/**
	 * Self-service account management:
	 *   PUT /api/auth/profile  — update firstName / lastName / phone / city of the logged-in user
	 *   PUT /api/auth/password — change own password (verifies current first)
	 * Both routes require an authenticated session and operate on
	 * session.userId only — the front-end cannot pass an arbitrary id.
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if ("/profile".equals(path)) { handleUpdateProfile(request, response); }
		else if ("/password".equals(path)) { handleUpdatePassword(request, response); }
		else { JsonUtil.sendError(response, 404, "Route non trouvee"); }
	}

	// ------------------------------------------------------------------
	// Account management handlers (self-service)
	// ------------------------------------------------------------------

	private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie"); return;
		}
		int userId = (int) session.getAttribute("userId");
		User current = metier.getUserById(userId);
		if (current == null) { JsonUtil.sendError(response, 404, "Utilisateur non trouve"); return; }

		String body = JsonUtil.readRequestBody(request);
		String firstName = JsonUtil.getStringValue(body, "firstName");
		String lastName  = JsonUtil.getStringValue(body, "lastName");
		String phone     = JsonUtil.getStringValue(body, "phone");
		String city      = JsonUtil.getStringValue(body, "city");

		if (firstName == null || firstName.trim().isEmpty()) {
			JsonUtil.sendError(response, 400, "Le prenom est obligatoire."); return;
		}
		if (lastName == null || lastName.trim().isEmpty()) {
			JsonUtil.sendError(response, 400, "Le nom est obligatoire."); return;
		}
		// Defensive length caps — DB columns are VARCHAR(255).
		if (firstName.length() > 100 || lastName.length() > 100
				|| (phone != null && phone.length() > 40)
				|| (city != null && city.length() > 100)) {
			JsonUtil.sendError(response, 400, "Une des informations est trop longue."); return;
		}
		// Light phone validation: tolerant of +212 / 0 / 00212 / international.
		if (phone != null && !phone.isEmpty()) {
			String compact = phone.replaceAll("[\\s\\-\\.]", "");
			if (!compact.matches("^\\+?\\d{8,15}$")) {
				JsonUtil.sendError(response, 400, "Numero de telephone invalide."); return;
			}
			phone = compact;
		}
		String cleanCity = (city == null || city.trim().isEmpty()) ? current.getCity() : city.trim();

		// Email is *never* taken from the request — login identifier, kept readonly.
		boolean ok = metier.updateUser(
			userId,
			firstName.trim(),
			lastName.trim(),
			current.getEmail(),  // unchanged
			phone == null ? current.getPhone() : phone,
			cleanCity
		);
		if (!ok) {
			JsonUtil.sendError(response, 500, "Impossible de mettre a jour vos informations pour le moment."); return;
		}

		// Refresh the cached User in session so other pages see the change immediately.
		User refreshed = metier.getUserById(userId);
		if (refreshed != null) session.setAttribute("user", refreshed);

		String json = "{\"success\":true,\"message\":\"Vos informations ont ete mises a jour avec succes.\""
			+ ",\"user\":{"
			+ "\"id\":" + refreshed.getId()
			+ ",\"firstName\":\"" + JsonUtil.escapeJson(refreshed.getFirstName()) + "\""
			+ ",\"lastName\":\""  + JsonUtil.escapeJson(refreshed.getLastName())  + "\""
			+ ",\"email\":\""     + JsonUtil.escapeJson(refreshed.getEmail())     + "\""
			+ ",\"phone\":\""     + JsonUtil.escapeJson(refreshed.getPhone() == null ? "" : refreshed.getPhone()) + "\""
			+ ",\"city\":\""      + JsonUtil.escapeJson(refreshed.getCity()  == null ? "" : refreshed.getCity())  + "\""
			+ "}}";
		JsonUtil.sendJson(response, json);
	}

	private void handleUpdatePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie"); return;
		}
		int userId = (int) session.getAttribute("userId");
		String body = JsonUtil.readRequestBody(request);
		String currentPwd = JsonUtil.getStringValue(body, "currentPassword");
		String newPwd     = JsonUtil.getStringValue(body, "newPassword");
		String confirmPwd = JsonUtil.getStringValue(body, "confirmPassword");

		if (currentPwd == null || newPwd == null || confirmPwd == null) {
			JsonUtil.sendError(response, 400, "Tous les champs sont obligatoires."); return;
		}
		if (!newPwd.equals(confirmPwd)) {
			JsonUtil.sendError(response, 400, "Les deux mots de passe ne correspondent pas."); return;
		}
		if (newPwd.length() < 8) {
			JsonUtil.sendError(response, 400, "Le nouveau mot de passe doit contenir au moins 8 caracteres."); return;
		}
		if (newPwd.equals(currentPwd)) {
			JsonUtil.sendError(response, 400, "Le nouveau mot de passe doit etre different de l'actuel."); return;
		}

		String result = metier.updateUserPassword(userId, currentPwd, newPwd);
		switch (result) {
			case "OK":
				JsonUtil.sendJson(response,
					"{\"success\":true,\"message\":\"Votre mot de passe a ete modifie avec succes.\"}");
				return;
			case "WRONG_CURRENT":
				JsonUtil.sendError(response, 400, "Le mot de passe actuel est incorrect.");
				return;
			case "TOO_SHORT":
				JsonUtil.sendError(response, 400, "Le nouveau mot de passe est trop court.");
				return;
			case "USER_NOT_FOUND":
				JsonUtil.sendError(response, 404, "Utilisateur introuvable.");
				return;
			default:
				JsonUtil.sendError(response, 500, "Impossible de modifier le mot de passe pour le moment.");
		}
	}

	// ============================================================
	// AUTH ENDPOINTS (login, register, logout, me)
	// ============================================================

	private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = JsonUtil.readRequestBody(request);
		String email = JsonUtil.getStringValue(body, "email");
		String password = JsonUtil.getStringValue(body, "password");
		if (email == null || password == null) { JsonUtil.sendError(response, 400, "Email et mot de passe requis"); return; }

		User user = metier.authenticate(email, password);
		if (user == null) { JsonUtil.sendError(response, 401, "Email ou mot de passe incorrect"); return; }

		HttpSession session = request.getSession();
		session.setAttribute("userId", user.getId());
		session.setAttribute("user", user);
		session.setAttribute("role", user.getRole());

		String json = "{\"success\":true,\"user\":{"
				+ "\"id\":" + user.getId()
				+ ",\"email\":\"" + JsonUtil.escapeJson(user.getEmail()) + "\""
				+ ",\"firstName\":\"" + JsonUtil.escapeJson(user.getFirstName()) + "\""
				+ ",\"lastName\":\"" + JsonUtil.escapeJson(user.getLastName()) + "\""
				+ ",\"subscriptionType\":\"" + user.getSubscriptionType() + "\""
				+ ",\"questionnaireCompleted\":" + user.isQuestionnaireCompleted()
				+ ",\"role\":\"" + user.getRole() + "\""
				+ ",\"vendorId\":" + user.getVendorId()
				+ "}}";
		JsonUtil.sendJson(response, json);
	}

	private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = JsonUtil.readRequestBody(request);
		String email = JsonUtil.getStringValue(body, "email");
		String password = JsonUtil.getStringValue(body, "password");
		String firstName = JsonUtil.getStringValue(body, "firstName");
		String lastName = JsonUtil.getStringValue(body, "lastName");
		String phone = JsonUtil.getStringValue(body, "phone");
		if (email == null || password == null || firstName == null || lastName == null) {
			JsonUtil.sendError(response, 400, "Champs obligatoires manquants"); return;
		}
		if (metier.getUserByEmail(email) != null) { JsonUtil.sendError(response, 409, "Cet email est deja utilise"); return; }

		// Regle metier : tout email se terminant par @ayora.ma est un compte
		// interne (equipe Ayora) et passe directement en PREMIUM.
		boolean isAyoraStaff = email.toLowerCase().endsWith("@ayora.ma");
		String plan = isAyoraStaff ? "PREMIUM" : "FREE";

		User user = new User();
		user.setEmail(email); user.setPassword(password);
		user.setFirstName(firstName); user.setLastName(lastName);
		user.setPhone(phone); user.setCity("Fes");
		user.setSubscriptionType(plan);
		int userId = metier.createUser(user);
		if (userId == -1) { JsonUtil.sendError(response, 500, "Erreur creation"); return; }

		Subscription sub = new Subscription(); sub.setUserId(userId); sub.setPlan(plan);
		metier.addSubscription(sub); user.setId(userId);

		// Le DAO peut ne pas avoir tenu compte du subscription_type passe en
		// objet User (selon implementation). On force ici la valeur en base
		// pour garantir le PREMIUM des comptes internes Ayora.
		if (isAyoraStaff) {
			metier.changeSubscription(userId, "PREMIUM");
		}

		HttpSession session = request.getSession();
		session.setAttribute("userId", userId); session.setAttribute("user", user); session.setAttribute("role", "CLIENT");

		String json = "{\"success\":true,\"user\":{"
				+ "\"id\":" + userId + ",\"email\":\"" + JsonUtil.escapeJson(email) + "\""
				+ ",\"firstName\":\"" + JsonUtil.escapeJson(firstName) + "\""
				+ ",\"lastName\":\"" + JsonUtil.escapeJson(lastName) + "\""
				+ ",\"subscriptionType\":\"" + plan + "\",\"questionnaireCompleted\":false"
				+ ",\"role\":\"CLIENT\",\"vendorId\":0}}";
		JsonUtil.sendJson(response, json);
	}

	private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session != null) session.invalidate();
		JsonUtil.sendSuccess(response, "Deconnexion reussie");
	}

	private void handleMe(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User user = metier.getUserById(userId);
		if (user == null) { JsonUtil.sendError(response, 404, "Utilisateur non trouve"); return; }
		Subscription sub = metier.getSubscription(userId);

		String json = "{\"user\":{"
				+ "\"id\":" + user.getId()
				+ ",\"email\":\"" + JsonUtil.escapeJson(user.getEmail()) + "\""
				+ ",\"firstName\":\"" + JsonUtil.escapeJson(user.getFirstName()) + "\""
				+ ",\"lastName\":\"" + JsonUtil.escapeJson(user.getLastName()) + "\""
				+ ",\"phone\":\"" + JsonUtil.escapeJson(user.getPhone() != null ? user.getPhone() : "") + "\""
				+ ",\"subscriptionType\":\"" + user.getSubscriptionType() + "\""
				+ ",\"questionnaireCompleted\":" + user.isQuestionnaireCompleted()
				+ ",\"role\":\"" + user.getRole() + "\""
				+ ",\"vendorId\":" + user.getVendorId()
				+ "},\"subscription\":{"
				+ "\"plan\":\"" + (sub != null ? sub.getPlan() : "FREE") + "\""
				+ ",\"invitationsSent\":" + (sub != null ? sub.getInvitationsSent() : 0)
				+ ",\"maxFree\":" + (sub != null ? sub.getMaxInvitationsFree() : 10)
				+ ",\"canSendInvitation\":" + (sub != null ? sub.canSendInvitation() : true)
				+ "}}";
		JsonUtil.sendJson(response, json);
	}

	// ============================================================
	// ADMIN ENDPOINTS
	// ============================================================

	private boolean checkAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
		HttpSession s = req.getSession(false);
		if (s == null || s.getAttribute("userId") == null) { JsonUtil.sendError(res, 401, "Non authentifie"); return false; }
		User u = metier.getUserById((int) s.getAttribute("userId"));
		if (u == null || !"ADMIN".equals(u.getRole())) { JsonUtil.sendError(res, 403, "Acces refuse"); return false; }
		return true;
	}

	private void handleAdminStats(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!checkAdmin(request, response)) return;
		int totalUsers = metier.countUsers();
		int totalClients = metier.countUsersByRole("CLIENT");
		int totalPrestataires = metier.countUsersByRole("PRESTATAIRE");
		int totalVendors = metier.getAllVendors().size();
		int totalDevis = metier.countDevis();
		int totalRdv = metier.countRendezVous();

		String json = "{\"totalUsers\":" + totalUsers + ",\"totalClients\":" + totalClients
				+ ",\"totalPrestataires\":" + totalPrestataires + ",\"totalVendors\":" + totalVendors
				+ ",\"totalDevis\":" + totalDevis + ",\"totalRdv\":" + totalRdv + "}";
		JsonUtil.sendJson(response, json);
	}

	private void handleAdminUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!checkAdmin(request, response)) return;
		List<User> users = metier.getAllUsers();
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			if (i > 0) json.append(",");
			json.append("{\"id\":").append(u.getId())
				.append(",\"email\":\"").append(JsonUtil.escapeJson(u.getEmail())).append("\"")
				.append(",\"firstName\":\"").append(JsonUtil.escapeJson(u.getFirstName())).append("\"")
				.append(",\"lastName\":\"").append(JsonUtil.escapeJson(u.getLastName())).append("\"")
				.append(",\"phone\":\"").append(JsonUtil.escapeJson(u.getPhone() != null ? u.getPhone() : "")).append("\"")
				.append(",\"city\":\"").append(JsonUtil.escapeJson(u.getCity() != null ? u.getCity() : "")).append("\"")
				.append(",\"subscriptionType\":\"").append(u.getSubscriptionType()).append("\"")
				.append(",\"role\":\"").append(u.getRole()).append("\"")
				.append(",\"questionnaireCompleted\":").append(u.isQuestionnaireCompleted()).append("}");
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}

	private void handleAdminVendors(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!checkAdmin(request, response)) return;
		List<Vendor> vendors = metier.getAllVendors();
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < vendors.size(); i++) {
			Vendor v = vendors.get(i);
			if (i > 0) json.append(",");
			json.append("{\"id\":").append(v.getId())
				.append(",\"name\":\"").append(JsonUtil.escapeJson(v.getName())).append("\"")
				.append(",\"categoryName\":\"").append(JsonUtil.escapeJson(v.getCategoryName())).append("\"")
				.append(",\"city\":\"").append(JsonUtil.escapeJson(v.getCity() != null ? v.getCity() : "")).append("\"")
				.append(",\"gamme\":\"").append(v.getGamme()).append("\"")
				.append(",\"prixMin\":").append(v.getPrixMin()).append(",\"prixMax\":").append(v.getPrixMax())
				.append(",\"rating\":").append(v.getRating()).append(",\"nbAvis\":").append(v.getNbAvis())
				.append(",\"phone\":\"").append(JsonUtil.escapeJson(v.getPhone() != null ? v.getPhone() : "")).append("\"")
				.append(",\"email\":\"").append(JsonUtil.escapeJson(v.getEmail() != null ? v.getEmail() : "")).append("\"")
				.append(",\"active\":").append(v.isActive()).append("}");
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}

	// ============================================================
	// DEVIS ENDPOINTS (create, list, update)
	// ============================================================

	private void handleDevisCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int clientId = (int) session.getAttribute("userId");

		String body = JsonUtil.readRequestBody(request);
		String vendorIdStr = JsonUtil.getStringValue(body, "vendorId");
		String budgetMinStr = JsonUtil.getStringValue(body, "budgetMin");
		String budgetMaxStr = JsonUtil.getStringValue(body, "budgetMax");
		String message = JsonUtil.getStringValue(body, "message");
		String dateMariage = JsonUtil.getStringValue(body, "dateMariage");
		String nbInvitesStr = JsonUtil.getStringValue(body, "nbInvites");

		if (vendorIdStr == null) { JsonUtil.sendError(response, 400, "vendorId requis"); return; }

		try {
			Devis d = new Devis();
			d.setClientId(clientId);
			d.setVendorId(Integer.parseInt(vendorIdStr));
			d.setBudgetMin(budgetMinStr != null ? Double.parseDouble(budgetMinStr) : 0);
			d.setBudgetMax(budgetMaxStr != null ? Double.parseDouble(budgetMaxStr) : 0);
			d.setMessage(message);
			d.setDateMariage(dateMariage);
			d.setNbInvites(nbInvitesStr != null ? Integer.parseInt(nbInvitesStr) : 0);

			int id = metier.addDevis(d);
			if (id <= 0) { JsonUtil.sendError(response, 500, "Erreur creation devis"); return; }
			JsonUtil.sendJson(response, "{\"success\":true,\"id\":" + id + "}");
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "Format numerique invalide");
		}
	}

	private void handleDevisList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User currentUser = metier.getUserById(userId);
		if (currentUser == null) { JsonUtil.sendError(response, 404, "User not found"); return; }

		List<Devis> devis;
		String role = currentUser.getRole();
		if ("CLIENT".equals(role)) {
			devis = metier.getDevisByClient(userId);
		} else if ("PRESTATAIRE".equals(role) && currentUser.getVendorId() > 0) {
			devis = metier.getDevisByVendor(currentUser.getVendorId());
		} else {
			devis = metier.getAllDevis();
		}

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < devis.size(); i++) {
			if (i > 0) json.append(",");
			json.append(buildDevisJson(devis.get(i)));
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}

	private void handleDevisUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }

		String body = JsonUtil.readRequestBody(request);
		String idStr = JsonUtil.getStringValue(body, "id");
		String statut = JsonUtil.getStringValue(body, "statut");
		String reponse = JsonUtil.getStringValue(body, "reponse");
		if (idStr == null || statut == null) { JsonUtil.sendError(response, 400, "id et statut requis"); return; }

		try {
			boolean ok = metier.updateDevisStatutAndReponse(Integer.parseInt(idStr), statut, reponse);
			if (ok) JsonUtil.sendJson(response, "{\"success\":true}");
			else JsonUtil.sendError(response, 500, "Erreur mise a jour");
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "id invalide");
		}
	}

	// ============================================================
	// RDV ENDPOINTS (create, list, update)
	// ============================================================

	private void handleRdvCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int clientId = (int) session.getAttribute("userId");

		String body = JsonUtil.readRequestBody(request);
		String vendorIdStr = JsonUtil.getStringValue(body, "vendorId");
		String dateRdv = JsonUtil.getStringValue(body, "dateRdv");
		String heureRdv = JsonUtil.getStringValue(body, "heureRdv");
		String lieu = JsonUtil.getStringValue(body, "lieu");
		String note = JsonUtil.getStringValue(body, "note");
		if (vendorIdStr == null || dateRdv == null) { JsonUtil.sendError(response, 400, "vendorId et dateRdv requis"); return; }

		try {
			RendezVous r = new RendezVous();
			r.setClientId(clientId);
			r.setVendorId(Integer.parseInt(vendorIdStr));
			r.setDateRdv(dateRdv);
			r.setHeureRdv(heureRdv);
			r.setLieu(lieu);
			r.setNote(note);
			int id = metier.addRendezVous(r);
			if (id <= 0) { JsonUtil.sendError(response, 500, "Erreur creation rdv"); return; }
			JsonUtil.sendJson(response, "{\"success\":true,\"id\":" + id + "}");
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "Format numerique invalide");
		}
	}

	private void handleRdvList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User currentUser = metier.getUserById(userId);
		if (currentUser == null) { JsonUtil.sendError(response, 404, "User not found"); return; }

		List<RendezVous> rdvs;
		String role = currentUser.getRole();
		if ("CLIENT".equals(role)) {
			rdvs = metier.getRendezVousByClient(userId);
		} else if ("PRESTATAIRE".equals(role) && currentUser.getVendorId() > 0) {
			rdvs = metier.getRendezVousByVendor(currentUser.getVendorId());
		} else {
			rdvs = metier.getAllRendezVous();
		}

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < rdvs.size(); i++) {
			if (i > 0) json.append(",");
			json.append(buildRdvJson(rdvs.get(i)));
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}

	private void handleRdvUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }

		String body = JsonUtil.readRequestBody(request);
		String idStr = JsonUtil.getStringValue(body, "id");
		String statut = JsonUtil.getStringValue(body, "statut");
		if (idStr == null || statut == null) { JsonUtil.sendError(response, 400, "id et statut requis"); return; }

		try {
			boolean ok = metier.updateRendezVousStatut(Integer.parseInt(idStr), statut);
			if (ok) JsonUtil.sendJson(response, "{\"success\":true}");
			else JsonUtil.sendError(response, 500, "Erreur mise a jour");
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "id invalide");
		}
	}

	// ============================================================
	// VENDOR PORTAL ENDPOINT
	// ============================================================

	private void handleVendorDashboard(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User user = metier.getUserById(userId);
		if (user == null || !"PRESTATAIRE".equals(user.getRole())) { JsonUtil.sendError(response, 403, "Acces refuse"); return; }

		int vendorId = user.getVendorId();
		if (vendorId <= 0) { JsonUtil.sendJson(response, "{\"vendor\":null}"); return; }
		Vendor vendor = metier.getVendor(vendorId);
		if (vendor == null) { JsonUtil.sendJson(response, "{\"vendor\":null}"); return; }

		StringBuilder json = new StringBuilder();
		json.append("{\"vendor\":{")
			.append("\"id\":").append(vendor.getId())
			.append(",\"name\":\"").append(JsonUtil.escapeJson(vendor.getName())).append("\"")
			.append(",\"categoryName\":\"").append(JsonUtil.escapeJson(vendor.getCategoryName())).append("\"")
			.append(",\"gamme\":\"").append(vendor.getGamme()).append("\"")
			.append(",\"prixMin\":").append(vendor.getPrixMin())
			.append(",\"prixMax\":").append(vendor.getPrixMax())
			.append(",\"rating\":").append(vendor.getRating())
			.append(",\"nbAvis\":").append(vendor.getNbAvis())
			.append(",\"phone\":\"").append(JsonUtil.escapeJson(vendor.getPhone() != null ? vendor.getPhone() : "")).append("\"")
			.append(",\"email\":\"").append(JsonUtil.escapeJson(vendor.getEmail() != null ? vendor.getEmail() : "")).append("\"")
			.append("}}");
		JsonUtil.sendJson(response, json.toString());
	}

	// ============================================================
	// JSON BUILDERS
	// ============================================================

	private String buildDevisJson(Devis d) {
		return "{\"id\":" + d.getId()
			+ ",\"clientId\":" + d.getClientId()
			+ ",\"vendorId\":" + d.getVendorId()
			+ ",\"clientName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getClientFirstName()) + " " + JsonUtil.safe(d.getClientLastName())) + "\""
			+ ",\"clientEmail\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getClientEmail())) + "\""
			+ ",\"clientPhone\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getClientPhone())) + "\""
			+ ",\"vendorName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getVendorName())) + "\""
			+ ",\"vendorCat\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getVendorCategory())) + "\""
			+ ",\"budgetMin\":" + d.getBudgetMin()
			+ ",\"budgetMax\":" + d.getBudgetMax()
			+ ",\"message\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getMessage())) + "\""
			+ ",\"dateMariage\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getDateMariage())) + "\""
			+ ",\"nbInvites\":" + d.getNbInvites()
			+ ",\"statut\":\"" + JsonUtil.safe(d.getStatut()) + "\""
			+ ",\"reponse\":\"" + JsonUtil.escapeJson(JsonUtil.safe(d.getReponsePrestataire())) + "\""
			+ ",\"createdAt\":\"" + JsonUtil.safe(d.getCreatedAt()) + "\""
			+ "}";
	}

	private String buildRdvJson(RendezVous r) {
		return "{\"id\":" + r.getId()
			+ ",\"clientId\":" + r.getClientId()
			+ ",\"vendorId\":" + r.getVendorId()
			+ ",\"clientName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(r.getClientFirstName()) + " " + JsonUtil.safe(r.getClientLastName())) + "\""
			+ ",\"clientPhone\":\"" + JsonUtil.escapeJson(JsonUtil.safe(r.getClientPhone())) + "\""
			+ ",\"vendorName\":\"" + JsonUtil.escapeJson(JsonUtil.safe(r.getVendorName())) + "\""
			+ ",\"dateRdv\":\"" + JsonUtil.safe(r.getDateRdv()) + "\""
			+ ",\"heureRdv\":\"" + JsonUtil.escapeJson(JsonUtil.safe(r.getHeureRdv())) + "\""
			+ ",\"lieu\":\"" + JsonUtil.escapeJson(JsonUtil.safe(r.getLieu())) + "\""
			+ ",\"note\":\"" + JsonUtil.escapeJson(JsonUtil.safe(r.getNote())) + "\""
			+ ",\"statut\":\"" + JsonUtil.safe(r.getStatut()) + "\""
			+ ",\"createdAt\":\"" + JsonUtil.safe(r.getCreatedAt()) + "\""
			+ "}";
	}

}
