package com.ayora.model;

public class Vendor {

	private int id;
	private int categoryId;
	private String categoryName;
	private String name;
	private String city;
	private String description;
	private double prixMin;
	private double prixMax;
	private String gamme;
	private String phone;
	private String email;
	private String instagram;
	private String address;
	private String tags;
	private double rating;
	private int nbAvis;
	private boolean active;

	public Vendor() {
		this.city = "Fes";
		this.active = true;
	}

	public Vendor(int id, String name, int categoryId, String gamme, double prixMin, double prixMax) {
		super();
		this.id = id;
		this.name = name;
		this.categoryId = categoryId;
		this.gamme = gamme;
		this.prixMin = prixMin;
		this.prixMax = prixMax;
		this.city = "Fes";
		this.active = true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrixMin() {
		return prixMin;
	}

	public void setPrixMin(double prixMin) {
		this.prixMin = prixMin;
	}

	public double getPrixMax() {
		return prixMax;
	}

	public void setPrixMax(double prixMax) {
		this.prixMax = prixMax;
	}

	public String getGamme() {
		return gamme;
	}

	public void setGamme(String gamme) {
		this.gamme = gamme;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInstagram() {
		return instagram;
	}

	public void setInstagram(String instagram) {
		this.instagram = instagram;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public int getNbAvis() {
		return nbAvis;
	}

	public void setNbAvis(int nbAvis) {
		this.nbAvis = nbAvis;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return id + ". " + name + " (" + gamme + ", a partir de " + prixMin + " DHS)";
	}
}
