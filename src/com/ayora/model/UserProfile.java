package com.ayora.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Profil utilisateur deduit du questionnaire.
 *
 * Sert d'abstraction propre entre QuestionnaireAnswer (donnees brutes du form)
 * et le moteur de recommandation. Le service construit ce profil une seule fois
 * via RecommendationService.buildUserProfile(answer) et l'utilise pour scorer
 * chaque vendor.
 *
 * Ce decoupage suit le pattern Service Layer du cours (p02-jee).
 */
public class UserProfile {

	private int userId;

	// Wedding identity
	private String style;            // TRADITIONNEL / MODERNE / MIXTE / LUXE / SIMPLE / INTIME
	private String ambiance;         // INTIME / GRANDIOSE / FESTIVE / ROMANTIQUE / FAMILIALE / LUXUEUSE / TRADITIONNELLE
	private String niveauLuxe;       // ECONOMIQUE / MOYEN / PREMIUM / ULTRA_LUXE
	private String themeCouleur;
	private String saison;

	// Budget profile
	private double budgetTotal;
	private String budgetFlexibility;
	private double budgetPerGuest;
	private String budgetTier;       // SERRE / CONFORTABLE / GENEREUX / ILLIMITE

	// Guests
	private int nbInvites;
	private String guestSize;        // INTIME (<100) / MOYEN (100-200) / GRAND (200-400) / TRES_GRAND (>400)

	// Preferences metier
	private String typeMusique;
	private String typeCuisine;
	private String prefPhoto;
	private String prefDecoration;
	private String styleNeggafa;
	private int nbTenuesNeggafa;

	// Priorities (1..5)
	private int prioriteSalle;
	private int prioriteTraiteur;
	private int prioritePhoto;
	private int prioriteMusique;
	private int prioriteDecoration;
	private int prioriteNeggafa;
	private int prioriteMakeup;

	// Top categories that matter most for this user (sorted by priorite desc)
	private List<Integer> topCategoryIds;

	// Postes ou l'utilisateur veut economiser (CSV)
	private String postesEconomie;

	// Mots-cles emotionnels extraits de notesSpeciales (JSON)
	private List<String> moodKeywords;

	// === Preferences enrichies extraites de notesSpeciales JSON ============
	// Liste des categories de prestataires demandees (cf. section 4 du
	// questionnaire). Les recommandations sont filtrees pour ne garder que
	// les vendors dans ces categories.
	private List<String> requestedServices;

	// Ville saisie par l'utilisateur dans le questionnaire (section 1).
	private String userCity;

	// 1 = strictement la meme ville ; 5 = ouvert partout au Maroc.
	// Utilise par AyoraRecommendationEngine.scoreCity pour ponderer la penalite
	// quand le vendor est dans une autre ville que celle de l'utilisateur.
	private int cityTolerance = 2;

	public UserProfile() {
		this.moodKeywords = new ArrayList<String>();
		this.topCategoryIds = new ArrayList<Integer>();
	}

	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public String getStyle() { return style; }
	public void setStyle(String style) { this.style = style; }

	public String getAmbiance() { return ambiance; }
	public void setAmbiance(String ambiance) { this.ambiance = ambiance; }

	public String getNiveauLuxe() { return niveauLuxe; }
	public void setNiveauLuxe(String niveauLuxe) { this.niveauLuxe = niveauLuxe; }

	public String getThemeCouleur() { return themeCouleur; }
	public void setThemeCouleur(String themeCouleur) { this.themeCouleur = themeCouleur; }

	public String getSaison() { return saison; }
	public void setSaison(String saison) { this.saison = saison; }

	public double getBudgetTotal() { return budgetTotal; }
	public void setBudgetTotal(double v) { this.budgetTotal = v; }

	public String getBudgetFlexibility() { return budgetFlexibility; }
	public void setBudgetFlexibility(String v) { this.budgetFlexibility = v; }

	public double getBudgetPerGuest() { return budgetPerGuest; }
	public void setBudgetPerGuest(double v) { this.budgetPerGuest = v; }

	public String getBudgetTier() { return budgetTier; }
	public void setBudgetTier(String v) { this.budgetTier = v; }

	public int getNbInvites() { return nbInvites; }
	public void setNbInvites(int v) { this.nbInvites = v; }

	public String getGuestSize() { return guestSize; }
	public void setGuestSize(String v) { this.guestSize = v; }

	public String getTypeMusique() { return typeMusique; }
	public void setTypeMusique(String v) { this.typeMusique = v; }

	public String getTypeCuisine() { return typeCuisine; }
	public void setTypeCuisine(String v) { this.typeCuisine = v; }

	public String getPrefPhoto() { return prefPhoto; }
	public void setPrefPhoto(String v) { this.prefPhoto = v; }

	public String getPrefDecoration() { return prefDecoration; }
	public void setPrefDecoration(String v) { this.prefDecoration = v; }

	public String getStyleNeggafa() { return styleNeggafa; }
	public void setStyleNeggafa(String v) { this.styleNeggafa = v; }

	public int getNbTenuesNeggafa() { return nbTenuesNeggafa; }
	public void setNbTenuesNeggafa(int v) { this.nbTenuesNeggafa = v; }

	public int getPrioriteSalle() { return prioriteSalle; }
	public void setPrioriteSalle(int v) { this.prioriteSalle = v; }
	public int getPrioriteTraiteur() { return prioriteTraiteur; }
	public void setPrioriteTraiteur(int v) { this.prioriteTraiteur = v; }
	public int getPrioritePhoto() { return prioritePhoto; }
	public void setPrioritePhoto(int v) { this.prioritePhoto = v; }
	public int getPrioriteMusique() { return prioriteMusique; }
	public void setPrioriteMusique(int v) { this.prioriteMusique = v; }
	public int getPrioriteDecoration() { return prioriteDecoration; }
	public void setPrioriteDecoration(int v) { this.prioriteDecoration = v; }
	public int getPrioriteNeggafa() { return prioriteNeggafa; }
	public void setPrioriteNeggafa(int v) { this.prioriteNeggafa = v; }
	public int getPrioriteMakeup() { return prioriteMakeup; }
	public void setPrioriteMakeup(int v) { this.prioriteMakeup = v; }

	public List<Integer> getTopCategoryIds() { return topCategoryIds; }
	public void setTopCategoryIds(List<Integer> v) { this.topCategoryIds = v; }

	public String getPostesEconomie() { return postesEconomie; }
	public void setPostesEconomie(String v) { this.postesEconomie = v; }

	public List<String> getMoodKeywords() { return moodKeywords; }
	public void setMoodKeywords(List<String> v) { this.moodKeywords = v; }

	public List<String> getRequestedServices() { return requestedServices; }
	public void setRequestedServices(List<String> v) { this.requestedServices = v; }

	public String getUserCity() { return userCity; }
	public void setUserCity(String v) { this.userCity = v; }

	public int getCityTolerance() { return cityTolerance; }
	public void setCityTolerance(int v) { this.cityTolerance = v; }
}
