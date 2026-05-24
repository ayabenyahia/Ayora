package com.ayora.servlet;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.config.AppWiring;
import com.ayora.metier.IAyoraMetier;
import com.ayora.model.Invitation;
import com.ayora.model.Subscription;
import com.ayora.util.JsonUtil;

@WebServlet("/api/invitations/*")
public class InvitationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAyoraMetier metier;

	// === Catalogue des templates avec leur niveau requis ===
	// Mappe le slug visible cote frontend vers (visualTemplate, niveau requis).
	// L'envoi reel des emails est gere par le frontend ; le backend se contente
	// de valider le droit d'acces et de marquer l'invitation comme envoyee.
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
		// === PREMIUM (nouveaux templates wow remplaçant les videos) ===
		TEMPLATE_CATALOG.put("or-liquide",     new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("caftan-ivoire",  new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("imperial",       new String[]{"luxe",      "PREMIUM"});

		// === 5 nouveaux styles (lot ajoute) ===
		TEMPLATE_CATALOG.put("ocean-blush",       new String[]{"classique", "FREE"});
		TEMPLATE_CATALOG.put("sunset-marrakech",  new String[]{"classique", "PRO"});
		TEMPLATE_CATALOG.put("vintage-postcard",  new String[]{"moderne",   "PRO"});
		TEMPLATE_CATALOG.put("art-deco-onyx",     new String[]{"luxe",      "PREMIUM"});
		TEMPLATE_CATALOG.put("constellation",     new String[]{"luxe",      "PREMIUM"});

		// === 3 styles supplementaires (lot final) ===
		TEMPLATE_CATALOG.put("lavender-dream",    new String[]{"classique", "FREE"});
		TEMPLATE_CATALOG.put("henna-garden",      new String[]{"classique", "PRO"});
		TEMPLATE_CATALOG.put("marble-rose-gold",  new String[]{"luxe",      "PREMIUM"});

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

	private static String requiredLevel(String slug) {
		String[] entry = TEMPLATE_CATALOG.get(slug);
		return entry != null ? entry[1] : "FREE";
	}

	@Override
	public void init() throws ServletException {
		this.metier = AppWiring.getMetier();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		List<Invitation> invitations = metier.getInvitationsByUser(userId);
		Subscription sub = metier.getSubscription(userId);

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
		Subscription subCheck = metier.getSubscription(userId);
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

		int invId = metier.addInvitation(invitation);
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
		Subscription sub = metier.getSubscription(userId);
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

		// Recuperer l'invitation ciblee
		List<Invitation> invitations = metier.getInvitationsByUser(userId);
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

		// L'envoi reel de l'email est gere cote frontend ; le backend ne fait
		// que marquer l'invitation comme envoyee et incrementer le compteur.
		boolean updated = metier.updateInvitationStatut(invitationId, "ENVOYEE");
		if (!updated) {
			JsonUtil.sendError(response, 500, "Erreur lors de l'envoi");
			return;
		}

		metier.incrementInvitationsSent(userId);

		// emailSent/emailMessage : champs conserves pour compatibilite avec
		// invitations.html (le frontend gere l'envoi reel de l'email).
		sub = metier.getSubscription(userId);
		JsonUtil.sendJson(response, "{\"success\":true,"
				+ "\"message\":\"Invitation envoyee\","
				+ "\"emailSent\":true,"
				+ "\"emailMessage\":\"Envoi gere par le frontend\","
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

		boolean success = metier.deleteInvitation(invId);
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
