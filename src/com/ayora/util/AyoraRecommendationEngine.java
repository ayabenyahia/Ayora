package com.ayora.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.model.UserProfile;
import com.ayora.model.Vendor;

/**
 * AYORA RECOMMENDATION ENGINE - IA locale et explicable.
 * =====================================================
 *
 * Approche : k-NN pondere par dimensions (k = 3 par categorie).
 * - Pas de modele entraine, pas d'API externe, pas de cle d'API.
 * - 100% Java pur, deterministe et explicable a la main.
 *
 * FORMULE DU SCORE FINAL (sur 100) :
 *   scoreFinal = budgetScore * 0.30
 *              + styleScore  * 0.25
 *              + cityScore   * 0.15
 *              + guestScore  * 0.15
 *              + luxuryScore * 0.10
 *              + qualityScore* 0.05
 */
public class AyoraRecommendationEngine {

	// === Poids des dimensions (somme = 1.0 = 100%) =====================
	public static final double WEIGHT_BUDGET  = 0.30;
	public static final double WEIGHT_STYLE   = 0.25;
	public static final double WEIGHT_CITY    = 0.15;
	public static final double WEIGHT_GUESTS  = 0.15;
	public static final double WEIGHT_LUXURY  = 0.10;
	public static final double WEIGHT_QUALITY = 0.05;

	// k-NN : nombre de voisins (= prestataires) gardes par categorie
	public static final int TOP_K_PER_CATEGORY = 3;

	// Categories ID (alignees avec la table vendor_categories)
	public static final int CAT_NEGGAFA    = 1;
	public static final int CAT_MAKEUP     = 2;
	public static final int CAT_PHOTO      = 4;
	public static final int CAT_CAKE       = 6;
	public static final int CAT_ISSAWA     = 7;
	public static final int CAT_ORCHESTRE  = 8;
	public static final int CAT_DECORATION = 9;
	public static final int CAT_SALLE      = 11;
	public static final int CAT_TRAITEUR   = 12;
	public static final int CAT_MYADI      = 13;
	public static final int CAT_DJ         = 14;
	public static final int CAT_HENNAYA    = 16;

	// Ville par defaut : projet centre sur Fes
	public static final String DEFAULT_CITY = "Fes";

	// ============================================================
	// 1. CONSTRUIRE LE PROFIL UTILISATEUR
	// ============================================================
	public UserProfile buildUserProfile(QuestionnaireAnswer a) {
		UserProfile p = new UserProfile();
		if (a == null) return p;

		// Identite du mariage
		p.setStyle(a.getStyleMariage());
		p.setAmbiance(a.getAmbiance());
		p.setNiveauLuxe(a.getNiveauLuxe());
		p.setThemeCouleur(a.getThemeCouleur());
		p.setSaison(a.getSaisonPreferee());

		// Budget
		p.setBudgetTotal(a.getBudgetTotal());
		p.setBudgetFlexibility(a.getBudgetFlexibility());
		p.setNbInvites(a.getNbInvites());
		if (a.getNbInvites() > 0 && a.getBudgetTotal() > 0) {
			p.setBudgetPerGuest(a.getBudgetTotal() / a.getNbInvites());
		}
		p.setBudgetTier(deriveBudgetTier(p.getBudgetPerGuest()));

		// Invites
		p.setGuestSize(deriveGuestSize(a.getNbInvites()));

		// Preferences metier
		p.setTypeMusique(a.getTypeMusique());
		p.setTypeCuisine(a.getTypeCuisine());
		p.setPrefPhoto(a.getPrefPhoto());
		p.setPrefDecoration(a.getPrefDecoration());
		p.setStyleNeggafa(a.getStyleNeggafa());
		p.setNbTenuesNeggafa(a.getNbTenuesNeggafa());

		// Priorites (1..5)
		p.setPrioriteSalle(a.getPrioriteSalle());
		p.setPrioriteTraiteur(a.getPrioriteTraiteur());
		p.setPrioritePhoto(a.getPrioritePhoto());
		p.setPrioriteMusique(a.getPrioriteMusique());
		p.setPrioriteDecoration(a.getPrioriteDecoration());
		p.setPrioriteNeggafa(a.getPrioriteNeggafa());
		p.setPrioriteMakeup(a.getPrioriteMakeup());

		p.setTopCategoryIds(rankCategoriesByPriority(a));
		p.setPostesEconomie(a.getPostesEconomie());

		// Mots-cles emotionnels (depuis JSON notesSpeciales si present)
		p.setMoodKeywords(extractMoodKeywords(a.getNotesSpeciales()));

		// === Preferences enrichies (notesSpeciales JSON) ====================
		// Fusionne services (section 4) + animations (orchestre/issawa/dj)
		// pour avoir l'ensemble des categories effectivement demandees.
		String notes = a.getNotesSpeciales();
		List<String> svcAll = new ArrayList<String>();
		List<String> svc = extractStringArray(notes, "services");
		if (svc != null) svcAll.addAll(svc);
		List<String> anim = extractStringArray(notes, "animations");
		if (anim != null) {
			for (int i = 0; i < anim.size(); i++) {
				String a2 = anim.get(i);
				if (a2 != null && !"AUCUN".equalsIgnoreCase(a2) && !svcAll.contains(a2)) svcAll.add(a2);
			}
		}
		p.setRequestedServices(svcAll);
		p.setEvenements(extractStringArray(notes, "evenements"));
		p.setUserCity(extractStringField(notes, "villeMariage"));
		p.setMixiteMariage(extractStringField(notes, "mixiteMariage"));
		p.setLanguePrestataires(extractStringField(notes, "languePrestataires"));
		p.setHalalStrict(extractStringField(notes, "halalStrict"));
		p.setPrioriteFassia(extractStringField(notes, "prioriteFassia"));
		int tol = extractIntField(notes, "cityTolerance", 2);
		if (tol < 1) tol = 1;
		if (tol > 5) tol = 5;
		p.setCityTolerance(tol);

		// === Restauration & gastronomie (nouveaux signaux IA) =================
		p.setStyleCulinaire(extractStringField(notes, "styleCulinaire"));
		p.setNbPlats(extractIntField(notes, "nbPlats", 5));
		p.setFormatService(extractStringField(notes, "formatService"));
		p.setFormatBar(extractStringField(notes, "formatBar"));
		p.setRestrictionsAlimentaires(extractStringArray(notes, "restrictionsAlimentaires"));
		p.setNbConvivesRestrictions(extractIntField(notes, "nbConvivesRestrictions", 0));
		p.setPatisserieMaroc(extractStringField(notes, "patisserieMaroc"));

		// === Profil invites & dynamique (nouveaux signaux IA) =================
		p.setPctInvitesLocaux(extractIntField(notes, "pctInvitesLocaux", 70));
		p.setPctInvitesIntl(extractIntField(notes, "pctInvitesIntl", 10));
		p.setTrancheAge(extractStringField(notes, "trancheAge"));
		p.setNbEnfants(extractIntField(notes, "nbEnfants", 0));
		int energie = extractIntField(notes, "energieFete", 3);
		if (energie < 1) energie = 1; if (energie > 5) energie = 5;
		p.setEnergieFete(energie);
		int volume = extractIntField(notes, "volumeSonore", 3);
		if (volume < 1) volume = 1; if (volume > 5) volume = 5;
		p.setVolumeSonore(volume);
		p.setDureeEvenement(extractStringField(notes, "dureeEvenement"));
		p.setStylePhoto(extractStringField(notes, "stylePhoto"));
		p.setConsiderationsSpeciales(extractStringArray(notes, "considerationsSpeciales"));

		// === Type de lieu de la ceremonie (RIAD / SALLE / JARDIN / ...) ====
		// Cle pour scorer les salles : on stocke la valeur dans le profil
		// pour que scoreCategorySpecific puisse la lire.
		String lieu = a.getLieuCeremonie();
		p.setLieuType(lieu);

		// Cas particulier : si la ceremonie est "DOMICILE" (a la maison), on
		// retire automatiquement la categorie SALLE des services demandes,
		// quoi qu'ait coche l'utilisateur en section 4.
		if ("DOMICILE".equalsIgnoreCase(lieu)) {
			List<String> currentSvc = p.getRequestedServices();
			if (currentSvc != null) {
				List<String> filtered = new ArrayList<String>();
				for (int i = 0; i < currentSvc.size(); i++) {
					if (!"SALLE".equalsIgnoreCase(currentSvc.get(i))) filtered.add(currentSvc.get(i));
				}
				p.setRequestedServices(filtered);
			}
		}

		return p;
	}

