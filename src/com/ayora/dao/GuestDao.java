package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import com.ayora.model.Guest;
import com.ayora.util.DatabaseConnection;

public class GuestDao {

	public GuestDao() {
	}

	public List<Guest> findByUserId(int userId) {
		List<Guest> list = new Vector<Guest>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM guests WHERE user_id = ? ORDER BY last_name, first_name";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapGuest(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByUserId guests : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public Guest findById(int id) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM guests WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapGuest(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findById guest : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public int create(Guest guest) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "INSERT INTO guests (user_id, first_name, last_name, phone, email, groupe, nb_personnes, note) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, guest.getUserId());
			ps.setString(2, guest.getFirstName());
			ps.setString(3, guest.getLastName());
			ps.setString(4, guest.getPhone());
			ps.setString(5, guest.getEmail());
			ps.setString(6, guest.getGroupe());
			ps.setInt(7, guest.getNbPersonnes());
			ps.setString(8, guest.getNote());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				return keys.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur create guest : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return -1;
	}

	public boolean update(Guest guest) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE guests SET first_name=?, last_name=?, phone=?, email=?, groupe=?, nb_personnes=?, note=? WHERE id=?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, guest.getFirstName());
			ps.setString(2, guest.getLastName());
			ps.setString(3, guest.getPhone());
			ps.setString(4, guest.getEmail());
			ps.setString(5, guest.getGroupe());
			ps.setInt(6, guest.getNbPersonnes());
			ps.setString(7, guest.getNote());
			ps.setInt(8, guest.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur update guest : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public boolean delete(int id) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "DELETE FROM guests WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur delete guest : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public int countByUserId(int userId) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT COUNT(*) FROM guests WHERE user_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur countByUserId : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return 0;
	}

	private Guest mapGuest(ResultSet rs) throws SQLException {
		Guest g = new Guest();
		g.setId(rs.getInt("id"));
		g.setUserId(rs.getInt("user_id"));
		g.setFirstName(rs.getString("first_name"));
		g.setLastName(rs.getString("last_name"));
		g.setPhone(rs.getString("phone"));
		g.setEmail(rs.getString("email"));
		g.setGroupe(rs.getString("groupe"));
		g.setNbPersonnes(rs.getInt("nb_personnes"));
		g.setNote(rs.getString("note"));
		return g;
	}
}
