package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ayora.model.Guest;
import com.ayora.util.Database;

/** DAO de l'entite Guest. */
public class GuestDao implements IDao {

	private final Database db;

	public GuestDao(Database db) {
		this.db = db;
	}

	public List<Guest> findByUserId(int userId) {
		return db.queryList(
			"SELECT * FROM guests WHERE user_id = ? ORDER BY last_name, first_name",
			this::mapGuest, userId);
	}

	public Guest findById(int id) {
		return db.queryOne(
			"SELECT * FROM guests WHERE id = ?",
			this::mapGuest, id);
	}

	public int create(Guest g) {
		return db.insertReturningKey(
			"INSERT INTO guests (user_id, first_name, last_name, phone, email, groupe, nb_personnes, note) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			g.getUserId(), g.getFirstName(), g.getLastName(), g.getPhone(),
			g.getEmail(), g.getGroupe(), g.getNbPersonnes(), g.getNote());
	}

	public boolean update(Guest g) {
		return db.executeUpdate(
			"UPDATE guests SET first_name=?, last_name=?, phone=?, email=?, groupe=?, nb_personnes=?, note=? WHERE id=?",
			g.getFirstName(), g.getLastName(), g.getPhone(), g.getEmail(),
			g.getGroupe(), g.getNbPersonnes(), g.getNote(), g.getId()) > 0;
	}

	public boolean delete(int id) {
		return db.executeUpdate("DELETE FROM guests WHERE id = ?", id) > 0;
	}

	public int countByUserId(int userId) {
		return db.queryInt("SELECT COUNT(*) FROM guests WHERE user_id = ?", userId);
	}

	private Guest mapGuest(ResultSet rs) throws SQLException {
		Guest g = new Guest();
		g.setId(rs.getInt("id"));
		g.setUserId(rs.getInt("user_id"));
		g.setFirstName(rs.getString("first_name"));
		g.setLastName(rs.getString("last_name"));
		g.setPhone(rs.getString("phone"));
		g.setEmail(rs.getString("email"));
		g.setGroupe(rs.getString("groupe"));
		g.setNbPersonnes(rs.getInt("nb_personnes"));
		g.setNote(rs.getString("note"));
		return g;
	}
}