	private String deriveBudgetTier(double budgetPerGuest) {
		if (budgetPerGuest <= 0) return "CONFORTABLE";
		if (budgetPerGuest < 600)  return "SERRE";
		if (budgetPerGuest < 1200) return "CONFORTABLE";
		if (budgetPerGuest < 2500) return "GENEREUX";
		return "ILLIMITE";
	}

	private String deriveGuestSize(int nbInvites) {
		if (nbInvites < 100) return "INTIME";
		if (nbInvites < 200) return "MOYEN";
		if (nbInvites < 400) return "GRAND";
		return "TRES_GRAND";
	}

	private List<Integer> rankCategoriesByPriority(QuestionnaireAnswer a) {
		// Tri par selection (algo simple, pattern du cours)
		int[][] cats = new int[][] {
				{ CAT_SALLE,      a.getPrioriteSalle() },
				{ CAT_TRAITEUR,   a.getPrioriteTraiteur() },
				{ CAT_PHOTO,      a.getPrioritePhoto() },
				{ CAT_ORCHESTRE,  a.getPrioriteMusique() },
				{ CAT_DECORATION, a.getPrioriteDecoration() },
				{ CAT_NEGGAFA,    a.getPrioriteNeggafa() },
				{ CAT_MAKEUP,     a.getPrioriteMakeup() }
		};
		for (int i = 0; i < cats.length - 1; i++) {
			int max = i;
			for (int j = i + 1; j < cats.length; j++) {
				if (cats[j][1] > cats[max][1]) max = j;
			}
			if (max != i) {
				int[] tmp = cats[i]; cats[i] = cats[max]; cats[max] = tmp;
			}
		}
		List<Integer> top = new ArrayList<Integer>();
		for (int i = 0; i < cats.length; i++) top.add(cats[i][0]);
		return top;
	}

	// ============================================================
	// HELPERS JSON (parsing leger sans dependance externe)
	// ============================================================
	private List<String> extractMoodKeywords(String notesJson) {
		List<String> out = new ArrayList<String>();
		if (notesJson == null) return out;
		String trimmed = notesJson.trim();
		if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return out;
		String[] candidates = { "motsCles", "moments", "momentsForts", "ambianceMots" };
		for (int i = 0; i < candidates.length; i++) {
			int idx = trimmed.indexOf("\"" + candidates[i] + "\"");
			if (idx < 0) continue;
			int arrStart = trimmed.indexOf("[", idx);
			int arrEnd = trimmed.indexOf("]", arrStart);
			if (arrStart < 0 || arrEnd < 0) continue;
			String[] parts = trimmed.substring(arrStart + 1, arrEnd).split(",");
			for (int j = 0; j < parts.length; j++) {
				String s = parts[j].trim().replace("\"", "");
				if (!s.isEmpty()) out.add(s.toLowerCase());
			}
		}
		return out;
	}

	/** Extrait un tableau de chaines depuis un JSON plat (regex simple). */
	private List<String> extractStringArray(String json, String key) {
		List<String> out = new ArrayList<String>();
		if (json == null) return out;
		String trimmed = json.trim();
		if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return out;
		int idx = trimmed.indexOf("\"" + key + "\"");
		if (idx < 0) return out;
		int arrStart = trimmed.indexOf("[", idx);
		int arrEnd = trimmed.indexOf("]", arrStart);
		if (arrStart < 0 || arrEnd < 0) return out;
		String[] parts = trimmed.substring(arrStart + 1, arrEnd).split(",");
		for (int j = 0; j < parts.length; j++) {
			String s = parts[j].trim().replace("\"", "");
			if (!s.isEmpty()) out.add(s);
		}
		return out;
	}

	/** Extrait une chaine depuis un JSON plat. */
	private String extractStringField(String json, String key) {
		if (json == null) return null;
		String trimmed = json.trim();
		int idx = trimmed.indexOf("\"" + key + "\"");
		if (idx < 0) return null;
		int colon = trimmed.indexOf(":", idx);
		if (colon < 0) return null;
		int q1 = trimmed.indexOf("\"", colon + 1);
		if (q1 < 0) return null;
		int q2 = trimmed.indexOf("\"", q1 + 1);
		while (q2 > 0 && trimmed.charAt(q2 - 1) == '\\') q2 = trimmed.indexOf("\"", q2 + 1);
		if (q2 < 0) return null;
		String v = trimmed.substring(q1 + 1, q2).trim();
		return v.isEmpty() ? null : v;
	}

	/** Extrait un entier depuis un JSON plat (avec valeur par defaut). */
	private int extractIntField(String json, String key, int dflt) {
		if (json == null) return dflt;
		String trimmed = json.trim();
		int idx = trimmed.indexOf("\"" + key + "\"");
		if (idx < 0) return dflt;
		int colon = trimmed.indexOf(":", idx);
		if (colon < 0) return dflt;
		int end = colon + 1;
		while (end < trimmed.length()) {
			char c = trimmed.charAt(end);
			if (c == ',' || c == '}' || c == ']') break;
			end++;
		}
		String num = trimmed.substring(colon + 1, end).trim().replace("\"", "");
		try { return Integer.parseInt(num); } catch (NumberFormatException e) { return dflt; }
	}

	// ============================================================
	// 6 SOUS-SCORES (chacun retourne une valeur dans [0, 1])
	// ============================================================

	/** budgetMatch (30%) : prix vendor vs budget alloue a sa categorie. */
	private double scoreBudget(Vendor v, UserProfile p) {
		double budget = p.getBudgetTotal();
		if (budget <= 0) return 0.6;
		double allocated = estimateCategoryBudget(v.getCategoryId(), p);
		double prixMin = v.getPrixMin();
		double prixMax = v.getPrixMax() > 0 ? v.getPrixMax() : prixMin * 1.3;
		double prixAvg = (prixMin + prixMax) / 2.0;

		if (prixMax <= allocated) return 1.0;
		if (prixAvg <= allocated) return 0.85;
		if (prixMin <= allocated) return 0.70;

		double tolerance = 0.15;
		String flex = p.getBudgetFlexibility();
		if ("FLEXIBLE".equalsIgnoreCase(flex))      tolerance = 0.30;
		else if ("TRES_FLEXIBLE".equalsIgnoreCase(flex)) tolerance = 0.50;

		if (prixMin <= allocated * (1 + tolerance))     return 0.50;
		if (prixMin <= allocated * (1 + tolerance * 2)) return 0.30;
		if (prixMin <= 0) return 0.10;
		return Math.max((allocated / prixMin) * 0.3, 0.05);
	}

	/** Repartition du budget total par categorie (pourcentages calibres). */
	private double estimateCategoryBudget(int categoryId, UserProfile p) {
		double budget = p.getBudgetTotal();
		switch (categoryId) {
			case CAT_SALLE:      return budget * 0.25;
			case CAT_TRAITEUR:   return budget * 0.30 / Math.max(1, p.getNbInvites());
			case CAT_NEGGAFA:    return budget * 0.10;
			case CAT_PHOTO:      return budget * 0.09;
			case CAT_ISSAWA:     return budget * 0.04;
			case CAT_ORCHESTRE:  return budget * 0.06;
			case CAT_DECORATION: return budget * 0.10;
			case CAT_MAKEUP:     return budget * 0.05;
			case CAT_CAKE:       return budget * 0.03;
			case CAT_MYADI:      return budget * 0.03;
			case CAT_DJ:         return budget * 0.04;
			case CAT_HENNAYA:    return budget * 0.01;
			default:             return budget * 0.05;
		}
	}

