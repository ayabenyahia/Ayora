package com.ayora.service;

import java.util.List;
import java.util.Vector;
import com.ayora.dao.RecommendationDao;
import com.ayora.dao.VendorDao;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.model.Vendor;

/**
 * Service de recommandation IA - Scoring multi-facteurs
 * Inspire de la logique metier du cours (pattern Service Layer)
 */
public class RecommendationService {

	private VendorDao vendorDao;
	private RecommendationDao recommendationDao;

	public RecommendationService() {
		vendorDao = new VendorDao();
		recommendationDao = new RecommendationDao();
	}

	public List<Recommendation> generateRecommendations(int userId, QuestionnaireAnswer answers) {
		// Supprimer les anciennes recommandations
		recommendationDao.deleteByUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		List<Recommendation> recommendations = new Vector<Recommendation>();

		for (int i = 0; i < allVendors.size(); i++) {
			Vendor vendor = allVendors.get(i);
			double score = calculateScore(vendor, answers);
			String raison = generateRaison(vendor, answers, score);

			if (score > 0) {
				Recommendation rec = new Recommendation(userId, vendor.getId(), score, raison);
				recommendationDao.create(rec);
				rec.setVendorName(vendor.getName());
				rec.setVendorCategory(vendor.getCategoryName());
				rec.setVendorGamme(vendor.getGamme());
				rec.setVendorPrixMin(vendor.getPrixMin());
				recommendations.add(rec);
			}
		}

		// Trier par score decroissant (tri par selection - pattern du prof)
		sortByScore(recommendations);
		return recommendations;
	}

	/**
	 * Calcul du score multi-facteurs ameliore
	 * Budget (30%) + Gamme (20%) + Style/Tags (20%) + Preferences specifiques (20%) + Priorite (10%)
	 */
	private double calculateScore(Vendor vendor, QuestionnaireAnswer answers) {
		double score = 0;
		double maxScore = 100;

		// 1. Score BUDGET (30 points max) - inspire de la logique budgetaire du cours
		score += calculateBudgetScore(vendor, answers) * 30;

		// 2. Score GAMME / LUXE (20 points max)
		score += calculateGammeScore(vendor, answers) * 20;

		// 3. Score STYLE / TAGS (20 points max)
		score += calculateStyleScore(vendor, answers) * 20;

		// 4. Score PREFERENCES SPECIFIQUES (20 points max) - nouveau
		score += calculatePreferenceScore(vendor, answers) * 20;

		// 5. Score PRIORITE categorie (10 points max)
		score += calculatePriorityScore(vendor, answers) * 10;

		// Bonus note/avis (jusqu'a +5 points)
		if (vendor.getRating() >= 4.5 && vendor.getNbAvis() > 50) {
			score += 5;
		} else if (vendor.getRating() >= 4.0 && vendor.getNbAvis() > 20) {
			score += 3;
		}

		// Normaliser sur 100
		return Math.min(Math.round(score * 10.0) / 10.0, maxScore);
	}

	private double calculateBudgetScore(Vendor vendor, QuestionnaireAnswer answers) {
		double budgetTotal = answers.getBudgetTotal();
		if (budgetTotal <= 0) {
			return 0.5;
		}

		// Estimation du budget par categorie (pourcentage du total)
		double categoryBudget = estimateCategoryBudget(vendor.getCategoryId(), budgetTotal, answers);

		if (vendor.getPrixMin() <= categoryBudget) {
			if (vendor.getPrixMax() > 0 && vendor.getPrixMax() <= categoryBudget * 1.2) {
				return 1.0; // Prix dans la fourchette ideale
			}
			return 0.85;
		}

		// Depassement tolere selon flexibilite
		String flexibility = answers.getBudgetFlexibility();
		double tolerance = 0.1;
		if ("FLEXIBLE".equals(flexibility)) {
			tolerance = 0.3;
		} else if ("TRES_FLEXIBLE".equals(flexibility)) {
			tolerance = 0.5;
		}

		if (vendor.getPrixMin() <= categoryBudget * (1 + tolerance)) {
			return 0.5;
		}

		// Penalite proportionnelle au depassement
		double ratio = categoryBudget / vendor.getPrixMin();
		return Math.max(ratio * 0.3, 0.05);
	}

