package com.ayora.metier;

import java.util.List;
import java.util.Map;

import com.ayora.model.Devis;
import com.ayora.model.UserProfile;
import com.ayora.model.Guest;
import com.ayora.model.Invitation;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.model.Recommendation;
import com.ayora.model.RendezVous;
import com.ayora.model.Subscription;
import com.ayora.model.User;
import com.ayora.model.UserPick;
import com.ayora.model.Vendor;
import com.ayora.model.VendorCategory;

/**
 * Interface metier unique de l'application Ayora.
 * Implementee par AyoraMetier (instance unique cree par AppWiring).
 * Les Servlets dependent de cette interface, pas des DAO concrets.
 */
public interface IAyoraMetier {

	// ============================================================
	// USERS
	// ============================================================
	User authenticate(String email, String password);
	User getUserByEmail(String email);
	User getUserById(int id);
	int createUser(User user);
	boolean markQuestionnaireCompleted(int userId, boolean completed);
	boolean changeSubscription(int userId, String type);
	List<User> getAllUsers();
	int countUsers();
	int countUsersByRole(String role);
	int countUsersByPlan(String plan);
	int countUsersByQuestionnaire(boolean completed);
	int countUsersByActive(boolean active);
	List<User> searchUsers(String keyword, String role, String plan,
			Boolean questionnaireCompleted, Boolean active, int offset, int limit);
	int countSearchUsers(String keyword, String role, String plan,
			Boolean questionnaireCompleted, Boolean active);
	boolean updateUser(int id, String firstName, String lastName,
			String email, String phone, String city);

	/**
	 * Self-service password change. Verifies the current password (via
	 * PasswordHasher, with lazy-migration support for legacy clear-text rows)
	 * and replaces it with a PBKDF2 hash of the new value.
	 *
	 * @return one of: {@code "OK"}, {@code "WRONG_CURRENT"},
	 *         {@code "TOO_SHORT"}, {@code "USER_NOT_FOUND"},
	 *         {@code "ERROR"}.
	 */
	String updateUserPassword(int userId, String currentPassword, String newPassword);

	boolean updateUserRole(int id, String role);
	boolean updateUserActive(int id, boolean active);
	boolean deleteUser(int id);

	// ============================================================
	// VENDORS
	// ============================================================
	Vendor getVendor(int id);
	List<Vendor> getAllVendors();
	List<Vendor> getVendorsByCategory(int categoryId);
	List<Vendor> getVendorsByGamme(String gamme);
	List<Vendor> searchVendors(String keyword);
	List<VendorCategory> getAllCategories();
	int countVendors();
	int countVendorsByActive(boolean active);
	int countVendorsIncomplete();
	List<Vendor> searchVendorsAll(String keyword, Integer categoryId, String city,
			String gamme, Boolean active, int offset, int limit);
	int countSearchVendors(String keyword, Integer categoryId, String city,
			String gamme, Boolean active);
	boolean updateVendor(int id, String name, String city, String description,
			Double prixMin, Double prixMax, String gamme, String phone, String email,
			String instagram, String address, String tags);
	boolean updateVendorActive(int id, boolean active);
	boolean deleteVendor(int id);
	Map<String, Integer> countVendorsByCategory();

	// ============================================================
	// QUESTIONNAIRE
	// ============================================================
	QuestionnaireAnswer getQuestionnaire(int userId);
	boolean saveQuestionnaire(QuestionnaireAnswer answer);

	// ============================================================
	// GUESTS
	// ============================================================
	List<Guest> getGuestsByUser(int userId);
	Guest getGuest(int id);
	int addGuest(Guest guest);
	boolean updateGuest(Guest guest);
	boolean deleteGuest(int id);
	int countGuestsByUser(int userId);

	// ============================================================
	// INVITATIONS
	// ============================================================
	List<Invitation> getInvitationsByUser(int userId);
	int addInvitation(Invitation invitation);
	boolean updateInvitationStatut(int id, String statut);
	boolean deleteInvitation(int id);