	/** styleMatch (25%) : tags du vendor vs style + ambiance + theme. */
	private double scoreStyle(Vendor v, UserProfile p) {
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		if (tags.isEmpty()) return 0.5;
		int hits = 0, checks = 0;

		String style = p.getStyle();
		if (style != null) {
			checks++;
			if (eq(style, "TRADITIONNEL") && hasAny(tags, "traditionnel", "fassi", "authentique", "heritage", "marocain")) hits++;
			else if (eq(style, "MODERNE") && hasAny(tags, "moderne", "contemporain", "tendance", "minimaliste")) hits++;
			else if (eq(style, "MIXTE") && hasAny(tags, "mixte", "moderne", "traditionnel", "fusion")) hits++;
			else if (eq(style, "LUXE") && hasAny(tags, "luxe", "premium", "haut-gamme", "prestige", "exclusif", "royal")) hits++;
			else if (eq(style, "SIMPLE") && hasAny(tags, "simple", "abordable", "economique", "essentiel")) hits++;
			else if (eq(style, "INTIME") && hasAny(tags, "intime", "petit", "charme", "boutique")) hits++;
		}
		String amb = p.getAmbiance();
		if (amb != null) {
			checks++;
			if (eq(amb, "INTIME") && hasAny(tags, "intime", "petit", "charme", "convivial")) hits++;
			else if (eq(amb, "GRANDIOSE") && hasAny(tags, "grand", "luxe", "complet", "royal", "spectaculaire")) hits++;
			else if (eq(amb, "FESTIVE") && hasAny(tags, "festif", "dynamique", "ambiance", "electrique", "danse")) hits++;
			else if (eq(amb, "ROMANTIQUE") && hasAny(tags, "romantique", "delicat", "doux", "lumineux", "fleuri")) hits++;
			else if (eq(amb, "FAMILIALE") && hasAny(tags, "familial", "convivial", "genereux", "chaleureux")) hits++;
			else if (eq(amb, "LUXUEUSE") && hasAny(tags, "luxe", "premium", "prestige", "haut-gamme")) hits++;
			else if (eq(amb, "TRADITIONNELLE") && hasAny(tags, "traditionnel", "fassi", "authentique", "heritage")) hits++;
		}
		String theme = p.getThemeCouleur() != null ? p.getThemeCouleur().toLowerCase() : "";
		if (!theme.isEmpty()) {
			checks++;
			if ((theme.contains("royal") || theme.contains("fassi"))
					&& hasAny(tags, "fassi", "royal", "andalous", "heritage", "or")) hits++;
			else if (theme.contains("oriental")
					&& hasAny(tags, "oriental", "marocain", "traditionnel", "or")) hits++;
			else if ((theme.contains("boheme") || theme.contains("boho"))
					&& hasAny(tags, "boheme", "naturel", "champetre", "fleuri")) hits++;
			else if ((theme.contains("minimal") || theme.contains("epure"))
					&& hasAny(tags, "minimaliste", "moderne", "epure", "blanc")) hits++;
			else if ((theme.contains("zellige") || theme.contains("andalou"))
					&& hasAny(tags, "zellige", "andalous", "traditionnel", "fassi")) hits++;
		}
		if (checks == 0) return 0.5;
		double base = Math.max((double) hits / checks, 0.2);

		// Bonus / malus specifique a la categorie en fonction des nouveaux signaux
		// du questionnaire (restauration, profil invites, dynamique). Borne dans
		// [0, 1] pour ne pas casser les autres ponderations.
		double adjust = scoreCategorySpecific(v, p, tags);
		double out = base + adjust;
		if (out < 0.0) out = 0.0;
		if (out > 1.0) out = 1.0;
		return out;
	}

