package com.ayora.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.UserDao;
import com.ayora.dao.SubscriptionDao;
import com.ayora.dao.VendorDao;
import com.ayora.model.User;
import com.ayora.model.Vendor;
import com.ayora.model.Subscription;
import com.ayora.util.DatabaseConnection;
import com.ayora.util.JsonUtil;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

	private UserDao userDao;
	private SubscriptionDao subscriptionDao;
	private VendorDao vendorDao;

	@Override
	public void init() throws ServletException {
		userDao = new UserDao();
		subscriptionDao = new SubscriptionDao();
		vendorDao = new VendorDao();
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

	// ============================================================
	// AUTH ENDPOINTS (login, register, logout, me)
	// ============================================================

	private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String body = JsonUtil.readRequestBody(request);
		String email = JsonUtil.getStringValue(body, "email");
		String password = JsonUtil.getStringValue(body, "password");
		if (email == null || password == null) { JsonUtil.sendError(response, 400, "Email et mot de passe requis"); return; }

		User user = userDao.authenticate(email, password);
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
		if (userDao.findByEmail(email) != null) { JsonUtil.sendError(response, 409, "Cet email est deja utilise"); return; }

		User user = new User();
		user.setEmail(email); user.setPassword(password);
		user.setFirstName(firstName); user.setLastName(lastName);
		user.setPhone(phone); user.setCity("Fes");
		int userId = userDao.create(user);
		if (userId == -1) { JsonUtil.sendError(response, 500, "Erreur creation"); return; }

		Subscription sub = new Subscription(); sub.setUserId(userId); sub.setPlan("FREE");
		subscriptionDao.create(sub); user.setId(userId);

		HttpSession session = request.getSession();
		session.setAttribute("userId", userId); session.setAttribute("user", user); session.setAttribute("role", "CLIENT");

		String json = "{\"success\":true,\"user\":{"
				+ "\"id\":" + userId + ",\"email\":\"" + JsonUtil.escapeJson(email) + "\""
				+ ",\"firstName\":\"" + JsonUtil.escapeJson(firstName) + "\""
				+ ",\"lastName\":\"" + JsonUtil.escapeJson(lastName) + "\""
				+ ",\"subscriptionType\":\"FREE\",\"questionnaireCompleted\":false"
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
		User user = userDao.findById(userId);
		if (user == null) { JsonUtil.sendError(response, 404, "Utilisateur non trouve"); return; }
		Subscription sub = subscriptionDao.findByUserId(userId);

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
		User u = userDao.findById((int) s.getAttribute("userId"));
		if (u == null || !"ADMIN".equals(u.getRole())) { JsonUtil.sendError(res, 403, "Acces refuse"); return false; }
		return true;
	}

	private void handleAdminStats(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!checkAdmin(request, response)) return;
		int totalUsers = userDao.countAll();
		int totalClients = userDao.countByRole("CLIENT");
		int totalPrestataires = userDao.countByRole("PRESTATAIRE");
		int totalVendors = vendorDao.findAll().size();

		// Count devis and rdv
		int totalDevis = 0; int totalRdv = 0;
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			ResultSet rs1 = conn.prepareStatement("SELECT COUNT(*) FROM demandes_devis").executeQuery();
			if (rs1.next()) totalDevis = rs1.getInt(1);
			ResultSet rs2 = conn.prepareStatement("SELECT COUNT(*) FROM rendez_vous").executeQuery();
			if (rs2.next()) totalRdv = rs2.getInt(1);
		} catch (SQLException e) { System.out.println("## Stats error: " + e.getMessage()); }
		finally { DatabaseConnection.closeConnection(conn); }

		String json = "{\"totalUsers\":" + totalUsers + ",\"totalClients\":" + totalClients
				+ ",\"totalPrestataires\":" + totalPrestataires + ",\"totalVendors\":" + totalVendors
				+ ",\"totalDevis\":" + totalDevis + ",\"totalRdv\":" + totalRdv + "}";
		JsonUtil.sendJson(response, json);
	}

	private void handleAdminUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!checkAdmin(request, response)) return;
		List<User> users = userDao.findAll();
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
		List<Vendor> vendors = vendorDao.findAll();
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

		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = "INSERT INTO demandes_devis (client_id, vendor_id, budget_min, budget_max, message, date_mariage, nb_invites) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, clientId);
			ps.setInt(2, Integer.parseInt(vendorIdStr));
			ps.setDouble(3, budgetMinStr != null ? Double.parseDouble(budgetMinStr) : 0);
			ps.setDouble(4, budgetMaxStr != null ? Double.parseDouble(budgetMaxStr) : 0);
			ps.setString(5, message != null ? message : "");
			ps.setString(6, dateMariage != null ? dateMariage : "");
			ps.setInt(7, nbInvitesStr != null ? Integer.parseInt(nbInvitesStr) : 0);
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			int id = keys.next() ? keys.getInt(1) : 0;
			JsonUtil.sendJson(response, "{\"success\":true,\"id\":" + id + "}");
		} catch (Exception e) {
			System.out.println("## Erreur devis create: " + e.getMessage());
			JsonUtil.sendError(response, 500, "Erreur creation devis");
		} finally { DatabaseConnection.closeConnection(conn); }
	}

	private void handleDevisList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User currentUser = userDao.findById(userId);
		if (currentUser == null) { JsonUtil.sendError(response, 404, "User not found"); return; }

		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = "SELECT d.*, u.first_name AS client_fn, u.last_name AS client_ln, u.email AS client_email, u.phone AS client_phone, "
					+ "v.name AS vendor_name, vc.name_fr AS vendor_cat "
					+ "FROM demandes_devis d "
					+ "JOIN users u ON d.client_id = u.id "
					+ "JOIN vendors v ON d.vendor_id = v.id "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id ";

			String role = currentUser.getRole();
			if ("CLIENT".equals(role)) {
				sql += "WHERE d.client_id = ? ";
			} else if ("PRESTATAIRE".equals(role) && currentUser.getVendorId() > 0) {
				sql += "WHERE d.vendor_id = ? ";
			}
			// ADMIN sees all
			sql += "ORDER BY d.created_at DESC";

			PreparedStatement ps = conn.prepareStatement(sql);
			if ("CLIENT".equals(role)) {
				ps.setInt(1, userId);
			} else if ("PRESTATAIRE".equals(role) && currentUser.getVendorId() > 0) {
				ps.setInt(1, currentUser.getVendorId());
			}

			ResultSet rs = ps.executeQuery();
			StringBuilder json = new StringBuilder("[");
			boolean first = true;
			while (rs.next()) {
				if (!first) json.append(",");
				first = false;
				json.append("{\"id\":").append(rs.getInt("id"))
					.append(",\"clientId\":").append(rs.getInt("client_id"))
					.append(",\"vendorId\":").append(rs.getInt("vendor_id"))
					.append(",\"clientName\":\"").append(JsonUtil.escapeJson(rs.getString("client_fn") + " " + rs.getString("client_ln"))).append("\"")
					.append(",\"clientEmail\":\"").append(JsonUtil.escapeJson(rs.getString("client_email") != null ? rs.getString("client_email") : "")).append("\"")
					.append(",\"clientPhone\":\"").append(JsonUtil.escapeJson(rs.getString("client_phone") != null ? rs.getString("client_phone") : "")).append("\"")
					.append(",\"vendorName\":\"").append(JsonUtil.escapeJson(rs.getString("vendor_name"))).append("\"")
					.append(",\"vendorCat\":\"").append(JsonUtil.escapeJson(rs.getString("vendor_cat"))).append("\"")
					.append(",\"budgetMin\":").append(rs.getDouble("budget_min"))
					.append(",\"budgetMax\":").append(rs.getDouble("budget_max"))
					.append(",\"message\":\"").append(JsonUtil.escapeJson(rs.getString("message") != null ? rs.getString("message") : "")).append("\"")
					.append(",\"dateMariage\":\"").append(JsonUtil.escapeJson(rs.getString("date_mariage") != null ? rs.getString("date_mariage") : "")).append("\"")
					.append(",\"nbInvites\":").append(rs.getInt("nb_invites"))
					.append(",\"statut\":\"").append(rs.getString("statut")).append("\"")
					.append(",\"reponse\":\"").append(JsonUtil.escapeJson(rs.getString("reponse_prestataire") != null ? rs.getString("reponse_prestataire") : "")).append("\"")
					.append(",\"createdAt\":\"").append(rs.getString("created_at")).append("\"")
					.append("}");
			}
			json.append("]");
			JsonUtil.sendJson(response, json.toString());
		} catch (SQLException e) {
			System.out.println("## Erreur devis list: " + e.getMessage());
			JsonUtil.sendJson(response, "[]");
		} finally { DatabaseConnection.closeConnection(conn); }
	}

	private void handleDevisUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }

		String body = JsonUtil.readRequestBody(request);
		String idStr = JsonUtil.getStringValue(body, "id");
		String statut = JsonUtil.getStringValue(body, "statut");
		String reponse = JsonUtil.getStringValue(body, "reponse");
		if (idStr == null || statut == null) { JsonUtil.sendError(response, 400, "id et statut requis"); return; }

		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = "UPDATE demandes_devis SET statut = ?, reponse_prestataire = ? WHERE id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, statut);
			ps.setString(2, reponse != null ? reponse : "");
			ps.setInt(3, Integer.parseInt(idStr));
			ps.executeUpdate();
			JsonUtil.sendJson(response, "{\"success\":true}");
		} catch (Exception e) {
			System.out.println("## Erreur devis update: " + e.getMessage());
			JsonUtil.sendError(response, 500, "Erreur mise a jour");
		} finally { DatabaseConnection.closeConnection(conn); }
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

		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = "INSERT INTO rendez_vous (client_id, vendor_id, date_rdv, heure_rdv, lieu, note) VALUES (?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, clientId);
			ps.setInt(2, Integer.parseInt(vendorIdStr));
			ps.setString(3, dateRdv);
			ps.setString(4, heureRdv != null ? heureRdv : "");
			ps.setString(5, lieu != null ? lieu : "A definir");
			ps.setString(6, note != null ? note : "");
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			int id = keys.next() ? keys.getInt(1) : 0;
			JsonUtil.sendJson(response, "{\"success\":true,\"id\":" + id + "}");
		} catch (Exception e) {
			System.out.println("## Erreur rdv create: " + e.getMessage());
			JsonUtil.sendError(response, 500, "Erreur creation rdv");
		} finally { DatabaseConnection.closeConnection(conn); }
	}

	private void handleRdvList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User currentUser = userDao.findById(userId);
		if (currentUser == null) { JsonUtil.sendError(response, 404, "User not found"); return; }

		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			String sql = "SELECT r.*, u.first_name AS client_fn, u.last_name AS client_ln, u.phone AS client_phone, "
					+ "v.name AS vendor_name FROM rendez_vous r "
					+ "JOIN users u ON r.client_id = u.id "
					+ "JOIN vendors v ON r.vendor_id = v.id ";

			String role = currentUser.getRole();
			if ("CLIENT".equals(role)) { sql += "WHERE r.client_id = ? "; }
			else if ("PRESTATAIRE".equals(role) && currentUser.getVendorId() > 0) { sql += "WHERE r.vendor_id = ? "; }
			sql += "ORDER BY r.date_rdv ASC";

			PreparedStatement ps = conn.prepareStatement(sql);
			if ("CLIENT".equals(role)) { ps.setInt(1, userId); }
			else if ("PRESTATAIRE".equals(role) && currentUser.getVendorId() > 0) { ps.setInt(1, currentUser.getVendorId()); }

			ResultSet rs = ps.executeQuery();
			StringBuilder json = new StringBuilder("[");
			boolean first = true;
			while (rs.next()) {
				if (!first) json.append(",");
				first = false;
				json.append("{\"id\":").append(rs.getInt("id"))
					.append(",\"clientId\":").append(rs.getInt("client_id"))
					.append(",\"vendorId\":").append(rs.getInt("vendor_id"))
					.append(",\"clientName\":\"").append(JsonUtil.escapeJson(rs.getString("client_fn") + " " + rs.getString("client_ln"))).append("\"")
					.append(",\"clientPhone\":\"").append(JsonUtil.escapeJson(rs.getString("client_phone") != null ? rs.getString("client_phone") : "")).append("\"")
					.append(",\"vendorName\":\"").append(JsonUtil.escapeJson(rs.getString("vendor_name"))).append("\"")
					.append(",\"dateRdv\":\"").append(rs.getString("date_rdv")).append("\"")
					.append(",\"heureRdv\":\"").append(JsonUtil.escapeJson(rs.getString("heure_rdv") != null ? rs.getString("heure_rdv") : "")).append("\"")
					.append(",\"lieu\":\"").append(JsonUtil.escapeJson(rs.getString("lieu") != null ? rs.getString("lieu") : "")).append("\"")
					.append(",\"note\":\"").append(JsonUtil.escapeJson(rs.getString("note") != null ? rs.getString("note") : "")).append("\"")
					.append(",\"statut\":\"").append(rs.getString("statut")).append("\"")
					.append(",\"createdAt\":\"").append(rs.getString("created_at")).append("\"")
					.append("}");
			}
			json.append("]");
			JsonUtil.sendJson(response, json.toString());
		} catch (SQLException e) {
			System.out.println("## Erreur rdv list: " + e.getMessage());
			JsonUtil.sendJson(response, "[]");
		} finally { DatabaseConnection.closeConnection(conn); }
	}

	private void handleRdvUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }

		String body = JsonUtil.readRequestBody(request);
		String idStr = JsonUtil.getStringValue(body, "id");
		String statut = JsonUtil.getStringValue(body, "statut");
		if (idStr == null || statut == null) { JsonUtil.sendError(response, 400, "id et statut requis"); return; }

		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement ps = conn.prepareStatement("UPDATE rendez_vous SET statut = ? WHERE id = ?");
			ps.setString(1, statut);
			ps.setInt(2, Integer.parseInt(idStr));
			ps.executeUpdate();
			JsonUtil.sendJson(response, "{\"success\":true}");
		} catch (Exception e) {
			System.out.println("## Erreur rdv update: " + e.getMessage());
			JsonUtil.sendError(response, 500, "Erreur mise a jour");
		} finally { DatabaseConnection.closeConnection(conn); }
	}

	// ============================================================
	// VENDOR PORTAL ENDPOINT
	// ============================================================

	private void handleVendorDashboard(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) { JsonUtil.sendError(response, 401, "Non authentifie"); return; }
		int userId = (int) session.getAttribute("userId");
		User user = userDao.findById(userId);
		if (user == null || !"PRESTATAIRE".equals(user.getRole())) { JsonUtil.sendError(response, 403, "Acces refuse"); return; }

		int vendorId = user.getVendorId();
		if (vendorId <= 0) { JsonUtil.sendJson(response, "{\"vendor\":null}"); return; }
		Vendor vendor = vendorDao.findById(vendorId);
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
}