	private double estimateCategoryBudget(int categoryId, double budgetTotal, QuestionnaireAnswer answers) {
		// Repartition type du budget mariage marocain a Fes
		// Ajustee selon les priorites utilisateur pour une recommandation plus pertinente
		double basePct;
		switch (categoryId) {
			case 11: basePct = 0.25; break; // SALLE
			case 12: // TRAITEUR (prix par personne)
				return budgetTotal * 0.25 / Math.max(1, answers.getNbInvites());
			case 1:  basePct = 0.10; break; // NEGGAFA
			case 4:  basePct = 0.05; break; // PHOTOGRAPHE
			case 5:  basePct = 0.04; break; // VIDEASTE
			case 7:  basePct = 0.04; break; // ISSAWA
			case 8:  basePct = 0.05; break; // ORCHESTRE
			case 9:  basePct = 0.07; break; // DECORATION
			case 10: basePct = 0.03; break; // FLEURISTE
			case 2:  basePct = 0.03; break; // MAKEUP
			case 3:  basePct = 0.02; break; // COIFFURE
			case 6:  basePct = 0.03; break; // CAKE
			case 13: basePct = 0.03; break; // MYADI
			case 14: basePct = 0.04; break; // DJ
			case 15: basePct = 0.02; break; // TRANSPORT
			case 16: basePct = 0.01; break; // HENNAYA
			case 17: basePct = 0.08; break; // WEDDING PLANNER
			default: basePct = 0.05; break;
		}

		// Ajuster selon la priorite utilisateur (si haute priorite, allouer plus)
		int priority = getPriorityForCategory(categoryId, answers);
		double adjustFactor = 1.0 + (priority - 3) * 0.15; // +/- 15% par point de priorite
		return budgetTotal * basePct * adjustFactor;
	}

	private double calculateGammeScore(Vendor vendor, QuestionnaireAnswer answers) {
		String niveauLuxe = answers.getNiveauLuxe();
		String vendorGamme = vendor.getGamme();

		if (niveauLuxe == null || vendorGamme == null) {
			return 0.5;
		}

		// Correspondance exacte
		if ("ECONOMIQUE".equals(niveauLuxe) && "ECONOMIQUE".equals(vendorGamme)) return 1.0;
		if ("MOYEN".equals(niveauLuxe) && "MOYEN".equals(vendorGamme)) return 1.0;
		if ("PREMIUM".equals(niveauLuxe) && "PREMIUM".equals(vendorGamme)) return 1.0;
		if ("ULTRA_LUXE".equals(niveauLuxe) && "PREMIUM".equals(vendorGamme)) return 1.0;

		// Correspondance proche
		if ("MOYEN".equals(niveauLuxe) && "ECONOMIQUE".equals(vendorGamme)) return 0.6;
		if ("MOYEN".equals(niveauLuxe) && "PREMIUM".equals(vendorGamme)) return 0.6;
		if ("ECONOMIQUE".equals(niveauLuxe) && "MOYEN".equals(vendorGamme)) return 0.4;
		if ("PREMIUM".equals(niveauLuxe) && "MOYEN".equals(vendorGamme)) return 0.5;
		if ("ULTRA_LUXE".equals(niveauLuxe) && "MOYEN".equals(vendorGamme)) return 0.3;

		return 0.2;
	}

