package com.ayora.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.config.AppWiring;
import com.ayora.metier.IAyoraMetier;
import com.ayora.model.Subscription;
import com.ayora.util.JsonUtil;

@WebServlet("/api/subscription/*")
public class SubscriptionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private IAyoraMetier metier;

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
		Subscription sub = metier.getSubscription(userId);

		if (sub == null) {
			JsonUtil.sendJson(response, "{\"plan\":\"FREE\",\"invitationsSent\":0,\"maxFree\":10,\"canSend\":true,\"remaining\":10}");
			return;
		}

		String json = "{\"plan\":\"" + sub.getPlan() + "\","
				+ "\"invitationsSent\":" + sub.getInvitationsSent() + ","
				+ "\"maxFree\":" + sub.getMaxInvitationsFree() + ","
				+ "\"canSend\":" + sub.canSendInvitation() + ","
				+ "\"remaining\":" + sub.getRemainingFreeInvitations()
				+ "}";
		JsonUtil.sendJson(response, json);
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

		if ("/upgrade".equals(path)) {
			String body = JsonUtil.readRequestBody(request);
			String plan = JsonUtil.getStringValue(body, "plan");
			if (plan == null || plan.isEmpty()) plan = "PREMIUM";
			plan = plan.toUpperCase();
			if (!plan.equals("PRO") && !plan.equals("PREMIUM")) {
				JsonUtil.sendError(response, 400, "Plan invalide (PRO ou PREMIUM attendu)");
				return;
			}
			boolean success = metier.updateSubscriptionPlan(userId, plan);
			if (success) {
				metier.changeSubscription(userId, plan);
				String label = "PRO".equals(plan) ? "Pro" : "Premium";
				JsonUtil.sendJson(response, "{\"success\":true,\"message\":\"Felicitations ! Vous etes maintenant " + label + ".\",\"plan\":\"" + plan + "\"}");
			} else {
				JsonUtil.sendError(response, 500, "Erreur lors de la mise a niveau");
			}
		} else {
			JsonUtil.sendError(response, 404, "Route non trouvee");
		}
	}
}
