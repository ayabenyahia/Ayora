package com.ayora.metier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.ayora.dao.AdminStatsDao;
import com.ayora.dao.DevisDao;
import com.ayora.dao.GuestDao;
import com.ayora.dao.InvitationDao;
import com.ayora.dao.QuestionnaireDao;
import com.ayora.dao.RecommendationDao;
import com.ayora.dao.RendezVousDao;
import com.ayora.dao.SubscriptionDao;
import com.ayora.dao.UserDao;
import com.ayora.dao.UserPickDao;
import com.ayora.dao.VendorDao;
import com.ayora.model.Devis;
import com.ayora.model.Guest;
import com.ayora.model.Invitation;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.model.RendezVous;
import com.ayora.model.Subscription;
import com.ayora.model.User;
import com.ayora.model.UserPick;
import com.ayora.model.UserProfile;
import com.ayora.model.Vendor;
import com.ayora.model.VendorCategory;
import com.ayora.util.AyoraRecommendationEngine;

/**
 * Implementation unique du metier Ayora.
 *
 * Recoit les 11 DAO par injection (constructeur) et delegue chaque appel
 * au DAO approprie. Instance unique creee par AppWiring au demarrage,
 * partagee par tous les Servlets via AppWiring.getMetier().
 */
public class AyoraMetier implements IAyoraMetier {

	// === Seuils de score pour les blocs de recommandations ===
	private static final double SCORE_GOOD        = 70.0;
	private static final double SCORE_OK          = 55.0;
	private static final double SCORE_MIN_TO_KEEP = 35.0;

	private final UserDao userDao;
	private final VendorDao vendorDao;
	private final GuestDao guestDao;
	private final InvitationDao invitationDao;
	private final QuestionnaireDao questionnaireDao;
	private final RecommendationDao recommendationDao;
	private final SubscriptionDao subscriptionDao;
	private final UserPickDao userPickDao;
	private final DevisDao devisDao;
	private final RendezVousDao rendezVousDao;
	private final AdminStatsDao adminStatsDao;

	// Moteur d'IA (k-NN pondere). Utilitaire de calcul prive, pas un metier.
	private final AyoraRecommendationEngine engine = new AyoraRecommendationEngine();

	public AyoraMetier(
			UserDao userDao,
			VendorDao vendorDao,
			GuestDao guestDao,
			InvitationDao invitationDao,
			QuestionnaireDao questionnaireDao,
			RecommendationDao recommendationDao,
			SubscriptionDao subscriptionDao,
			UserPickDao userPickDao,
			DevisDao devisDao,
			RendezVousDao rendezVousDao,
			AdminStatsDao adminStatsDao) {
		this.userDao = userDao;
		this.vendorDao = vendorDao;
		this.guestDao = guestDao;
		this.invitationDao = invitationDao;
		this.questionnaireDao = questionnaireDao;
		this.recommendationDao = recommendationDao;
		this.subscriptionDao = subscriptionDao;
		this.userPickDao = userPickDao;
		this.devisDao = devisDao;
		this.rendezVousDao = rendezVousDao;
		this.adminStatsDao = adminStatsDao;
	}

