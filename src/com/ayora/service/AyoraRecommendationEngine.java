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
}
