package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.ayora.model.Subscription;
import com.ayora.util.DatabaseConnection;

public class SubscriptionDao {

	public SubscriptionDao() {
	}

	public Subscription findByUserId(int userId) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM subscriptions WHERE user_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapSubscription(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByUserId subscription : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public boolean create(Subscription sub) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "INSERT INTO subscriptions (user_id, plan, invitations_sent) VALUES (?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, sub.getUserId());
			ps.setString(2, sub.getPlan());
			ps.setInt(3, sub.getInvitationsSent());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur create subscription : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public boolean upgradeToPremium(int userId) {
		return updatePlan(userId, "PREMIUM");
	}

	public boolean updatePlan(int userId, String plan) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE subscriptions SET plan = ? WHERE user_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, plan);
			ps.setInt(2, userId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur updatePlan : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public boolean incrementInvitationsSent(int userId) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE subscriptions SET invitations_sent = invitations_sent + 1 WHERE user_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur incrementInvitationsSent : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	private Subscription mapSubscription(ResultSet rs) throws SQLException {
		Subscription s = new Subscription();
		s.setId(rs.getInt("id"));
		s.setUserId(rs.getInt("user_id"));
		s.setPlan(rs.getString("plan"));
		s.setInvitationsSent(rs.getInt("invitations_sent"));
		s.setMaxInvitationsFree(rs.getInt("max_invitations_free"));
		return s;
	}
}
