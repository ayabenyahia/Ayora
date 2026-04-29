package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.UserDao;
import com.ayora.dao.VendorDao;
import com.ayora.model.User;
import com.ayora.model.Vendor;
import com.ayora.util.JsonUtil;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

	private UserDao userDao;
	private VendorDao vendorDao;

	@Override
	public void init() throws ServletException {
		userDao = new UserDao();
		vendorDao = new VendorDao();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		String role = (String) session.getAttribute("role");
		if (!"ADMIN".equals(role)) {
			JsonUtil.sendError(response, 403, "Acces refuse - Admin requis");
			return;
		}

		String path = request.getPathInfo();
		if (path == null) path = "/";

		if ("/stats".equals(path)) {
			handleStats(request, response);
		} else if ("/users".equals(path)) {
			handleUsers(request, response);
		} else if ("/vendors".equals(path)) {
			handleVendors(request, response);
		} else {
			JsonUtil.sendError(response, 404, "Route admin non trouvee");
		}
	}

	private void handleStats(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int totalUsers = userDao.countAll();
		int totalClients = userDao.countByRole("CLIENT");
		int totalPrestataires = userDao.countByRole("PRESTATAIRE");

		List<Vendor> vendors = vendorDao.findAll();
		int totalVendors = vendors.size();

		String json = "{\"totalUsers\":" + totalUsers
				+ ",\"totalClients\":" + totalClients
				+ ",\"totalPrestataires\":" + totalPrestataires
				+ ",\"totalVendors\":" + totalVendors
				+ "}";
		JsonUtil.sendJson(response, json);
	}

	private void handleUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<User> users = userDao.findAll();

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			if (i > 0) json.append(",");
			json.append("{\"id\":" + u.getId()
					+ ",\"email\":\"" + JsonUtil.escapeJson(u.getEmail()) + "\""
					+ ",\"firstName\":\"" + JsonUtil.escapeJson(u.getFirstName()) + "\""
					+ ",\"lastName\":\"" + JsonUtil.escapeJson(u.getLastName()) + "\""
					+ ",\"phone\":\"" + JsonUtil.escapeJson(u.getPhone() != null ? u.getPhone() : "") + "\""
					+ ",\"city\":\"" + JsonUtil.escapeJson(u.getCity() != null ? u.getCity() : "") + "\""
					+ ",\"subscriptionType\":\"" + u.getSubscriptionType() + "\""
					+ ",\"role\":\"" + u.getRole() + "\""
					+ ",\"questionnaireCompleted\":" + u.isQuestionnaireCompleted()
					+ "}");
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}

	private void handleVendors(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<Vendor> vendors = vendorDao.findAll();

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < vendors.size(); i++) {
			Vendor v = vendors.get(i);
			if (i > 0) json.append(",");
			json.append("{\"id\":" + v.getId()
					+ ",\"name\":\"" + JsonUtil.escapeJson(v.getName()) + "\""
					+ ",\"categoryName\":\"" + JsonUtil.escapeJson(v.getCategoryName()) + "\""
					+ ",\"city\":\"" + JsonUtil.escapeJson(v.getCity() != null ? v.getCity() : "") + "\""
					+ ",\"gamme\":\"" + v.getGamme() + "\""
					+ ",\"prixMin\":" + v.getPrixMin()
					+ ",\"prixMax\":" + v.getPrixMax()
					+ ",\"rating\":" + v.getRating()
					+ ",\"nbAvis\":" + v.getNbAvis()
					+ ",\"phone\":\"" + JsonUtil.escapeJson(v.getPhone() != null ? v.getPhone() : "") + "\""
					+ ",\"email\":\"" + JsonUtil.escapeJson(v.getEmail() != null ? v.getEmail() : "") + "\""
					+ ",\"active\":" + v.isActive()
					+ "}");
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}
}
