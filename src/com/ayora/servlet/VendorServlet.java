package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ayora.dao.VendorDao;
import com.ayora.model.Vendor;
import com.ayora.model.VendorCategory;
import com.ayora.util.JsonUtil;

@WebServlet("/api/vendors/*")
public class VendorServlet extends HttpServlet {

	private VendorDao vendorDao;

	@Override
	public void init() throws ServletException {
		vendorDao = new VendorDao();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();

		if (path == null || "/".equals(path)) {
			handleList(request, response);
		} else if ("/categories".equals(path)) {
			handleCategories(response);
		} else if ("/search".equals(path)) {
			handleSearch(request, response);
		} else {
			// Essayer de parser l'id
			try {
				int id = Integer.parseInt(path.substring(1));
				handleGetById(response, id);
			} catch (NumberFormatException e) {
				JsonUtil.sendError(response, 404, "Route non trouvee");
			}
		}
	}

	private void handleList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String categoryParam = request.getParameter("category");
		String gammeParam = request.getParameter("gamme");

		List<Vendor> vendors;
		if (categoryParam != null && !categoryParam.isEmpty()) {
			try {
				int categoryId = Integer.parseInt(categoryParam);
				vendors = vendorDao.findByCategory(categoryId);
			} catch (NumberFormatException e) {
				vendors = vendorDao.findAll();
			}
		} else if (gammeParam != null && !gammeParam.isEmpty()) {
			vendors = vendorDao.findByGamme(gammeParam);
		} else {
			vendors = vendorDao.findAll();
		}

		String json = buildVendorListJson(vendors);
		JsonUtil.sendJson(response, json);
	}

	private void handleCategories(HttpServletResponse response) throws IOException {
		List<VendorCategory> categories = vendorDao.findAllCategories();
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < categories.size(); i++) {
			VendorCategory cat = categories.get(i);
			if (i > 0) json.append(",");
			json.append("{\"id\":" + cat.getId());
			json.append(",\"name\":\"" + JsonUtil.escapeJson(cat.getName()) + "\"");
			json.append(",\"nameFr\":\"" + JsonUtil.escapeJson(cat.getNameFr()) + "\"");
			json.append(",\"description\":\"" + JsonUtil.escapeJson(cat.getDescription()) + "\"");
			json.append(",\"icon\":\"" + JsonUtil.escapeJson(cat.getIcon()) + "\"}");
		}
		json.append("]");
		JsonUtil.sendJson(response, json.toString());
	}

	private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String keyword = request.getParameter("q");
		if (keyword == null || keyword.trim().isEmpty()) {
			JsonUtil.sendError(response, 400, "Mot-cle requis");
			return;
		}
		List<Vendor> vendors = vendorDao.search(keyword.trim());
		String json = buildVendorListJson(vendors);
		JsonUtil.sendJson(response, json);
	}

	private void handleGetById(HttpServletResponse response, int id) throws IOException {
		Vendor vendor = vendorDao.findById(id);
		if (vendor == null) {
			JsonUtil.sendError(response, 404, "Prestataire non trouve");
			return;
		}
		String json = buildVendorJson(vendor);
		JsonUtil.sendJson(response, json);
	}

	private String buildVendorListJson(List<Vendor> vendors) {
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < vendors.size(); i++) {
			if (i > 0) json.append(",");
			json.append(buildVendorJson(vendors.get(i)));
		}
		json.append("]");
		return json.toString();
	}

	private String buildVendorJson(Vendor v) {
		return "{\"id\":" + v.getId()
				+ ",\"categoryId\":" + v.getCategoryId()
				+ ",\"categoryName\":\"" + JsonUtil.escapeJson(v.getCategoryName()) + "\""
				+ ",\"name\":\"" + JsonUtil.escapeJson(v.getName()) + "\""
				+ ",\"city\":\"" + JsonUtil.escapeJson(v.getCity()) + "\""
				+ ",\"description\":\"" + JsonUtil.escapeJson(v.getDescription()) + "\""
				+ ",\"prixMin\":" + v.getPrixMin()
				+ ",\"prixMax\":" + v.getPrixMax()
				+ ",\"gamme\":\"" + JsonUtil.escapeJson(v.getGamme()) + "\""
				+ ",\"phone\":\"" + JsonUtil.escapeJson(v.getPhone() != null ? v.getPhone() : "") + "\""
				+ ",\"instagram\":\"" + JsonUtil.escapeJson(v.getInstagram() != null ? v.getInstagram() : "") + "\""
				+ ",\"tags\":\"" + JsonUtil.escapeJson(v.getTags() != null ? v.getTags() : "") + "\""
				+ ",\"address\":\"" + JsonUtil.escapeJson(v.getAddress() != null ? v.getAddress() : "") + "\""
				+ ",\"rating\":" + v.getRating()
				+ ",\"nbAvis\":" + v.getNbAvis()
				+ "}";
	}
}
