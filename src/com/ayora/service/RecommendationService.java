package com.ayora.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import com.ayora.dao.RecommendationDao;
import com.ayora.dao.VendorDao;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.model.UserProfile;
import com.ayora.model.Vendor;
import com.ayora.util.JsonUtil;

/**
 * Moteur de recommandation IA - Refonte personnalisee.
 *
 * Pattern Service Layer (cours p02-jee) :
 *   1. buildUserProfile  -> abstrait les donnees du questionnaire en profil exploitable
 *   2. scoreVendor       -> calcule un score multi-criteres (budget, style, luxe, popularite, culturel)
 *   3. generateTags      -> genere des tags pertinents (Coup de coeur, Bon rapport qualite-prix, ...)
 *   4. generateRaison    -> raison textuelle contextualisee, fait reference aux reponses
 *   5. buildBlocks       -> regroupe les recommandations en blocs (top, qualite-prix, chic, ...)
 *
 * Contrairement a la version basique, on ne fait plus juste des matchs textuels :
 * on raisonne par profil et par categorie, avec des seuils explicites.
 */
public class RecommendationService {

	// Seuils de score pour les blocs / qualificatifs
	public static final double SCORE_EXCELLENT = 85.0;
	public static final double SCORE_GOOD = 70.0;
	public static final double SCORE_OK = 55.0;
	public static final double SCORE_MIN_TO_KEEP = 35.0;

	// Categories ID (alignes avec la table vendor_categories)
	// Apres migration v4 : 12 categories actives, les autres sont obsoletes / supprimees
	public static final int CAT_NEGGAFA = 1;
	public static final int CAT_MAKEUP = 2;          // = "Maquillage & Coiffure"
	public static final int CAT_COIFFURE = 3;        // [obsolete] fusionne dans CAT_MAKEUP
	public static final int CAT_PHOTO = 4;           // = "Photographe & Videaste"
	public static final int CAT_VIDEO = 5;           // [obsolete] fusionne dans CAT_PHOTO
	public static final int CAT_CAKE = 6;
	public static final int CAT_ISSAWA = 7;
	public static final int CAT_ORCHESTRE = 8;
	public static final int CAT_DECORATION = 9;      // = "Decoration & Fleuriste"
	public static final int CAT_FLEURISTE = 10;      // [obsolete] fusionne dans CAT_DECORATION
	public static final int CAT_SALLE = 11;
	public static final int CAT_TRAITEUR = 12;
	public static final int CAT_MYADI = 13;
	public static final int CAT_DJ = 14;
	public static final int CAT_TRANSPORT = 15;      // [supprime]
	public static final int CAT_HENNAYA = 16;

	private VendorDao vendorDao;
	private RecommendationDao recommendationDao;

	public RecommendationService() {
		vendorDao = new VendorDao();
		recommendationDao = new RecommendationDao();
	}

	// ============================================================
	// API PUBLIQUE
	// ============================================================

	/**
	 * Genere les recommandations, les persiste et les retourne.
	 * Appele depuis QuestionnaireServlet apres soumission du form.
	 */
	public List<Recommendation> generateRecommendations(int userId, QuestionnaireAnswer answers) {
		recommendationDao.deleteByUserId(userId);

		UserProfile profile = buildUserProfile(answers);
		profile.setUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		List<Recommendation> recommendations = new Vector<Recommendation>();

		for (int i = 0; i < allVendors.size(); i++) {
			Vendor vendor = allVendors.get(i);
			Recommendation rec = scoreVendor(vendor, profile, userId);
			if (rec.getScore() >= SCORE_MIN_TO_KEEP) {
				recommendationDao.create(rec);
				recommendations.add(rec);
			}
		}

		sortByScore(recommendations);
		return recommendations;
	}

	/**
	 * Recalcule les recommandations sans toucher la BDD (pour doGet).
	 * Permet de toujours servir des resultats frais (tags, raisons recalcules).
	 */
	public List<Recommendation> computeRecommendations(int userId, QuestionnaireAnswer answers) {
		UserProfile profile = buildUserProfile(answers);
		profile.setUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		List<Recommendation> recommendations = new Vector<Recommendation>();

		for (int i = 0; i < allVendors.size(); i++) {
			Vendor vendor = allVendors.get(i);
			Recommendation rec = scoreVendor(vendor, profile, userId);
			if (rec.getScore() >= SCORE_MIN_TO_KEEP) {
				recommendations.add(rec);
			}
		}
		sortByScore(recommendations);
		return recommendations;
	}

