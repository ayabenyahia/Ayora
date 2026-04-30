package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.InvitationDao;
import com.ayora.dao.SubscriptionDao;
import com.ayora.dao.GuestDao;
import com.ayora.dao.UserDao;
import com.ayora.dao.QuestionnaireDao;
import com.ayora.model.Invitation;
import com.ayora.model.Subscription;
import com.ayora.model.Guest;
import com.ayora.model.User;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.service.EmailService;
import com.ayora.util.JsonUtil;

@WebServlet("/api/invitations/*")
public class InvitationServlet extends HttpServlet {

	private InvitationDao invitationDao;
	private SubscriptionDao subscriptionDao;
	private GuestDao guestDao;
	private UserDao userDao;
	private QuestionnaireDao questionnaireDao;
	private EmailService emailService;

	// === Catalogue des templates avec leur niveau requis ===
	// Mappe le slug visible cote frontend vers (visualTemplate utilise par EmailService, niveau requis).
	// FREE = libre / PRO = abonnement Pro / PREMIUM = abonnement Premium.
	private static final java.util.Map<String, String[]> TEMPLATE_CATALOG = new java.util.HashMap<>();
	static {
		// === FREE ===
		TEMPLATE_CATALOG.put("ivory-gold",     new String[]{"classique", "FREE"});
		TEMPLATE_CATALOG.put("minimal-luxury", new String[]{"moderne",   "FREE"});
		TEMPLATE_CATALOG.put("soft-cal",       new String[]{"classique", "FREE"});
		// === PRO ===
		TEMPLATE_CATALOG.put("desert-rose",    new String[]{"classique", "PRO"});
		TEMPLATE_CATALOG.put("garden-elegance",new String[]{"classique", "PRO"});
		TEMPLATE_CATALOG.put("pearl",          new String[]{"luxe",      "PRO"});
		TEMPLATE_CATALOG.put("zellige-pearl",  new String[]{"luxe",      "PRO"});  // zellige marocain Pro
		TEMPLATE_CATALOG.put("palace-cream",   new String[]{"luxe",      "PRO"});
		// === PREMIUM (statiques) ===
		TEMPLATE_CATALOG.put("royal-black",    new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("emerald-night",  new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("andalusian",     new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("zellige-royal",  new String[]{"luxe",      "PREMIUM"});  // zellige marocain Premium
		// === PREMIUM (video) ===
		TEMPLATE_CATALOG.put("video-cinema",   new String[]{"video",     "PREMIUM"});
		TEMPLATE_CATALOG.put("video-fassi",    new String[]{"video",     "PREMIUM"});
		TEMPLATE_CATALOG.put("video-luxe",     new String[]{"video",     "PREMIUM"});

		// === retro-compat avec anciens slugs ===
		TEMPLATE_CATALOG.put("classique",      new String[]{"classique", "FREE"});
		TEMPLATE_CATALOG.put("blanc-dore",     new String[]{"classique", "FREE"});
		TEMPLATE_CATALOG.put("floral",         new String[]{"classique", "FREE"});
		TEMPLATE_CATALOG.put("soft-pink",      new String[]{"classique", "PRO"});
		TEMPLATE_CATALOG.put("oriental",       new String[]{"luxe",      "PRO"});
		TEMPLATE_CATALOG.put("marocain",       new String[]{"luxe",      "PRO"});
		TEMPLATE_CATALOG.put("beige",          new String[]{"moderne",   "PRO"});
		TEMPLATE_CATALOG.put("green-garden",   new String[]{"classique", "PRO"});
		TEMPLATE_CATALOG.put("royal",          new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("luxe",           new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("calligraphy",    new String[]{"moderne",   "PREMIUM"});
		TEMPLATE_CATALOG.put("cream",          new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("moderne",        new String[]{"moderne",   "PRO"});
	}

	private static boolean isVideoTemplate(String slug) {
		String[] entry = TEMPLATE_CATALOG.get(slug);
		return entry != null && "video".equals(entry[0]);
	}

	private static String resolveVisual(String slug) {
		String[] entry = TEMPLATE_CATALOG.get(slug);
		return entry != null ? entry[0] : "classique";
	}

	private static String requiredLevel(String slug) {
		String[] entry = TEMPLATE_CATALOG.get(slug);
		return entry != null ? entry[1] : "FREE";
	}

	@Override
	public void init() throws ServletException {
		invitationDao = new InvitationDao();
		subscriptionDao = new SubscriptionDao();
		guestDao = new GuestDao();
		userDao = new UserDao();
		questionnaireDao = new QuestionnaireDao();
		emailService = new EmailService();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		List<Invitation> invitations = invitationDao.findByUserId(userId);
		Subscription sub = subscriptionDao.findByUserId(userId);

		StringBuilder json = new StringBuilder("{\"invitations\":[");
		for (int i = 0; i < invitations.size(); i++) {
			Invitation inv = invitations.get(i);
			if (i > 0) json.append(",");
			json.append(buildInvitationJson(inv));
		}
		json.append("],");
		json.append("\"subscription\":{");
		json.append("\"plan\":\"" + (sub != null ? sub.getPlan() : "FREE") + "\",");
		json.append("\"invitationsSent\":" + (sub != null ? sub.getInvitationsSent() : 0) + ",");
		json.append("\"maxFree\":" + (sub != null ? sub.getMaxInvitationsAllowed() : 10) + ",");
		json.append("\"canSend\":" + (sub != null ? sub.canSendInvitation() : true) + ",");
		json.append("\"remaining\":" + (sub != null ? sub.getRemainingInvitations() : 10));
		json.append("}}");

		JsonUtil.sendJson(response, json.toString());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		String path = request.getPathInfo();

		if (path != null && path.startsWith("/send/")) {
			handleSend(request, response, userId, path);
			return;
		}

		String body = JsonUtil.readRequestBody(request);
		int guestId = JsonUtil.getIntValue(body, "guestId");
		String templateName = JsonUtil.getStringValue(body, "templateName");
		String messagePerso = JsonUtil.getStringValue(body, "messagePerso");
		String videoUrl = JsonUtil.getStringValue(body, "videoUrl");

		if (guestId <= 0) {
			JsonUtil.sendError(response, 400, "ID invite requis");
			return;
		}

		String slug = templateName != null ? templateName : "classique";
		String requiredLvl = requiredLevel(slug);

		// Verifier le droit d'acces au template selon l'abonnement
		Subscription subCheck = subscriptionDao.findByUserId(userId);
		if (subCheck == null) subCheck = new Subscription();
		if (!subCheck.canUseTemplateLevel(requiredLvl)) {
			JsonUtil.sendError(response, 403,
				"Ce modele necessite un abonnement " + requiredLvl
				+ ". Votre plan actuel : " + subCheck.getPlan() + ".");
			return;
		}

		Invitation invitation = new Invitation();
		invitation.setGuestId(guestId);
		invitation.setUserId(userId);
		invitation.setTemplateName(slug);
		invitation.setMessagePerso(messagePerso);
		invitation.setVideoUrl(videoUrl);

		int invId = invitationDao.create(invitation);
		if (invId == -1) {
			JsonUtil.sendError(response, 500, "Erreur lors de la creation de l'invitation");
			return;
		}

		JsonUtil.sendJson(response, "{\"success\":true,\"id\":" + invId + ",\"requiredLevel\":\"" + requiredLvl + "\"}");
	}

	private void handleSend(HttpServletRequest request, HttpServletResponse response, int userId, String path) throws IOException {
		int invitationId;
		try {
			invitationId = Integer.parseInt(path.substring("/send/".length()));
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "ID invitation invalide");
			return;
		}

		// Verifier la limite d'invitations
		Subscription sub = subscriptionDao.findByUserId(userId);
		if (sub == null) {
			JsonUtil.sendError(response, 500, "Abonnement non trouve");
			return;
		}

		int maxAllowed = sub.getMaxInvitationsAllowed();
		if (!sub.canSendInvitation()) {
			String limitMsg = "Limite de " + maxAllowed + " invitations atteinte sur le plan " + sub.getPlan() + ". "
				+ ("FREE".equals(sub.getPlan()) ? "Passez en Pro ou Premium pour en envoyer plus."
					: "Passez en Premium pour des invitations illimitees.");
			JsonUtil.sendJson(response, "{\"success\":false,"
					+ "\"limitReached\":true,"
					+ "\"message\":\"" + JsonUtil.escapeJson(limitMsg) + "\","
					+ "\"invitationsSent\":" + sub.getInvitationsSent() + ","
					+ "\"maxFree\":" + maxAllowed
					+ "}");
			return;
		}

		// Recuperer les infos de l'invitation pour l'email
		List<Invitation> invitations = invitationDao.findByUserId(userId);
		Invitation targetInv = null;
		for (Invitation inv : invitations) {
			if (inv.getId() == invitationId) {
				targetInv = inv;
				break;
			}
		}

		if (targetInv == null) {
			JsonUtil.sendError(response, 404, "Invitation non trouvee");
			return;
		}

		// Verifier le droit d'acces au template choisi (defense en profondeur)
		String reqLvl = requiredLevel(targetInv.getTemplateName());
		if (!sub.canUseTemplateLevel(reqLvl)) {
			JsonUtil.sendError(response, 403,
				"Ce modele necessite un abonnement " + reqLvl
				+ ". Votre plan actuel : " + sub.getPlan() + ".");
			return;
		}

		// Recuperer l'invite et l'utilisateur
		Guest guest = guestDao.findById(targetInv.getGuestId());
		User user = userDao.findById(userId);

		// Envoyer l'email reel si l'invite a un email
		boolean emailSent = false;
		String emailMessage = "";

		if (guest != null && guest.getEmail() != null && !guest.getEmail().isEmpty()) {
			// Recuperer les infos du questionnaire pour la date et le lieu
			String dateMariage = "";
			String lieuMariage = "";
			QuestionnaireAnswer qa = questionnaireDao.findByUserId(userId);
			if (qa != null) {
				dateMariage = qa.getDateMariage() != null ? qa.getDateMariage() : "";
				lieuMariage = qa.getLieuCeremonie() != null ? qa.getLieuCeremonie() : "";
			}

			String hostName = user != null ? (user.getFirstName() + " " + user.getLastName()) : "Les maries";
			String guestName = guest.getFirstName() + " " + guest.getLastName();

			// Mappe le slug visible (ex: "royal", "soft-pink"...) vers le visuel HTML
			// disponible dans EmailService (classique / moderne / luxe / video).
			String visualTemplate = resolveVisual(targetInv.getTemplateName());

			// Pour les modeles video, on enrichit le messagePerso avec le lien video
			String finalMessage = targetInv.getMessagePerso();
			if ("video".equals(visualTemplate) && targetInv.getVideoUrl() != null && !targetInv.getVideoUrl().isEmpty()) {
				String prefix = (finalMessage != null && !finalMessage.isEmpty()) ? finalMessage + "\n\n" : "";
				finalMessage = prefix + "[VIDEO_INVITATION_URL]=" + targetInv.getVideoUrl();
			}

			emailSent = emailService.sendInvitation(
				guest.getEmail(),
				guestName,
				hostName,
				dateMariage,
				lieuMariage,
				visualTemplate,
				finalMessage
			);

			emailMessage = emailSent ? "Email envoye a " + guest.getEmail() : "Echec envoi email";
		} else {
			emailMessage = "Pas d'email pour cet invite";
		}

		// Mettre a jour le statut
		boolean updated = invitationDao.updateStatut(invitationId, "ENVOYEE");
		if (!updated) {
			JsonUtil.sendError(response, 500, "Erreur lors de l'envoi");
			return;
		}

		// Incrementer le compteur
		subscriptionDao.incrementInvitationsSent(userId);

		sub = subscriptionDao.findByUserId(userId);
		JsonUtil.sendJson(response, "{\"success\":true,"
				+ "\"message\":\"Invitation envoyee\","
				+ "\"emailSent\":" + emailSent + ","
				+ "\"emailMessage\":\"" + JsonUtil.escapeJson(emailMessage) + "\","
				+ "\"invitationsSent\":" + sub.getInvitationsSent() + ","
				+ "\"remaining\":" + sub.getRemainingFreeInvitations()
				+ "}");
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		String path = request.getPathInfo();
		if (path == null || path.length() < 2) {
			JsonUtil.sendError(response, 400, "ID invitation requis");
			return;
		}

		int invId;
		try {
			invId = Integer.parseInt(path.substring(1));
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "ID invalide");
			return;
		}

		boolean success = invitationDao.delete(invId);
		if (success) {
			JsonUtil.sendSuccess(response, "Invitation supprimee");
		} else {
			JsonUtil.sendError(response, 500, "Erreur suppression");
		}
	}

	private String buildInvitationJson(Invitation inv) {
		return "{\"id\":" + inv.getId()
				+ ",\"guestId\":" + inv.getGuestId()
				+ ",\"guestName\":\"" + JsonUtil.escapeJson(inv.getGuestFirstName() + " " + inv.getGuestLastName()) + "\""
				+ ",\"statut\":\"" + inv.getStatut() + "\""
				+ ",\"templateName\":\"" + JsonUtil.escapeJson(inv.getTemplateName()) + "\""
				+ ",\"dateEnvoi\":\"" + JsonUtil.escapeJson(inv.getDateEnvoi() != null ? inv.getDateEnvoi() : "") + "\""
				+ ",\"messagePerso\":\"" + JsonUtil.escapeJson(inv.getMessagePerso() != null ? inv.getMessagePerso() : "") + "\""
				+ "}";
	}
}
