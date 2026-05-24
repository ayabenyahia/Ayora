package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ayora.model.QuestionnaireAnswer;
import com.ayora.util.Database;

/** DAO de l'entite QuestionnaireAnswer. */
public class QuestionnaireDao implements IDao {

	private final Database db;

	public QuestionnaireDao(Database db) {
		this.db = db;
	}

	public QuestionnaireAnswer findByUserId(int userId) {
		return db.queryOne(
			"SELECT * FROM questionnaire_answers WHERE user_id = ?",
			this::mapAnswer, userId);
	}

	public boolean create(QuestionnaireAnswer a) {
		return db.executeUpdate(
			"INSERT INTO questionnaire_answers (user_id, budget_total, budget_flexibility, "
			+ "nb_invites, nb_invites_femmes, nb_invites_hommes, "
			+ "date_mariage, saison_preferee, lieu_ceremonie, "
			+ "style_mariage, ambiance, theme_couleur, niveau_luxe, "
			+ "priorite_salle, priorite_traiteur, priorite_photo, priorite_musique, "
			+ "priorite_decoration, priorite_neggafa, priorite_makeup, "
			+ "type_cuisine, type_musique, pref_photo, pref_decoration, "
			+ "nb_tenues_neggafa, style_neggafa, postes_economie, notes_speciales) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			a.getUserId(), a.getBudgetTotal(), a.getBudgetFlexibility(),
			a.getNbInvites(), a.getNbInvitesFemmes(), a.getNbInvitesHommes(),
			a.getDateMariage(), a.getSaisonPreferee(), a.getLieuCeremonie(),
			a.getStyleMariage(), a.getAmbiance(), a.getThemeCouleur(), a.getNiveauLuxe(),
			a.getPrioriteSalle(), a.getPrioriteTraiteur(), a.getPrioritePhoto(), a.getPrioriteMusique(),
			a.getPrioriteDecoration(), a.getPrioriteNeggafa(), a.getPrioriteMakeup(),
			a.getTypeCuisine(), a.getTypeMusique(), a.getPrefPhoto(), a.getPrefDecoration(),
			a.getNbTenuesNeggafa(), a.getStyleNeggafa(), a.getPostesEconomie(), a.getNotesSpeciales()) > 0;
	}

	public boolean update(QuestionnaireAnswer a) {
		return db.executeUpdate(
			"UPDATE questionnaire_answers SET budget_total=?, budget_flexibility=?, "
			+ "nb_invites=?, nb_invites_femmes=?, nb_invites_hommes=?, "
			+ "date_mariage=?, saison_preferee=?, lieu_ceremonie=?, "
			+ "style_mariage=?, ambiance=?, theme_couleur=?, niveau_luxe=?, "
			+ "priorite_salle=?, priorite_traiteur=?, priorite_photo=?, priorite_musique=?, "
			+ "priorite_decoration=?, priorite_neggafa=?, priorite_makeup=?, "
			+ "type_cuisine=?, type_musique=?, pref_photo=?, pref_decoration=?, "
			+ "nb_tenues_neggafa=?, style_neggafa=?, postes_economie=?, notes_speciales=? "
			+ "WHERE user_id=?",
			a.getBudgetTotal(), a.getBudgetFlexibility(),
			a.getNbInvites(), a.getNbInvitesFemmes(), a.getNbInvitesHommes(),
			a.getDateMariage(), a.getSaisonPreferee(), a.getLieuCeremonie(),
			a.getStyleMariage(), a.getAmbiance(), a.getThemeCouleur(), a.getNiveauLuxe(),
			a.getPrioriteSalle(), a.getPrioriteTraiteur(), a.getPrioritePhoto(), a.getPrioriteMusique(),
			a.getPrioriteDecoration(), a.getPrioriteNeggafa(), a.getPrioriteMakeup(),
			a.getTypeCuisine(), a.getTypeMusique(), a.getPrefPhoto(), a.getPrefDecoration(),
			a.getNbTenuesNeggafa(), a.getStyleNeggafa(), a.getPostesEconomie(), a.getNotesSpeciales(),
			a.getUserId()) > 0;
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