	/**
	 * Construit le profil utilisateur a partir des reponses brutes.
	 */
	public UserProfile buildUserProfile(QuestionnaireAnswer a) {
		UserProfile p = new UserProfile();

		p.setStyle(a.getStyleMariage());
		p.setAmbiance(a.getAmbiance());
		p.setNiveauLuxe(a.getNiveauLuxe());
		p.setThemeCouleur(a.getThemeCouleur());
		p.setSaison(a.getSaisonPreferee());

		p.setBudgetTotal(a.getBudgetTotal());
		p.setBudgetFlexibility(a.getBudgetFlexibility());
		p.setNbInvites(a.getNbInvites());

		// Budget per guest
		if (a.getNbInvites() > 0 && a.getBudgetTotal() > 0) {
			p.setBudgetPerGuest(a.getBudgetTotal() / a.getNbInvites());
		}

		// Budget tier (par invite, repere mariage Fes)
		double bpg = p.getBudgetPerGuest();
		if (bpg <= 0) p.setBudgetTier("CONFORTABLE");
		else if (bpg < 600) p.setBudgetTier("SERRE");
		else if (bpg < 1200) p.setBudgetTier("CONFORTABLE");
		else if (bpg < 2500) p.setBudgetTier("GENEREUX");
		else p.setBudgetTier("ILLIMITE");

		// Guest size
		int n = a.getNbInvites();
		if (n < 100) p.setGuestSize("INTIME");
		else if (n < 200) p.setGuestSize("MOYEN");
		else if (n < 400) p.setGuestSize("GRAND");
		else p.setGuestSize("TRES_GRAND");

		p.setTypeMusique(a.getTypeMusique());
		p.setTypeCuisine(a.getTypeCuisine());
		p.setPrefPhoto(a.getPrefPhoto());
		p.setPrefDecoration(a.getPrefDecoration());
		p.setStyleNeggafa(a.getStyleNeggafa());
		p.setNbTenuesNeggafa(a.getNbTenuesNeggafa());

		p.setPrioriteSalle(a.getPrioriteSalle());
		p.setPrioriteTraiteur(a.getPrioriteTraiteur());
		p.setPrioritePhoto(a.getPrioritePhoto());
		p.setPrioriteMusique(a.getPrioriteMusique());
		p.setPrioriteDecoration(a.getPrioriteDecoration());
		p.setPrioriteNeggafa(a.getPrioriteNeggafa());
		p.setPrioriteMakeup(a.getPrioriteMakeup());

		// Top categories : trier par priorite descendante
		List<int[]> prios = new ArrayList<int[]>();
		prios.add(new int[]{CAT_SALLE, a.getPrioriteSalle()});
		prios.add(new int[]{CAT_TRAITEUR, a.getPrioriteTraiteur()});
		prios.add(new int[]{CAT_PHOTO, a.getPrioritePhoto()});
		prios.add(new int[]{CAT_ORCHESTRE, a.getPrioriteMusique()});
		prios.add(new int[]{CAT_DECORATION, a.getPrioriteDecoration()});
		prios.add(new int[]{CAT_NEGGAFA, a.getPrioriteNeggafa()});
		prios.add(new int[]{CAT_MAKEUP, a.getPrioriteMakeup()});
		// Tri par selection (pattern du prof)
		for (int i = 0; i < prios.size() - 1; i++) {
			int max = i;
			for (int j = i + 1; j < prios.size(); j++) {
				if (prios.get(j)[1] > prios.get(max)[1]) max = j;
			}
			if (max != i) {
				int[] tmp = prios.get(i);
				prios.set(i, prios.get(max));
				prios.set(max, tmp);
			}
		}
		List<Integer> top = new ArrayList<Integer>();
		for (int i = 0; i < prios.size(); i++) top.add(prios.get(i)[0]);
		p.setTopCategoryIds(top);

		p.setPostesEconomie(a.getPostesEconomie());

		// Mots-cles depuis notesSpeciales (JSON section 6)
		List<String> moods = extractMoodKeywords(a.getNotesSpeciales());
		p.setMoodKeywords(moods);

		return p;
	}

	private List<String> extractMoodKeywords(String notesJson) {
		List<String> out = new ArrayList<String>();
		if (notesJson == null) return out;
		String trimmed = notesJson.trim();
		if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return out;
		// Cherche les tableaux motsCles ou momentsForts
		String[] candidates = {"motsCles", "moments", "momentsForts", "ambianceMots"};
		for (int i = 0; i < candidates.length; i++) {
			int idx = trimmed.indexOf("\"" + candidates[i] + "\"");
			if (idx < 0) continue;
			int arrStart = trimmed.indexOf("[", idx);
			int arrEnd = trimmed.indexOf("]", arrStart);
			if (arrStart < 0 || arrEnd < 0) continue;
			String arr = trimmed.substring(arrStart + 1, arrEnd);
			String[] parts = arr.split(",");
			for (int j = 0; j < parts.length; j++) {
				String s = parts[j].trim().replace("\"", "");
				if (!s.isEmpty()) out.add(s.toLowerCase());
			}
		}
		return out;
	}

	// ============================================================
	// SCORING
	// ============================================================

	/**
	 * Score un vendor pour le profil donne.
	 * Retourne un Recommendation enrichi (score + sub-scores + tags + raison).
	 *
	 * Repartition des points :
	 *   - Budget       : 30 pts (compatibilite prix vs budget alloue par categorie)
	 *   - Luxe/Gamme   : 20 pts (gamme du prestataire vs niveau de luxe demande)
	 *   - Style        : 20 pts (matchs sur tags style + ambiance + theme)
	 *   - Preferences  : 20 pts (preferences specifiques selon categorie)
	 *   - Priorite     : 10 pts (categorie prioritaire pour l'utilisateur)
	 *   - Bonus        : +5 pts max (popularite : rating + nb avis)
	 *   - Bonus mood   : +5 pts max (mots-cles section 6)
	 */
	public Recommendation scoreVendor(Vendor vendor, UserProfile p, int userId) {
		Recommendation r = new Recommendation();
		r.setUserId(userId);
		r.setVendorId(vendor.getId());

		double sBudget = scoreBudget(vendor, p);          // [0,1]
		double sLuxe = scoreLuxe(vendor, p);              // [0,1]
		double sStyle = scoreStyle(vendor, p);            // [0,1]
		double sPref = scorePreferences(vendor, p);       // [0,1]
		double sPriority = scorePriority(vendor, p);      // [0,1]
		double sPop = scorePopularite(vendor);            // [0,1]
		double sCulture = scoreCulturel(vendor, p);       // [0,1]
		double sMood = scoreMood(vendor, p);              // [0,1]

		double score = sBudget * 30 + sLuxe * 20 + sStyle * 20 + sPref * 20 + sPriority * 10
				+ sPop * 5 + sMood * 5;

		// Bonus correspondance culturelle (Fes / fassi / theme heritage)
		score += sCulture * 5;

		score = Math.min(Math.round(score * 10.0) / 10.0, 100.0);

		r.setScore(score);
		r.setScoreBudget(round1(sBudget * 100));
		r.setScoreLuxe(round1(sLuxe * 100));
		r.setScoreStyle(round1(sStyle * 100));
		r.setScorePopularite(round1(sPop * 100));
		r.setScoreCulturel(round1(sCulture * 100));

		// Vendor enriched fields
		r.setVendorName(vendor.getName());
		r.setVendorCategory(vendor.getCategoryName());
		r.setVendorCategoryId(vendor.getCategoryId());
		r.setVendorGamme(vendor.getGamme());
		r.setVendorPrixMin(vendor.getPrixMin());
		r.setVendorPrixMax(vendor.getPrixMax());
		r.setVendorCity(vendor.getCity());
		r.setVendorPhone(vendor.getPhone());
		r.setVendorInstagram(vendor.getInstagram());
		r.setVendorTags(vendor.getTags());
		r.setVendorRating(vendor.getRating());
		r.setVendorNbAvis(vendor.getNbAvis());

		// Tags + raisons (apres score pour pouvoir conditionner)
		generateTags(r, vendor, p, sBudget, sLuxe, sStyle, sPref, sPop);
		generateRaison(r, vendor, p);

		return r;
	}

