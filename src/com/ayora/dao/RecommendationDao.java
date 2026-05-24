package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ayora.model.Recommendation;
import com.ayora.util.Database;

/** DAO de l'entite Recommendation. */
public class RecommendationDao implements IDao {

	private static final String SELECT_BASE = "SELECT r.*, "
			+ "v.name AS vendor_name, v.category_id AS v_cat_id, "
			+ "vc.name_fr AS vendor_category, "
			+ "v.gamme AS vendor_gamme, v.prix_min AS vendor_prix_min, v.prix_max AS vendor_prix_max, "
			+ "v.city AS vendor_city, v.phone AS vendor_phone, v.instagram AS vendor_instagram, "
			+ "v.tags AS vendor_tags, v.rating AS vendor_rating, v.nb_avis AS vendor_nb_avis "
			+ "FROM recommendations r "
			+ "JOIN vendors v ON r.vendor_id = v.id "
			+ "JOIN vendor_categories vc ON v.category_id = vc.id ";

	private final Database db;

	public RecommendationDao(Database db) {
		this.db = db;
	}

	public List<Recommendation> findByUserId(int userId) {
		return db.queryList(
			SELECT_BASE + "WHERE r.user_id = ? ORDER BY r.score DESC",
			this::mapRecommendation, userId);
	}

	public List<Recommendation> findByVendorId(int vendorId) {
		return db.queryList(
			SELECT_BASE + "WHERE r.vendor_id = ? ORDER BY r.score DESC",
			this::mapRecommendation, vendorId);
	}

	public boolean create(Recommendation rec) {
		return db.executeUpdate(
			"INSERT INTO recommendations (user_id, vendor_id, score, raison) VALUES (?, ?, ?, ?)",
			rec.getUserId(), rec.getVendorId(), rec.getScore(), rec.getRaison()) > 0;
	}

	public boolean deleteByUserId(int userId) {
		// >= 0 : DELETE peut retourner 0 lignes si l'utilisateur n'avait
		// pas encore de recommendations (1er appel) : on considere ca un succes.
		return db.executeUpdate("DELETE FROM recommendations WHERE user_id = ?", userId) >= 0;
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
		r.setVendorCategoryId(rs.getInt("v_cat_id"));
		r.setVendorGamme(rs.getString("vendor_gamme"));
		r.setVendorPrixMin(rs.getDouble("vendor_prix_min"));
		r.setVendorPrixMax(rs.getDouble("vendor_prix_max"));
		r.setVendorCity(rs.getString("vendor_city"));
		r.setVendorPhone(rs.getString("vendor_phone"));
		r.setVendorInstagram(rs.getString("vendor_instagram"));
		r.setVendorTags(rs.getString("vendor_tags"));
		r.setVendorRating(rs.getDouble("vendor_rating"));
		r.setVendorNbAvis(rs.getInt("vendor_nb_avis"));
		return r;
	}
}
