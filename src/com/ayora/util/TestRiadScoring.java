package com.ayora.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ayora.dao.VendorDao;
import com.ayora.model.Recommendation;
import com.ayora.model.UserProfile;
import com.ayora.model.Vendor;

/**
 * Test : prouve que les choix du questionnaire (lieuCeremonie) modifient
 * VRAIMENT le classement des recommandations. Compare deux profils
 * strictement identiques sauf le type de lieu : RIAD vs HOTEL.
 *
 * Resultat attendu :
 *   - Profil RIAD : riads/palais en tete
 *   - Profil HOTEL : hotels/resorts en tete
 *   - Les deux classements sont DIFFERENTS (prouve par diff de noms)
 */
public class TestRiadScoring {

	public static void main(String[] args) throws Exception {
		MySQLDataSource ds = new MySQLDataSource("ayora_db", "root", "");
		Database db = new Database(ds);
		VendorDao vDao = new VendorDao(db);
		AyoraRecommendationEngine engine = new AyoraRecommendationEngine();

		List<Vendor> salles = vDao.findByCategory(11);
		System.out.println(">> " + salles.size() + " vendors categorie SALLE (id=11) en base\n");

		// Compte les venue_type pour confirmer la migration
		int nbRiad = 0, nbPalais = 0, nbSalle = 0, nbHotel = 0, nbJardin = 0, nbPiscine = 0, nbAutre = 0, nbNull = 0;
		for (Vendor v : salles) {
			String t = v.getVenueType();
			if (t == null || t.isEmpty()) nbNull++;
			else if ("RIAD".equalsIgnoreCase(t)) nbRiad++;
			else if ("PALAIS".equalsIgnoreCase(t)) nbPalais++;
			else if ("SALLE".equalsIgnoreCase(t)) nbSalle++;
			else if ("HOTEL".equalsIgnoreCase(t)) nbHotel++;
			else if ("JARDIN".equalsIgnoreCase(t)) nbJardin++;
			else if ("PISCINE".equalsIgnoreCase(t)) nbPiscine++;
			else nbAutre++;
		}
		System.out.println("Repartition venue_type : RIAD=" + nbRiad + " PALAIS=" + nbPalais
			+ " SALLE=" + nbSalle + " HOTEL=" + nbHotel + " JARDIN=" + nbJardin
			+ " PISCINE=" + nbPiscine + " AUTRE=" + nbAutre + " NULL=" + nbNull + "\n");

		// Profil de base identique pour les deux tests.
		UserProfile base = new UserProfile();
		base.setBudgetTotal(150000);
		base.setNbInvites(200);
		base.setBudgetPerGuest(750);
		base.setBudgetTier("CONFORTABLE");
		base.setGuestSize("MOYEN");
		base.setNiveauLuxe("PREMIUM");
		base.setStyle("TRADITIONNEL");
		base.setUserCity("Fes");
		base.setCityTolerance(2);
		base.setPrioriteSalle(5);

		System.out.println("================================================================================");
		System.out.println("VALEURS DU QUESTIONNAIRE UTILISEES");
		System.out.println("================================================================================");
		System.out.println("  budgetTotal      = " + base.getBudgetTotal());
		System.out.println("  nbInvites        = " + base.getNbInvites());
		System.out.println("  budgetPerGuest   = " + base.getBudgetPerGuest());
		System.out.println("  niveauLuxe       = " + base.getNiveauLuxe());
		System.out.println("  style            = " + base.getStyle());
		System.out.println("  userCity         = " + base.getUserCity());
		System.out.println("  cityTolerance    = " + base.getCityTolerance());
		System.out.println("  prioriteSalle    = " + base.getPrioriteSalle());
		System.out.println();

		System.out.println("================================================================================");
		System.out.println(" PROFIL A : lieuCeremonie = RIAD");
		System.out.println("================================================================================");
		UserProfile pA = clone(base);
		pA.setLieuType("RIAD");
		List<Recommendation> topA = scoreAndSort(salles, pA, engine);
		showTop(topA, salles, 5);

		System.out.println("\n================================================================================");
		System.out.println(" PROFIL B : lieuCeremonie = HOTEL");
		System.out.println("================================================================================");
		UserProfile pB = clone(base);
		pB.setLieuType("HOTEL");
		List<Recommendation> topB = scoreAndSort(salles, pB, engine);
		showTop(topB, salles, 5);

		// === PREUVE QUE LES CLASSEMENTS SONT DIFFERENTS ===
		System.out.println("\n================================================================================");
		System.out.println(" PREUVE : les deux classements sont DIFFERENTS");
		System.out.println("================================================================================");
		int diffPositions = 0;
		int compareLen = Math.min(5, Math.min(topA.size(), topB.size()));
		for (int i = 0; i < compareLen; i++) {
			String a = topA.get(i).getVendorName();
			String b = topB.get(i).getVendorName();
			boolean same = a.equals(b);
			System.out.printf("  Position #%d : RIAD=%-32s | HOTEL=%-32s | %s%n",
				(i+1), trunc(a, 30), trunc(b, 30), same ? "identique" : "DIFFERENT");
			if (!same) diffPositions++;
		}
		System.out.println();
		if (diffPositions == 0) {
			System.out.println("  /!\\ ECHEC : les deux classements sont strictement identiques !");
			System.out.println("      lieuCeremonie n'a aucun impact sur l'ordre des recommandations.");
		} else {
			System.out.println("  OK : " + diffPositions + " / " + compareLen
				+ " positions du top sont differentes entre RIAD et HOTEL.");
			System.out.println("  Conclusion : lieuCeremonie influence reellement le classement.");
		}

		// === Verification : le top 1 RIAD doit etre un riad, le top 1 HOTEL un hotel ===
		System.out.println();
		String top1Aname = topA.get(0).getVendorName();
		String top1Bname = topB.get(0).getVendorName();
		Vendor top1Av = findVendorByName(salles, top1Aname);
		Vendor top1Bv = findVendorByName(salles, top1Bname);
		System.out.println("  Top #1 RIAD : '" + top1Aname + "' (venue_type=" + (top1Av != null ? top1Av.getVenueType() : "?") + ")");
		System.out.println("  Top #1 HOTEL: '" + top1Bname + "' (venue_type=" + (top1Bv != null ? top1Bv.getVenueType() : "?") + ")");

		// === Bonus : profil SALLE ===
		System.out.println("\n================================================================================");
		System.out.println(" PROFIL C (bonus) : lieuCeremonie = SALLE");
		System.out.println("================================================================================");
		UserProfile pC = clone(base);
		pC.setLieuType("SALLE");
		List<Recommendation> topC = scoreAndSort(salles, pC, engine);
		showTop(topC, salles, 5);

		boolean top1IsSalle = false;
		Vendor top1Cv = findVendorByName(salles, topC.get(0).getVendorName());
		if (top1Cv != null && "SALLE".equalsIgnoreCase(top1Cv.getVenueType())) top1IsSalle = true;
		if (top1IsSalle) System.out.println("  OK : top #1 du profil SALLE est bien une SALLE (venue_type confirme).");
		else System.out.println("  /!\\ ECHEC : top #1 du profil SALLE n'est PAS une SALLE.");

		// === BIS : profil PALAIS (les palais doivent etre devant les riads et autres) ===
		System.out.println("\n================================================================================");
		System.out.println(" PROFIL D (nouveau) : lieuCeremonie = PALAIS");
		System.out.println("================================================================================");
		UserProfile pD = clone(base);
		pD.setLieuType("PALAIS");
		List<Recommendation> topD = scoreAndSort(salles, pD, engine);
		showTop(topD, salles, 5);

		Vendor top1Dv = findVendorByName(salles, topD.get(0).getVendorName());
		boolean top1IsPalais = top1Dv != null && "PALAIS".equalsIgnoreCase(top1Dv.getVenueType());
		if (top1IsPalais) System.out.println("  OK : top #1 du profil PALAIS est bien un PALAIS.");
		else System.out.println("  /!\\ ECHEC : top #1 du profil PALAIS n'est PAS un PALAIS (venue_type=" + (top1Dv != null ? top1Dv.getVenueType() : "?") + ").");

		// Verification : un PALAIS doit etre devant les RIAD pour profil PALAIS
		// (RIAD = famille esthetique proche, donc en alternative, pas en top 1).
		System.out.println("\n  Top 3 PALAIS detaille :");
		for (int i = 0; i < Math.min(3, topD.size()); i++) {
			Vendor vv = findVendorByName(salles, topD.get(i).getVendorName());
			System.out.println("    #" + (i+1) + " " + topD.get(i).getVendorName()
				+ " (type=" + (vv != null ? vv.getVenueType() : "?")
				+ ", score=" + topD.get(i).getScore() + ")");
		}
	}

