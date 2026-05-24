package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ayora.model.Subscription;
import com.ayora.util.Database;

/** DAO de l'entite Subscription. */
public class SubscriptionDao implements IDao {

	private final Database db;

	public SubscriptionDao(Database db) {
		this.db = db;
	}

	public Subscription findByUserId(int userId) {
		return db.queryOne(
			"SELECT * FROM subscriptions WHERE user_id = ?",
			this::mapSubscription, userId);
	}

	public boolean create(Subscription sub) {
		return db.executeUpdate(
			"INSERT INTO subscriptions (user_id, plan, invitations_sent) VALUES (?, ?, ?)",
			sub.getUserId(), sub.getPlan(), sub.getInvitationsSent()) > 0;
	}

	public boolean updatePlan(int userId, String plan) {
		return db.executeUpdate(
			"UPDATE subscriptions SET plan = ? WHERE user_id = ?",
			plan, userId) > 0;
	}

	public boolean incrementInvitationsSent(int userId) {
		return db.executeUpdate(
			"UPDATE subscriptions SET invitations_sent = invitations_sent + 1 WHERE user_id = ?",
			userId) > 0;
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