	/**
	 * Ajustement specifique a chaque categorie de prestataire, base sur les
	 * nouveaux signaux : styleCulinaire / formatService / restrictionsAlimentaires
	 * pour les traiteurs, stylePhoto pour les photographes, energieFete et
	 * volumeSonore pour les vendeurs musique, considerationsSpeciales pour les
	 * salles. Retourne un delta dans [-0.25, +0.25] qui s'additionne au scoreStyle.
	 */
	private double scoreCategorySpecific(Vendor v, UserProfile p, String tags) {
		double delta = 0.0;
		int cat = v.getCategoryId();

		if (cat == CAT_TRAITEUR) {
			String style = p.getStyleCulinaire();
			if (style != null) {
				if (eq(style, "MAROC_TRADI") && hasAny(tags, "traditionnel", "marocain", "fassi", "tagine", "pastilla", "mechoui")) delta += 0.10;
				else if (eq(style, "MAROC_RAFFINE") && hasAny(tags, "raffine", "signature", "fassi", "gastronomique", "premium")) delta += 0.10;
				else if (eq(style, "FUSION") && hasAny(tags, "fusion", "moderne", "creatif", "innovant")) delta += 0.10;
				else if (eq(style, "INTERNATIONAL") && hasAny(tags, "international", "europeen", "fusion", "moderne")) delta += 0.10;
				else if (eq(style, "MEDITERRANEEN") && hasAny(tags, "mediterraneen", "italien", "espagnol", "grec")) delta += 0.10;
				else if (eq(style, "LIBANAIS") && hasAny(tags, "libanais", "oriental", "levantin", "mezze")) delta += 0.10;
			}
			String bar = p.getFormatBar();
			if (bar != null) {
				if (eq(bar, "OPEN_BAR") && hasAny(tags, "open-bar", "open bar", "bar", "cocktail", "alcool")) delta += 0.06;
				else if (eq(bar, "VIN_CHAMPAGNE") && hasAny(tags, "vin", "champagne", "alcool")) delta += 0.05;
				else if (eq(bar, "SANS_ALCOOL") && hasAny(tags, "halal", "sans-alcool", "famille")) delta += 0.04;
				else if (eq(bar, "MOCKTAILS") && hasAny(tags, "mocktail", "bar-sans-alcool", "premium")) delta += 0.05;
			}
			List<String> restrictions = p.getRestrictionsAlimentaires();
			if (restrictions != null && !restrictions.isEmpty() && !restrictions.contains("AUCUN")) {
				int restrHits = 0;
				if (restrictions.contains("VEGETARIEN") && hasAny(tags, "vegetarien", "vegetal", "menu-vegetarien")) restrHits++;
				if (restrictions.contains("VEGAN") && hasAny(tags, "vegan", "vegane")) restrHits++;
				if (restrictions.contains("SANS_GLUTEN") && hasAny(tags, "sans-gluten", "gluten-free")) restrHits++;
				if (restrictions.contains("SANS_LACTOSE") && hasAny(tags, "sans-lactose", "lactose-free")) restrHits++;
				if (restrictions.contains("ALLERGIE_NOIX") && hasAny(tags, "sans-noix", "allergie-noix", "allergies")) restrHits++;
				if (restrictions.contains("ALLERGIE_FRUITS_MER") && hasAny(tags, "sans-fruits-mer", "allergies")) restrHits++;
				if (restrictions.contains("DIABETIQUE") && hasAny(tags, "diabetique", "peu-sucre", "sante")) restrHits++;
				// Bonus si le vendor couvre des restrictions ; legere penalite si
				// le vendor n'affiche aucune flexibilite alors que > 3 convives.
				if (restrHits > 0) delta += Math.min(0.10, restrHits * 0.04);
				else if (p.getNbConvivesRestrictions() >= 3 && !hasAny(tags, "allergies", "menu-special", "personnalise")) delta -= 0.05;
			}
		} else if (cat == CAT_PHOTO) {
			String sp = p.getStylePhoto();
			if (sp != null) {
				if (eq(sp, "DOCUMENTAIRE") && hasAny(tags, "documentaire", "reportage", "naturel", "candide", "spontane")) delta += 0.12;
				else if (eq(sp, "EDITORIAL") && hasAny(tags, "editorial", "mode", "fashion", "pose", "studio")) delta += 0.12;
				else if (eq(sp, "CINEMA") && hasAny(tags, "cinema", "film", "cinematographique", "narratif", "video")) delta += 0.12;
				else if (eq(sp, "TRADITIONNEL") && hasAny(tags, "traditionnel", "famille", "fassi", "marocain", "classique")) delta += 0.10;
				else if (eq(sp, "MIXTE") && hasAny(tags, "mixte", "polyvalent", "complet")) delta += 0.08;
			}
		} else if (cat == CAT_ORCHESTRE || cat == CAT_DJ || cat == CAT_ISSAWA) {
			int energie = p.getEnergieFete();
			int volume = p.getVolumeSonore();
			if (energie >= 4 && hasAny(tags, "festif", "dynamique", "danse", "electrique", "club")) delta += 0.10;
			else if (energie <= 2 && hasAny(tags, "lounge", "acoustique", "doux", "intime", "calme")) delta += 0.10;
			if (volume >= 4 && hasAny(tags, "puissant", "sono", "club", "fort")) delta += 0.05;
			else if (volume <= 2 && hasAny(tags, "discret", "lounge", "ambiance", "doux")) delta += 0.05;
		} else if (cat == CAT_SALLE) {
			List<String> cs = p.getConsiderationsSpeciales();
			if (cs != null) {
				if (cs.contains("AINES_NOMBREUX") && hasAny(tags, "accessible", "plain-pied", "ascenseur", "confort")) delta += 0.08;
				if (cs.contains("MIXITE_CULTURELLE") && hasAny(tags, "international", "moderne", "mixte")) delta += 0.05;
				if (cs.contains("FAMILLE_CONSERVATRICE") && hasAny(tags, "traditionnel", "fassi", "famille", "tradi")) delta += 0.05;
			}
			if (p.getNbEnfants() >= 10 && hasAny(tags, "enfants", "kids", "espace-jeu", "familial")) delta += 0.06;

			// === TYPE DE LIEU (lieuCeremonie) : RIAD / SALLE / JARDIN / ... ===
			// Critique : la mariee a explicitement coche un type. On bonifie
			// fortement les lieux qui matchent, on penalise franchement ceux
			// qui ne correspondent pas.
			String lieuType = p.getLieuType();
			if (lieuType != null && !lieuType.isEmpty()) {
				if (eq(lieuType, "RIAD")) {
					if (hasAny(tags, "riad", "palais", "medina", "fassi", "andalous", "patio", "zellige", "authentique", "heritage", "traditionnel")) delta += 0.18;
					if (hasAny(tags, "moderne", "climatise") && !hasAny(tags, "riad", "palais", "fassi")) delta -= 0.12;
				} else if (eq(lieuType, "SALLE")) {
					if (hasAny(tags, "salle", "moderne", "climatise", "polyvalent", "spacieux")) delta += 0.15;
					if (hasAny(tags, "riad", "medina", "intime") && !hasAny(tags, "salle", "moderne")) delta -= 0.10;
				} else if (eq(lieuType, "JARDIN")) {
					if (hasAny(tags, "jardin", "exterieur", "verdure", "nature", "vue", "terrasse", "outdoor")) delta += 0.18;
					if (hasAny(tags, "interieur", "climatise") && !hasAny(tags, "jardin", "exterieur", "terrasse")) delta -= 0.10;
				} else if (eq(lieuType, "PISCINE")) {
					if (hasAny(tags, "piscine", "resort", "hotel", "bord-piscine", "exterieur", "vue")) delta += 0.18;
				} else if (eq(lieuType, "HOTEL")) {
					if (hasAny(tags, "hotel", "resort", "complexe", "logement", "suites", "international")) delta += 0.15;
				} else if (eq(lieuType, "MIXTE")) {
					if (hasAny(tags, "polyvalent", "mixte", "interieur-exterieur", "complet", "modulable")) delta += 0.10;
				}
			}
		}

		// === MUSIQUE : typeMusique (chaabi / rai / andalou / pop / electro) ===
		// S'applique a ORCHESTRE/DJ/ISSAWA en plus de l'energie/volume deja gere.
		if (cat == CAT_ORCHESTRE || cat == CAT_DJ || cat == CAT_ISSAWA) {
			String typeMusique = p.getTypeMusique();
			if (typeMusique != null && !typeMusique.isEmpty()) {
				String tm = typeMusique.toLowerCase();
				if (tm.contains("chaabi") && hasAny(tags, "chaabi", "marocain", "populaire", "festif")) delta += 0.10;
				else if (tm.contains("andalou") && hasAny(tags, "andalou", "andalous", "classique", "traditionnel", "fassi")) delta += 0.12;
				else if (tm.contains("rai") && hasAny(tags, "rai", "moderne", "pop", "festif")) delta += 0.08;
				else if (tm.contains("oriental") && hasAny(tags, "oriental", "arabe", "libanais", "egyptien")) delta += 0.08;
				else if (tm.contains("pop") && hasAny(tags, "pop", "moderne", "international", "varie")) delta += 0.08;
				else if (tm.contains("rnb") && hasAny(tags, "rnb", "r&b", "soul", "moderne")) delta += 0.08;
				else if (tm.contains("electro") && hasAny(tags, "electro", "house", "club", "edm", "moderne")) delta += 0.10;
				else if (tm.contains("classique") && hasAny(tags, "classique", "violoniste", "instrumental", "acoustique")) delta += 0.10;
				else if (tm.contains("jazz") && hasAny(tags, "jazz", "lounge", "acoustique")) delta += 0.10;
				else if (tm.contains("varie") && hasAny(tags, "varie", "polyvalent", "complet", "eclectique")) delta += 0.08;
			}
		}

		// === SAISON : ETE / AUTOMNE / HIVER / PRINTEMPS ===
		// Bonus/malus selon la categorie : un jardin en ete = parfait,
		// un jardin en hiver = risque. Une salle climatisee en ete = bonus.
		String saison = p.getSaison();
		if (saison != null) {
			String sa = saison.toUpperCase();
			if (cat == CAT_SALLE) {
				if (sa.equals("ETE")) {
					if (hasAny(tags, "climatise", "jardin", "piscine", "terrasse", "exterieur")) delta += 0.06;
					if (hasAny(tags, "chauffe") && !hasAny(tags, "climatise")) delta -= 0.02;
				} else if (sa.equals("HIVER")) {
					if (hasAny(tags, "interieur", "chauffe", "couvert", "climatise")) delta += 0.06;
					if (hasAny(tags, "jardin", "piscine", "exterieur", "outdoor") && !hasAny(tags, "couvert", "interieur")) delta -= 0.10;
				} else if (sa.equals("PRINTEMPS") || sa.equals("AUTOMNE")) {
					if (hasAny(tags, "polyvalent", "jardin", "interieur-exterieur", "terrasse")) delta += 0.04;
				}
			} else if (cat == CAT_PHOTO) {
				if (sa.equals("ETE")) {
					if (hasAny(tags, "naturel", "lumiere", "exterieur", "outdoor")) delta += 0.05;
				} else if (sa.equals("HIVER")) {
					if (hasAny(tags, "studio", "interieur", "lumiere-artificielle", "moody")) delta += 0.04;
				}
			} else if (cat == CAT_DECORATION) {
				if (sa.equals("ETE") && hasAny(tags, "frais", "lumineux", "floral", "boheme", "fleurs")) delta += 0.04;
				if (sa.equals("HIVER") && hasAny(tags, "chaleureux", "bougies", "cosy", "doree", "ambre")) delta += 0.04;
			}
		}

		// === CUISINE : Halal strict (TRAITEUR uniquement) ===
		// Renforce le scoring de scoreCategorySpecific deja existant.
		if (cat == CAT_TRAITEUR) {
			String halal = p.getHalalStrict();
			if (halal != null) {
				String h = halal.toUpperCase();
				if (h.equals("STRICT") || h.equals("OUI") || h.equals("HALAL")) {
					if (hasAny(tags, "halal", "famille", "fassi", "traditionnel")) delta += 0.08;
					if (hasAny(tags, "alcool", "vin", "champagne", "open-bar")) delta -= 0.10;
				}
			}
		}

		// Borne stricte du delta.
		if (delta > 0.35) delta = 0.35;
		if (delta < -0.35) delta = -0.35;
		return delta;
	}

	/** cityMatch (15%) : meme ville (defaut Fes) ? Sinon meme region ? Sinon penalisation moduleee. */
	private double scoreCity(Vendor v, UserProfile p) {
		String userCity = userCity(p);
		String vendorCity = v.getCity() != null ? v.getCity().trim() : "";
		if (vendorCity.isEmpty()) return 0.5;
		if (sameCity(userCity, vendorCity)) return 1.0;
		int tol = (p != null) ? p.getCityTolerance() : 2;
		if (tol < 1) tol = 1; else if (tol > 5) tol = 5;
		if (sameRegion(userCity, vendorCity)) {
			return Math.min(1.0, 0.45 + tol * 0.11);
		}
		return Math.min(1.0, 0.05 + tol * 0.16);
	}

	private String userCity(UserProfile p) {
		if (p != null && p.getUserCity() != null && !p.getUserCity().isEmpty()) {
			return p.getUserCity();
		}
		if (p != null) {
			List<String> mood = p.getMoodKeywords();
			if (mood != null) {
				for (int i = 0; i < mood.size(); i++) {
					String m = mood.get(i);
					if (m != null && m.length() >= 3 && isMoroccanCity(m)) return m;
				}
			}
		}
		return DEFAULT_CITY;
	}

	private boolean isMoroccanCity(String s) {
		String n = normalize(s);
		String[] cities = { "fes", "meknes", "rabat", "sale", "casablanca", "marrakech",
				"tanger", "agadir", "oujda", "kenitra", "tetouan", "ifrane" };
		for (int i = 0; i < cities.length; i++) {
			if (cities[i].equals(n)) return true;
		}
		return false;
	}

