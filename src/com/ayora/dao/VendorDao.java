package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ayora.model.Vendor;
import com.ayora.model.VendorCategory;
import com.ayora.util.Database;

/** DAO de l'entite Vendor. */
public class VendorDao implements IDao {

	private static final String SELECT_VENDOR_BASE =
		"SELECT v.*, vc.name_fr AS category_name FROM vendors v "
		+ "JOIN vendor_categories vc ON v.category_id = vc.id ";

	private final Database db;

	public VendorDao(Database db) {
		this.db = db;
	}

	public Vendor findById(int id) {
		return db.queryOne(SELECT_VENDOR_BASE + "WHERE v.id = ?", this::mapVendor, id);
	}

	public int create(Vendor v) {
		return db.insertReturningKey(
			"INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, "
			+ "phone, email, instagram, address, tags, is_active) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
			v.getCategoryId(), v.getName(), v.getCity(), v.getDescription(),
			v.getPrixMin(), v.getPrixMax(), v.getGamme(), v.getPhone(),
			v.getEmail(), v.getInstagram(), v.getAddress(), v.getTags(), v.isActive());
	}

	public List<Vendor> findAll() {
		return db.queryList(
			SELECT_VENDOR_BASE + "WHERE v.is_active = TRUE ORDER BY v.name",
			this::mapVendor);
	}

	public List<Vendor> findByCategory(int categoryId) {
		return db.queryList(
			SELECT_VENDOR_BASE + "WHERE v.category_id = ? AND v.is_active = TRUE ORDER BY v.name",
			this::mapVendor, categoryId);
	}

	public List<Vendor> findByGamme(String gamme) {
		return db.queryList(
			SELECT_VENDOR_BASE + "WHERE v.gamme = ? AND v.is_active = TRUE ORDER BY v.name",
			this::mapVendor, gamme);
	}

	public List<Vendor> search(String keyword) {
		String pattern = "%" + keyword + "%";
		return db.queryList(
			SELECT_VENDOR_BASE
			+ "WHERE v.is_active = TRUE AND (v.name LIKE ? OR v.tags LIKE ? OR v.description LIKE ?) "
			+ "ORDER BY v.name",
			this::mapVendor, pattern, pattern, pattern);
	}

	public List<VendorCategory> findAllCategories() {
		return db.queryList(
			"SELECT * FROM vendor_categories "
			+ "WHERE name_fr NOT LIKE '[Obsolete]%' AND name_fr NOT LIKE '[Supprime]%' "
			+ "ORDER BY name_fr",
			rs -> {
				VendorCategory cat = new VendorCategory();
				cat.setId(rs.getInt("id"));
				cat.setName(rs.getString("name"));
				cat.setNameFr(rs.getString("name_fr"));
				cat.setDescription(rs.getString("description"));
				cat.setIcon(rs.getString("icon"));
				return cat;
			});
	}

	public List<Vendor> searchAll(String keyword, Integer categoryId, String city, String gamme,
			Boolean active, int offset, int limit) {
		StringBuilder sql = new StringBuilder(SELECT_VENDOR_BASE + "WHERE 1=1");
		List<Object> params = new ArrayList<Object>();
		appendVendorFilters(sql, params, keyword, categoryId, city, gamme, active);
		sql.append(" ORDER BY v.name LIMIT ? OFFSET ?");
		params.add(limit); params.add(offset);
		return db.queryList(sql.toString(), this::mapVendor, params.toArray());
	}

