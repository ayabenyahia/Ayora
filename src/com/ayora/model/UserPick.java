package com.ayora.model;

/**
 * Choix d'un prestataire par la mariee parmi ses recommandations.
 *
 * Une seule selection par categorie (UNIQUE KEY user_id, category_id) :
 * la mariee retient UN salle, UN traiteur, UN photographe, etc.
 *
 * Les champs vendor* sont des champs joints depuis la table vendors
 * (city, prixMin, gamme, instagram, phone) pour eviter un n+1 cote
 * MyChoices page.
 */
public class UserPick {

	private int id;
	private int userId;
	private int vendorId;
	private int categoryId;
	private String pickedAt;

	// Joined fields
	private String vendorName;
	private String vendorCategory;
	private String vendorGamme;
	private double vendorPrixMin;
	private String vendorCity;
	private String vendorPhone;
	private String vendorInstagram;
	private String vendorDescription;

	public UserPick() {}

	public UserPick(int userId, int vendorId, int categoryId) {
		this.userId = userId;
		this.vendorId = vendorId;
		this.categoryId = categoryId;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public int getVendorId() { return vendorId; }
	public void setVendorId(int vendorId) { this.vendorId = vendorId; }

	public int getCategoryId() { return categoryId; }
	public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

	public String getPickedAt() { return pickedAt; }
	public void setPickedAt(String pickedAt) { this.pickedAt = pickedAt; }

	public String getVendorName() { return vendorName; }
	public void setVendorName(String s) { this.vendorName = s; }

	public String getVendorCategory() { return vendorCategory; }
	public void setVendorCategory(String s) { this.vendorCategory = s; }

	public String getVendorGamme() { return vendorGamme; }
	public void setVendorGamme(String s) { this.vendorGamme = s; }

	public double getVendorPrixMin() { return vendorPrixMin; }
	public void setVendorPrixMin(double v) { this.vendorPrixMin = v; }

	public String getVendorCity() { return vendorCity; }
	public void setVendorCity(String s) { this.vendorCity = s; }

	public String getVendorPhone() { return vendorPhone; }
	public void setVendorPhone(String s) { this.vendorPhone = s; }

	public String getVendorInstagram() { return vendorInstagram; }
	public void setVendorInstagram(String s) { this.vendorInstagram = s; }

	public String getVendorDescription() { return vendorDescription; }
	public void setVendorDescription(String s) { this.vendorDescription = s; }
}
