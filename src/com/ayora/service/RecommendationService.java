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
 * Couche service au-dessus de l'IA Ayora.
 *
 * Le moteur d'IA proprement dit est dans {@link AyoraRecommendationEngine}
 * (algorithme k-NN pondere a 6 dimensions, explicable et 100% local). Cette
 * classe se charge des aspects "infrastructure" :
 *
 *   - lecture des prestataires en base (VendorDao)
 *   - persistance des recommandations (RecommendationDao)
 *   - regroupement par blocs thematiques (top, bestValue, ...)
 *   - serialisation JSON pour le frontend
 *
 * On garde la signature historique des methodes publiques pour ne pas casser
 * l'API utilisee par les servlets.
 */
public class RecommendationService {

	// Seuils de score pour les blocs / qualificatifs
	public static final double SCORE_EXCELLENT  = 85.0;
	public static final double SCORE_GOOD       = 70.0;
	public static final double SCORE_OK         = 55.0;
	public static final double SCORE_MIN_TO_KEEP = 35.0;

	private final VendorDao vendorDao;
	private final RecommendationDao recommendationDao;
	private final AyoraRecommendationEngine engine;

	public RecommendationService() {
		this.vendorDao = new VendorDao();
		this.recommendationDao = new RecommendationDao();
		this.engine = new AyoraRecommendationEngine();
	}

	// ============================================================
	// API PUBLIQUE
	// ============================================================

	/** Genere les recommandations, les persiste et les retourne. */
	public List<Recommendation> generateRecommendations(int userId, QuestionnaireAnswer answers) {
		recommendationDao.deleteByUserId(userId);

		UserProfile profile = engine.buildUserProfile(answers);
		profile.setUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		java.util.Set<Integer> wantedCats = engine.wantedCategoryIds(profile);
		List<Recommendation> recommendations = new Vector<Recommendation>();

		for (int i = 0; i < allVendors.size(); i++) {
			Vendor v = allVendors.get(i);
			if (wantedCats != null && !wantedCats.isEmpty()
					&& !wantedCats.contains(v.getCategoryId())) continue;
			Recommendation rec = engine.scoreVendor(v, profile, userId);
			if (rec.getScore() >= SCORE_MIN_TO_KEEP) {
				recommendationDao.create(rec);
				recommendations.add(rec);
			}
		}
		sortByScore(recommendations);
		return recommendations;
	}

	/** Recalcule sans persister (pour les GET). */
	public List<Recommendation> computeRecommendations(int userId, QuestionnaireAnswer answers) {
		UserProfile profile = engine.buildUserProfile(answers);
		profile.setUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		java.util.Set<Integer> wantedCats = engine.wantedCategoryIds(profile);
		List<Recommendation> recommendations = new Vector<Recommendation>();
		for (int i = 0; i < allVendors.size(); i++) {
			Vendor v = allVendors.get(i);
			if (wantedCats != null && !wantedCats.isEmpty()
					&& !wantedCats.contains(v.getCategoryId())) continue;
			Recommendation rec = engine.scoreVendor(v, profile, userId);
			if (rec.getScore() >= SCORE_MIN_TO_KEEP) recommendations.add(rec);
		}
		sortByScore(recommendations);
		return recommendations;
	}

	/** Construction du profil (delegue au moteur IA). */
	public UserProfile buildUserProfile(QuestionnaireAnswer a) {
		return engine.buildUserProfile(a);
	}

	/** Score d'un vendor pour un profil (delegue au moteur IA). */
	public Recommendation scoreVendor(Vendor vendor, UserProfile p, int userId) {
		return engine.scoreVendor(vendor, p, userId);
	}

	/** Acces au moteur IA (utile pour exposer les poids cote front si besoin). */
	public AyoraRecommendationEngine getEngine() {
		return engine;
	}

	// ============================================================
	// BLOCS DE RECOMMANDATION
	// ============================================================