	// ============================================================
	// SUBSCRIPTIONS
	// ============================================================
	Subscription getSubscription(int userId);
	boolean addSubscription(Subscription sub);
	boolean incrementInvitationsSent(int userId);
	boolean updateSubscriptionPlan(int userId, String plan);

	// ============================================================
	// RECOMMENDATIONS
	// ============================================================
	List<Recommendation> getRecommendationsByUser(int userId);
	List<Recommendation> getRecommendationsByVendor(int vendorId);
	boolean addRecommendation(Recommendation rec);
	boolean deleteRecommendationsByUser(int userId);

	// ============================================================
	// USER PICKS
	// ============================================================
	List<UserPick> getPicksByUser(int userId);
	java.util.Set<Integer> getPickedVendorIds(int userId);
	boolean pickVendor(int userId, int vendorId, int categoryId);
	boolean unpickVendor(int userId, int vendorId);

	// ============================================================
	// DEVIS (demandes_devis)
	// ============================================================
	List<Devis> getDevisByClient(int clientId);
	List<Devis> getDevisByVendor(int vendorId);
	List<Devis> getAllDevis();
	List<Devis> getAllDevis(String statutFilter);
	int addDevis(Devis devis);
	boolean updateDevisStatutAndReponse(int id, String statut, String reponse);
	boolean updateDevisStatut(int id, String statut);
	int countDevis();
	int countDevisByStatut(String statut);
	int countDevisByClient(int clientId);
	int countDevisByVendor(int vendorId);

	// ============================================================
	// RENDEZ-VOUS (rendez_vous)
	// ============================================================
	List<RendezVous> getRendezVousByClient(int clientId);
	List<RendezVous> getRendezVousByVendor(int vendorId);
	List<RendezVous> getAllRendezVous();
	List<RendezVous> getAllRendezVous(String statutFilter);
	int addRendezVous(RendezVous rdv);
	boolean updateRendezVousStatut(int id, String statut);
	int countRendezVous();
	int countRendezVousByStatut(String statut);
	int countRendezVousByClient(int clientId);
	int countRendezVousByVendor(int vendorId);

	// ============================================================
	// ADMIN STATS
	// ============================================================
	int countQuestionnaireByUser(int userId);
	int countPicksByUser(int userId);
	int countRecommendationsByVendor(int vendorId);
	int countPicksByVendor(int vendorId);
	boolean syncSubscriptionPlan(int userId, String plan);

	List<Map<String, Object>> recentUsers(int limit);
	List<Map<String, Object>> recentDevis(int limit);
	List<Map<String, Object>> recentRdv(int limit);
	List<Map<String, Object>> recentQuestionnaires(int limit);

	List<Map<String, Object>> pendingDevis(int limit);
	List<Map<String, Object>> pendingRdv(int limit);
	List<Map<String, Object>> incompleteVendors(int limit);
	List<Map<String, Object>> clientsWithoutQuestionnaire(int limit);

	List<Map<String, Object>> signupsByMonth();
	List<Map<String, Object>> plansDistribution();
	List<Map<String, Object>> devisByStatus();

	// ============================================================
	// RECOMMANDATIONS - MOTEUR IA AYORA (k-NN pondere)
	// ============================================================

	/** Construit le profil utilisateur a partir de ses reponses au questionnaire. */
	UserProfile buildUserProfile(QuestionnaireAnswer answers);

	/** Genere les recommandations (calcul + persistance) et les retourne triees. */
	List<Recommendation> generateRecommendations(int userId, QuestionnaireAnswer answers);

	/** Recalcule les recommandations sans les persister (pour les GET). */
	List<Recommendation> computeRecommendations(int userId, QuestionnaireAnswer answers);

	/** Decoupe les recommandations en blocs thematiques (topPicks, bestValue, ...). */
	Map<String, List<Recommendation>> buildRecommendationBlocks(List<Recommendation> all, UserProfile profile);

	/** Vue principale : top 3 prestataires par categorie (k-NN, k = 3 par classe). */
	Map<String, List<Recommendation>> buildTopRecommendationsPerCategory(List<Recommendation> all, UserProfile profile);
}