	private double calculateStyleScore(Vendor vendor, QuestionnaireAnswer answers) {
		String tags = vendor.getTags();
		if (tags == null) {
			return 0.5;
		}
		tags = tags.toLowerCase();

		int matches = 0;
		int checks = 0;

		// Verifier style mariage
		String style = answers.getStyleMariage();
		if (style != null) {
			checks++;
			if ("TRADITIONNEL".equals(style) && (tags.contains("traditionnel") || tags.contains("fassi") || tags.contains("authentique"))) matches++;
			else if ("MODERNE".equals(style) && (tags.contains("moderne") || tags.contains("contemporain") || tags.contains("tendance"))) matches++;
			else if ("MIXTE".equals(style) && (tags.contains("mixte") || tags.contains("moderne") || tags.contains("traditionnel"))) matches++;
			else if ("LUXE".equals(style) && (tags.contains("luxe") || tags.contains("premium") || tags.contains("haut-gamme") || tags.contains("prestige"))) matches++;
			else if ("SIMPLE".equals(style) && (tags.contains("simple") || tags.contains("abordable") || tags.contains("economique") || tags.contains("essentiel"))) matches++;
		}

		// Verifier ambiance
		String ambiance = answers.getAmbiance();
		if (ambiance != null) {
			checks++;
			if ("INTIME".equals(ambiance) && (tags.contains("intime") || tags.contains("petit") || tags.contains("charme"))) matches++;
			else if ("GRANDIOSE".equals(ambiance) && (tags.contains("grand") || tags.contains("luxe") || tags.contains("complet") || tags.contains("royal"))) matches++;
			else if ("FESTIVE".equals(ambiance) && (tags.contains("festif") || tags.contains("dynamique") || tags.contains("ambiance") || tags.contains("electrique"))) matches++;
			else if ("ROMANTIQUE".equals(ambiance) && (tags.contains("romantique") || tags.contains("delicat") || tags.contains("doux") || tags.contains("lumineux"))) matches++;
			else if ("FAMILIALE".equals(ambiance) && (tags.contains("familial") || tags.contains("convivial") || tags.contains("genereux"))) matches++;
		}

		if (checks == 0) {
			return 0.5;
		}
		double score = (double) matches / checks;
		return Math.max(score, 0.2);
	}

	/**
	 * Nouveau : Score de preferences specifiques
	 * Analyse les choix detailles (musique, cuisine, photo, decoration, neggafa)
	 */
	private double calculatePreferenceScore(Vendor vendor, QuestionnaireAnswer answers) {
		String tags = vendor.getTags();
		if (tags == null) return 0.5;
		tags = tags.toLowerCase();
		int categoryId = vendor.getCategoryId();

		// Musique : ORCHESTRE (8), ISSAWA (7), DJ (14)
		if (categoryId == 8 || categoryId == 7 || categoryId == 14) {
			return matchMusicPreference(tags, categoryId, answers);
		}

		// Traiteur (12)
		if (categoryId == 12) {
			return matchCuisinePreference(tags, answers);
		}

		// Photographe (4) / Videaste (5)
		if (categoryId == 4 || categoryId == 5) {
			return matchPhotoPreference(tags, answers);
		}

		// Decoration (9) / Fleuriste (10)
		if (categoryId == 9 || categoryId == 10) {
			return matchDecorationPreference(tags, answers);
		}

		// Neggafa (1) / Myadi (13)
		if (categoryId == 1 || categoryId == 13) {
			return matchNeggafaPreference(tags, answers);
		}

		// Makeup (2) / Coiffure (3)
		if (categoryId == 2 || categoryId == 3) {
			// Verifier le style (moderne vs traditionnel)
			String style = answers.getStyleMariage();
			if (style != null) {
				if ("TRADITIONNEL".equals(style) && (tags.contains("oriental") || tags.contains("traditionnel"))) return 1.0;
				if ("MODERNE".equals(style) && (tags.contains("moderne") || tags.contains("tendance"))) return 1.0;
				if ("MIXTE".equals(style)) return 0.8;
			}
			return 0.5;
		}

		return 0.5; // Score neutre pour les categories sans preference specifique
	}