	// ============================================================
	// USERS
	// ============================================================
	/**
	 * Authentification : verifie le hash PBKDF2 si present, sinon tombe sur
	 * le mot de passe clair (legacy) et hash-migre a la volee.
	 *
	 * Cycle d'un compte cree avant la migration security_v1 :
	 *   1) login N1 : check password (clair) -> OK -> hash + update password_hash, password=NULL
	 *   2) login N2 : check password_hash (PBKDF2) -> OK
	 *
	 * Pour un compte cree apres : tout passe direct par password_hash.
	 */
	@Override public User authenticate(String email, String password) {
		if (email == null || password == null) return null;
		User u = userDao.findByEmail(email);
		if (u == null) return null;

		// 1) Hash present : verification PBKDF2 standard.
		String hash = u.getPasswordHash();
		if (com.ayora.util.PasswordHasher.isHashed(hash)) {
			if (com.ayora.util.PasswordHasher.verify(password, hash)) {
				u.setPassword(null); // ne pas garder le clair en memoire
				return u;
			}
			return null;
		}

		// 2) Hash absent : compte legacy (clair en base). Verif + migration auto.
		String storedClear = u.getPassword();
		if (storedClear != null && storedClear.equals(password)) {
			String newHash = com.ayora.util.PasswordHasher.hash(password);
			userDao.updatePasswordHash(u.getId(), newHash);
			u.setPasswordHash(newHash);
			u.setPassword(null);
			return u;
		}
		return null;
	}
	@Override public User getUserByEmail(String email) { return userDao.findByEmail(email); }
	@Override public User getUserById(int id) { return userDao.findById(id); }
	@Override public int createUser(User user) {
		// Toute creation passe par PBKDF2. Le clair n'est jamais stocke.
		if (user.getPasswordHash() == null && user.getPassword() != null) {
			user.setPasswordHash(com.ayora.util.PasswordHasher.hash(user.getPassword()));
			user.setPassword(null);
		}
		return userDao.create(user);
	}
	@Override public boolean markQuestionnaireCompleted(int userId, boolean completed) {
		return userDao.updateQuestionnaireStatus(userId, completed);
	}
	@Override public boolean changeSubscription(int userId, String type) {
		return userDao.updateSubscription(userId, type);
	}
	@Override public List<User> getAllUsers() { return userDao.findAll(); }
	@Override public int countUsers() { return userDao.countAll(); }
	@Override public int countUsersByRole(String role) { return userDao.countByRole(role); }
	@Override public int countUsersByPlan(String plan) { return userDao.countByPlan(plan); }
	@Override public int countUsersByQuestionnaire(boolean completed) {
		return userDao.countByQuestionnaire(completed);
	}
	@Override public int countUsersByActive(boolean active) { return userDao.countByActive(active); }
	@Override public List<User> searchUsers(String keyword, String role, String plan,
			Boolean questionnaireCompleted, Boolean active, int offset, int limit) {
		return userDao.search(keyword, role, plan, questionnaireCompleted, active, offset, limit);
	}
	@Override public int countSearchUsers(String keyword, String role, String plan,
			Boolean questionnaireCompleted, Boolean active) {
		return userDao.countSearch(keyword, role, plan, questionnaireCompleted, active);
	}
	@Override public boolean updateUser(int id, String firstName, String lastName,
			String email, String phone, String city) {
		return userDao.update(id, firstName, lastName, email, phone, city);
	}

	/**
	 * Self-service password change. The current password is verified the
	 * same way {@link #authenticate(String, String)} does it — PBKDF2 hash
	 * if present, lazy-migrated clear-text otherwise. On success the new
	 * password is stored as a fresh PBKDF2 hash. The clear value is never
	 * persisted, never logged, never returned.
	 */
	@Override public String updateUserPassword(int userId, String currentPassword, String newPassword) {
		if (newPassword == null || newPassword.length() < 8) return "TOO_SHORT";
		if (currentPassword == null) return "WRONG_CURRENT";

		User u = userDao.findById(userId);
		if (u == null) return "USER_NOT_FOUND";

		// 1) Verify current password (hash first, then legacy clear).
		String hash = u.getPasswordHash();
		boolean ok = false;
		if (com.ayora.util.PasswordHasher.isHashed(hash)) {
			ok = com.ayora.util.PasswordHasher.verify(currentPassword, hash);
		} else if (u.getPassword() != null) {
			ok = u.getPassword().equals(currentPassword);
		}
		if (!ok) return "WRONG_CURRENT";

		// 2) Hash + persist new password.
		try {
			String newHash = com.ayora.util.PasswordHasher.hash(newPassword);
			boolean persisted = userDao.updatePasswordHash(userId, newHash);
			return persisted ? "OK" : "ERROR";
		} catch (RuntimeException e) {
			return "ERROR";
		}
	}
	@Override public boolean updateUserRole(int id, String role) { return userDao.updateRole(id, role); }
	@Override public boolean updateUserActive(int id, boolean active) {
		return userDao.updateActive(id, active);
	}
	@Override public boolean deleteUser(int id) { return userDao.delete(id); }

