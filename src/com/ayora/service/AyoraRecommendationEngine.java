package com.ayora.service;

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

		// Cas particulier : si la ceremonie est "DOMICILE" (a la maison), on
		// retire automatiquement la categorie SALLE des services demandes,
		// quoi qu'ait coche l'utilisateur en section 4.
		String lieu = a.getLieuCeremonie();
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
		return Math.max((double) hits / checks, 0.2);
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
		out = out.replace('é','e').replace('è','e').replace('ê','e').replace('ë','e');
		out = out.replace('à','a').replace('â','a').replace('ä','a');
		out = out.replace('î','i').replace('ï','i');
		out = out.replace('ô','o').replace('ö','o');
		out = out.replace('û','u').replace('ü','u');
		out = out.replace('ç','c');
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

	private boolean hasAny(String text, String... keywords) {
		if (text == null) return false;
		for (int i = 0; i < keywords.length; i++) {
			if (text.contains(keywords[i])) return true;
		}
		return false;
	}

	private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}