	private double matchMusicPreference(String tags, int categoryId, QuestionnaireAnswer answers) {
		String typeMusique = answers.getTypeMusique();
		if (typeMusique == null) return 0.5;

		// Si l'utilisateur veut un orchestre et c'est un orchestre
		if ("ORCHESTRE".equals(typeMusique) && categoryId == 8) return 1.0;
		if ("ISSAWA".equals(typeMusique) && categoryId == 7) return 1.0;
		if ("DJ".equals(typeMusique) && categoryId == 14) return 1.0;

		// TRADITIONNELLE = preference pour Issawa + Orchestre chaabi/andalous
		if ("TRADITIONNELLE".equals(typeMusique)) {
			if (categoryId == 7) return 0.9;
			if (categoryId == 8 && (tags.contains("chaabi") || tags.contains("andalous"))) return 0.85;
			if (categoryId == 14) return 0.3;
		}

		// MODERNE = DJ ou orchestre moderne
		if ("MODERNE".equals(typeMusique)) {
			if (categoryId == 14) return 0.9;
			if (categoryId == 8 && tags.contains("moderne")) return 0.7;
			if (categoryId == 7) return 0.3;
		}

		// MIXTE = tout est pertinent
		if ("MIXTE".equals(typeMusique)) return 0.8;

		return 0.5;
	}

	private double matchCuisinePreference(String tags, QuestionnaireAnswer answers) {
		String typeCuisine = answers.getTypeCuisine();
		if (typeCuisine == null) return 0.5;

		if ("MAROCAINE".equals(typeCuisine) && (tags.contains("marocain") || tags.contains("fassi") || tags.contains("authentique") || tags.contains("tajine"))) return 1.0;
		if ("INTERNATIONALE".equals(typeCuisine) && (tags.contains("international") || tags.contains("moderne") || tags.contains("creatif"))) return 1.0;
		if ("MIXTE".equals(typeCuisine)) return 0.8;

		return 0.5;
	}

	private double matchPhotoPreference(String tags, QuestionnaireAnswer answers) {
		String prefPhoto = answers.getPrefPhoto();
		if (prefPhoto == null) return 0.5;

		if ("CLASSIQUE".equals(prefPhoto) && (tags.contains("classique") || tags.contains("studio"))) return 1.0;
		if ("ARTISTIQUE".equals(prefPhoto) && (tags.contains("artistique") || tags.contains("creatif") || tags.contains("editorial"))) return 1.0;
		if ("REPORTAGE".equals(prefPhoto) && (tags.contains("reportage") || tags.contains("naturel") || tags.contains("emotionnel"))) return 1.0;
		if ("DRONE".equals(prefPhoto) && tags.contains("drone")) return 1.0;

		return 0.4;
	}

	private double matchDecorationPreference(String tags, QuestionnaireAnswer answers) {
		String prefDeco = answers.getPrefDecoration();
		if (prefDeco == null) return 0.5;

		if ("TRADITIONNELLE".equals(prefDeco) && (tags.contains("traditionnel") || tags.contains("oriental"))) return 1.0;
		if ("MODERNE".equals(prefDeco) && (tags.contains("moderne") || tags.contains("elegant") || tags.contains("contemporain"))) return 1.0;
		if ("FLORALE".equals(prefDeco) && (tags.contains("floral") || tags.contains("roses") || tags.contains("pivoines") || tags.contains("frais"))) return 1.0;
		if ("MINIMALISTE".equals(prefDeco) && (tags.contains("minimaliste") || tags.contains("simple") || tags.contains("abordable"))) return 1.0;
		if ("LUXUEUSE".equals(prefDeco) && (tags.contains("luxe") || tags.contains("haut-gamme") || tags.contains("sur-mesure") || tags.contains("premium"))) return 1.0;

		return 0.4;
	}