	private double round1(double v) { return Math.round(v * 10.0) / 10.0; }

	// ----------- Score: BUDGET -----------
	private double scoreBudget(Vendor v, UserProfile p) {
		double budget = p.getBudgetTotal();
		if (budget <= 0) return 0.6;

		double categoryBudget = estimateCategoryBudget(v.getCategoryId(), p);
		double prixMin = v.getPrixMin();
		double prixMax = v.getPrixMax() > 0 ? v.getPrixMax() : prixMin * 1.3;
		double prixAvg = (prixMin + prixMax) / 2.0;

		// Cas ideal : prixMax tient dans le budget alloue
		if (prixMax <= categoryBudget) return 1.0;
		// Prix moyen tient
		if (prixAvg <= categoryBudget) return 0.85;
		// Prix min tient
		if (prixMin <= categoryBudget) return 0.7;

		// Hors budget mais dans la tolerance (selon flexibilite)
		double tolerance = 0.15;
		String flex = p.getBudgetFlexibility();
		if ("FLEXIBLE".equals(flex)) tolerance = 0.30;
		else if ("TRES_FLEXIBLE".equals(flex)) tolerance = 0.50;

		if (prixMin <= categoryBudget * (1 + tolerance)) return 0.5;
		if (prixMin <= categoryBudget * (1 + tolerance * 2)) return 0.3;

		// Largement hors budget
		double ratio = categoryBudget / prixMin;
		return Math.max(ratio * 0.3, 0.05);
	}

	private double estimateCategoryBudget(int categoryId, UserProfile p) {
		double budget = p.getBudgetTotal();
		double basePct;
		// Pourcentages calibrés sur les 12 catégories actives (post-fusions v4)
		// Photo & Videaste (fusionnees), Decoration & Fleuriste (fusionnees),
		// Maquillage & Coiffure (fusionnees). Transport et Wedding Planner supprimes.
		switch (categoryId) {
			case CAT_SALLE: basePct = 0.25; break;
			case CAT_TRAITEUR:
				// Traiteur : prix par personne
				return budget * 0.30 / Math.max(1, p.getNbInvites());
			case CAT_NEGGAFA: basePct = 0.10; break;
			case CAT_PHOTO: basePct = 0.09; break;        // photo + video fusionnes
			case CAT_ISSAWA: basePct = 0.04; break;
			case CAT_ORCHESTRE: basePct = 0.06; break;
			case CAT_DECORATION: basePct = 0.10; break;   // decoration + fleuriste fusionnes
			case CAT_MAKEUP: basePct = 0.05; break;       // maquillage + coiffure fusionnes
			case CAT_CAKE: basePct = 0.03; break;
			case CAT_MYADI: basePct = 0.03; break;
			case CAT_DJ: basePct = 0.04; break;
			case CAT_HENNAYA: basePct = 0.01; break;
			default: basePct = 0.05; break;               // categories obsoletes / inconnues
		}
		// Ajuster selon priorite categorie (+/- 20%)
		int priority = priorityForCategory(categoryId, p);
		double adj = 1.0 + (priority - 3) * 0.20;
		return budget * basePct * adj;
	}

