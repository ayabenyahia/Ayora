package com.ayora.model;

public class QuestionnaireAnswer {

	private int id;
	private int userId;
	// Budget
	private double budgetTotal;
	private String budgetFlexibility;
	// Invites
	private int nbInvites;
	private int nbInvitesFemmes;
	private int nbInvitesHommes;
	// Date et lieu
	private String dateMariage;
	private String saisonPreferee;
	private String lieuCeremonie;
	// Style et ambiance
	private String styleMariage;
	private String ambiance;
	private String themeCouleur;
	// Niveau de luxe
	private String niveauLuxe;
	// Priorites
	private int prioriteSalle;
	private int prioriteTraiteur;
	private int prioritePhoto;
	private int prioriteMusique;
	private int prioriteDecoration;
	private int prioriteNeggafa;
	private int prioriteMakeup;
	// Preferences
	private String typeCuisine;
	private String typeMusique;
	private String prefPhoto;
	private String prefDecoration;
	// Neggafa
	private int nbTenuesNeggafa;
	private String styleNeggafa;
	// Economies
	private String postesEconomie;
	// Notes
	private String notesSpeciales;

	public QuestionnaireAnswer() {
		this.prioriteSalle = 3;
		this.prioriteTraiteur = 3;
		this.prioritePhoto = 3;
		this.prioriteMusique = 3;
		this.prioriteDecoration = 3;
		this.prioriteNeggafa = 3;
		this.prioriteMakeup = 3;
		this.nbTenuesNeggafa = 3;
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

	public double getBudgetTotal() {
		return budgetTotal;
	}

	public void setBudgetTotal(double budgetTotal) {
		this.budgetTotal = budgetTotal;
	}

	public String getBudgetFlexibility() {
		return budgetFlexibility;
	}

	public void setBudgetFlexibility(String budgetFlexibility) {
		this.budgetFlexibility = budgetFlexibility;
	}

	public int getNbInvites() {
		return nbInvites;
	}

	public void setNbInvites(int nbInvites) {
		this.nbInvites = nbInvites;
	}

	public int getNbInvitesFemmes() {
		return nbInvitesFemmes;
	}

	public void setNbInvitesFemmes(int nbInvitesFemmes) {
		this.nbInvitesFemmes = nbInvitesFemmes;
	}

	public int getNbInvitesHommes() {
		return nbInvitesHommes;
	}

	public void setNbInvitesHommes(int nbInvitesHommes) {
		this.nbInvitesHommes = nbInvitesHommes;
	}

	public String getDateMariage() {
		return dateMariage;
	}

	public void setDateMariage(String dateMariage) {
		this.dateMariage = dateMariage;
	}

	public String getSaisonPreferee() {
		return saisonPreferee;
	}

	public void setSaisonPreferee(String saisonPreferee) {
		this.saisonPreferee = saisonPreferee;
	}

	public String getLieuCeremonie() {
		return lieuCeremonie;
	}

	public void setLieuCeremonie(String lieuCeremonie) {
		this.lieuCeremonie = lieuCeremonie;
	}

	public String getStyleMariage() {
		return styleMariage;
	}

	public void setStyleMariage(String styleMariage) {
		this.styleMariage = styleMariage;
	}

	public String getAmbiance() {
		return ambiance;
	}

	public void setAmbiance(String ambiance) {
		this.ambiance = ambiance;
	}

	public String getThemeCouleur() {
		return themeCouleur;
	}

	public void setThemeCouleur(String themeCouleur) {
		this.themeCouleur = themeCouleur;
	}

	public String getNiveauLuxe() {
		return niveauLuxe;
	}

	public void setNiveauLuxe(String niveauLuxe) {
		this.niveauLuxe = niveauLuxe;
	}

	public int getPrioriteSalle() {
		return prioriteSalle;
	}

	public void setPrioriteSalle(int prioriteSalle) {
		this.prioriteSalle = prioriteSalle;
	}

	public int getPrioriteTraiteur() {
		return prioriteTraiteur;
	}

	public void setPrioriteTraiteur(int prioriteTraiteur) {
		this.prioriteTraiteur = prioriteTraiteur;
	}

	public int getPrioritePhoto() {
		return prioritePhoto;
	}

	public void setPrioritePhoto(int prioritePhoto) {
		this.prioritePhoto = prioritePhoto;
	}

	public int getPrioriteMusique() {
		return prioriteMusique;
	}

	public void setPrioriteMusique(int prioriteMusique) {
		this.prioriteMusique = prioriteMusique;
	}

	public int getPrioriteDecoration() {
		return prioriteDecoration;
	}

	public void setPrioriteDecoration(int prioriteDecoration) {
		this.prioriteDecoration = prioriteDecoration;
	}

	public int getPrioriteNeggafa() {
		return prioriteNeggafa;
	}

	public void setPrioriteNeggafa(int prioriteNeggafa) {
		this.prioriteNeggafa = prioriteNeggafa;
	}

	public int getPrioriteMakeup() {
		return prioriteMakeup;
	}

	public void setPrioriteMakeup(int prioriteMakeup) {
		this.prioriteMakeup = prioriteMakeup;
	}

	public String getTypeCuisine() {
		return typeCuisine;
	}

	public void setTypeCuisine(String typeCuisine) {
		this.typeCuisine = typeCuisine;
	}

	public String getTypeMusique() {
		return typeMusique;
	}

	public void setTypeMusique(String typeMusique) {
		this.typeMusique = typeMusique;
	}

	public String getPrefPhoto() {
		return prefPhoto;
	}

	public void setPrefPhoto(String prefPhoto) {
		this.prefPhoto = prefPhoto;
	}

	public String getPrefDecoration() {
		return prefDecoration;
	}

	public void setPrefDecoration(String prefDecoration) {
		this.prefDecoration = prefDecoration;
	}

	public int getNbTenuesNeggafa() {
		return nbTenuesNeggafa;
	}

	public void setNbTenuesNeggafa(int nbTenuesNeggafa) {
		this.nbTenuesNeggafa = nbTenuesNeggafa;
	}

	public String getStyleNeggafa() {
		return styleNeggafa;
	}

	public void setStyleNeggafa(String styleNeggafa) {
		this.styleNeggafa = styleNeggafa;
	}

	public String getPostesEconomie() {
		return postesEconomie;
	}

	public void setPostesEconomie(String postesEconomie) {
		this.postesEconomie = postesEconomie;
	}

	public String getNotesSpeciales() {
		return notesSpeciales;
	}

	public void setNotesSpeciales(String notesSpeciales) {
		this.notesSpeciales = notesSpeciales;
	}

	@Override
	public String toString() {
		return "Questionnaire [userId=" + userId + ", budget=" + budgetTotal + ", style=" + styleMariage + "]";
	}
}