	// ============================================================
	// VENDORS
	// ============================================================
	@Override public Vendor getVendor(int id) { return vendorDao.findById(id); }
	@Override public List<Vendor> getAllVendors() { return vendorDao.findAll(); }
	@Override public List<Vendor> getVendorsByCategory(int categoryId) { return vendorDao.findByCategory(categoryId); }
	@Override public List<Vendor> getVendorsByGamme(String gamme) { return vendorDao.findByGamme(gamme); }
	@Override public List<Vendor> searchVendors(String keyword) { return vendorDao.search(keyword); }
	@Override public List<VendorCategory> getAllCategories() { return vendorDao.findAllCategories(); }
	@Override public int countVendors() { return vendorDao.countAll(); }
	@Override public int countVendorsByActive(boolean active) { return vendorDao.countActive(active); }
	@Override public int countVendorsIncomplete() { return vendorDao.countIncomplete(); }
	@Override public List<Vendor> searchVendorsAll(String keyword, Integer categoryId, String city,
			String gamme, Boolean active, int offset, int limit) {
		return vendorDao.searchAll(keyword, categoryId, city, gamme, active, offset, limit);
	}
	@Override public int countSearchVendors(String keyword, Integer categoryId, String city,
			String gamme, Boolean active) {
		return vendorDao.countSearch(keyword, categoryId, city, gamme, active);
	}
	@Override public boolean updateVendor(int id, String name, String city, String description,
			Double prixMin, Double prixMax, String gamme, String phone, String email,
			String instagram, String address, String tags) {
		return vendorDao.update(id, name, city, description, prixMin, prixMax, gamme, phone, email, instagram, address, tags);
	}
	@Override public boolean updateVendorActive(int id, boolean active) {
		return vendorDao.updateActive(id, active);
	}
	@Override public boolean deleteVendor(int id) { return vendorDao.delete(id); }
	@Override public Map<String, Integer> countVendorsByCategory() {
		return vendorDao.countByCategory();
	}

	// ============================================================
	// QUESTIONNAIRE
	// ============================================================
	@Override public QuestionnaireAnswer getQuestionnaire(int userId) {
		return questionnaireDao.findByUserId(userId);
	}
	@Override public boolean saveQuestionnaire(QuestionnaireAnswer answer) {
		// Si un questionnaire existe deja pour ce user, on fait un update ; sinon create.
		QuestionnaireAnswer existing = questionnaireDao.findByUserId(answer.getUserId());
		if (existing != null) return questionnaireDao.update(answer);
		return questionnaireDao.create(answer);
	}

	// ============================================================
	// GUESTS
	// ============================================================
	@Override public List<Guest> getGuestsByUser(int userId) { return guestDao.findByUserId(userId); }
	@Override public Guest getGuest(int id) { return guestDao.findById(id); }
	@Override public int addGuest(Guest guest) { return guestDao.create(guest); }
	@Override public boolean updateGuest(Guest guest) { return guestDao.update(guest); }
	@Override public boolean deleteGuest(int id) { return guestDao.delete(id); }
	@Override public int countGuestsByUser(int userId) { return guestDao.countByUserId(userId); }

	// ============================================================
	// INVITATIONS
	// ============================================================
	@Override public List<Invitation> getInvitationsByUser(int userId) { return invitationDao.findByUserId(userId); }
	@Override public int addInvitation(Invitation invitation) { return invitationDao.create(invitation); }
	@Override public boolean updateInvitationStatut(int id, String statut) {
		return invitationDao.updateStatut(id, statut);
	}
	@Override public boolean deleteInvitation(int id) { return invitationDao.delete(id); }