	// ----------- Score: GAMME / LUXE -----------
	private double scoreLuxe(Vendor v, UserProfile p) {
		String want = p.getNiveauLuxe();
		String have = v.getGamme();
		if (want == null || have == null) return 0.5;
		// Match table
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

	// ----------- Score: STYLE -----------
	private double scoreStyle(Vendor v, UserProfile p) {
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		if (tags.isEmpty()) return 0.5;

		int matches = 0;
		int checks = 0;

		// Style mariage
		String style = p.getStyle();
		if (style != null) {
			checks++;
			if (eq(style, "TRADITIONNEL") && hasAny(tags, "traditionnel", "fassi", "authentique", "heritage", "marocain")) matches++;
			else if (eq(style, "MODERNE") && hasAny(tags, "moderne", "contemporain", "tendance", "minimaliste")) matches++;
			else if (eq(style, "MIXTE") && hasAny(tags, "mixte", "moderne", "traditionnel", "fusion")) matches++;
			else if (eq(style, "LUXE") && hasAny(tags, "luxe", "premium", "haut-gamme", "prestige", "exclusif", "royal")) matches++;
			else if (eq(style, "SIMPLE") && hasAny(tags, "simple", "abordable", "economique", "essentiel")) matches++;
			else if (eq(style, "INTIME") && hasAny(tags, "intime", "petit", "charme", "boutique")) matches++;
		}

		// Ambiance
		String amb = p.getAmbiance();
		if (amb != null) {
			checks++;
			if (eq(amb, "INTIME") && hasAny(tags, "intime", "petit", "charme", "convivial")) matches++;
			else if (eq(amb, "GRANDIOSE") && hasAny(tags, "grand", "luxe", "complet", "royal", "spectaculaire")) matches++;
			else if (eq(amb, "FESTIVE") && hasAny(tags, "festif", "dynamique", "ambiance", "electrique", "danse")) matches++;
			else if (eq(amb, "ROMANTIQUE") && hasAny(tags, "romantique", "delicat", "doux", "lumineux", "fleuri")) matches++;
			else if (eq(amb, "FAMILIALE") && hasAny(tags, "familial", "convivial", "genereux", "chaleureux")) matches++;
			else if (eq(amb, "LUXUEUSE") && hasAny(tags, "luxe", "premium", "prestige", "haut-gamme")) matches++;
			else if (eq(amb, "TRADITIONNELLE") && hasAny(tags, "traditionnel", "fassi", "authentique", "heritage")) matches++;
		}

		// Theme couleur (correspondances thematiques)
		String theme = p.getThemeCouleur();
		if (theme != null) {
			checks++;
			String t = theme.toLowerCase();
			if (t.contains("royal") || t.contains("fassi")) {
				if (hasAny(tags, "fassi", "royal", "andalous", "heritage", "or")) matches++;
			} else if (t.contains("oriental")) {
				if (hasAny(tags, "oriental", "marocain", "traditionnel", "or")) matches++;
			} else if (t.contains("boheme") || t.contains("boho")) {
				if (hasAny(tags, "boheme", "naturel", "champetre", "fleuri")) matches++;
			} else if (t.contains("minimal") || t.contains("epure")) {
				if (hasAny(tags, "minimaliste", "moderne", "epure", "blanc")) matches++;
			} else if (t.contains("zellige") || t.contains("andalou")) {
				if (hasAny(tags, "zellige", "andalous", "traditionnel", "fassi")) matches++;
			} else {
				// Correspondance partielle
				matches += 0;
			}
		}

		if (checks == 0) return 0.5;
		return Math.max((double) matches / checks, 0.2);
	}

	// ----------- Score: PREFERENCES SPECIFIQUES -----------
	private double scorePreferences(Vendor v, UserProfile p) {
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		int cat = v.getCategoryId();

		switch (cat) {
			case CAT_ORCHESTRE:
			case CAT_ISSAWA:
			case CAT_DJ:
				return matchMusic(tags, cat, p);
			case CAT_TRAITEUR:
				return matchCuisine(tags, p);
			case CAT_PHOTO:
			case CAT_VIDEO:
				return matchPhoto(tags, p);
			case CAT_DECORATION:
			case CAT_FLEURISTE:
				return matchDecoration(tags, p);
			case CAT_NEGGAFA:
			case CAT_MYADI:
				return matchNeggafa(tags, p);
			case CAT_MAKEUP:
			case CAT_COIFFURE:
			case CAT_HENNAYA:
				return matchBeauty(tags, p);
			case CAT_SALLE:
				return matchSalle(tags, p);
			case CAT_CAKE:
				return matchCake(tags, p);
			default:
				return 0.5;
		}
	}

	private double matchMusic(String tags, int cat, UserProfile p) {
		String want = p.getTypeMusique();
		if (want == null) return 0.5;
		if (eq(want, "ORCHESTRE") && cat == CAT_ORCHESTRE) return 1.0;
		if (eq(want, "ISSAWA") && cat == CAT_ISSAWA) return 1.0;
		if (eq(want, "DJ") && cat == CAT_DJ) return 1.0;
		if (eq(want, "TRADITIONNELLE")) {
			if (cat == CAT_ISSAWA) return 1.0;
			if (cat == CAT_ORCHESTRE && hasAny(tags, "chaabi", "andalous", "traditionnel")) return 0.85;
			if (cat == CAT_DJ) return 0.25;
		}
		if (eq(want, "MODERNE")) {
			if (cat == CAT_DJ) return 0.95;
			if (cat == CAT_ORCHESTRE && tags.contains("moderne")) return 0.7;
			if (cat == CAT_ISSAWA) return 0.25;
		}
		if (eq(want, "MIXTE")) return 0.85;
		return 0.45;
	}

	private double matchCuisine(String tags, UserProfile p) {
		String want = p.getTypeCuisine();
		if (want == null) return 0.5;
		if (eq(want, "MAROCAINE") && hasAny(tags, "marocain", "fassi", "authentique", "tajine", "pastilla")) return 1.0;
		if (eq(want, "INTERNATIONALE") && hasAny(tags, "international", "moderne", "creatif", "fusion")) return 1.0;
		if (eq(want, "MIXTE")) return 0.85;
		return 0.45;
	}

	private double matchPhoto(String tags, UserProfile p) {
		String want = p.getPrefPhoto();
		if (want == null) return 0.5;
		if (eq(want, "CLASSIQUE") && hasAny(tags, "classique", "studio", "elegant")) return 1.0;
		if (eq(want, "ARTISTIQUE") && hasAny(tags, "artistique", "creatif", "editorial", "concept")) return 1.0;
		if (eq(want, "REPORTAGE") && hasAny(tags, "reportage", "naturel", "emotionnel", "instant")) return 1.0;
		if (eq(want, "DRONE") && tags.contains("drone")) return 1.0;
		return 0.4;
	}

	private double matchDecoration(String tags, UserProfile p) {
		String want = p.getPrefDecoration();
		if (want == null) return 0.5;
		if (eq(want, "TRADITIONNELLE") && hasAny(tags, "traditionnel", "oriental", "fassi", "zellige")) return 1.0;
		if (eq(want, "MODERNE") && hasAny(tags, "moderne", "elegant", "contemporain", "epure")) return 1.0;
		if (eq(want, "FLORALE") && hasAny(tags, "floral", "roses", "pivoines", "frais", "bouquet")) return 1.0;
		if (eq(want, "MINIMALISTE") && hasAny(tags, "minimaliste", "simple", "abordable", "epure")) return 1.0;
		if (eq(want, "LUXUEUSE") && hasAny(tags, "luxe", "haut-gamme", "sur-mesure", "premium")) return 1.0;
		return 0.4;
	}

	private double matchNeggafa(String tags, UserProfile p) {
		String want = p.getStyleNeggafa();
		double score = 0.5;
		if (want != null) {
			if (eq(want, "TRADITIONNEL") && hasAny(tags, "traditionnel", "fassi", "authentique", "heritage")) score = 1.0;
			else if (eq(want, "MODERNE") && hasAny(tags, "moderne", "contemporain", "chic")) score = 1.0;
			else if (eq(want, "MIXTE")) score = 0.85;
			else score = 0.4;
		}
		// Bonus showroom etoffe pour beaucoup de tenues
		if (p.getNbTenuesNeggafa() >= 5 && hasAny(tags, "showroom", "exclusif", "choix", "collection")) {
			score = Math.min(1.0, score + 0.1);
		}
		return score;
	}

	private double matchBeauty(String tags, UserProfile p) {
		String style = p.getStyle();
		if (style == null) return 0.5;
		if (eq(style, "TRADITIONNEL") && hasAny(tags, "oriental", "traditionnel", "fassi")) return 1.0;
		if (eq(style, "MODERNE") && hasAny(tags, "moderne", "tendance", "glow")) return 1.0;
		if (eq(style, "LUXE") && hasAny(tags, "luxe", "haut-gamme", "premium")) return 1.0;
		if (eq(style, "MIXTE")) return 0.85;
		return 0.5;
	}

	private double matchSalle(String tags, UserProfile p) {
		double score = 0.5;
		String guestSize = p.getGuestSize();
		// Match capacite via tags
		if ("INTIME".equals(guestSize) && hasAny(tags, "intime", "petit", "boutique", "charme")) score = 1.0;
		else if ("MOYEN".equals(guestSize) && hasAny(tags, "moyen", "polyvalent", "elegant")) score = 0.9;
		else if ("GRAND".equals(guestSize) && hasAny(tags, "grand", "spacieux", "complet")) score = 1.0;
		else if ("TRES_GRAND".equals(guestSize) && hasAny(tags, "tres-grand", "complet", "royal", "luxe")) score = 1.0;
		// Lieu : palais / villa / hotel
		String lieu = p.getStyle(); // approximation : on n'a pas lieuCeremonie typed sur profil
		if (lieu != null && eq(lieu, "TRADITIONNEL") && hasAny(tags, "palais", "riad", "fassi")) score = Math.min(1.0, score + 0.1);
		return score;
	}

	private double matchCake(String tags, UserProfile p) {
		String style = p.getStyle();
		if (style == null) return 0.6;
		if (eq(style, "MODERNE") && hasAny(tags, "moderne", "design", "minimaliste")) return 1.0;
		if (eq(style, "TRADITIONNEL") && hasAny(tags, "fassi", "traditionnel", "marocain")) return 1.0;
		if (eq(style, "LUXE") && hasAny(tags, "luxe", "premium", "exclusif")) return 1.0;
		return 0.6;
	}

	// ----------- Score: PRIORITE -----------
	private double scorePriority(Vendor v, UserProfile p) {
		int prio = priorityForCategory(v.getCategoryId(), p);
		return prio / 5.0;
	}

	private int priorityForCategory(int cat, UserProfile p) {
		switch (cat) {
			case CAT_SALLE: return p.getPrioriteSalle();
			case CAT_TRAITEUR: return p.getPrioriteTraiteur();
			case CAT_PHOTO:
			case CAT_VIDEO: return p.getPrioritePhoto();
			case CAT_ORCHESTRE:
			case CAT_ISSAWA:
			case CAT_DJ: return p.getPrioriteMusique();
			case CAT_DECORATION:
			case CAT_FLEURISTE: return p.getPrioriteDecoration();
			case CAT_NEGGAFA:
			case CAT_MYADI: return p.getPrioriteNeggafa();
			case CAT_MAKEUP:
			case CAT_COIFFURE:
			case CAT_HENNAYA: return p.getPrioriteMakeup();
			default: return 3;
		}
	}

	// ----------- Score: POPULARITE -----------
	private double scorePopularite(Vendor v) {
		double rating = v.getRating();
		int avis = v.getNbAvis();
		if (rating <= 0) return 0.5;
		double r = (rating - 3.0) / 2.0; // 3.0->0, 5.0->1
		if (r < 0) r = 0;
		// pondere par volume d'avis (saturation a 100 avis)
		double vol = Math.min(avis / 100.0, 1.0);
		return 0.6 * r + 0.4 * vol;
	}

	// ----------- Score: CULTUREL (fes / fassi / heritage) -----------
	private double scoreCulturel(Vendor v, UserProfile p) {
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		String city = v.getCity() != null ? v.getCity().toLowerCase() : "";
		double score = 0;
		if (city.contains("fes") || city.contains("fès")) score += 0.5;
		String style = p.getStyle();
		String theme = p.getThemeCouleur() != null ? p.getThemeCouleur().toLowerCase() : "";
		if (eq(style, "TRADITIONNEL") || theme.contains("fassi") || theme.contains("royal") || theme.contains("zellige")) {
			if (hasAny(tags, "fassi", "andalous", "heritage", "zellige")) score += 0.5;
		}
		return Math.min(score, 1.0);
	}

	// ----------- Score: MOOD KEYWORDS (section 6) -----------
	private double scoreMood(Vendor v, UserProfile p) {
		List<String> moods = p.getMoodKeywords();
		if (moods == null || moods.isEmpty()) return 0.0;
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		String desc = v.getDescription() != null ? v.getDescription().toLowerCase() : "";
		int hits = 0;
		for (int i = 0; i < moods.size(); i++) {
			String m = moods.get(i);
			if (m.length() < 3) continue;
			if (tags.contains(m) || desc.contains(m)) hits++;
		}
		if (hits == 0) return 0.0;
		return Math.min(1.0, hits / 3.0);
	}

	// ============================================================
	// TAGS
	// ============================================================

	private void generateTags(Recommendation r, Vendor v, UserProfile p,
			double sBudget, double sLuxe, double sStyle, double sPref, double sPop) {
		double total = r.getScore();

		// Tags principaux
		if (total >= SCORE_EXCELLENT && sBudget >= 0.8 && sLuxe >= 0.8) r.addTag("Coup de coeur");
		if (sBudget >= 0.85 && sLuxe >= 0.7 && sPref >= 0.7) r.addTag("Bon rapport qualite/prix");
		if (sLuxe >= 0.95 && eq(v.getGamme(), "PREMIUM")) r.addTag("Choix luxe");
		if (eq(v.getGamme(), "ECONOMIQUE") && sBudget >= 0.9) r.addTag("Petit budget");
		if (sStyle >= 0.85) r.addTag("Style aligne");
		if (sPref >= 0.95) r.addTag("Preference exacte");
		if (sPop >= 0.8 && v.getNbAvis() >= 50) r.addTag("Tres bien note");

		// Tag culturel
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		if (hasAny(tags, "fassi", "heritage") && eq(p.getStyle(), "TRADITIONNEL")) r.addTag("Authenticite fassie");
		if (hasAny(tags, "moderne", "tendance") && eq(p.getStyle(), "MODERNE")) r.addTag("Esprit moderne");
		if (hasAny(tags, "drone") && v.getCategoryId() == CAT_PHOTO) r.addTag("Drone inclus");

		// Tag categorie prioritaire
		List<Integer> top = p.getTopCategoryIds();
		if (top != null && !top.isEmpty() && top.get(0) == v.getCategoryId()) {
			r.addTag("Categorie prioritaire");
		}

		// Tag depassement budget
		if (sBudget < 0.4) r.addTag("Hors budget");

		// Tag intimite/grandeur
		if ("INTIME".equals(p.getGuestSize()) && hasAny(tags, "intime", "petit")) r.addTag("Format intime");
		if ("TRES_GRAND".equals(p.getGuestSize()) && hasAny(tags, "grand", "complet")) r.addTag("Grand evenement");

		// Si aucun tag, en ajouter un par defaut
		if (r.getTags().isEmpty()) {
			if (total >= SCORE_GOOD) r.addTag("Bon choix");
			else if (total >= SCORE_OK) r.addTag("A considerer");
			else r.addTag("Alternative");
		}
	}

	// ============================================================
	// RAISONS
	// ============================================================

	private void generateRaison(Recommendation r, Vendor v, UserProfile p) {
		StringBuilder full = new StringBuilder();
		double s = r.getScore();

		// Qualificatif
		String qualif;
		if (s >= SCORE_EXCELLENT) qualif = "Correspondance ideale";
		else if (s >= SCORE_GOOD) qualif = "Excellente correspondance";
		else if (s >= SCORE_OK) qualif = "Bonne correspondance";
		else qualif = "Option envisageable";
		full.append(qualif);

		// Pourquoi : 2 ou 3 raisons concretes pointant le questionnaire
		List<String> reasons = new ArrayList<String>();

		// Budget
		if (r.getScoreBudget() >= 80) reasons.add("rentre dans votre budget pour " + categoryLabel(v.getCategoryId()));
		else if (r.getScoreBudget() < 40) reasons.add("legerement au-dessus de votre budget alloue");

		// Luxe
		if (r.getScoreLuxe() >= 95) {
			String want = p.getNiveauLuxe();
			if (want != null) reasons.add("gamme " + v.getGamme().toLowerCase() + " alignee avec votre niveau \"" + niveauLabel(want) + "\"");
		}

		// Style
		String style = p.getStyle();
		if (style != null && r.getScoreStyle() >= 70) {
			reasons.add("style " + styleLabel(style).toLowerCase() + " confirme par les tags du prestataire");
		}

		// Preferences specifiques
		String prefReason = preferenceReason(v, p);
		if (prefReason != null) reasons.add(prefReason);

		// Popularite
		if (v.getRating() >= 4.5 && v.getNbAvis() >= 30) {
			reasons.add("tres bien note (" + v.getRating() + "/5 sur " + v.getNbAvis() + " avis)");
		}

		// Categorie prioritaire
		if (priorityForCategory(v.getCategoryId(), p) >= 4) {
			reasons.add("categorie marquee comme prioritaire dans votre questionnaire");
		}

		// Construire la phrase
		if (!reasons.isEmpty()) {
			full.append(" : ");
			int max = Math.min(3, reasons.size());
			for (int i = 0; i < max; i++) {
				if (i > 0) full.append(i == max - 1 ? " et " : ", ");
				full.append(reasons.get(i));
			}
			full.append(".");
		} else {
			full.append(".");
		}

		r.setRaison(full.toString());
		r.setRaisonShort(qualif);
	}

	private String preferenceReason(Vendor v, UserProfile p) {
		String tags = v.getTags() != null ? v.getTags().toLowerCase() : "";
		int cat = v.getCategoryId();
		if (cat == CAT_TRAITEUR && p.getTypeCuisine() != null) {
			if (eq(p.getTypeCuisine(), "MAROCAINE") && hasAny(tags, "marocain", "fassi"))
				return "cuisine marocaine authentique comme demandee";
			if (eq(p.getTypeCuisine(), "INTERNATIONALE") && hasAny(tags, "international", "creatif"))
				return "cuisine internationale conforme a vos gouts";
		}
		if ((cat == CAT_ORCHESTRE || cat == CAT_ISSAWA || cat == CAT_DJ) && p.getTypeMusique() != null) {
			return "type de musique \"" + p.getTypeMusique().toLowerCase().replace("_", " ") + "\" coche";
		}
		if (cat == CAT_PHOTO && p.getPrefPhoto() != null && hasAny(tags, p.getPrefPhoto().toLowerCase())) {
			return "style photo " + p.getPrefPhoto().toLowerCase() + " maitrise";
		}
		if ((cat == CAT_DECORATION || cat == CAT_FLEURISTE) && p.getPrefDecoration() != null) {
			return "decoration " + p.getPrefDecoration().toLowerCase() + " correspondante";
		}
		if (cat == CAT_NEGGAFA && p.getStyleNeggafa() != null) {
			return "neggafa " + p.getStyleNeggafa().toLowerCase() + " et " + p.getNbTenuesNeggafa() + " tenues prevues";
		}
		return null;
	}

	private String categoryLabel(int cat) {
		switch (cat) {
			case CAT_SALLE: return "la salle";
			case CAT_TRAITEUR: return "le traiteur";
			case CAT_PHOTO: return "le photographe";
			case CAT_VIDEO: return "le videaste";
			case CAT_NEGGAFA: return "la neggafa";
			case CAT_ORCHESTRE: return "l'orchestre";
			case CAT_ISSAWA: return "le groupe issawa";
			case CAT_DJ: return "le DJ";
			case CAT_DECORATION: return "la decoration";
			case CAT_FLEURISTE: return "le fleuriste";
			case CAT_MAKEUP: return "le maquillage";
			case CAT_COIFFURE: return "la coiffure";
			case CAT_CAKE: return "le wedding cake";
			case CAT_MYADI: return "le myadi";
			default: return "ce poste";
		}
	}

	private String styleLabel(String s) {
		if (eq(s, "TRADITIONNEL")) return "Traditionnel";
		if (eq(s, "MODERNE")) return "Moderne";
		if (eq(s, "MIXTE")) return "Mixte";
		if (eq(s, "LUXE")) return "Luxe";
		if (eq(s, "SIMPLE")) return "Simple";
		if (eq(s, "INTIME")) return "Intime";
		return s;
	}

	private String niveauLabel(String s) {
		if (eq(s, "ECONOMIQUE")) return "Economique";
		if (eq(s, "MOYEN")) return "Moyen";
		if (eq(s, "PREMIUM")) return "Premium";
		if (eq(s, "ULTRA_LUXE")) return "Ultra-luxe";
		return s;
	}

	// ============================================================
	// BLOCS DE RECOMMANDATION
	// ============================================================

	/**
	 * Decoupe les recommandations en blocs thematiques.
	 * Cles :
	 *   - topPicks       : top 12 (les meilleurs scores globaux)
	 *   - bestValue      : meilleur rapport qualite/prix (eco/moyen + bon score)
	 *   - mostChic       : premium/luxe + score eleve
	 *   - economic       : gamme economique + score correct
	 *   - premium        : gamme premium + score eleve
	 *   - prioritePicks  : prestataires des 2 categories les plus prioritaires
	 *   - alternatives   : score 35-55 (si on veut comparer)
	 */
	public Map<String, List<Recommendation>> buildBlocks(List<Recommendation> all, UserProfile p) {
		Map<String, List<Recommendation>> blocks = new LinkedHashMap<String, List<Recommendation>>();

		blocks.put("topPicks", takeTop(filterByMinScore(all, SCORE_GOOD), 12));

		List<Recommendation> bestValue = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (r.getScoreBudget() >= 80 && r.getScore() >= SCORE_OK
					&& (eq(r.getVendorGamme(), "ECONOMIQUE") || eq(r.getVendorGamme(), "MOYEN"))) {
				bestValue.add(r);
			}
		}
		blocks.put("bestValue", takeTop(bestValue, 8));

		List<Recommendation> chic = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (eq(r.getVendorGamme(), "PREMIUM") && r.getScore() >= SCORE_OK) {
				chic.add(r);
			}
		}
		blocks.put("mostChic", takeTop(chic, 8));

