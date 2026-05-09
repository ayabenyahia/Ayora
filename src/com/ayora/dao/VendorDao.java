package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import com.ayora.model.Vendor;
import com.ayora.model.VendorCategory;
import com.ayora.util.DatabaseConnection;

public class VendorDao {

	public VendorDao() {
	}

	public List<Vendor> findAll() {
		List<Vendor> list = new Vector<Vendor>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT v.*, vc.name_fr AS category_name FROM vendors v "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE v.is_active = TRUE ORDER BY v.name";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapVendor(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findAll vendors : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public List<Vendor> findByCategory(int categoryId) {
		List<Vendor> list = new Vector<Vendor>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT v.*, vc.name_fr AS category_name FROM vendors v "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE v.category_id = ? AND v.is_active = TRUE ORDER BY v.name";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, categoryId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapVendor(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByCategory : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public List<Vendor> findByGamme(String gamme) {
		List<Vendor> list = new Vector<Vendor>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT v.*, vc.name_fr AS category_name FROM vendors v "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE v.gamme = ? AND v.is_active = TRUE ORDER BY v.name";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, gamme);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapVendor(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByGamme : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public List<Vendor> findByBudgetRange(double minBudget, double maxBudget) {
		List<Vendor> list = new Vector<Vendor>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT v.*, vc.name_fr AS category_name FROM vendors v "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE v.prix_min <= ? AND v.is_active = TRUE ORDER BY v.prix_min";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setDouble(1, maxBudget);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapVendor(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByBudgetRange : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public Vendor findById(int id) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT v.*, vc.name_fr AS category_name FROM vendors v "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE v.id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapVendor(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findById vendor : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public List<Vendor> search(String keyword) {
		List<Vendor> list = new Vector<Vendor>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT v.*, vc.name_fr AS category_name FROM vendors v "
					+ "JOIN vendor_categories vc ON v.category_id = vc.id "
					+ "WHERE v.is_active = TRUE AND (v.name LIKE ? OR v.tags LIKE ? OR v.description LIKE ?) "
					+ "ORDER BY v.name";
			PreparedStatement ps = connection.prepareStatement(sql);
			String pattern = "%" + keyword + "%";
			ps.setString(1, pattern);
			ps.setString(2, pattern);
			ps.setString(3, pattern);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapVendor(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur search vendors : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public List<VendorCategory> findAllCategories() {
		List<VendorCategory> list = new Vector<VendorCategory>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			// On masque les categories marquees [Obsolete] (fusionnees) et [Supprime]
			// (Transport / Wedding Planner) - la migration v4 les a desactivees mais
			// on garde les lignes pour ne pas casser les FK historiques.
			String sql = "SELECT * FROM vendor_categories "
					+ "WHERE name_fr NOT LIKE '[Obsolete]%' AND name_fr NOT LIKE '[Supprime]%' "
					+ "ORDER BY name_fr";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				VendorCategory cat = new VendorCategory();
				cat.setId(rs.getInt("id"));
				cat.setName(rs.getString("name"));
				cat.setNameFr(rs.getString("name_fr"));
				cat.setDescription(rs.getString("description"));
				cat.setIcon(rs.getString("icon"));
				list.add(cat);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findAllCategories : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	private Vendor mapVendor(ResultSet rs) throws SQLException {
		Vendor v = new Vendor();
		v.setId(rs.getInt("id"));
		v.setCategoryId(rs.getInt("category_id"));
		v.setCategoryName(rs.getString("category_name"));
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
		return v;
	}
}