	private boolean sameCity(String a, String b) {
		if (a == null || b == null) return false;
		return normalize(a).equals(normalize(b));
	}

	private boolean sameRegion(String a, String b) {
		String[][] regions = new String[][] {
				{ "fes", "meknes", "ifrane", "sefrou" },
				{ "rabat", "sale", "kenitra", "temara" },
				{ "casablanca", "mohammedia", "settat" },
				{ "marrakech", "essaouira" },
				{ "tanger", "tetouan", "asilah" }
		};
		String aa = normalize(a);
		String bb = normalize(b);
		for (int i = 0; i < regions.length; i++) {
			boolean inA = false, inB = false;
			for (int j = 0; j < regions[i].length; j++) {
				if (regions[i][j].equals(aa)) inA = true;
				if (regions[i][j].equals(bb)) inB = true;
			}
			if (inA && inB) return true;
		}
		return false;
	}

	private String normalize(String s) {
		if (s == null) return "";
		String out = s.trim().toLowerCase();
		// Accents -> sans accent. Codepoints Unicode echappes pour etre 100%
		// portables (fonctionne quel que soit l'encodage du fichier source).
		out = out.replace('\u00e9', 'e').replace('\u00e8', 'e').replace('\u00ea', 'e').replace('\u00eb', 'e');
		out = out.replace('\u00e0', 'a').replace('\u00e2', 'a').replace('\u00e4', 'a');
		out = out.replace('\u00ee', 'i').replace('\u00ef', 'i');
		out = out.replace('\u00f4', 'o').replace('\u00f6', 'o');
		out = out.replace('\u00fb', 'u').replace('\u00fc', 'u');
		out = out.replace('\u00e7', 'c');
		return out;
	}

	/** guestCountMatch (15%) : capacite vendor vs nb invites (deduit des tags). */
	private double scoreGuestCount(Vendor v, UserProfile p) {
		int cat = v.getCategoryId();
		if (cat != CAT_SALLE && cat != CAT_TRAITEUR && cat != CAT_DECORATION) return 1.0;
		int nbInvites = p.getNbInvites();
		if (nbInvites <= 0) return 0.6;

		int[] capacity = inferCapacity(v);
		int capMin = capacity[0];
		int capMax = capacity[1];

		if (nbInvites >= capMin && nbInvites <= capMax) return 1.0;
		if (nbInvites < capMin) {
			double ratio = (double) nbInvites / Math.max(capMin, 1);
			return Math.max(ratio, 0.4);
		}
		double ratio = (double) capMax / nbInvites;
		return Math.max(ratio * 0.8, 0.2);
	}

	private int[] inferCapacity(Vendor v) {
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		if (tags.contains("tres-grand") || tags.contains("tres grand")) return new int[] { 400, 800 };
		if (tags.contains("grand") || tags.contains("complet") || tags.contains("royal")) return new int[] { 200, 500 };
		if (tags.contains("moyen") || tags.contains("polyvalent")) return new int[] { 100, 300 };
		if (tags.contains("intime") || tags.contains("petit") || tags.contains("boutique")) return new int[] { 30, 120 };
		return new int[] { 50, 350 };
	}

	/** luxuryMatch (10%) : gamme vendor vs niveau de luxe demande. */
	private double scoreLuxury(Vendor v, UserProfile p) {
		String want = p.getNiveauLuxe();
		String have = v.getGamme();
		if (want == null || have == null) return 0.5;
		if (eq(want, "ECONOMIQUE")) {
			if (eq(have, "ECONOMIQUE")) return 1.0;
			if (eq(have, "MOYEN")) return 0.45;
			return 0.15;
		}
		if (eq(want, "MOYEN")) {
			if (eq(have, "MOYEN")) return 1.0;
			if (eq(have, "ECONOMIQUE")) return 0.65;
			if (eq(have, "PREMIUM")) return 0.55;
			return 0.5;
		}
		if (eq(want, "PREMIUM")) {
			if (eq(have, "PREMIUM")) return 1.0;
			if (eq(have, "MOYEN")) return 0.5;
			return 0.2;
		}
		if (eq(want, "ULTRA_LUXE")) {
			if (eq(have, "PREMIUM")) return 1.0;
			if (eq(have, "MOYEN")) return 0.4;
			return 0.1;
		}
		return 0.5;
	}

	/** qualityMatch (5%) : note moyenne et nombre d'avis. */
	private double scoreQuality(Vendor v) {
		double rating = v.getRating();
		int avis = v.getNbAvis();
		if (rating <= 0) return 0.5;
		double rNorm = Math.max(0, (rating - 3.0) / 2.0);   // 3.0 -> 0, 5.0 -> 1
		double volume = Math.min(avis / 100.0, 1.0);        // sature a 100 avis
		return 0.7 * rNorm + 0.3 * volume;
	}

	// ============================================================
	// HELPERS
	// ============================================================
	private boolean eq(String a, String b) { return a != null && a.equalsIgnoreCase(b); }

	/**
	 * Indique si deux types de lieux appartiennent a la meme "famille esthetique".
	 * Riad et Palais partagent zellige, patio, medina, mais different en
	 * capacite et formalisme : on les traite comme alternatives mutuelles.
	 */
	private boolean isRiadPalaisFamily(String a, String b) {
		if (a == null || b == null) return false;
		boolean aRP = "RIAD".equalsIgnoreCase(a) || "PALAIS".equalsIgnoreCase(a);
		boolean bRP = "RIAD".equalsIgnoreCase(b) || "PALAIS".equalsIgnoreCase(b);
		return aRP && bRP;
	}

	private boolean hasAny(String text, String... keywords) {
		if (text == null) return false;
		for (int i = 0; i < keywords.length; i++) {
			if (text.contains(keywords[i])) return true;
		}
		return false;
	}

	private double round1(double v) { return Math.round(v * 10.0) / 10.0; }

