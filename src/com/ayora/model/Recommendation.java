package com.ayora.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Recommandation enrichie : score, tags, raison contextualisee
 * + donnees prestataires complete pour cards riches cote front.
 *
 * Les champs "vendor*" sont des champs joints (issus du JOIN dans le DAO).
 * Les champs "tags", "raisonShort" et "subScores" ne sont pas persistes :
 * ils sont calcules a la volee par le RecommendationService.
 */
public class Recommendation {

	private int id;
	private int userId;
	private int vendorId;
	private double score;
	private String raison;
	private boolean viewed;

	// Joined vendor fields (from DAO)
	private String vendorName;
	private String vendorCategory;
	private int vendorCategoryId;
	private String vendorGamme;
	private double vendorPrixMin;
	private double vendorPrixMax;
	private String vendorCity;
	private String vendorPhone;
	private String vendorInstagram;
	private String vendorTags;
	private double vendorRating;
	private int vendorNbAvis;

	// Computed fields (not persisted)
	private List<String> tags;
	private String raisonShort;
	private double scoreBudget;
	private double scoreStyle;
	private double scoreLuxe;
	private double scorePopularite;
	private double scoreCulturel;
	// Sous-score IA Ayora : proximite geographique (15% du score final)
	private double scoreCity;

	public Recommendation() {
		this.tags = new ArrayList<String>();
	}

	public Recommendation(int userId, int vendorId, double score, String raison) {
		this();
		this.userId = userId;
		this.vendorId = vendorId;
		this.score = score;
		this.raison = raison;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public int getVendorId() { return vendorId; }
	public void setVendorId(int vendorId) { this.vendorId = vendorId; }

	public double getScore() { return score; }
	public void setScore(double score) { this.score = score; }

	public String getRaison() { return raison; }
	public void setRaison(String raison) { this.raison = raison; }

	public boolean isViewed() { return viewed; }
	public void setViewed(boolean viewed) { this.viewed = viewed; }

	public String getVendorName() { return vendorName; }
	public void setVendorName(String vendorName) { this.vendorName = vendorName; }

	public String getVendorCategory() { return vendorCategory; }
	public void setVendorCategory(String vendorCategory) { this.vendorCategory = vendorCategory; }

	public int getVendorCategoryId() { return vendorCategoryId; }
	public void setVendorCategoryId(int vendorCategoryId) { this.vendorCategoryId = vendorCategoryId; }

	public String getVendorGamme() { return vendorGamme; }
	public void setVendorGamme(String vendorGamme) { this.vendorGamme = vendorGamme; }

	public double getVendorPrixMin() { return vendorPrixMin; }
	public void setVendorPrixMin(double vendorPrixMin) { this.vendorPrixMin = vendorPrixMin; }

	public double getVendorPrixMax() { return vendorPrixMax; }
	public void setVendorPrixMax(double vendorPrixMax) { this.vendorPrixMax = vendorPrixMax; }

	public String getVendorCity() { return vendorCity; }
	public void setVendorCity(String vendorCity) { this.vendorCity = vendorCity; }

	public String getVendorPhone() { return vendorPhone; }
	public void setVendorPhone(String vendorPhone) { this.vendorPhone = vendorPhone; }

	public String getVendorInstagram() { return vendorInstagram; }
	public void setVendorInstagram(String vendorInstagram) { this.vendorInstagram = vendorInstagram; }

	public String getVendorTags() { return vendorTags; }
	public void setVendorTags(String vendorTags) { this.vendorTags = vendorTags; }

	public double getVendorRating() { return vendorRating; }
	public void setVendorRating(double vendorRating) { this.vendorRating = vendorRating; }

	public int getVendorNbAvis() { return vendorNbAvis; }
	public void setVendorNbAvis(int vendorNbAvis) { this.vendorNbAvis = vendorNbAvis; }

	public List<String> getTags() { return tags; }
	public void setTags(List<String> tags) { this.tags = tags; }
	public void addTag(String tag) {
		if (this.tags == null) this.tags = new ArrayList<String>();
		if (!this.tags.contains(tag)) this.tags.add(tag);
	}

	public String getRaisonShort() { return raisonShort; }
	public void setRaisonShort(String raisonShort) { this.raisonShort = raisonShort; }

	public double getScoreBudget() { return scoreBudget; }
	public void setScoreBudget(double s) { this.scoreBudget = s; }

	public double getScoreStyle() { return scoreStyle; }
	public void setScoreStyle(double s) { this.scoreStyle = s; }

	public double getScoreLuxe() { return scoreLuxe; }
	public void setScoreLuxe(double s) { this.scoreLuxe = s; }

	public double getScorePopularite() { return scorePopularite; }
	public void setScorePopularite(double s) { this.scorePopularite = s; }

	public double getScoreCulturel() { return scoreCulturel; }
	public void setScoreCulturel(double s) { this.scoreCulturel = s; }

	public double getScoreCity() { return scoreCity; }
	public void setScoreCity(double s) { this.scoreCity = s; }

	@Override
	public String toString() {
		return vendorName + " - Score: " + score + " (" + raison + ")";
	}
}
