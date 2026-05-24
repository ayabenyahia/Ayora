package com.ayora.model;

/**
 * Rendez-vous fixe entre un client et un prestataire (table rendez_vous).
 *
 * Modele plat porteur des champs de la table + des champs joints
 * (utilisateur / prestataire) pour l'affichage cote frontend.
 */
public class RendezVous {

	private int id;
	private int clientId;
	private int vendorId;
	private String dateRdv;
	private String heureRdv;
	private String lieu;
	private String note;
	private String statut;
	private String createdAt;

	private String clientFirstName;
	private String clientLastName;
	private String clientPhone;
	private String vendorName;

	public RendezVous() { }

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getClientId() { return clientId; }
	public void setClientId(int clientId) { this.clientId = clientId; }
	public int getVendorId() { return vendorId; }
	public void setVendorId(int vendorId) { this.vendorId = vendorId; }
	public String getDateRdv() { return dateRdv; }
	public void setDateRdv(String dateRdv) { this.dateRdv = dateRdv; }
	public String getHeureRdv() { return heureRdv; }
	public void setHeureRdv(String heureRdv) { this.heureRdv = heureRdv; }
	public String getLieu() { return lieu; }
	public void setLieu(String lieu) { this.lieu = lieu; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public String getStatut() { return statut; }
	public void setStatut(String statut) { this.statut = statut; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String s) { this.createdAt = s; }

	public String getClientFirstName() { return clientFirstName; }
	public void setClientFirstName(String s) { this.clientFirstName = s; }
	public String getClientLastName() { return clientLastName; }
	public void setClientLastName(String s) { this.clientLastName = s; }
	public String getClientPhone() { return clientPhone; }
	public void setClientPhone(String s) { this.clientPhone = s; }
	public String getVendorName() { return vendorName; }
	public void setVendorName(String s) { this.vendorName = s; }
}
