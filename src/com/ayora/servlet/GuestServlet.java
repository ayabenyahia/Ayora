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
import com.ayora.model.Guest;
import com.ayora.util.JsonUtil;

@WebServlet("/api/guests/*")
public class GuestServlet extends HttpServlet {
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
		List<Guest> guests = metier.getGuestsByUser(userId);

		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < guests.size(); i++) {
			Guest g = guests.get(i);
			if (i > 0) json.append(",");
			json.append(buildGuestJson(g));
		}
		json.append("]");
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
		String body = JsonUtil.readRequestBody(request);

		String firstName = JsonUtil.getStringValue(body, "firstName");
		String lastName = JsonUtil.getStringValue(body, "lastName");

		if (firstName == null || lastName == null) {
			JsonUtil.sendError(response, 400, "Prenom et nom requis");
			return;
		}

		Guest guest = new Guest();
		guest.setUserId(userId);
		guest.setFirstName(firstName);
		guest.setLastName(lastName);
		guest.setPhone(JsonUtil.getStringValue(body, "phone"));
		guest.setEmail(JsonUtil.getStringValue(body, "email"));
		guest.setGroupe(JsonUtil.getStringValue(body, "groupe"));
		int nbPersonnes = JsonUtil.getIntValue(body, "nbPersonnes");
		guest.setNbPersonnes(nbPersonnes > 0 ? nbPersonnes : 1);
		guest.setNote(JsonUtil.getStringValue(body, "note"));

		if (guest.getGroupe() == null) {
			guest.setGroupe("AUTRES");
		}

		int guestId = metier.addGuest(guest);
		if (guestId == -1) {
			JsonUtil.sendError(response, 500, "Erreur lors de l'ajout de l'invite");
			return;
		}

		guest.setId(guestId);
		JsonUtil.sendJson(response, "{\"success\":true,\"guest\":" + buildGuestJson(guest) + "}");
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			JsonUtil.sendError(response, 401, "Non authentifie");
			return;
		}

		String path = request.getPathInfo();
		if (path == null || path.length() < 2) {
			JsonUtil.sendError(response, 400, "ID invite requis");
			return;
		}

		int guestId;
		try {
			guestId = Integer.parseInt(path.substring(1));
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "ID invalide");
			return;
		}

		String body = JsonUtil.readRequestBody(request);
		Guest guest = metier.getGuest(guestId);
		if (guest == null) {
			JsonUtil.sendError(response, 404, "Invite non trouve");
			return;
		}

		String firstName = JsonUtil.getStringValue(body, "firstName");
		String lastName = JsonUtil.getStringValue(body, "lastName");
		if (firstName != null) guest.setFirstName(firstName);
		if (lastName != null) guest.setLastName(lastName);

		String phone = JsonUtil.getStringValue(body, "phone");
		if (phone != null) guest.setPhone(phone);
		String email = JsonUtil.getStringValue(body, "email");
		if (email != null) guest.setEmail(email);
		String groupe = JsonUtil.getStringValue(body, "groupe");
		if (groupe != null) guest.setGroupe(groupe);
		int nbPersonnes = JsonUtil.getIntValue(body, "nbPersonnes");
		if (nbPersonnes > 0) guest.setNbPersonnes(nbPersonnes);
		String note = JsonUtil.getStringValue(body, "note");
		if (note != null) guest.setNote(note);

		boolean success = metier.updateGuest(guest);
		if (success) {
			JsonUtil.sendSuccess(response, "Invite mis a jour");
		} else {
			JsonUtil.sendError(response, 500, "Erreur mise a jour");
		}
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
			JsonUtil.sendError(response, 400, "ID invite requis");
			return;
		}

		int guestId;
		try {
			guestId = Integer.parseInt(path.substring(1));
		} catch (NumberFormatException e) {
			JsonUtil.sendError(response, 400, "ID invalide");
			return;
		}

		boolean success = metier.deleteGuest(guestId);
		if (success) {
			JsonUtil.sendSuccess(response, "Invite supprime");
		} else {
			JsonUtil.sendError(response, 500, "Erreur suppression");
		}
	}

	private String buildGuestJson(Guest g) {
		return "{\"id\":" + g.getId()
				+ ",\"firstName\":\"" + JsonUtil.escapeJson(g.getFirstName()) + "\""
				+ ",\"lastName\":\"" + JsonUtil.escapeJson(g.getLastName()) + "\""
				+ ",\"phone\":\"" + JsonUtil.escapeJson(g.getPhone() != null ? g.getPhone() : "") + "\""
				+ ",\"email\":\"" + JsonUtil.escapeJson(g.getEmail() != null ? g.getEmail() : "") + "\""
				+ ",\"groupe\":\"" + g.getGroupe() + "\""
				+ ",\"nbPersonnes\":" + g.getNbPersonnes()
				+ ",\"note\":\"" + JsonUtil.escapeJson(g.getNote() != null ? g.getNote() : "") + "\""
				+ "}";
	}
}