	// ============================================================
	// SUBSCRIPTIONS
	// ============================================================
	@Override public Subscription getSubscription(int userId) { return subscriptionDao.findByUserId(userId); }
	@Override public boolean addSubscription(Subscription sub) { return subscriptionDao.create(sub); }
	@Override public boolean incrementInvitationsSent(int userId) {
		return subscriptionDao.incrementInvitationsSent(userId);
	}
	@Override public boolean updateSubscriptionPlan(int userId, String plan) {
		return subscriptionDao.updatePlan(userId, plan);
	}

	// ============================================================
	// RECOMMENDATIONS
	// ============================================================
	@Override public List<Recommendation> getRecommendationsByUser(int userId) {
		return recommendationDao.findByUserId(userId);
	}
	@Override public List<Recommendation> getRecommendationsByVendor(int vendorId) {
		return recommendationDao.findByVendorId(vendorId);
	}
	@Override public boolean addRecommendation(Recommendation rec) { return recommendationDao.create(rec); }
	@Override public boolean deleteRecommendationsByUser(int userId) {
		return recommendationDao.deleteByUserId(userId);
	}

	// ============================================================
	// USER PICKS
	// ============================================================
	@Override public List<UserPick> getPicksByUser(int userId) { return userPickDao.findByUserId(userId); }
	@Override public Set<Integer> getPickedVendorIds(int userId) {
		return userPickDao.findPickedVendorIds(userId);
	}
	@Override public boolean pickVendor(int userId, int vendorId, int categoryId) {
		return userPickDao.pick(userId, vendorId, categoryId);
	}
	@Override public boolean unpickVendor(int userId, int vendorId) {
		return userPickDao.unpick(userId, vendorId);
	}

	// ============================================================
	// DEVIS
	// ============================================================
	@Override public List<Devis> getDevisByClient(int clientId) { return devisDao.findByClient(clientId); }
	@Override public List<Devis> getDevisByVendor(int vendorId) { return devisDao.findByVendor(vendorId); }
	@Override public List<Devis> getAllDevis() { return devisDao.findAll(); }
	@Override public List<Devis> getAllDevis(String statutFilter) { return devisDao.findAll(statutFilter); }
	@Override public int addDevis(Devis devis) { return devisDao.create(devis); }
	@Override public boolean updateDevisStatutAndReponse(int id, String statut, String reponse) {
		return devisDao.updateStatutAndReponse(id, statut, reponse);
	}
	@Override public boolean updateDevisStatut(int id, String statut) {
		return devisDao.updateStatut(id, statut);
	}
	@Override public int countDevis() { return devisDao.countAll(); }
	@Override public int countDevisByStatut(String statut) { return devisDao.countByStatut(statut); }
	@Override public int countDevisByClient(int clientId) { return devisDao.countByClient(clientId); }
	@Override public int countDevisByVendor(int vendorId) { return devisDao.countByVendor(vendorId); }

	// ============================================================
	// RENDEZ-VOUS
	// ============================================================
	@Override public List<RendezVous> getRendezVousByClient(int clientId) { return rendezVousDao.findByClient(clientId); }
	@Override public List<RendezVous> getRendezVousByVendor(int vendorId) { return rendezVousDao.findByVendor(vendorId); }
	@Override public List<RendezVous> getAllRendezVous() { return rendezVousDao.findAll(); }
	@Override public List<RendezVous> getAllRendezVous(String statutFilter) { return rendezVousDao.findAll(statutFilter); }
	@Override public int addRendezVous(RendezVous rdv) { return rendezVousDao.create(rdv); }
	@Override public boolean updateRendezVousStatut(int id, String statut) {
		return rendezVousDao.updateStatut(id, statut);
	}
	@Override public int countRendezVous() { return rendezVousDao.countAll(); }
	@Override public int countRendezVousByStatut(String statut) { return rendezVousDao.countByStatut(statut); }
	@Override public int countRendezVousByClient(int clientId) { return rendezVousDao.countByClient(clientId); }
	@Override public int countRendezVousByVendor(int vendorId) { return rendezVousDao.countByVendor(vendorId); }

