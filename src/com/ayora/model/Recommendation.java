package com.ayora.model;

public class Recommendation {

	private int id;
	private int userId;
	private int vendorId;
	private double score;
	private String raison;
	private boolean viewed;
	// Joined fields
	private String vendorName;
	private String vendorCategory;
	private String vendorGamme;
	private double vendorPrixMin;

	public Recommendation() {
	}

	public Recommendation(int userId, int vendorId, double score, String raison) {
		super();
		this.userId = userId;
		this.vendorId = vendorId;
		this.score = score;
		this.raison = raison;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getRaison() {
		return raison;
	}

	public void setRaison(String raison) {
		this.raison = raison;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorCategory() {
		return vendorCategory;
	}

	public void setVendorCategory(String vendorCategory) {
		this.vendorCategory = vendorCategory;
	}

	public String getVendorGamme() {
		return vendorGamme;
	}

	public void setVendorGamme(String vendorGamme) {
		this.vendorGamme = vendorGamme;
	}

	public double getVendorPrixMin() {
		return vendorPrixMin;
	}

	public void setVendorPrixMin(double vendorPrixMin) {
		this.vendorPrixMin = vendorPrixMin;
	}

	@Override
	public String toString() {
		return vendorName + " - Score: " + score + " (" + raison + ")";
	}
}