		List<Recommendation> eco = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (eq(r.getVendorGamme(), "ECONOMIQUE") && r.getScore() >= 50) {
				eco.add(r);
			}
		}
		blocks.put("economic", takeTop(eco, 8));

		List<Recommendation> premium = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (eq(r.getVendorGamme(), "PREMIUM") && r.getScore() >= SCORE_GOOD) {
				premium.add(r);
			}
		}
		blocks.put("premium", takeTop(premium, 6));

		// Prioritaires : les 2 categories au top
		List<Integer> top = p.getTopCategoryIds();
		List<Recommendation> prio = new ArrayList<Recommendation>();
		if (top != null && top.size() >= 2) {
			Integer c1 = top.get(0);
			Integer c2 = top.get(1);
			for (int i = 0; i < all.size(); i++) {
				Recommendation r = all.get(i);
				if (r.getVendorCategoryId() == c1 || r.getVendorCategoryId() == c2) {
					prio.add(r);
				}
			}
		}
		blocks.put("prioritePicks", takeTop(prio, 8));

		List<Recommendation> alt = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (r.getScore() >= SCORE_MIN_TO_KEEP && r.getScore() < SCORE_OK) {
				alt.add(r);
			}
		}
		blocks.put("alternatives", takeTop(alt, 6));

		return blocks;
	}

	private List<Recommendation> filterByMinScore(List<Recommendation> all, double min) {
		List<Recommendation> out = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).getScore() >= min) out.add(all.get(i));
		}
		return out;
	}

	private List<Recommendation> takeTop(List<Recommendation> in, int n) {
		List<Recommendation> out = new ArrayList<Recommendation>();
		int max = Math.min(n, in.size());
		for (int i = 0; i < max; i++) out.add(in.get(i));
		return out;
	}

	// ============================================================
	// JSON SERIALIZATION
	// ============================================================

	public String toJson(Recommendation r) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"vendorId\":").append(r.getVendorId());
		sb.append(",\"vendorName\":\"").append(JsonUtil.escapeJson(r.getVendorName())).append("\"");
		sb.append(",\"category\":\"").append(JsonUtil.escapeJson(safe(r.getVendorCategory()))).append("\"");
		sb.append(",\"categoryId\":").append(r.getVendorCategoryId());
		sb.append(",\"gamme\":\"").append(JsonUtil.escapeJson(safe(r.getVendorGamme()))).append("\"");
		sb.append(",\"prixMin\":").append(r.getVendorPrixMin());
		sb.append(",\"prixMax\":").append(r.getVendorPrixMax());
		sb.append(",\"city\":\"").append(JsonUtil.escapeJson(safe(r.getVendorCity()))).append("\"");
		sb.append(",\"phone\":\"").append(JsonUtil.escapeJson(safe(r.getVendorPhone()))).append("\"");
		sb.append(",\"instagram\":\"").append(JsonUtil.escapeJson(safe(r.getVendorInstagram()))).append("\"");
		sb.append(",\"rating\":").append(r.getVendorRating());
		sb.append(",\"nbAvis\":").append(r.getVendorNbAvis());
		sb.append(",\"score\":").append(r.getScore());
		sb.append(",\"scoreBudget\":").append(r.getScoreBudget());
		sb.append(",\"scoreLuxe\":").append(r.getScoreLuxe());
		sb.append(",\"scoreStyle\":").append(r.getScoreStyle());
		sb.append(",\"scorePopularite\":").append(r.getScorePopularite());
		sb.append(",\"raison\":\"").append(JsonUtil.escapeJson(safe(r.getRaison()))).append("\"");
		sb.append(",\"tags\":[");
		List<String> tags = r.getTags();
		if (tags != null) {
			for (int i = 0; i < tags.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append("\"").append(JsonUtil.escapeJson(tags.get(i))).append("\"");
			}
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	public String profileToJson(UserProfile p) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"style\":\"").append(JsonUtil.escapeJson(safe(p.getStyle()))).append("\"");
		sb.append(",\"ambiance\":\"").append(JsonUtil.escapeJson(safe(p.getAmbiance()))).append("\"");
		sb.append(",\"niveauLuxe\":\"").append(JsonUtil.escapeJson(safe(p.getNiveauLuxe()))).append("\"");
		sb.append(",\"themeCouleur\":\"").append(JsonUtil.escapeJson(safe(p.getThemeCouleur()))).append("\"");
		sb.append(",\"saison\":\"").append(JsonUtil.escapeJson(safe(p.getSaison()))).append("\"");
		sb.append(",\"budgetTotal\":").append(p.getBudgetTotal());
		sb.append(",\"budgetPerGuest\":").append(p.getBudgetPerGuest());
		sb.append(",\"budgetTier\":\"").append(JsonUtil.escapeJson(safe(p.getBudgetTier()))).append("\"");
		sb.append(",\"budgetFlexibility\":\"").append(JsonUtil.escapeJson(safe(p.getBudgetFlexibility()))).append("\"");
		sb.append(",\"nbInvites\":").append(p.getNbInvites());
		sb.append(",\"guestSize\":\"").append(JsonUtil.escapeJson(safe(p.getGuestSize()))).append("\"");
		sb.append(",\"typeMusique\":\"").append(JsonUtil.escapeJson(safe(p.getTypeMusique()))).append("\"");
		sb.append(",\"typeCuisine\":\"").append(JsonUtil.escapeJson(safe(p.getTypeCuisine()))).append("\"");
		sb.append(",\"prefPhoto\":\"").append(JsonUtil.escapeJson(safe(p.getPrefPhoto()))).append("\"");
		sb.append(",\"prefDecoration\":\"").append(JsonUtil.escapeJson(safe(p.getPrefDecoration()))).append("\"");
		sb.append(",\"styleNeggafa\":\"").append(JsonUtil.escapeJson(safe(p.getStyleNeggafa()))).append("\"");
		sb.append(",\"nbTenuesNeggafa\":").append(p.getNbTenuesNeggafa());
		sb.append(",\"topCategories\":[");
		List<Integer> top = p.getTopCategoryIds();
		if (top != null) {
			for (int i = 0; i < top.size(); i++) {
				if (i > 0) sb.append(",");
				sb.append(top.get(i));
			}
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	// ============================================================
	// HELPERS
	// ============================================================

	private boolean eq(String a, String b) {
		return a != null && a.equalsIgnoreCase(b);
	}

	private boolean hasAny(String text, String... keywords) {
		if (text == null) return false;
		for (int i = 0; i < keywords.length; i++) {
			if (text.contains(keywords[i])) return true;
		}
		return false;
	}

	private String safe(String s) {
		return s != null ? s : "";
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
