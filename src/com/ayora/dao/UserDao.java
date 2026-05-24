package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ayora.model.User;
import com.ayora.util.Database;

/** DAO de l'entite User. */
public class UserDao implements IDao {

	private final Database db;

	public UserDao(Database db) {
		this.db = db;
	}

	public User findByEmail(String email) {
		return db.queryOne("SELECT * FROM users WHERE email = ?", this::mapUser, email);
	}

	public User findById(int id) {
		return db.queryOne("SELECT * FROM users WHERE id = ?", this::mapUser, id);
	}

	/**
	 * NE PAS UTILISER pour l'auth nouvelle generation : utilise authenticate()
	 * dans AyoraMetier qui verifie le hash PBKDF2. Methode conservee pour
	 * compatibilite mais marquee @Deprecated.
	 */
	@Deprecated
	public User authenticate(String email, String password) {
		return db.queryOne(
			"SELECT * FROM users WHERE email = ? AND password = ?",
			this::mapUser, email, password);
	}

	public int create(User user) {
		// Si passwordHash est renseigne, on l'utilise (nouvelle norme).
		// Sinon on tombe sur le clair (compat anciens tests). password sera
		// migre au prochain login via updatePasswordHash().
		String hash = user.getPasswordHash();
		return db.insertReturningKey(
			"INSERT INTO users (email, password, password_hash, first_name, last_name, phone, city, subscription_type) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			user.getEmail(),
			hash != null ? null : user.getPassword(),  // clair NULL si on a deja le hash
			hash,                                       // hash PBKDF2
			user.getFirstName(), user.getLastName(),
			user.getPhone(), user.getCity() != null ? user.getCity() : "Fes", "FREE");
	}

	/** Migration progressive : remplace le clair par un hash a la volee. */
	public boolean updatePasswordHash(int userId, String passwordHash) {
		return db.executeUpdate(
			"UPDATE users SET password_hash = ?, password = NULL WHERE id = ?",
			passwordHash, userId) > 0;
	}

	public boolean updateQuestionnaireStatus(int userId, boolean completed) {
		return db.executeUpdate(
			"UPDATE users SET questionnaire_completed = ? WHERE id = ?",
			completed, userId) > 0;
	}

	public boolean updateSubscription(int userId, String type) {
		return db.executeUpdate(
			"UPDATE users SET subscription_type = ? WHERE id = ?",
			type, userId) > 0;
	}

	public List<User> findAll() {
		return db.queryList("SELECT * FROM users ORDER BY id DESC", this::mapUser);
	}

	public int countAll() {
		return db.queryInt("SELECT COUNT(*) FROM users");
	}

	public int countByRole(String role) {
		return db.queryInt("SELECT COUNT(*) FROM users WHERE role = ?", role);
	}

	public int countByPlan(String plan) {
		return db.queryInt("SELECT COUNT(*) FROM users WHERE subscription_type = ?", plan);
	}

	public int countByQuestionnaire(boolean completed) {
		return db.queryInt(
			"SELECT COUNT(*) FROM users WHERE questionnaire_completed = ? AND role='CLIENT'",
			completed);
	}

	public int countByActive(boolean active) {
		return db.queryInt("SELECT COUNT(*) FROM users WHERE is_active = ?", active);
	}