	// ============================================================
	// ADMIN STATS
	// ============================================================
	@Override public int countQuestionnaireByUser(int userId) {
		return adminStatsDao.countQuestionnaireByUser(userId);
	}
	@Override public int countPicksByUser(int userId) { return adminStatsDao.countPicksByUser(userId); }
	@Override public int countRecommendationsByVendor(int vendorId) {
		return adminStatsDao.countRecommendationsByVendor(vendorId);
	}
	@Override public int countPicksByVendor(int vendorId) { return adminStatsDao.countPicksByVendor(vendorId); }
	@Override public boolean syncSubscriptionPlan(int userId, String plan) {
		return adminStatsDao.syncSubscriptionPlan(userId, plan);
	}

	@Override public List<Map<String, Object>> recentUsers(int limit) { return adminStatsDao.recentUsers(limit); }
	@Override public List<Map<String, Object>> recentDevis(int limit) { return adminStatsDao.recentDevis(limit); }
	@Override public List<Map<String, Object>> recentRdv(int limit) { return adminStatsDao.recentRdv(limit); }
	@Override public List<Map<String, Object>> recentQuestionnaires(int limit) {
		return adminStatsDao.recentQuestionnaires(limit);
	}

	@Override public List<Map<String, Object>> pendingDevis(int limit) { return adminStatsDao.pendingDevis(limit); }
	@Override public List<Map<String, Object>> pendingRdv(int limit) { return adminStatsDao.pendingRdv(limit); }
	@Override public List<Map<String, Object>> incompleteVendors(int limit) {
		return adminStatsDao.incompleteVendors(limit);
	}
	@Override public List<Map<String, Object>> clientsWithoutQuestionnaire(int limit) {
		return adminStatsDao.clientsWithoutQuestionnaire(limit);
	}

	@Override public List<Map<String, Object>> signupsByMonth() { return adminStatsDao.signupsByMonth(); }
	@Override public List<Map<String, Object>> plansDistribution() { return adminStatsDao.plansDistribution(); }
	@Override public List<Map<String, Object>> devisByStatus() { return adminStatsDao.devisByStatus(); }

	// ============================================================
	// RECOMMANDATIONS - MOTEUR IA AYORA (logique fusionnee depuis l'ancien
	// RecommendationService pour respecter "1 interface + 1 classe" metier)
	// ============================================================

	@Override
	public UserProfile buildUserProfile(QuestionnaireAnswer a) {
		return engine.buildUserProfile(a);
	}

	@Override
	public List<Recommendation> generateRecommendations(int userId, QuestionnaireAnswer answers) {
		recommendationDao.deleteByUserId(userId);

		UserProfile profile = engine.buildUserProfile(answers);
		profile.setUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		Set<Integer> wantedCats = engine.wantedCategoryIds(profile);
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

	@Override
	public List<Recommendation> computeRecommendations(int userId, QuestionnaireAnswer answers) {
		UserProfile profile = engine.buildUserProfile(answers);
		profile.setUserId(userId);

		List<Vendor> allVendors = vendorDao.findAll();
		Set<Integer> wantedCats = engine.wantedCategoryIds(profile);
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

	@Override
	public Map<String, List<Recommendation>> buildRecommendationBlocks(List<Recommendation> all, UserProfile p) {
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

	@Override
	public Map<String, List<Recommendation>> buildTopRecommendationsPerCategory(List<Recommendation> all, UserProfile p) {
		return engine.topPerCategory(all, p);
	}

	// === Helpers prives moteur IA ===

	private boolean eq(String a, String b) {
		return a != null && a.equalsIgnoreCase(b);
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

	/** Tri par selection (pattern simple, aligne sur le style du cours). */
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