	/**
	 * Decoupe les recommandations en blocs thematiques (vue secondaire).
	 *
	 *   - topPicks       : top 12 (meilleurs scores globaux)
	 *   - bestValue      : meilleur rapport qualite/prix
	 *   - mostChic       : premium/luxe + score eleve
	 *   - economic       : gamme economique + score correct
	 *   - premium        : gamme premium + score eleve
	 *   - prioritePicks  : prestataires des 2 categories prioritaires
	 *   - alternatives   : score 35-55 (a comparer)
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
			if (eq(r.getVendorGamme(), "PREMIUM") && r.getScore() >= SCORE_OK) chic.add(r);
		}
		blocks.put("mostChic", takeTop(chic, 8));

		List<Recommendation> eco = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (eq(r.getVendorGamme(), "ECONOMIQUE") && r.getScore() >= 50) eco.add(r);
		}
		blocks.put("economic", takeTop(eco, 8));

		List<Recommendation> premium = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (eq(r.getVendorGamme(), "PREMIUM") && r.getScore() >= SCORE_GOOD) premium.add(r);
		}
		blocks.put("premium", takeTop(premium, 6));

		List<Integer> top = p.getTopCategoryIds();
		List<Recommendation> prio = new ArrayList<Recommendation>();
		if (top != null && top.size() >= 2) {
			Integer c1 = top.get(0);
			Integer c2 = top.get(1);
			for (int i = 0; i < all.size(); i++) {
				Recommendation r = all.get(i);
				if (r.getVendorCategoryId() == c1 || r.getVendorCategoryId() == c2) prio.add(r);
			}
		}
		blocks.put("prioritePicks", takeTop(prio, 8));

		List<Recommendation> alt = new ArrayList<Recommendation>();
		for (int i = 0; i < all.size(); i++) {
			Recommendation r = all.get(i);
			if (r.getScore() >= SCORE_MIN_TO_KEEP && r.getScore() < SCORE_OK) alt.add(r);
		}
		blocks.put("alternatives", takeTop(alt, 6));

		return blocks;
	}

	/**
	 * Vue principale : 3 prestataires par categorie (k-NN, k = 3 par classe).
	 * Delegue directement au moteur d'IA.
	 */
	public Map<String, List<Recommendation>> buildTopPerCategory(List<Recommendation> all, UserProfile p) {
		return engine.topPerCategory(all, p);
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
	// JSON
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
		// Sous-scores IA Ayora (les 6 dimensions du k-NN pondere)
		sb.append(",\"scoreBudget\":").append(r.getScoreBudget());
		sb.append(",\"scoreStyle\":").append(r.getScoreStyle());
		sb.append(",\"scoreCity\":").append(r.getScoreCity());
		sb.append(",\"scoreGuestCount\":").append(r.getScoreGuestCount());
		sb.append(",\"scoreLuxe\":").append(r.getScoreLuxe());
		sb.append(",\"scoreQuality\":").append(r.getScoreQuality());
		// Compatibilite : on continue d'exposer scorePopularite (= scoreQuality)
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
		// Poids du moteur IA (transparence pour le frontend)
		sb.append(",\"aiWeights\":{");
		sb.append("\"budget\":").append(AyoraRecommendationEngine.WEIGHT_BUDGET);
		sb.append(",\"style\":").append(AyoraRecommendationEngine.WEIGHT_STYLE);
		sb.append(",\"city\":").append(AyoraRecommendationEngine.WEIGHT_CITY);
		sb.append(",\"guests\":").append(AyoraRecommendationEngine.WEIGHT_GUESTS);
		sb.append(",\"luxury\":").append(AyoraRecommendationEngine.WEIGHT_LUXURY);
		sb.append(",\"quality\":").append(AyoraRecommendationEngine.WEIGHT_QUALITY);
		sb.append("}");
		sb.append("}");
		return sb.toString();
	}

	// ============================================================
	// HELPERS
	// ============================================================

	private boolean eq(String a, String b) {
		return a != null && a.equalsIgnoreCase(b);
	}

	private String safe(String s) {
		return s != null ? s : "";
	}

	/** Tri par selection (pattern simple du cours). */
	private void sortByScore(List<Recommendation> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			int maxIndex = i;
			for (int j = i + 1; j < list.size(); j++) {
				if (list.get(j).getScore() > list.get(maxIndex).getScore()) maxIndex = j;
			}
			if (maxIndex != i) {
				Recommendation tmp = list.get(i);
				list.set(i, list.get(maxIndex));
				list.set(maxIndex, tmp);
			}
		}
	}
}
