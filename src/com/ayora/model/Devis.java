package com.ayora.model;

/**
 * Demande de devis envoyee par un client a un prestataire.
 *
 * Modele plat utilise par le servlet : porte les champs de la table
 * demandes_devis et les champs d'affichage (joins users/vendors) renseignes
 * par le DAO via JOIN dans la requete SELECT.
 */
public class Devis {

	private int id;
	private int clientId;
	private int vendorId;
	private double budgetMin;
	private double budgetMax;
	private String message;
	private String dateMariage;
	private int nbInvites;
	private String statut;
	private String reponsePrestataire;
	private String createdAt;

	// Champs joints pour l'affichage (peuvent etre null si non charges)
	private String clientFirstName;
	private String clientLastName;
	private String clientEmail;
	private String clientPhone;
	private String vendorName;
	private String vendorCategory;

	public Devis() { }

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getClientId() { return clientId; }
	public void setClientId(int clientId) { this.clientId = clientId; }
	public int getVendorId() { return vendorId; }
	public void setVendorId(int vendorId) { this.vendorId = vendorId; }
	public double getBudgetMin() { return budgetMin; }
	public void setBudgetMin(double budgetMin) { this.budgetMin = budgetMin; }
	public double getBudgetMax() { return budgetMax; }
	public void setBudgetMax(double budgetMax) { this.budgetMax = budgetMax; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getDateMariage() { return dateMariage; }
	public void setDateMariage(String dateMariage) { this.dateMariage = dateMariage; }
	public int getNbInvites() { return nbInvites; }
	public void setNbInvites(int nbInvites) { this.nbInvites = nbInvites; }
	public String getStatut() { return statut; }
	public void setStatut(String statut) { this.statut = statut; }
	public String getReponsePrestataire() { return reponsePrestataire; }
	public void setReponsePrestataire(String r) { this.reponsePrestataire = r; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

	public String getClientFirstName() { return clientFirstName; }
	public void setClientFirstName(String s) { this.clientFirstName = s; }
	public String getClientLastName() { return clientLastName; }
	public void setClientLastName(String s) { this.clientLastName = s; }
	public String getClientEmail() { return clientEmail; }
	public void setClientEmail(String s) { this.clientEmail = s; }
	public String getClientPhone() { return clientPhone; }
	public void setClientPhone(String s) { this.clientPhone = s; }
	public String getVendorName() { return vendorName; }
	public void setVendorName(String s) { this.vendorName = s; }
	public String getVendorCategory() { return vendorCategory; }
	public void setVendorCategory(String s) { this.vendorCategory = s; }
}
