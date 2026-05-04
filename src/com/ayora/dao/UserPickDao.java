package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.Set;
import com.ayora.model.UserPick;
import com.ayora.util.DatabaseConnection;

/**
 * DAO de la table user_picks (choix retenus par la mariee).
 *
 * Pattern p02-jee : JDBC pur, PreparedStatement, mapping ResultSet -> POJO.
 * Une selection par categorie pour un user donne (UNIQUE KEY).
 */
public class UserPickDao {

	private static final String SELECT_BASE =
		"SELECT p.id, p.user_id, p.vendor_id, p.category_id, p.picked_at, "
		+ "v.name AS vendor_name, vc.name_fr AS vendor_category, "
		+ "v.gamme AS vendor_gamme, v.prix_min AS vendor_prix_min, "
		+ "v.city AS vendor_city, v.phone AS vendor_phone, "
		+ "v.instagram AS vendor_instagram, v.description AS vendor_description "
		+ "FROM user_picks p "
		+ "JOIN vendors v ON p.vendor_id = v.id "
		+ "JOIN vendor_categories vc ON p.category_id = vc.id ";

	public UserPickDao() {}

	public List<UserPick> findByUserId(int userId) {
		List<UserPick> list = new Vector<UserPick>();
		Connection c = null;
		try {
			c = DatabaseConnection.getConnection();
			String sql = SELECT_BASE + "WHERE p.user_id = ? ORDER BY vc.id, p.picked_at DESC";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) list.add(map(rs));
		} catch (SQLException e) {
			System.out.println("## Erreur findByUserId user_picks : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(c);
		}
		return list;
	}

	/** Retourne les vendor_id deja choisis par cet utilisateur. Sert a marquer
	 *  les cards comme "Choisi" cote frontend. */
	public Set<Integer> findPickedVendorIds(int userId) {
		Set<Integer> ids = new HashSet<Integer>();
		Connection c = null;
		try {
			c = DatabaseConnection.getConnection();
			PreparedStatement ps = c.prepareStatement(
				"SELECT vendor_id FROM user_picks WHERE user_id = ?");
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) ids.add(rs.getInt(1));
		} catch (SQLException e) {
			System.out.println("## Erreur findPickedVendorIds : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(c);
		}
		return ids;
	}

	/** Choisit (ou remplace) le prestataire pour une categorie. Comme la cle
	 *  unique est (user_id, category_id), on supprime la ligne existante puis
	 *  on insere la nouvelle, dans une transaction. */
	public boolean pick(int userId, int vendorId, int categoryId) {
		Connection c = null;
		try {
			c = DatabaseConnection.getConnection();
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
			DatabaseConnection.closeConnection(c);
		}
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
		return p;
	}
}