	public int countSearch(String keyword, Integer categoryId, String city, String gamme, Boolean active) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM vendors v WHERE 1=1");
		List<Object> params = new ArrayList<Object>();
		appendVendorFilters(sql, params, keyword, categoryId, city, gamme, active);
		return db.queryInt(sql.toString(), params.toArray());
	}

	private void appendVendorFilters(StringBuilder sql, List<Object> params, String keyword,
			Integer categoryId, String city, String gamme, Boolean active) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (LOWER(v.name) LIKE ? OR LOWER(v.city) LIKE ? OR LOWER(v.tags) LIKE ?)");
			String k = "%" + keyword.toLowerCase().trim() + "%";
			params.add(k); params.add(k); params.add(k);
		}
		if (categoryId != null && categoryId > 0) { sql.append(" AND v.category_id = ?"); params.add(categoryId); }
		if (city != null && !city.isEmpty())       { sql.append(" AND v.city = ?"); params.add(city); }
		if (gamme != null && !gamme.isEmpty())     { sql.append(" AND v.gamme = ?"); params.add(gamme); }
		if (active != null)                        { sql.append(" AND v.is_active = ?"); params.add(active); }
	}

	public int countAll() {
		return db.queryInt("SELECT COUNT(*) FROM vendors");
	}

	public int countActive(boolean active) {
		return db.queryInt("SELECT COUNT(*) FROM vendors WHERE is_active = ?", active);
	}

	public int countIncomplete() {
		return db.queryInt(
			"SELECT COUNT(*) FROM vendors WHERE description IS NULL OR description='' "
			+ "OR phone IS NULL OR phone='' OR instagram IS NULL OR instagram='' "
			+ "OR tags IS NULL OR tags=''");
	}

	public boolean update(int id, String name, String city, String description, Double prixMin, Double prixMax,
			String gamme, String phone, String email, String instagram, String address, String tags) {
		// COALESCE pour ne pas ecraser avec null.
		Connection c = null;
		try {
			c = db.getConnection();
			PreparedStatement ps = c.prepareStatement(
				"UPDATE vendors SET name=COALESCE(?, name), city=COALESCE(?, city), description=COALESCE(?, description), "
				+ "prix_min=COALESCE(?, prix_min), prix_max=COALESCE(?, prix_max), gamme=COALESCE(?, gamme), "
				+ "phone=COALESCE(?, phone), email=COALESCE(?, email), instagram=COALESCE(?, instagram), "
				+ "address=COALESCE(?, address), tags=COALESCE(?, tags) WHERE id=?");
			ps.setString(1, name); ps.setString(2, city); ps.setString(3, description);
			if (prixMin != null) ps.setDouble(4, prixMin); else ps.setNull(4, java.sql.Types.DECIMAL);
			if (prixMax != null) ps.setDouble(5, prixMax); else ps.setNull(5, java.sql.Types.DECIMAL);
			ps.setString(6, gamme); ps.setString(7, phone); ps.setString(8, email);
			ps.setString(9, instagram); ps.setString(10, address); ps.setString(11, tags);
			ps.setInt(12, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## update vendor : " + e.getMessage());
			return false;
		} finally {
			db.close(c);
		}
	}

	public boolean updateActive(int id, boolean active) {
		return db.executeUpdate("UPDATE vendors SET is_active=? WHERE id=?", active, id) > 0;
	}

	public boolean delete(int id) {
		// Transaction : suppression cascade manuelle des dependances.
		Connection c = null;
		try {
			c = db.getConnection();
			c.setAutoCommit(false);
			deleteRows(c, "DELETE FROM user_picks WHERE vendor_id=?", id);
			deleteRows(c, "DELETE FROM recommendations WHERE vendor_id=?", id);
			deleteRows(c, "DELETE FROM demandes_devis WHERE vendor_id=?", id);
			deleteRows(c, "DELETE FROM rendez_vous WHERE vendor_id=?", id);
			PreparedStatement ps = c.prepareStatement("DELETE FROM vendors WHERE id=?");
			ps.setInt(1, id);
			int n = ps.executeUpdate();
			c.commit();
			return n > 0;
		} catch (SQLException e) {
			System.out.println("## delete vendor : " + e.getMessage());
			try { if (c != null) c.rollback(); } catch (SQLException ignore) {}
			return false;
		} finally {
			try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignore) {}
			db.close(c);
		}
	}

	public Map<String, Integer> countByCategory() {
		Map<String, Integer> out = new LinkedHashMap<String, Integer>();
		List<Object[]> rows = db.queryList(
			"SELECT vc.name_fr, COUNT(v.id) FROM vendors v "
			+ "JOIN vendor_categories vc ON v.category_id=vc.id "
			+ "WHERE v.is_active=1 GROUP BY v.category_id ORDER BY vc.name_fr",
			rs -> new Object[] { rs.getString(1), rs.getInt(2) });
		for (Object[] row : rows) {
			out.put((String) row[0], (Integer) row[1]);
		}
		return out;
	}

	private void deleteRows(Connection c, String sql, int id) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setInt(1, id);
		ps.executeUpdate();
	}

	private Vendor mapVendor(ResultSet rs) throws SQLException {
		Vendor v = new Vendor();
		v.setId(rs.getInt("id"));
		v.setCategoryId(rs.getInt("category_id"));
		v.setCategoryName(safeGetString(rs, "category_name"));
		v.setName(rs.getString("name"));
		v.setCity(rs.getString("city"));
		v.setDescription(rs.getString("description"));
		v.setPrixMin(rs.getDouble("prix_min"));
		v.setPrixMax(rs.getDouble("prix_max"));
		v.setGamme(rs.getString("gamme"));
		v.setPhone(rs.getString("phone"));
		v.setEmail(rs.getString("email"));
		v.setInstagram(rs.getString("instagram"));
		v.setAddress(rs.getString("address"));
		v.setTags(rs.getString("tags"));
		v.setRating(rs.getDouble("rating"));
		v.setNbAvis(rs.getInt("nb_avis"));
		v.setActive(rs.getBoolean("is_active"));
		v.setPhotoUrl(safeGetString(rs, "photo_url"));
		v.setGalleryUrls(safeGetString(rs, "gallery_urls"));
		v.setReelUrl(safeGetString(rs, "reel_url"));
		v.setVenueType(safeGetString(rs, "venue_type"));
		return v;
	}

	private static String safeGetString(ResultSet rs, String column) {
		try { return rs.getString(column); }
		catch (SQLException e) { return null; }
	}

	/**
	 * Score de completude (0-100) d'un vendor.
	 * Static utility utilisee par l'admin pour le graphique de completude.
	 */
	public static int computeCompleteness(Vendor v) {
		if (v == null) return 0;
		int total = 10, score = 0;
		if (v.getName() != null && !v.getName().isEmpty()) score++;
		if (v.getCity() != null && !v.getCity().isEmpty()) score++;
		if (v.getDescription() != null && v.getDescription().length() > 30) score++;
		if (v.getPrixMin() > 0) score++;
		if (v.getPrixMax() > 0) score++;
		if (v.getGamme() != null && !v.getGamme().isEmpty()) score++;
		if (v.getPhone() != null && !v.getPhone().isEmpty()) score++;
		if (v.getInstagram() != null && !v.getInstagram().isEmpty()) score++;
		if (v.getTags() != null && v.getTags().length() > 5) score++;
		if (v.getRating() > 0) score++;
		return (int) Math.round(score * 100.0 / total);
	}
}
