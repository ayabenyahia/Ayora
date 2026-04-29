package com.ayora.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ayora.dao.SubscriptionDao;
import com.ayora.dao.UserDao;
import com.ayora.model.Subscription;
import com.ayora.util.JsonUtil;

@WebServlet("/api/subscription/*")
public class SubscriptionServlet extends HttpServlet {

	private SubscriptionDao subscriptionDao;
	private UserDao userDao;

	@Override
	public void init() throws ServletException {
		subscriptionDao = new SubscriptionDao();
		userDao = new UserDao();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		int userId = (int) session.getAttribute("userId");
		Subscription sub = subscriptionDao.findByUserId(userId);

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
			boolean success = subscriptionDao.upgradeToPremium(userId);
			if (success) {
				userDao.updateSubscription(userId, "PREMIUM");
				JsonUtil.sendJson(response, "{\"success\":true,\"message\":\"Felicitations ! Vous etes maintenant Premium.\",\"plan\":\"PREMIUM\"}");
			} else {
				JsonUtil.sendError(response, 500, "Erreur lors de la mise a niveau");
			}
		} else {
			JsonUtil.sendError(response, 404, "Route non trouvee");
		}
	}
}
