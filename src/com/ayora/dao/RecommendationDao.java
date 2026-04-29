package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import com.ayora.model.Recommendation;
import com.ayora.util.DatabaseConnection;

public class RecommendationDao {

	public RecommendationDao() {
	}

	public List<Recommendation> findByUserId(int userId) {
		List<Recommendation> list = new Vector<Recommendation>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT r.*, v.name AS vendor_name, vc.name_fr AS vendor_category, "
					+ "v.gamme AS vendor_gamme, v.prix_min AS vendor_prix_min "
					+ "FROM recommendations r "
					+ "JOIN vendors v ON r.vendor_id = v.id "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE r.user_id = ? ORDER BY r.score DESC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapRecommendation(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByUserId recommendations : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public List<Recommendation> findByVendorId(int vendorId) {
		List<Recommendation> list = new Vector<Recommendation>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT r.*, v.name AS vendor_name, vc.name_fr AS vendor_category, "
					+ "v.gamme AS vendor_gamme, v.prix_min AS vendor_prix_min "
					+ "FROM recommendations r "
					+ "JOIN vendors v ON r.vendor_id = v.id "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE r.vendor_id = ? ORDER BY r.score DESC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, vendorId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapRecommendation(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByVendorId recommendations : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public boolean create(Recommendation rec) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "INSERT INTO recommendations (user_id, vendor_id, score, raison) VALUES (?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, rec.getUserId());
			ps.setInt(2, rec.getVendorId());
			ps.setDouble(3, rec.getScore());
			ps.setString(4, rec.getRaison());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur create recommendation : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public boolean deleteByUserId(int userId) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "DELETE FROM recommendations WHERE user_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			return ps.executeUpdate() >= 0;
		} catch (SQLException e) {
			System.out.println("## Erreur deleteByUserId recommendations : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	private Recommendation mapRecommendation(ResultSet rs) throws SQLException {
		Recommendation r = new Recommendation();
		r.setId(rs.getInt("id"));
		r.setUserId(rs.getInt("user_id"));
		r.setVendorId(rs.getInt("vendor_id"));
		r.setScore(rs.getDouble("score"));
		r.setRaison(rs.getString("raison"));
		r.setViewed(rs.getBoolean("is_viewed"));
		r.setVendorName(rs.getString("vendor_name"));
		r.setVendorCategory(rs.getString("vendor_category"));
		r.setVendorGamme(rs.getString("vendor_gamme"));
		r.setVendorPrixMin(rs.getDouble("vendor_prix_min"));
		return r;
	}
}
