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
}

