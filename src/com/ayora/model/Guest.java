package com.ayora.model;

public class Guest {

	private int id;
	private int userId;
	private String firstName;
	private String lastName;
	private String phone;
	private String email;
	private String groupe;
	private int nbPersonnes;
	private String note;

	public Guest() {
		this.nbPersonnes = 1;
		this.groupe = "AUTRES";
	}

	public Guest(int id, int userId, String firstName, String lastName, String groupe, int nbPersonnes) {
		super();
		this.id = id;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.groupe = groupe;
		this.nbPersonnes = nbPersonnes;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGroupe() {
		return groupe;
	}

	public void setGroupe(String groupe) {
		this.groupe = groupe;
	}

	public int getNbPersonnes() {
		return nbPersonnes;
	}

	public void setNbPersonnes(int nbPersonnes) {
		this.nbPersonnes = nbPersonnes;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return id + ". " + firstName + " " + lastName + " (" + groupe + ", " + nbPersonnes + " pers.)";
	}
}