	private double matchNeggafaPreference(String tags, QuestionnaireAnswer answers) {
		String styleNeggafa = answers.getStyleNeggafa();
		if (styleNeggafa == null) return 0.5;

		if ("TRADITIONNEL".equals(styleNeggafa) && (tags.contains("traditionnel") || tags.contains("fassi") || tags.contains("authentique") || tags.contains("heritage"))) return 1.0;
		if ("MODERNE".equals(styleNeggafa) && (tags.contains("moderne") || tags.contains("contemporain") || tags.contains("chic"))) return 1.0;
		if ("MIXTE".equals(styleNeggafa)) return 0.8;

		// Bonus pour le nombre de tenues
		int nbTenues = answers.getNbTenuesNeggafa();
		if (nbTenues >= 4 && (tags.contains("showroom") || tags.contains("choix") || tags.contains("exclusif"))) return 0.9;

		return 0.5;
	}

	private double calculatePriorityScore(Vendor vendor, QuestionnaireAnswer answers) {
		int priority = getPriorityForCategory(vendor.getCategoryId(), answers);
		return priority / 5.0;
	}

	private int getPriorityForCategory(int categoryId, QuestionnaireAnswer answers) {
		switch (categoryId) {
			case 11: return answers.getPrioriteSalle();
			case 12: return answers.getPrioriteTraiteur();
			case 4: case 5: return answers.getPrioritePhoto();
			case 7: case 8: case 14: return answers.getPrioriteMusique();
			case 9: case 10: return answers.getPrioriteDecoration();
			case 1: case 13: return answers.getPrioriteNeggafa();
			case 2: case 3: case 16: return answers.getPrioriteMakeup();
			default: return 3;
		}
	}

	/**
	 * Generation de raison amelioree - plus detaillee et contextualisee
	 */
	private String generateRaison(Vendor vendor, QuestionnaireAnswer answers, double score) {
		StringBuilder raison = new StringBuilder();

		// Qualificatif principal
		if (score >= 85) {
			raison.append("Correspondance ideale");
		} else if (score >= 70) {
			raison.append("Excellente correspondance");
		} else if (score >= 55) {
			raison.append("Bonne correspondance");
		} else if (score >= 40) {
			raison.append("Correspondance correcte");
		} else {
			raison.append("Option envisageable");
		}

		// Detail gamme
		String gamme = vendor.getGamme();
		if (gamme != null) {
			if ("ECONOMIQUE".equals(gamme)) {
				raison.append(" - Prix accessible");
			} else if ("PREMIUM".equals(gamme)) {
				raison.append(" - Service premium");
			} else {
				raison.append(" - Bon rapport qualite-prix");
			}
		}

		// Detail specifique selon la categorie
		int catId = vendor.getCategoryId();
		String tags = vendor.getTags() != null ? vendor.getTags().toLowerCase() : "";

		if (catId == 8 || catId == 7 || catId == 14) { // Musique
			if (tags.contains("chaabi")) raison.append(". Style chaabi");
			else if (tags.contains("moderne")) raison.append(". Style moderne");
			else if (tags.contains("andalous")) raison.append(". Style andalous");
		} else if (catId == 1) { // Neggafa
			if (tags.contains("fassi")) raison.append(". Tradition fassie");
			else if (tags.contains("moderne")) raison.append(". Style moderne");
		} else if (catId == 4 || catId == 5) { // Photo/Video
			if (tags.contains("drone")) raison.append(". Couverture drone incluse");
			else if (tags.contains("artistique")) raison.append(". Style artistique");
		}

		// Note de reputation
		if (vendor.getRating() >= 4.8 && vendor.getNbAvis() > 100) {
			raison.append(". Tres bien note (" + vendor.getRating() + "/5)");
		}

		return raison.toString();
	}

	/**
	 * Tri par selection (pattern du prof - algorithme de tri simple)
	 */
	private void sortByScore(List<Recommendation> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			int maxIndex = i;
			for (int j = i + 1; j < list.size(); j++) {
				if (list.get(j).getScore() > list.get(maxIndex).getScore()) {
					maxIndex = j;
				}
			}
			if (maxIndex != i) {
				Recommendation temp = list.get(i);
				list.set(i, list.get(maxIndex));
				list.set(maxIndex, temp);
			}
		}
	}
}
