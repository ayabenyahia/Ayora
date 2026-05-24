package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ayora.model.UserPick;
import com.ayora.util.Database;

/**
 * DAO de l'entite UserPick.
 *
 * pick() utilise une transaction (delete+insert atomique) ; elle obtient
 * sa Connection via Database (centralise) plutot que par les methodes
 * template, pour pouvoir commit/rollback.
 */
public class UserPickDao implements IDao {

	private static final String SELECT_BASE =
		"SELECT p.id, p.user_id, p.vendor_id, p.category_id, p.picked_at, "
		+ "v.name AS vendor_name, vc.name_fr AS vendor_category, "
		+ "v.gamme AS vendor_gamme, v.prix_min AS vendor_prix_min, "
		+ "v.city AS vendor_city, v.phone AS vendor_phone, "
		+ "v.instagram AS vendor_instagram, v.description AS vendor_description, "
		+ "v.rating AS vendor_rating, v.nb_avis AS vendor_nb_avis "
		+ "FROM user_picks p "
		+ "JOIN vendors v ON p.vendor_id = v.id "
		+ "JOIN vendor_categories vc ON p.category_id = vc.id ";

	private final Database db;

	public UserPickDao(Database db) {
		this.db = db;
	}

	public List<UserPick> findByUserId(int userId) {
		return db.queryList(
			SELECT_BASE + "WHERE p.user_id = ? ORDER BY vc.id, p.picked_at DESC",
			this::map, userId);
	}

	public Set<Integer> findPickedVendorIds(int userId) {
		List<Integer> ids = db.queryList(
			"SELECT vendor_id FROM user_picks WHERE user_id = ?",
			rs -> rs.getInt(1), userId);
		return new HashSet<Integer>(ids);
	}

	public boolean pick(int userId, int vendorId, int categoryId) {
		// Transaction : delete + insert dans la meme connection
		Connection c = null;
		try {
			c = db.getConnection();
			c.setAutoCommit(false);
			PreparedStatement del = c.prepareStatement(
				"DELETE FROM user_picks WHERE user_id = ? AND category_id = ?");
			del.setInt(1, userId);
			del.setInt(2, categoryId);
			del.executeUpdate();
			PreparedStatement ins = c.prepareStatement(
				"INSERT INTO user_picks (user_id, vendor_id, category_id) VALUES (?, ?, ?)");
			ins.setInt(1, userId);
			ins.setInt(2, vendorId);
			ins.setInt(3, categoryId);
			boolean ok = ins.executeUpdate() > 0;
			c.commit();
			return ok;
		} catch (SQLException e) {
			System.out.println("## Erreur pick : " + e.getMessage());
			try { if (c != null) c.rollback(); } catch (SQLException ignore) {}
			return false;
		} finally {
			try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignore) {}
			db.close(c);
		}
	}

	public boolean unpick(int userId, int vendorId) {
		return db.executeUpdate(
			"DELETE FROM user_picks WHERE user_id = ? AND vendor_id = ?",
			userId, vendorId) > 0;
	}

	private UserPick map(ResultSet rs) throws SQLException {
		UserPick p = new UserPick();
		p.setId(rs.getInt("id"));
		p.setUserId(rs.getInt("user_id"));
		p.setVendorId(rs.getInt("vendor_id"));
		p.setCategoryId(rs.getInt("category_id"));
		p.setPickedAt(rs.getString("picked_at"));
		p.setVendorName(rs.getString("vendor_name"));
		p.setVendorCategory(rs.getString("vendor_category"));
		p.setVendorGamme(rs.getString("vendor_gamme"));
		p.setVendorPrixMin(rs.getDouble("vendor_prix_min"));
		p.setVendorCity(rs.getString("vendor_city"));
		p.setVendorPhone(rs.getString("vendor_phone"));
		p.setVendorInstagram(rs.getString("vendor_instagram"));
		p.setVendorDescription(rs.getString("vendor_description"));
		p.setVendorRating(rs.getDouble("vendor_rating"));
		p.setVendorNbAvis(rs.getInt("vendor_nb_avis"));
		return p;
	}
}
