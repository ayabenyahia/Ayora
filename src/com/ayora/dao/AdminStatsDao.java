package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ayora.util.Database;
import com.ayora.util.RowMapper;

/** DAO des requetes transverses du back-office admin (timeline, analytics, actions). */
public class AdminStatsDao implements IDao {

	private final Database db;

	public AdminStatsDao(Database db) {
		this.db = db;
	}

	public int countQuestionnaireByUser(int userId) {
		return db.queryInt("SELECT COUNT(*) FROM questionnaire_answers WHERE user_id = ?", userId);
	}

	public int countPicksByUser(int userId) {
		return db.queryInt("SELECT COUNT(*) FROM user_picks WHERE user_id = ?", userId);
	}

	public int countRecommendationsByVendor(int vendorId) {
		return db.queryInt("SELECT COUNT(*) FROM recommendations WHERE vendor_id = ?", vendorId);
	}

	public int countPicksByVendor(int vendorId) {
		return db.queryInt("SELECT COUNT(*) FROM user_picks WHERE vendor_id = ?", vendorId);
	}

	public boolean syncSubscriptionPlan(int userId, String plan) {
		return db.executeUpdate(
			"UPDATE subscriptions SET plan = ? WHERE user_id = ?",
			plan, userId) >= 0;
	}

	// ============================================================
	// Timeline / activity
	// ============================================================

	public List<Map<String, Object>> recentUsers(int limit) {
		return db.queryList(
			"SELECT id, first_name, last_name, role, created_at FROM users "
			+ "ORDER BY created_at DESC LIMIT ?",
			rowToMap("id", "first_name", "last_name", "role", "created_at"),
			limit);
	}

	public List<Map<String, Object>> recentDevis(int limit) {
		return db.queryList(
			"SELECT d.id, d.statut, d.created_at, u.first_name, u.last_name, v.name AS vendor_name "
			+ "FROM demandes_devis d "
			+ "JOIN users u ON d.client_id = u.id "
			+ "JOIN vendors v ON d.vendor_id = v.id "
			+ "ORDER BY d.created_at DESC LIMIT ?",
			rowToMap("id", "statut", "created_at", "first_name", "last_name", "vendor_name"),
			limit);
	}

	public List<Map<String, Object>> recentRdv(int limit) {
		return db.queryList(
			"SELECT r.id, r.statut, r.created_at, u.first_name, u.last_name, v.name AS vendor_name "
			+ "FROM rendez_vous r "
			+ "JOIN users u ON r.client_id = u.id "
			+ "JOIN vendors v ON r.vendor_id = v.id "
			+ "ORDER BY r.created_at DESC LIMIT ?",
			rowToMap("id", "statut", "created_at", "first_name", "last_name", "vendor_name"),
			limit);
	}

	public List<Map<String, Object>> recentQuestionnaires(int limit) {
		return db.queryList(
			"SELECT q.user_id, q.created_at, u.first_name, u.last_name "
			+ "FROM questionnaire_answers q JOIN users u ON q.user_id = u.id "
			+ "ORDER BY q.created_at DESC LIMIT ?",
			rowToMap("user_id", "created_at", "first_name", "last_name"),
			limit);
	}

	// ============================================================
	// Actions a traiter (priorites)
	// ============================================================

	public List<Map<String, Object>> pendingDevis(int limit) {
		return db.queryList(
			"SELECT d.id, u.first_name, u.last_name, v.name AS vendor_name "
			+ "FROM demandes_devis d "
			+ "JOIN users u ON d.client_id = u.id "
			+ "JOIN vendors v ON d.vendor_id = v.id "
			+ "WHERE d.statut = 'EN_ATTENTE' ORDER BY d.created_at DESC LIMIT ?",
			rowToMap("id", "first_name", "last_name", "vendor_name"),
			limit);
	}

	public List<Map<String, Object>> pendingRdv(int limit) {
		return db.queryList(
			"SELECT r.id, u.first_name, u.last_name, v.name AS vendor_name, r.date_rdv "
			+ "FROM rendez_vous r "
			+ "JOIN users u ON r.client_id = u.id "
			+ "JOIN vendors v ON r.vendor_id = v.id "
			+ "WHERE r.statut = 'EN_ATTENTE' ORDER BY r.date_rdv ASC LIMIT ?",
			rowToMap("id", "first_name", "last_name", "vendor_name", "date_rdv"),
			limit);
	}

	public List<Map<String, Object>> incompleteVendors(int limit) {
		return db.queryList(
			"SELECT id, name FROM vendors "
			+ "WHERE is_active = 1 AND (description IS NULL OR description = '' "
			+ "OR phone IS NULL OR phone = '' OR instagram IS NULL OR instagram = '') LIMIT ?",
			rowToMap("id", "name"),
			limit);
	}

	public List<Map<String, Object>> clientsWithoutQuestionnaire(int limit) {
		return db.queryList(
			"SELECT id, first_name, last_name FROM users "
			+ "WHERE role = 'CLIENT' AND questionnaire_completed = 0 AND is_active = 1 "
			+ "ORDER BY created_at DESC LIMIT ?",
			rowToMap("id", "first_name", "last_name"),
			limit);
	}

	// ============================================================
	// Analytics
	// ============================================================

	public List<Map<String, Object>> signupsByMonth() {
		return db.queryList(
			"SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count "
			+ "FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH) "
			+ "GROUP BY month ORDER BY month",
			rowToMap("month", "count"));
	}

	public List<Map<String, Object>> plansDistribution() {
		return db.queryList(
			"SELECT subscription_type AS plan, COUNT(*) AS count FROM users GROUP BY subscription_type",
			rowToMap("plan", "count"));
	}

	public List<Map<String, Object>> devisByStatus() {
		return db.queryList(
			"SELECT statut, COUNT(*) AS count FROM demandes_devis GROUP BY statut",
			rowToMap("statut", "count"));
	}

	// ============================================================
	// Helper : RowMapper generique qui transforme une ligne en LinkedHashMap.
	// ============================================================
	private RowMapper<Map<String, Object>> rowToMap(final String... columns) {
		return new RowMapper<Map<String, Object>>() {
			public Map<String, Object> map(ResultSet rs) throws SQLException {
				Map<String, Object> row = new LinkedHashMap<String, Object>();
				for (String col : columns) {
					row.put(col, rs.getObject(col));
				}
				return row;
			}
		};
	}
}
