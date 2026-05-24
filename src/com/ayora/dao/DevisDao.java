package com.ayora.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ayora.model.Devis;
import com.ayora.util.Database;

/** DAO de l'entite Devis (table demandes_devis). */
public class DevisDao implements IDao {

	private static final String SELECT_BASE =
		"SELECT d.*, u.first_name AS client_fn, u.last_name AS client_ln, "
		+ "u.email AS client_email, u.phone AS client_phone, "
		+ "v.name AS vendor_name, vc.name_fr AS vendor_cat "
		+ "FROM demandes_devis d "
		+ "JOIN users u ON d.client_id = u.id "
		+ "JOIN vendors v ON d.vendor_id = v.id "
		+ "JOIN vendor_categories vc ON v.category_id = vc.id ";

	private final Database db;

	public DevisDao(Database db) {
		this.db = db;
	}

	public List<Devis> findByClient(int clientId) {
		return db.queryList(
			SELECT_BASE + "WHERE d.client_id = ? ORDER BY d.created_at DESC",
			this::map, clientId);
	}

	public List<Devis> findByVendor(int vendorId) {
		return db.queryList(
			SELECT_BASE + "WHERE d.vendor_id = ? ORDER BY d.created_at DESC",
			this::map, vendorId);
	}

	public List<Devis> findAll() {
		return db.queryList(SELECT_BASE + "ORDER BY d.created_at DESC", this::map);
	}

	public List<Devis> findAll(String statutFilter) {
		if (statutFilter == null || statutFilter.isEmpty()) {
			return db.queryList(
				SELECT_BASE + "ORDER BY d.created_at DESC LIMIT 200",
				this::map);
		}
		return db.queryList(
			SELECT_BASE + "WHERE d.statut = ? ORDER BY d.created_at DESC LIMIT 200",
			this::map, statutFilter);
	}

	public int create(Devis d) {
		return db.insertReturningKey(
			"INSERT INTO demandes_devis (client_id, vendor_id, budget_min, budget_max, message, date_mariage, nb_invites) "
			+ "VALUES (?,?,?,?,?,?,?)",
			d.getClientId(), d.getVendorId(), d.getBudgetMin(), d.getBudgetMax(),
			d.getMessage() != null ? d.getMessage() : "",
			d.getDateMariage() != null ? d.getDateMariage() : "",
			d.getNbInvites());
	}

	public boolean updateStatutAndReponse(int id, String statut, String reponse) {
		return db.executeUpdate(
			"UPDATE demandes_devis SET statut = ?, reponse_prestataire = ? WHERE id = ?",
			statut, reponse != null ? reponse : "", id) > 0;
	}

	public boolean updateStatut(int id, String statut) {
		return db.executeUpdate(
			"UPDATE demandes_devis SET statut = ? WHERE id = ?",
			statut, id) > 0;
	}

	public int countAll() {
		return db.queryInt("SELECT COUNT(*) FROM demandes_devis");
	}

	public int countByStatut(String statut) {
		return db.queryInt("SELECT COUNT(*) FROM demandes_devis WHERE statut = ?", statut);
	}

	public int countByClient(int clientId) {
		return db.queryInt("SELECT COUNT(*) FROM demandes_devis WHERE client_id = ?", clientId);
	}

	public int countByVendor(int vendorId) {
		return db.queryInt("SELECT COUNT(*) FROM demandes_devis WHERE vendor_id = ?", vendorId);
	}

	private Devis map(ResultSet rs) throws SQLException {
		Devis d = new Devis();
		d.setId(rs.getInt("id"));
		d.setClientId(rs.getInt("client_id"));
		d.setVendorId(rs.getInt("vendor_id"));
		d.setBudgetMin(rs.getDouble("budget_min"));
		d.setBudgetMax(rs.getDouble("budget_max"));
		d.setMessage(rs.getString("message"));
		d.setDateMariage(rs.getString("date_mariage"));
		d.setNbInvites(rs.getInt("nb_invites"));
		d.setStatut(rs.getString("statut"));
		d.setReponsePrestataire(rs.getString("reponse_prestataire"));
		d.setCreatedAt(rs.getString("created_at"));
		d.setClientFirstName(rs.getString("client_fn"));
		d.setClientLastName(rs.getString("client_ln"));
		d.setClientEmail(rs.getString("client_email"));
		d.setClientPhone(rs.getString("client_phone"));
		d.setVendorName(rs.getString("vendor_name"));
		d.setVendorCategory(rs.getString("vendor_cat"));
		return d;
	}
}