	public List<User> search(String keyword, String role, String plan, Boolean questionnaireCompleted,
			Boolean active, int offset, int limit) {
		StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
		List<Object> params = new ArrayList<Object>();
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? OR phone LIKE ?)");
			String k = "%" + keyword.toLowerCase().trim() + "%";
			params.add(k); params.add(k); params.add(k); params.add(k);
		}
		if (role != null && !role.isEmpty())  { sql.append(" AND role = ?"); params.add(role); }
		if (plan != null && !plan.isEmpty())  { sql.append(" AND subscription_type = ?"); params.add(plan); }
		if (questionnaireCompleted != null)   { sql.append(" AND questionnaire_completed = ?"); params.add(questionnaireCompleted); }
		if (active != null)                   { sql.append(" AND is_active = ?"); params.add(active); }
		sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
		params.add(limit); params.add(offset);
		return db.queryList(sql.toString(), this::mapUser, params.toArray());
	}

	public int countSearch(String keyword, String role, String plan, Boolean questionnaireCompleted, Boolean active) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
		List<Object> params = new ArrayList<Object>();
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? OR phone LIKE ?)");
			String k = "%" + keyword.toLowerCase().trim() + "%";
			params.add(k); params.add(k); params.add(k); params.add(k);
		}
		if (role != null && !role.isEmpty())  { sql.append(" AND role = ?"); params.add(role); }
		if (plan != null && !plan.isEmpty())  { sql.append(" AND subscription_type = ?"); params.add(plan); }
		if (questionnaireCompleted != null)   { sql.append(" AND questionnaire_completed = ?"); params.add(questionnaireCompleted); }
		if (active != null)                   { sql.append(" AND is_active = ?"); params.add(active); }
		return db.queryInt(sql.toString(), params.toArray());
	}

	public boolean update(int id, String firstName, String lastName, String email, String phone, String city) {
		return db.executeUpdate(
			"UPDATE users SET first_name=COALESCE(?, first_name), last_name=COALESCE(?, last_name), "
			+ "email=COALESCE(?, email), phone=COALESCE(?, phone), city=COALESCE(?, city) WHERE id=?",
			firstName, lastName, email, phone, city, id) > 0;
	}

	public boolean updateRole(int id, String role) {
		return db.executeUpdate("UPDATE users SET role=? WHERE id=?", role, id) > 0;
	}

	public boolean updateActive(int id, boolean active) {
		return db.executeUpdate("UPDATE users SET is_active=? WHERE id=?", active, id) > 0;
	}

	public boolean delete(int id) {
		// Suppression cascade manuelle (FK pas toutes en ON DELETE CASCADE).
		// Transaction : on garde la Connection en local pour atomicite.
		Connection c = null;
		try {
			c = db.getConnection();
			c.setAutoCommit(false);
			deleteRows(c, "DELETE FROM user_picks WHERE user_id=?", id);
			deleteRows(c, "DELETE FROM recommendations WHERE user_id=?", id);
			deleteRows(c, "DELETE FROM questionnaire_answers WHERE user_id=?", id);
			deleteRows(c, "DELETE FROM subscriptions WHERE user_id=?", id);
			deleteRows(c, "DELETE FROM demandes_devis WHERE client_id=?", id);
			deleteRows(c, "DELETE FROM rendez_vous WHERE client_id=?", id);
			deleteRows(c, "DELETE FROM guests WHERE user_id=?", id);
			deleteRows(c, "DELETE FROM invitations WHERE user_id=?", id);
			PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?");
			ps.setInt(1, id);
			int n = ps.executeUpdate();
			c.commit();
			return n > 0;
		} catch (SQLException e) {
			System.out.println("## delete user : " + e.getMessage());
			try { if (c != null) c.rollback(); } catch (SQLException ignore) {}
			return false;
		} finally {
			try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignore) {}
			db.close(c);
		}
	}

	private void deleteRows(Connection c, String sql, int id) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setInt(1, id);
		ps.executeUpdate();
	}

	private User mapUser(ResultSet rs) throws SQLException {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setEmail(rs.getString("email"));
		user.setPassword(rs.getString("password"));
		try { user.setPasswordHash(rs.getString("password_hash")); }
			catch (SQLException e) { user.setPasswordHash(null); }
		user.setFirstName(rs.getString("first_name"));
		user.setLastName(rs.getString("last_name"));
		user.setPhone(rs.getString("phone"));
		user.setCity(rs.getString("city"));
		user.setSubscriptionType(rs.getString("subscription_type"));
		user.setQuestionnaireCompleted(rs.getBoolean("questionnaire_completed"));
		try { user.setRole(rs.getString("role")); }
			catch (SQLException e) { user.setRole("CLIENT"); }
		try { user.setVendorId(rs.getInt("vendor_id")); }
			catch (SQLException e) { user.setVendorId(0); }
		try { user.setActive(rs.getBoolean("is_active")); }
			catch (SQLException e) { user.setActive(true); }
		try {
			java.sql.Timestamp ts = rs.getTimestamp("created_at");
			if (ts != null) user.setCreatedAt(ts.toString().substring(0, 19));
		} catch (SQLException e) { /* optionnel */ }
		return user;
	}
}