	private static Vendor findVendorByName(List<Vendor> all, String name) {
		for (Vendor v : all) if (v.getName().equals(name)) return v;
		return null;
	}

	private static List<Recommendation> scoreAndSort(List<Vendor> salles, UserProfile profile, AyoraRecommendationEngine engine) {
		List<Recommendation> recos = new ArrayList<Recommendation>();
		for (Vendor v : salles) recos.add(engine.scoreVendor(v, profile, 1));
		recos.sort(new Comparator<Recommendation>() {
			public int compare(Recommendation a, Recommendation b) {
				return Double.compare(b.getScore(), a.getScore());
			}
		});
		return recos;
	}

	private static void showTop(List<Recommendation> recos, List<Vendor> allVendors, int n) {
		int lim = Math.min(n, recos.size());
		System.out.printf("%-4s %-32s %-8s %-7s %s%n", "#", "Nom", "Type", "Score", "Raison");
		System.out.println("---------------------------------------------------------------------------------------------");
		for (int i = 0; i < lim; i++) {
			Recommendation r = recos.get(i);
			Vendor vv = findVendorByName(allVendors, r.getVendorName());
			String vType = vv == null || vv.getVenueType() == null ? "-" : vv.getVenueType();
			String raison = r.getRaison() == null ? "" : r.getRaison();
			if (raison.length() > 90) raison = raison.substring(0, 88) + "...";
			System.out.printf("%-4d %-32s %-8s %5.1f   %s%n",
				(i+1),
				trunc(r.getVendorName(), 30),
				vType,
				r.getScore(),
				raison);
		}
	}

	private static UserProfile clone(UserProfile p) {
		UserProfile c = new UserProfile();
		c.setBudgetTotal(p.getBudgetTotal());
		c.setNbInvites(p.getNbInvites());
		c.setBudgetPerGuest(p.getBudgetPerGuest());
		c.setBudgetTier(p.getBudgetTier());
		c.setGuestSize(p.getGuestSize());
		c.setNiveauLuxe(p.getNiveauLuxe());
		c.setStyle(p.getStyle());
		c.setUserCity(p.getUserCity());
		c.setCityTolerance(p.getCityTolerance());
		c.setPrioriteSalle(p.getPrioriteSalle());
		return c;
	}

	private static String trunc(String s, int n) {
		if (s == null) return "";
		if (s.length() <= n) return s;
		return s.substring(0, n - 1) + "...";
	}
}
