package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ayora.model.Invitation;
import com.ayora.util.Database;

/** DAO de l'entite Invitation. */
public class InvitationDao implements IDao {

	private final Database db;

	public InvitationDao(Database db) {
		this.db = db;
	}

	public List<Invitation> findByUserId(int userId) {
		return db.queryList(
			"SELECT i.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name "
			+ "FROM invitations i JOIN guests g ON i.guest_id = g.id "
			+ "WHERE i.user_id = ? ORDER BY i.created_at DESC",
			this::mapInvitation, userId);
	}

	public int create(Invitation inv) {
		return db.insertReturningKey(
			"INSERT INTO invitations (guest_id, user_id, statut, template_name, message_perso, video_url) "
			+ "VALUES (?, ?, ?, ?, ?, ?)",
			inv.getGuestId(), inv.getUserId(), inv.getStatut(),
			inv.getTemplateName(), inv.getMessagePerso(), inv.getVideoUrl());
	}

	public boolean updateStatut(int id, String statut) {
		return db.executeUpdate(
			"UPDATE invitations SET statut = ?, "
			+ "date_envoi = CASE WHEN ? = 'ENVOYEE' THEN NOW() ELSE date_envoi END, "
			+ "date_reponse = CASE WHEN ? IN ('CONFIRMEE','DECLINEE') THEN NOW() ELSE date_reponse END "
			+ "WHERE id = ?",
			statut, statut, statut, id) > 0;
	}

	public boolean delete(int id) {
		return db.executeUpdate("DELETE FROM invitations WHERE id = ?", id) > 0;
	}

	private Invitation mapInvitation(ResultSet rs) throws SQLException {
		Invitation inv = new Invitation();
		inv.setId(rs.getInt("id"));
		inv.setGuestId(rs.getInt("guest_id"));
		inv.setUserId(rs.getInt("user_id"));
		inv.setStatut(rs.getString("statut"));
		inv.setTemplateName(rs.getString("template_name"));
		inv.setDateEnvoi(rs.getString("date_envoi"));
		inv.setDateReponse(rs.getString("date_reponse"));
		inv.setMessagePerso(rs.getString("message_perso"));
		try { inv.setVideoUrl(rs.getString("video_url")); } catch (SQLException ignore) {}
		inv.setGuestFirstName(rs.getString("guest_first_name"));
		inv.setGuestLastName(rs.getString("guest_last_name"));
		return inv;
	}
}
