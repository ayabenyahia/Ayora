package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import com.ayora.model.Invitation;
import com.ayora.util.DatabaseConnection;

public class InvitationDao {

	public InvitationDao() {
	}

	public List<Invitation> findByUserId(int userId) {
		List<Invitation> list = new Vector<Invitation>();
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT i.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name "
					+ "FROM invitations i JOIN guests g ON i.guest_id = g.id "
					+ "WHERE i.user_id = ? ORDER BY i.created_at DESC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(mapInvitation(rs));
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByUserId invitations : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return list;
	}

	public int create(Invitation invitation) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "INSERT INTO invitations (guest_id, user_id, statut, template_name, message_perso) "
					+ "VALUES (?, ?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, invitation.getGuestId());
			ps.setInt(2, invitation.getUserId());
			ps.setString(3, invitation.getStatut());
			ps.setString(4, invitation.getTemplateName());
			ps.setString(5, invitation.getMessagePerso());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				return keys.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur create invitation : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return -1;
	}

	public boolean updateStatut(int id, String statut) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE invitations SET statut = ?, date_envoi = CASE WHEN ? = 'ENVOYEE' THEN NOW() ELSE date_envoi END, "
					+ "date_reponse = CASE WHEN ? IN ('CONFIRMEE','DECLINEE') THEN NOW() ELSE date_reponse END "
					+ "WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, statut);
			ps.setString(2, statut);
			ps.setString(3, statut);
			ps.setInt(4, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur updateStatut invitation : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public int countSentByUserId(int userId) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT COUNT(*) FROM invitations WHERE user_id = ? AND statut IN ('ENVOYEE','CONFIRMEE','DECLINEE')";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur countSentByUserId : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return 0;
	}

	public boolean delete(int id) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "DELETE FROM invitations WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur delete invitation : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	private Invitation mapInvitation(ResultSet rs) throws SQLException {
		Invitation inv = new Invitation();
		inv.setId(rs.getInt("id"));
		inv.setGuestId(rs.getInt("guest_id"));
		inv.setUserId(rs.getInt("user_id"));
		inv.setStatut(rs.getString("statut"));
		inv.setTemplateName(rs.getString("template_name"));
		inv.setDateEnvoi(rs.getString("date_envoi"));
		inv.setDateReponse(rs.getString("date_reponse"));
		inv.setMessagePerso(rs.getString("message_perso"));
		inv.setGuestFirstName(rs.getString("guest_first_name"));
		inv.setGuestLastName(rs.getString("guest_last_name"));
		return inv;
	}
}
