package com.ayora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.ayora.model.QuestionnaireAnswer;
import com.ayora.util.DatabaseConnection;

public class QuestionnaireDao {

	public QuestionnaireDao() {
	}

	public QuestionnaireAnswer findByUserId(int userId) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM questionnaire_answers WHERE user_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return mapAnswer(rs);
			}
		} catch (SQLException e) {
			System.out.println("## Erreur findByUserId : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return null;
	}

	public boolean create(QuestionnaireAnswer answer) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "INSERT INTO questionnaire_answers (user_id, budget_total, budget_flexibility, "
					+ "nb_invites, nb_invites_femmes, nb_invites_hommes, "
					+ "date_mariage, saison_preferee, lieu_ceremonie, "
					+ "style_mariage, ambiance, theme_couleur, "
					+ "niveau_luxe, "
					+ "priorite_salle, priorite_traiteur, priorite_photo, priorite_musique, "
					+ "priorite_decoration, priorite_neggafa, priorite_makeup, "
					+ "type_cuisine, type_musique, pref_photo, pref_decoration, "
					+ "nb_tenues_neggafa, style_neggafa, "
					+ "postes_economie, notes_speciales) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, answer.getUserId());
			ps.setDouble(2, answer.getBudgetTotal());
			ps.setString(3, answer.getBudgetFlexibility());
			ps.setInt(4, answer.getNbInvites());
			ps.setInt(5, answer.getNbInvitesFemmes());
			ps.setInt(6, answer.getNbInvitesHommes());
			ps.setString(7, answer.getDateMariage());
			ps.setString(8, answer.getSaisonPreferee());
			ps.setString(9, answer.getLieuCeremonie());
			ps.setString(10, answer.getStyleMariage());
			ps.setString(11, answer.getAmbiance());
			ps.setString(12, answer.getThemeCouleur());
			ps.setString(13, answer.getNiveauLuxe());
			ps.setInt(14, answer.getPrioriteSalle());
			ps.setInt(15, answer.getPrioriteTraiteur());
			ps.setInt(16, answer.getPrioritePhoto());
			ps.setInt(17, answer.getPrioriteMusique());
			ps.setInt(18, answer.getPrioriteDecoration());
			ps.setInt(19, answer.getPrioriteNeggafa());
			ps.setInt(20, answer.getPrioriteMakeup());
			ps.setString(21, answer.getTypeCuisine());
			ps.setString(22, answer.getTypeMusique());
			ps.setString(23, answer.getPrefPhoto());
			ps.setString(24, answer.getPrefDecoration());
			ps.setInt(25, answer.getNbTenuesNeggafa());
			ps.setString(26, answer.getStyleNeggafa());
			ps.setString(27, answer.getPostesEconomie());
			ps.setString(28, answer.getNotesSpeciales());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur create questionnaire : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	public boolean update(QuestionnaireAnswer answer) {
		Connection connection = null;
		try {
			connection = DatabaseConnection.getConnection();
			String sql = "UPDATE questionnaire_answers SET budget_total=?, budget_flexibility=?, "
					+ "nb_invites=?, nb_invites_femmes=?, nb_invites_hommes=?, "
					+ "date_mariage=?, saison_preferee=?, lieu_ceremonie=?, "
					+ "style_mariage=?, ambiance=?, theme_couleur=?, "
					+ "niveau_luxe=?, "
					+ "priorite_salle=?, priorite_traiteur=?, priorite_photo=?, priorite_musique=?, "
					+ "priorite_decoration=?, priorite_neggafa=?, priorite_makeup=?, "
					+ "type_cuisine=?, type_musique=?, pref_photo=?, pref_decoration=?, "
					+ "nb_tenues_neggafa=?, style_neggafa=?, "
					+ "postes_economie=?, notes_speciales=? "
					+ "WHERE user_id=?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setDouble(1, answer.getBudgetTotal());
			ps.setString(2, answer.getBudgetFlexibility());
			ps.setInt(3, answer.getNbInvites());
			ps.setInt(4, answer.getNbInvitesFemmes());
			ps.setInt(5, answer.getNbInvitesHommes());
			ps.setString(6, answer.getDateMariage());
			ps.setString(7, answer.getSaisonPreferee());
			ps.setString(8, answer.getLieuCeremonie());
			ps.setString(9, answer.getStyleMariage());
			ps.setString(10, answer.getAmbiance());
			ps.setString(11, answer.getThemeCouleur());
			ps.setString(12, answer.getNiveauLuxe());
			ps.setInt(13, answer.getPrioriteSalle());
			ps.setInt(14, answer.getPrioriteTraiteur());
			ps.setInt(15, answer.getPrioritePhoto());
			ps.setInt(16, answer.getPrioriteMusique());
			ps.setInt(17, answer.getPrioriteDecoration());
			ps.setInt(18, answer.getPrioriteNeggafa());
			ps.setInt(19, answer.getPrioriteMakeup());
			ps.setString(20, answer.getTypeCuisine());
			ps.setString(21, answer.getTypeMusique());
			ps.setString(22, answer.getPrefPhoto());
			ps.setString(23, answer.getPrefDecoration());
			ps.setInt(24, answer.getNbTenuesNeggafa());
			ps.setString(25, answer.getStyleNeggafa());
			ps.setString(26, answer.getPostesEconomie());
			ps.setString(27, answer.getNotesSpeciales());
			ps.setInt(28, answer.getUserId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("## Erreur update questionnaire : " + e.getMessage());
		} finally {
			DatabaseConnection.closeConnection(connection);
		}
		return false;
	}

	private QuestionnaireAnswer mapAnswer(ResultSet rs) throws SQLException {
		QuestionnaireAnswer a = new QuestionnaireAnswer();
		a.setId(rs.getInt("id"));
		a.setUserId(rs.getInt("user_id"));
		a.setBudgetTotal(rs.getDouble("budget_total"));
		a.setBudgetFlexibility(rs.getString("budget_flexibility"));
		a.setNbInvites(rs.getInt("nb_invites"));
		a.setNbInvitesFemmes(rs.getInt("nb_invites_femmes"));
		a.setNbInvitesHommes(rs.getInt("nb_invites_hommes"));
		a.setDateMariage(rs.getString("date_mariage"));
		a.setSaisonPreferee(rs.getString("saison_preferee"));
		a.setLieuCeremonie(rs.getString("lieu_ceremonie"));
		a.setStyleMariage(rs.getString("style_mariage"));
		a.setAmbiance(rs.getString("ambiance"));
		a.setThemeCouleur(rs.getString("theme_couleur"));
		a.setNiveauLuxe(rs.getString("niveau_luxe"));
		a.setPrioriteSalle(rs.getInt("priorite_salle"));
		a.setPrioriteTraiteur(rs.getInt("priorite_traiteur"));
		a.setPrioritePhoto(rs.getInt("priorite_photo"));
		a.setPrioriteMusique(rs.getInt("priorite_musique"));
		a.setPrioriteDecoration(rs.getInt("priorite_decoration"));
		a.setPrioriteNeggafa(rs.getInt("priorite_neggafa"));
		a.setPrioriteMakeup(rs.getInt("priorite_makeup"));
		a.setTypeCuisine(rs.getString("type_cuisine"));
		a.setTypeMusique(rs.getString("type_musique"));
		a.setPrefPhoto(rs.getString("pref_photo"));
		a.setPrefDecoration(rs.getString("pref_decoration"));
		a.setNbTenuesNeggafa(rs.getInt("nb_tenues_neggafa"));
		a.setStyleNeggafa(rs.getString("style_neggafa"));
		a.setPostesEconomie(rs.getString("postes_economie"));
		a.setNotesSpeciales(rs.getString("notes_speciales"));
		return a;
	}
}
