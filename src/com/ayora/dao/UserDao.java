package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import com.ayora.model.User;
import com.ayora.util.DatabaseConnection;

public class UserDao {

	public UserDao() {
	}

	public User findByEmail(String email) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM users WHERE email = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapUser(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByEmail : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public User findById(int id) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM users WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapUser(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findById : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public User authenticate(String email, String password) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, email);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapUser(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur authenticate : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public int create(User user) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "INSERT INTO users (email, password, first_name, last_name, phone, city, subscription_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, user.getEmail());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getFirstName());
			ps.setString(4, user.getLastName());
			ps.setString(5, user.getPhone());
			ps.setString(6, user.getCity() != null ? user.getCity() : "Fes");
			ps.setString(7, "FREE");
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				return keys.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur create user : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return -1;
	}

	public boolean updateQuestionnaireStatus(int userId, boolean completed) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE users SET questionnaire_completed = ? WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setBoolean(1, completed);
			ps.setInt(2, userId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur updateQuestionnaireStatus : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public boolean updateSubscription(int userId, String type) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE users SET subscription_type = ? WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, type);
			ps.setInt(2, userId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur updateSubscription : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public List<User> findAll() {
		List<User> list = new Vector<User>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM users ORDER BY id DESC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapUser(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findAll users : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public int countAll() {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT COUNT(*) FROM users";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur countAll users : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return 0;
	}

	public int countByRole(String role) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, role);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur countByRole : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return 0;
	}

	private User mapUser(ResultSet rs) throws SQLException {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setEmail(rs.getString("email"));
		user.setPassword(rs.getString("password"));
		user.setFirstName(rs.getString("first_name"));
		user.setLastName(rs.getString("last_name"));
		user.setPhone(rs.getString("phone"));
		user.setCity(rs.getString("city"));
		user.setSubscriptionType(rs.getString("subscription_type"));
		user.setQuestionnaireCompleted(rs.getBoolean("questionnaire_completed"));
		try {
			user.setRole(rs.getString("role"));
		} catch (SQLException e) {
			user.setRole("CLIENT");
		}
		try {
			user.setVendorId(rs.getInt("vendor_id"));
		} catch (SQLException e) {
			user.setVendorId(0);
		}
		return user;
	}
}
