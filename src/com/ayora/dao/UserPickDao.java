package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
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
