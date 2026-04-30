package com.ayora.model;

public class Invitation {

	private int id;
	private int guestId;
	private int userId;
	private String statut;
	private String templateName;
	private String dateEnvoi;
	private String dateReponse;
	private String messagePerso;
	// URL d'une video d'invitation (modeles video Premium uniquement)
	private String videoUrl;
	// Joined fields
	private String guestFirstName;
	private String guestLastName;

	public Invitation() {
		this.statut = "EN_ATTENTE";
		this.templateName = "classique";
	}

	public Invitation(int id, int guestId, int userId, String statut) {
		super();
		this.id = id;
		this.guestId = guestId;
		this.userId = userId;
		this.statut = statut;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGuestId() {
		return guestId;
	}

	public void setGuestId(int guestId) {
		this.guestId = guestId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getDateEnvoi() {
		return dateEnvoi;
	}

	public void setDateEnvoi(String dateEnvoi) {
		this.dateEnvoi = dateEnvoi;
	}

	public String getDateReponse() {
		return dateReponse;
	}

	public void setDateReponse(String dateReponse) {
		this.dateReponse = dateReponse;
	}

	public String getMessagePerso() {
		return messagePerso;
	}

	public void setMessagePerso(String messagePerso) {
		this.messagePerso = messagePerso;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getGuestFirstName() {
		return guestFirstName;
	}

	public void setGuestFirstName(String guestFirstName) {
		this.guestFirstName = guestFirstName;
	}

	public String getGuestLastName() {
		return guestLastName;
	}

	public void setGuestLastName(String guestLastName) {
		this.guestLastName = guestLastName;
	}

	@Override
	public String toString() {
		return id + ". Invitation pour " + guestFirstName + " " + guestLastName + " (" + statut + ")";
	}
}
