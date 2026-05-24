package com.ayora.model;

public class User {

	private int id;
	private String email;
	private String password;       // legacy clair (migration progressive via lazy-hash)
	private String passwordHash;   // PBKDF2 (nouvelle norme securite)
	private String firstName;
	private String lastName;
	private String phone;
	private String city;
	private String subscriptionType;
	private boolean questionnaireCompleted;
	private String role;
	private int vendorId;
	private boolean active = true;
	private String createdAt;

	public User() {
		this.role = "CLIENT";
	}

	public User(int id, String email, String password, String firstName, String lastName, String phone, String city, String subscriptionType, boolean questionnaireCompleted) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.city = city;
		this.subscriptionType = subscriptionType;
		this.questionnaireCompleted = questionnaireCompleted;
		this.role = "CLIENT";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(String subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	public boolean isQuestionnaireCompleted() {
		return questionnaireCompleted;
	}

	public void setQuestionnaireCompleted(boolean questionnaireCompleted) {
		this.questionnaireCompleted = questionnaireCompleted;
	}

	public String getRole() {
		return role != null ? role : "CLIENT";
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

	@Override
	public String toString() {
		return id + ". " + firstName + " " + lastName + " (" + email + ")";
	}
}