	// ============================================================
	// 2. SCORER UN PRESTATAIRE (combine les 6 sous-scores)
	// ============================================================
	public Recommendation scoreVendor(Vendor v, UserProfile p, int userId) {
		Recommendation r = new Recommendation();
		r.setUserId(userId);
		r.setVendorId(v.getId());

		double sBudget  = scoreBudget(v, p);
		double sStyle   = scoreStyle(v, p);
		double sCity    = scoreCity(v, p);
		double sGuests  = scoreGuestCount(v, p);
		double sLuxury  = scoreLuxury(v, p);
		double sQuality = scoreQuality(v);

		double scoreFinal =
				  sBudget  * WEIGHT_BUDGET
				+ sStyle   * WEIGHT_STYLE
				+ sCity    * WEIGHT_CITY
				+ sGuests  * WEIGHT_GUESTS
				+ sLuxury  * WEIGHT_LUXURY
				+ sQuality * WEIGHT_QUALITY;

		// ============================================================
		// CONTRAINTES FORTES (HARD CONSTRAINTS).
		// ------------------------------------------------------------
		// Avant : les choix critiques du questionnaire etaient un simple
		// "bonus" (+0.18). Resultat : un riad mal note pouvait etre classe
		// apres une salle moderne bien notee, meme si l'utilisateur avait
		// explicitement coche RIAD. Inacceptable.
		//
		// Apres : on applique un MULTIPLICATEUR severe quand un choix
		// fort du questionnaire est viole. Le score chute brutalement,
		// ce qui pousse les vrais matches en tete et les non-matches
		// dans une zone "Alternatives".
		//
		// On garde la trace de chaque contrainte dans constraintReasons
		// pour generer un texte de raison precis et honnete.
		// ============================================================
		List<String> constraintReasons = new ArrayList<String>();
		double constraintMultiplier = applyHardConstraints(v, p, constraintReasons);
		scoreFinal *= constraintMultiplier;

		scoreFinal = Math.round(scoreFinal * 1000.0) / 10.0;
		if (scoreFinal > 100) scoreFinal = 100;
		if (scoreFinal < 0) scoreFinal = 0;

		r.setScore(scoreFinal);
		r.setScoreBudget(round1(sBudget * 100));
		r.setScoreStyle(round1(sStyle * 100));
		r.setScoreCity(round1(sCity * 100));
		r.setScoreGuestCount(round1(sGuests * 100));
		r.setScoreLuxe(round1(sLuxury * 100));
		r.setScoreQuality(round1(sQuality * 100));
		r.setScorePopularite(round1(sQuality * 100));

		// Donnees vendor (pour le rendu sur la card)
		r.setVendorName(v.getName());
		r.setVendorCategory(v.getCategoryName());
		r.setVendorCategoryId(v.getCategoryId());
		r.setVendorGamme(v.getGamme());
		r.setVendorPrixMin(v.getPrixMin());
		r.setVendorPrixMax(v.getPrixMax());
		r.setVendorCity(v.getCity());
		r.setVendorPhone(v.getPhone());
		r.setVendorInstagram(v.getInstagram());
		r.setVendorTags(v.getTags());
		r.setVendorRating(v.getRating());
		r.setVendorNbAvis(v.getNbAvis());
		r.setVendorPhotoUrl(v.getPhotoUrl());
		r.setVendorGalleryUrls(v.getGalleryUrls());
		r.setVendorReelUrl(v.getReelUrl());

		generateBadges(r, v, p);
		generateMatchHighlights(r, v, p);
		// Les raisons issues des contraintes fortes prennent le pas sur
		// l'explication generique : elles citent explicitement le choix
		// du questionnaire qui a fait monter ou descendre le score.
		String baseExplain = generateExplanation(r, v, p);
		if (!constraintReasons.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < constraintReasons.size(); i++) {
				if (i > 0) sb.append(" • ");
				sb.append(constraintReasons.get(i));
			}
			sb.append(". ").append(baseExplain);
			r.setRaison(sb.toString());
		} else {
			r.setRaison(baseExplain);
		}
		r.setRaisonShort(qualifier(scoreFinal));
		return r;
	}

	/**
	 * Applique les CONTRAINTES FORTES dictees par le questionnaire.
	 * Retourne un multiplicateur (0.30 a 1.10) applique au score final.
	 *
	 * Regle 1 : lieuCeremonie (categorie SALLE / id=11)
	 *   - venueType MATCH -> 1.10 (boost +10% en tete de classement)
	 *   - lieuType == MIXTE -> 1.00 (l'utilisateur accepte tout)
	 *   - venueType NULL (legacy, non typee) -> 0.85 (penalite douce)
	 *   - venueType NON-MATCH -> 0.40 (penalite forte, classe en alternative)
	 *
	 * Regle 2 : halalStrict (categorie TRAITEUR / id=12)
	 *   - User STRICT + vendor avec tag alcool/champagne/open-bar -> 0.30
	 *     (exclusion quasi-totale)
	 *   - User STRICT + tag halal/traditionnel -> 1.08 (boost)
	 *
	 * Regle 3 : typeCuisine (categorie TRAITEUR)
	 *   - Si typeCuisine specifique (marocaine/orientale/internationale/fusion)
	 *     et vendor avec tag correspondant -> 1.05
	 *   - Si typeCuisine specifique et vendor sans tag correspondant -> 0.85
	 *
	 * Regle 4 : typeMusique (categories ORCHESTRE / DJ / ISSAWA)
	 *   - Match concret entre le style musical demande et les tags vendor -> 1.05
	 *   - Mismatch fort (chaabi vs electro, andalou vs DJ) -> 0.80
	 *
	 * Regle 5 : saison + categorie outdoor (JARDIN/PHOTO/DECO)
	 *   - HIVER + venue JARDIN/PISCINE -> 0.70 (risque meteo)
	 *   - ETE + venue JARDIN -> 1.05 (saison ideale)
	 */
	private double applyHardConstraints(Vendor v, UserProfile p, List<String> reasons) {
		double mult = 1.0;
		int cat = v.getCategoryId();

		// ===== Regle 1 : lieuCeremonie pour SALLE =====
		// Sept types possibles : RIAD, PALAIS, SALLE, JARDIN, PISCINE, HOTEL, MIXTE.
		// RIAD et PALAIS partagent l'esthetique traditionnelle (medina, zellige,
		// patio) mais different fortement en taille et formalisme. On les
		// traite comme "proches mais distincts" : si l'utilisateur veut un
		// RIAD intime, on lui propose un PALAIS uniquement en seconde ligne
		// (et inversement).
		if (cat == CAT_SALLE) {
			String chosenLieu = p.getLieuType();
			String vType = v.getVenueType();
			if (chosenLieu != null && !chosenLieu.isEmpty()) {
				if ("MIXTE".equalsIgnoreCase(chosenLieu)) {
					// L'utilisateur accepte tout, pas de penalite.
					mult *= 1.0;
				} else if (vType == null || vType.isEmpty()) {
					// Vendor pas encore type : penalite douce.
					mult *= 0.85;
					reasons.add("Type de lieu non renseigne pour ce prestataire");
				} else if (chosenLieu.equalsIgnoreCase(vType)) {
					// MATCH PARFAIT.
					mult *= 1.10;
					reasons.add("Correspond a votre choix : " + chosenLieu);
				} else if (isRiadPalaisFamily(chosenLieu, vType)) {
					// Famille traditionnelle (Riad <-> Palais) : pénalité légère
					// car esthetique proche mais experience differente.
					mult *= 0.70;
					if ("RIAD".equalsIgnoreCase(chosenLieu)) {
						reasons.add("Alternative grand format : ce palais est plus formel que le riad intime que vous cherchez");
					} else {
						reasons.add("Alternative intime : ce riad est plus petit qu'un palais classique");
					}
				} else {
					// MISMATCH classique : penalite forte.
					mult *= 0.40;
					reasons.add("Alternative proposee : ce lieu est de type "
						+ vType + ", vous avez choisi " + chosenLieu);
				}
			}
		}

		// ===== Regle 2 : halalStrict pour TRAITEUR =====
		if (cat == CAT_TRAITEUR) {
			String halal = p.getHalalStrict();
			String tags = v.getTags() == null ? "" : v.getTags().toLowerCase();
			if (halal != null) {
				String h = halal.toUpperCase();
				boolean userStrict = h.equals("STRICT") || h.equals("OUI") || h.equals("HALAL");
				if (userStrict) {
					boolean vendorServesAlcohol = hasAny(tags, "alcool", "vin", "champagne", "open-bar", "bar");
					boolean vendorHalal = hasAny(tags, "halal", "traditionnel", "famille", "marocain");
					if (vendorServesAlcohol && !vendorHalal) {
						mult *= 0.30;
						reasons.add("Alternative : ce traiteur propose de l'alcool, vous avez demande halal strict");
					} else if (vendorHalal) {
						mult *= 1.08;
						reasons.add("Cuisine halal confirmee (votre choix)");
					}
				}
			}
		}

		// ===== Regle 3 : typeCuisine pour TRAITEUR =====
		if (cat == CAT_TRAITEUR) {
			String cuisine = p.getTypeCuisine();
			String tags = v.getTags() == null ? "" : v.getTags().toLowerCase();
			if (cuisine != null && !cuisine.isEmpty()) {
				String cz = cuisine.toLowerCase();
				boolean match = false;
				if (cz.contains("marocaine") && hasAny(tags, "marocain", "fassi", "traditionnel", "tagine", "couscous")) match = true;
				else if (cz.contains("orientale") && hasAny(tags, "oriental", "libanais", "egyptien", "syrien")) match = true;
				else if (cz.contains("internationale") && hasAny(tags, "international", "moderne", "fusion", "varie")) match = true;
				else if (cz.contains("fusion") && hasAny(tags, "fusion", "moderne", "creatif")) match = true;
				else if (cz.contains("francaise") && hasAny(tags, "francais", "gastronomique", "raffine")) match = true;

				if (match) {
					mult *= 1.05;
					reasons.add("Cuisine " + cuisine + " confirmee");
				} else {
					// Mismatch leger : on rabaisse mais ne disqualifie pas.
					mult *= 0.85;
					reasons.add("Cuisine " + cuisine + " non explicitement annoncee");
				}
			}
		}

		// ===== Regle 4 : typeMusique pour ORCHESTRE / DJ / ISSAWA =====
		if (cat == CAT_ORCHESTRE || cat == CAT_DJ || cat == CAT_ISSAWA) {
			String tm = p.getTypeMusique();
			String tags = v.getTags() == null ? "" : v.getTags().toLowerCase();
			if (tm != null && !tm.isEmpty()) {
				String t = tm.toLowerCase();
				boolean match =
					(t.contains("chaabi")    && hasAny(tags, "chaabi", "marocain", "populaire")) ||
					(t.contains("andalou")   && hasAny(tags, "andalou", "andalous", "classique", "fassi")) ||
					(t.contains("rai")       && hasAny(tags, "rai", "moderne", "pop")) ||
					(t.contains("oriental")  && hasAny(tags, "oriental", "arabe", "libanais")) ||
					(t.contains("pop")       && hasAny(tags, "pop", "moderne", "varie", "international")) ||
					(t.contains("rnb")       && hasAny(tags, "rnb", "r&b", "soul", "moderne")) ||
					(t.contains("electro")   && hasAny(tags, "electro", "house", "club", "edm")) ||
					(t.contains("classique") && hasAny(tags, "classique", "violoniste", "instrumental")) ||
					(t.contains("jazz")      && hasAny(tags, "jazz", "lounge", "acoustique")) ||
					(t.contains("varie")     && hasAny(tags, "varie", "polyvalent", "complet"));

				// Mismatches forts incompatibles
				boolean strongMismatch =
					(t.contains("chaabi")   && cat == CAT_DJ && hasAny(tags, "electro", "club", "edm")) ||
					(t.contains("electro")  && cat == CAT_ISSAWA) ||
					(t.contains("andalou")  && cat == CAT_DJ && !hasAny(tags, "varie", "polyvalent"));

				if (match) {
					mult *= 1.05;
					reasons.add("Style " + tm + " confirme");
				} else if (strongMismatch) {
					mult *= 0.80;
					reasons.add("Style musical eloigne de votre choix " + tm);
				}
			}
		}

		// ===== Regle 5 : saisonPreferee pour outdoor =====
		String saison = p.getSaison();
		if (saison != null) {
			String sa = saison.toUpperCase();
			String vType = v.getVenueType();
			if (cat == CAT_SALLE && vType != null) {
				if (sa.equals("HIVER") && ("JARDIN".equalsIgnoreCase(vType) || "PISCINE".equalsIgnoreCase(vType))) {
					mult *= 0.70;
					reasons.add("Risque meteo : " + vType + " en hiver");
				} else if (sa.equals("ETE") && "JARDIN".equalsIgnoreCase(vType)) {
					mult *= 1.05;
					reasons.add("Saison ideale pour un jardin");
				}
			}
		}

		// Borne pour eviter les multiplicateurs degenerees
		if (mult < 0.10) mult = 0.10;
		if (mult > 1.20) mult = 1.20;
		return mult;
	}

	/**
	 * Construit la liste des "matchs concrets" entre les reponses du
	 * questionnaire et ce prestataire. Chaque match est une phrase courte
	 * (max 3 mots) qui sera affichee en pastille sur la carte. Le but est
	 * que le client voie d'un coup d'oeil pourquoi l'IA a choisi ce
	 * prestataire pour lui (pas un score abstrait, une raison concrete).
	 */
	private void generateMatchHighlights(Recommendation r, Vendor v, UserProfile p) {
		int cat = v.getCategoryId();
		String tags = (v.getTags() == null) ? "" : v.getTags().toLowerCase();

		// 1. Lieu (RIAD / SALLE / JARDIN / ...) - CONTRAINTE FORTE sur les salles.
		// On se base prioritairement sur venue_type (structure), puis fallback
		// sur les tags si le vendor n'est pas encore typé en base.
		if (cat == CAT_SALLE && p.getLieuType() != null && !p.getLieuType().isEmpty()) {
			String lt = p.getLieuType();
			String vType = v.getVenueType();
			boolean ok = false;
			if (vType != null && !vType.isEmpty()) {
				ok = lt.equalsIgnoreCase(vType) || "MIXTE".equalsIgnoreCase(lt) || "MIXTE".equalsIgnoreCase(vType);
			} else {
				// Fallback tags (legacy)
				if (eq(lt, "RIAD") && hasAny(tags, "riad", "palais", "medina", "fassi", "andalous", "patio", "zellige")) ok = true;
				else if (eq(lt, "SALLE") && hasAny(tags, "salle", "moderne", "climatise", "polyvalent", "spacieux")) ok = true;
				else if (eq(lt, "JARDIN") && hasAny(tags, "jardin", "exterieur", "verdure", "terrasse")) ok = true;
				else if (eq(lt, "PISCINE") && hasAny(tags, "piscine", "resort", "bord-piscine")) ok = true;
				else if (eq(lt, "HOTEL") && hasAny(tags, "hotel", "resort", "complexe", "suites")) ok = true;
				else if (eq(lt, "MIXTE") && hasAny(tags, "polyvalent", "mixte", "modulable")) ok = true;
			}
			if (ok) r.addMatchHighlight("Lieu " + lt + " ✓");
			else if (vType != null && !vType.isEmpty()) r.addMatchHighlight("Type " + vType + " (alternative)");
		}

		// 2. Style mariage
		if (r.getScoreStyle() >= 75 && p.getStyle() != null && !p.getStyle().isEmpty()) {
			r.addMatchHighlight("Style " + p.getStyle().toLowerCase() + " ✓");
		}

		// 3. Budget
		if (r.getScoreBudget() >= 80) {
			r.addMatchHighlight("Budget OK ✓");
		} else if (r.getScoreBudget() < 40) {
			r.addMatchHighlight("Hors budget !");
		}

		// 4. Ville
		if (r.getScoreCity() >= 95 && p.getUserCity() != null) {
			r.addMatchHighlight(p.getUserCity() + " ✓");
		}

		// 5. Saison
		String saison = p.getSaison();
		if (saison != null && !saison.isEmpty()) {
			String sa = saison.toUpperCase();
			boolean ok = false;
			if (cat == CAT_SALLE) {
				if (sa.equals("ETE") && hasAny(tags, "climatise", "jardin", "piscine", "terrasse")) ok = true;
				else if (sa.equals("HIVER") && hasAny(tags, "interieur", "chauffe", "couvert", "climatise")) ok = true;
			} else if (cat == CAT_PHOTO) {
				if (sa.equals("ETE") && hasAny(tags, "naturel", "lumiere", "exterieur")) ok = true;
				else if (sa.equals("HIVER") && hasAny(tags, "studio", "interieur")) ok = true;
			}
			if (ok) r.addMatchHighlight("Saison " + sa.toLowerCase() + " ✓");
		}

		// 6. Musique (ORCHESTRE / DJ / ISSAWA)
		if ((cat == CAT_ORCHESTRE || cat == CAT_DJ || cat == CAT_ISSAWA)
				&& p.getTypeMusique() != null && !p.getTypeMusique().isEmpty()) {
			String tm = p.getTypeMusique().toLowerCase();
			if ((tm.contains("chaabi") && hasAny(tags, "chaabi", "marocain"))
				|| (tm.contains("andalou") && hasAny(tags, "andalou", "andalous", "classique"))
				|| (tm.contains("rai") && hasAny(tags, "rai", "moderne"))
				|| (tm.contains("oriental") && hasAny(tags, "oriental", "arabe", "libanais"))
				|| (tm.contains("pop") && hasAny(tags, "pop", "moderne", "varie"))
				|| (tm.contains("electro") && hasAny(tags, "electro", "house", "club"))) {
				r.addMatchHighlight("Musique " + tm + " ✓");
			}
		}

		// 7. Cuisine halal stricte (TRAITEUR)
		if (cat == CAT_TRAITEUR && p.getHalalStrict() != null) {
			String h = p.getHalalStrict().toUpperCase();
			if ((h.equals("STRICT") || h.equals("OUI") || h.equals("HALAL"))
					&& hasAny(tags, "halal", "famille", "traditionnel")) {
				r.addMatchHighlight("Halal ✓");
			}
		}

		// 8. Categorie prioritaire selon le questionnaire
		List<Integer> top = p.getTopCategoryIds();
		if (top != null && !top.isEmpty() && top.get(0) == cat) {
			r.addMatchHighlight("Priorite #1 ✓");
		}

		// 9. Capacite (SALLE / TRAITEUR / DECORATION)
		if ((cat == CAT_SALLE || cat == CAT_TRAITEUR || cat == CAT_DECORATION)
				&& r.getScoreGuestCount() >= 95 && p.getNbInvites() > 0) {
			r.addMatchHighlight(p.getNbInvites() + " invites ✓");
		}

		// 10. Niveau de luxe
		if (r.getScoreLuxe() >= 95 && p.getNiveauLuxe() != null && !p.getNiveauLuxe().isEmpty()) {
			r.addMatchHighlight("Niveau " + p.getNiveauLuxe().toLowerCase().replace("_"," ") + " ✓");
		}
	}

	// ============================================================
	// 3. RECOMMANDER + filtrage par categories demandees
	// ============================================================
	public List<Recommendation> recommend(List<Vendor> vendors, UserProfile profile, int userId) {
		List<Recommendation> out = new ArrayList<Recommendation>();
		if (vendors == null) return out;
		java.util.Set<Integer> wantedCats = wantedCategoryIds(profile);
		for (int i = 0; i < vendors.size(); i++) {
			Vendor v = vendors.get(i);
			if (wantedCats != null && !wantedCats.isEmpty()
					&& !wantedCats.contains(v.getCategoryId())) {
				continue;
			}
			out.add(scoreVendor(v, profile, userId));
		}
		sortByScoreDesc(out);
		return out;
	}

	/** Convertit la liste services -> Set de category_id ; null si rien coche. */
	public java.util.Set<Integer> wantedCategoryIds(UserProfile p) {
		if (p == null) return null;
		List<String> codes = p.getRequestedServices();
		if (codes == null || codes.isEmpty()) return null;
		java.util.Set<Integer> out = new java.util.HashSet<Integer>();
		for (int i = 0; i < codes.size(); i++) {
			Integer cat = serviceCodeToCategoryId(codes.get(i));
			if (cat != null) out.add(cat);
		}
		return out;
	}

	private Integer serviceCodeToCategoryId(String code) {
		if (code == null) return null;
		String c = code.toUpperCase();
		if (c.equals("SALLE")) return CAT_SALLE;
		if (c.equals("TRAITEUR")) return CAT_TRAITEUR;
		if (c.equals("NEGGAFA")) return CAT_NEGGAFA;
		if (c.equals("MAKEUP")) return CAT_MAKEUP;
		if (c.equals("PHOTOGRAPHE")) return CAT_PHOTO;
		if (c.equals("DECORATION")) return CAT_DECORATION;
		if (c.equals("CAKE_DESIGNER") || c.equals("CAKE")) return CAT_CAKE;
		if (c.equals("HENNAYA")) return CAT_HENNAYA;
		if (c.equals("MYADI")) return CAT_MYADI;
		if (c.equals("ORCHESTRE")) return CAT_ORCHESTRE;
		if (c.equals("ISSAWA")) return CAT_ISSAWA;
		if (c.equals("DJ")) return CAT_DJ;
		return null;
	}

	// ============================================================
	// 4. TOP-K PAR CATEGORIE (k = 3) + tri
	// ============================================================
	public Map<String, List<Recommendation>> topPerCategory(List<Recommendation> sorted, UserProfile p) {
		Map<Integer, List<Recommendation>> byCat = new LinkedHashMap<Integer, List<Recommendation>>();
		for (int i = 0; i < sorted.size(); i++) {
			Recommendation r = sorted.get(i);
			Integer cat = r.getVendorCategoryId();
			List<Recommendation> bucket = byCat.get(cat);
			if (bucket == null) {
				bucket = new ArrayList<Recommendation>();
				byCat.put(cat, bucket);
			}
			bucket.add(r);
		}

		LinkedHashMap<String, List<Recommendation>> out = new LinkedHashMap<String, List<Recommendation>>();
		java.util.Set<Integer> used = new java.util.HashSet<Integer>();
		if (p != null && p.getTopCategoryIds() != null) {
			for (int i = 0; i < p.getTopCategoryIds().size(); i++) {
				Integer cat = p.getTopCategoryIds().get(i);
				List<Recommendation> bucket = byCat.get(cat);
				if (bucket == null || bucket.isEmpty()) continue;
				used.add(cat);
				String name = bucket.get(0).getVendorCategory();
				out.put(name, takeTop(bucket, TOP_K_PER_CATEGORY));
			}
		}
		for (Map.Entry<Integer, List<Recommendation>> e : byCat.entrySet()) {
			if (used.contains(e.getKey())) continue;
			if (e.getValue().isEmpty()) continue;
			String name = e.getValue().get(0).getVendorCategory();
			out.put(name, takeTop(e.getValue(), TOP_K_PER_CATEGORY));
		}
		return out;
	}

	private List<Recommendation> takeTop(List<Recommendation> in, int n) {
		List<Recommendation> out = new ArrayList<Recommendation>();
		int max = Math.min(n, in.size());
		for (int i = 0; i < max; i++) out.add(in.get(i));
		return out;
	}

	/** Tri par selection (pattern simple du cours). */
	private void sortByScoreDesc(List<Recommendation> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			int max = i;
			for (int j = i + 1; j < list.size(); j++) {
				if (list.get(j).getScore() > list.get(max).getScore()) max = j;
			}
			if (max != i) {
				Recommendation tmp = list.get(i);
				list.set(i, list.get(max));
				list.set(max, tmp);
			}
		}
	}

	// ============================================================
	// 5. BADGES + PHRASE D'EXPLICATION (deterministe, pas de LLM)
	// ============================================================
	public void generateBadges(Recommendation r, Vendor v, UserProfile p) {
		if (r.getScoreBudget() >= 80)         r.addTag("Budget adapte");
		else if (r.getScoreBudget() < 40)     r.addTag("Hors budget");

		if (r.getScoreStyle() >= 80)          r.addTag("Style compatible");
		if (r.getScoreCity() >= 100)          r.addTag("Proche de vous");
		if (r.getScoreGuestCount() >= 100)    r.addTag("Capacite parfaite");

		if (r.getScoreLuxe() >= 95) {
			if (eq(v.getGamme(), "PREMIUM"))      r.addTag("Premium");
			else if (eq(v.getGamme(), "ECONOMIQUE")) r.addTag("Petit budget");
		}

		if (r.getScoreBudget() >= 85 && r.getScoreLuxe() >= 70 && r.getScoreStyle() >= 70) {
			r.addTag("Bon rapport qualite/prix");
		}
		if (v.getRating() >= 4.5 && v.getNbAvis() >= 30) r.addTag("Tres bien note");
		if (r.getScore() >= 90)                r.addTag("Coup de coeur");

		List<Integer> top = p.getTopCategoryIds();
		if (top != null && !top.isEmpty() && top.get(0) == v.getCategoryId()) {
			r.addTag("Categorie prioritaire");
		}
		if (r.getTags().isEmpty()) r.addTag("A considerer");
	}

	public String generateExplanation(Recommendation r, Vendor v, UserProfile p) {
		List<String> reasons = new ArrayList<String>();
		if (r.getScoreBudget() >= 70)        reasons.add("a votre budget");
		if (r.getScoreStyle() >= 70) {
			String desc = humanStyle(p);
			if (desc != null) reasons.add("a votre style " + desc);
			else              reasons.add("a votre style");
		}
		if (r.getScoreCity() >= 80)          reasons.add("a votre ville");
		if (r.getScoreGuestCount() >= 80 && (v.getCategoryId() == CAT_SALLE
				|| v.getCategoryId() == CAT_TRAITEUR
				|| v.getCategoryId() == CAT_DECORATION)) {
			reasons.add("a votre nombre d'invites");
		}
		if (r.getScoreLuxe() >= 90 && p.getNiveauLuxe() != null) {
			reasons.add("a votre niveau de luxe " + p.getNiveauLuxe().toLowerCase().replace("_"," "));
		}
		if (r.getScoreQuality() >= 80 && v.getRating() > 0) {
			reasons.add("a une excellente reputation (" + v.getRating() + "/5 sur " + v.getNbAvis() + " avis)");
		}

		String qualif = qualifier(r.getScore());
		if (reasons.isEmpty()) {
			return qualif + " : alternative a considerer si vous etes flexible.";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(qualif).append(" : ce prestataire est recommande car il correspond ");
		int n = Math.min(4, reasons.size());
		for (int i = 0; i < n; i++) {
			if (i > 0) sb.append(i == n - 1 ? " et " : ", ");
			sb.append(reasons.get(i));
		}
		sb.append(".");
		return sb.toString();
	}

	private String humanStyle(UserProfile p) {
		String style = p.getStyle();
		String amb = p.getAmbiance();
		if (style == null && amb == null) return null;
		StringBuilder sb = new StringBuilder();
		if (amb != null) {
			sb.append(amb.toLowerCase().replace("_"," "));
			if (style != null) sb.append(" ");
		}
		if (style != null) sb.append(style.toLowerCase().replace("_"," "));
		return sb.toString().trim();
	}

	private String qualifier(double score) {
		if (score >= 85) return "Correspondance ideale";
		if (score >= 70) return "Excellente correspondance";
		if (score >= 55) return "Bonne correspondance";
		return "Option envisageable";
	}
}

