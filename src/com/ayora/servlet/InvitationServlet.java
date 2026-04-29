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
		json.append("\"maxFree\":" + (sub != null ? sub.getMaxInvitationsFree() : 10) + ",");
		json.append("\"canSend\":" + (sub != null ? sub.canSendInvitation() : true) + ",");
		json.append("\"remaining\":" + (sub != null ? sub.getRemainingFreeInvitations() : 10));
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

		if (guestId <= 0) {
			JsonUtil.sendError(response, 400, "ID invite requis");
			return;
		}

		Invitation invitation = new Invitation();
		invitation.setGuestId(guestId);
		invitation.setUserId(userId);
		invitation.setTemplateName(templateName != null ? templateName : "classique");
		invitation.setMessagePerso(messagePerso);

		int invId = invitationDao.create(invitation);
		if (invId == -1) {
			JsonUtil.sendError(response, 500, "Erreur lors de la creation de l'invitation");
			return;
		}

		JsonUtil.sendJson(response, "{\"success\":true,\"id\":" + invId + "}");
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

		if (!sub.canSendInvitation()) {
			JsonUtil.sendJson(response, "{\"success\":false,"
					+ "\"limitReached\":true,"
					+ "\"message\":\"Limite de " + sub.getMaxInvitationsFree() + " invitations gratuites atteinte. Passez en Premium pour envoyer plus d'invitations.\","
					+ "\"invitationsSent\":" + sub.getInvitationsSent() + ","
					+ "\"maxFree\":" + sub.getMaxInvitationsFree()
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

			emailSent = emailService.sendInvitation(
				guest.getEmail(),
				guestName,
				hostName,
				dateMariage,
				lieuMariage,
				targetInv.getTemplateName(),
				targetInv.getMessagePerso()
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
