package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ayora.model.RendezVous;
import com.ayora.util.Database;

/** DAO de l'entite RendezVous (table rendez_vous). */
public class RendezVousDao implements IDao {

	private static final String SELECT_BASE =
		"SELECT r.*, u.first_name AS client_fn, u.last_name AS client_ln, "
		+ "u.phone AS client_phone, v.name AS vendor_name "
		+ "FROM rendez_vous r "
		+ "JOIN users u ON r.client_id = u.id "
		+ "JOIN vendors v ON r.vendor_id = v.id ";

	private final Database db;

	public RendezVousDao(Database db) {
		this.db = db;
	}

	public List<RendezVous> findByClient(int clientId) {
		return db.queryList(
			SELECT_BASE + "WHERE r.client_id = ? ORDER BY r.date_rdv ASC",
			this::map, clientId);
	}

	public List<RendezVous> findByVendor(int vendorId) {
		return db.queryList(
			SELECT_BASE + "WHERE r.vendor_id = ? ORDER BY r.date_rdv ASC",
			this::map, vendorId);
	}

	public List<RendezVous> findAll() {
		return db.queryList(SELECT_BASE + "ORDER BY r.date_rdv ASC", this::map);
	}

	public List<RendezVous> findAll(String statutFilter) {
		if (statutFilter == null || statutFilter.isEmpty()) {
			return db.queryList(
				SELECT_BASE + "ORDER BY r.date_rdv DESC LIMIT 200",
				this::map);
		}
		return db.queryList(
			SELECT_BASE + "WHERE r.statut = ? ORDER BY r.date_rdv DESC LIMIT 200",
			this::map, statutFilter);
	}

	public int create(RendezVous r) {
		return db.insertReturningKey(
			"INSERT INTO rendez_vous (client_id, vendor_id, date_rdv, heure_rdv, lieu, note) "
			+ "VALUES (?,?,?,?,?,?)",
			r.getClientId(), r.getVendorId(),
			r.getDateRdv() != null ? r.getDateRdv() : "",
			r.getHeureRdv() != null ? r.getHeureRdv() : "",
			r.getLieu() != null ? r.getLieu() : "A definir",
			r.getNote() != null ? r.getNote() : "");
	}

	public boolean updateStatut(int id, String statut) {
		return db.executeUpdate(
			"UPDATE rendez_vous SET statut = ? WHERE id = ?",
			statut, id) > 0;
	}

	public int countAll() {
		return db.queryInt("SELECT COUNT(*) FROM rendez_vous");
	}

	public int countByStatut(String statut) {
		return db.queryInt("SELECT COUNT(*) FROM rendez_vous WHERE statut = ?", statut);
	}

	public int countByClient(int clientId) {
		return db.queryInt("SELECT COUNT(*) FROM rendez_vous WHERE client_id = ?", clientId);
	}

	public int countByVendor(int vendorId) {
		return db.queryInt("SELECT COUNT(*) FROM rendez_vous WHERE vendor_id = ?", vendorId);
	}

	private RendezVous map(ResultSet rs) throws SQLException {
		RendezVous r = new RendezVous();
		r.setId(rs.getInt("id"));
		r.setClientId(rs.getInt("client_id"));
		r.setVendorId(rs.getInt("vendor_id"));
		r.setDateRdv(rs.getString("date_rdv"));
		r.setHeureRdv(rs.getString("heure_rdv"));
		r.setLieu(rs.getString("lieu"));
		r.setNote(rs.getString("note"));
		r.setStatut(rs.getString("statut"));
		r.setCreatedAt(rs.getString("created_at"));
		r.setClientFirstName(rs.getString("client_fn"));
		r.setClientLastName(rs.getString("client_ln"));
		r.setClientPhone(rs.getString("client_phone"));
		r.setVendorName(rs.getString("vendor_name"));
		return r;
	}
}
