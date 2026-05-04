package com.ayora.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import com.ayora.dao.UserPickDao;
import com.ayora.dao.VendorDao;

/**
 * API des choix utilisateur (page "Mes Choix").
 *
 * Routes :
 *   GET    /api/picks                 -> liste des choix de la mariee
 *   POST   /api/picks                 -> retient un prestataire
 *   DELETE /api/picks/{vendorId}      -> retire ce choix
 */
@WebServlet("/api/picks/*")
public class UserPickServlet extends HttpServlet {

	private UserPickDao pickDao;
	private VendorDao vendorDao;

	@Override
	public void init() throws ServletException {
		pickDao = new UserPickDao();
		vendorDao = new VendorDao();
	}
}
