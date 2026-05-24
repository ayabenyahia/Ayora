package com.ayora.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Profil utilisateur deduit du questionnaire.
 *
 * Sert d'abstraction propre entre QuestionnaireAnswer (donnees brutes du form)
 * et le moteur de recommandation. AyoraMetier construit ce profil une seule fois
 * via metier.buildUserProfile(answer) et l'utilise pour scorer chaque vendor.
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

	// Mariage mixte/separe/hybride
	private String mixiteMariage;
	// Langue d'echange avec les prestataires
	private String languePrestataires;
	// Liste des evenements (MARIAGE, FIANCAILLES, HENNE, LENDEMAIN, MELHA)
	private List<String> evenements;
	// Sensibilite halal pour le traiteur
	private String halalStrict;
	// Priorite a la communaute fassia
	private String prioriteFassia;

	// === Type de lieu de ceremonie (questionnaire section 3) ===================
	// Vraie nature du lieu choisi par la mariee. Critique pour le scoring des SALLES :
	// RIAD/PALAIS doit privilegier les riads et palais historiques en medina ;
	// SALLE doit privilegier les salles de fete modernes ; JARDIN les espaces
	// exterieurs avec verdure ; PISCINE les hotels/resorts avec piscine ; HOTEL
	// les resorts complets ; MIXTE les espaces polyvalents (riad + jardin).
	private String lieuType;           // RIAD / SALLE / JARDIN / PISCINE / HOTEL / DOMICILE / MIXTE

	// === Restauration & gastronomie (questionnaire section 6) =================
	// Servent au scoring des traiteurs : style culinaire vs tags du vendor,
	// restrictions alimentaires comme filtre/penalite, format de service
	// (buffet, table, cocktail...), boissons (sans alcool / open bar...),
	// nombre de plats au menu, et politique patisserie marocaine.
	private String styleCulinaire;     // MAROC_TRADI / MAROC_RAFFINE / FUSION / INTERNATIONAL / MEDITERRANEEN / LIBANAIS
	private int nbPlats;               // 3 / 5 / 7 / 9
	private String formatService;      // SERVICE_TABLE / BUFFET / COCKTAIL_DINATOIRE / MIXTE / STATIONS
	private String formatBar;          // SANS_ALCOOL / MOCKTAILS / VIN_CHAMPAGNE / OPEN_BAR
	private List<String> restrictionsAlimentaires;
	private int nbConvivesRestrictions;
	private String patisserieMaroc;    // INCLUS_GENEREUX / INCLUS_STANDARD / MIXTE / NON

	// === Profil invites & dynamique de la fete ================================
	// Le moteur ponderera : style photo selon stylePhoto, vendor musique selon
	// energieFete/volumeSonore, accessibilite/confort selon trancheAge et
	// considerationsSpeciales, presence d'animations pour enfants selon nbEnfants.
	private int pctInvitesLocaux;      // 0..100 (% invites de Fes)
	private int pctInvitesIntl;        // 0..100 (% invites internationaux)
	private String trancheAge;         // JEUNE / MIXTE / MATURE / SENIOR
	private int nbEnfants;
	private int energieFete = 3;       // 1..5 (1 = posee, 5 = festive)
	private int volumeSonore = 3;      // 1..5
	private String dureeEvenement;     // COURTE / STANDARD / LONGUE / NUIT_COMPLETE
	private String stylePhoto;         // DOCUMENTAIRE / EDITORIAL / CINEMA / TRADITIONNEL / MIXTE
	private List<String> considerationsSpeciales;

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

	public String getMixiteMariage() { return mixiteMariage; }
	public void setMixiteMariage(String v) { this.mixiteMariage = v; }

	public String getLanguePrestataires() { return languePrestataires; }
	public void setLanguePrestataires(String v) { this.languePrestataires = v; }

	public String getLieuType() { return lieuType; }
	public void setLieuType(String v) { this.lieuType = v; }

	public List<String> getEvenements() { return evenements; }
	public void setEvenements(List<String> v) { this.evenements = v; }

	public String getHalalStrict() { return halalStrict; }
	public void setHalalStrict(String v) { this.halalStrict = v; }

	public String getPrioriteFassia() { return prioriteFassia; }
	public void setPrioriteFassia(String v) { this.prioriteFassia = v; }

	// Restauration & gastronomie
	public String getStyleCulinaire() { return styleCulinaire; }
	public void setStyleCulinaire(String v) { this.styleCulinaire = v; }
	public int getNbPlats() { return nbPlats; }
	public void setNbPlats(int v) { this.nbPlats = v; }
	public String getFormatService() { return formatService; }
	public void setFormatService(String v) { this.formatService = v; }
	public String getFormatBar() { return formatBar; }
	public void setFormatBar(String v) { this.formatBar = v; }
	public List<String> getRestrictionsAlimentaires() { return restrictionsAlimentaires; }
	public void setRestrictionsAlimentaires(List<String> v) { this.restrictionsAlimentaires = v; }
	public int getNbConvivesRestrictions() { return nbConvivesRestrictions; }
	public void setNbConvivesRestrictions(int v) { this.nbConvivesRestrictions = v; }
	public String getPatisserieMaroc() { return patisserieMaroc; }
	public void setPatisserieMaroc(String v) { this.patisserieMaroc = v; }

	// Profil invites & dynamique
	public int getPctInvitesLocaux() { return pctInvitesLocaux; }
	public void setPctInvitesLocaux(int v) { this.pctInvitesLocaux = v; }
	public int getPctInvitesIntl() { return pctInvitesIntl; }
	public void setPctInvitesIntl(int v) { this.pctInvitesIntl = v; }
	public String getTrancheAge() { return trancheAge; }
	public void setTrancheAge(String v) { this.trancheAge = v; }
	public int getNbEnfants() { return nbEnfants; }
	public void setNbEnfants(int v) { this.nbEnfants = v; }
	public int getEnergieFete() { return energieFete; }
	public void setEnergieFete(int v) { this.energieFete = v; }
	public int getVolumeSonore() { return volumeSonore; }
	public void setVolumeSonore(int v) { this.volumeSonore = v; }
	public String getDureeEvenement() { return dureeEvenement; }
	public void setDureeEvenement(String v) { this.dureeEvenement = v; }
	public String getStylePhoto() { return stylePhoto; }
	public void setStylePhoto(String v) { this.stylePhoto = v; }
	public List<String> getConsiderationsSpeciales() { return considerationsSpeciales; }
	public void setConsiderationsSpeciales(List<String> v) { this.considerationsSpeciales = v; }
}
