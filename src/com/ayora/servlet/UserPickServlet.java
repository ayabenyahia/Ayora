package com.ayora.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

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
}
