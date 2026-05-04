package com.ayora.dao;

import java.sql.Connection;
import java.sql.SQLException;
import com.ayora.util.DatabaseConnection;

/**
 * DAO de la table user_picks (choix retenus par la mariee).
 *
 * Pattern p02-jee : JDBC pur, PreparedStatement, mapping ResultSet -> POJO.
 * Une selection par categorie pour un user donne (UNIQUE KEY).
 */
public class UserPickDao {

	private static final String SELECT_BASE =
		"SELECT p.id, p.user_id, p.vendor_id, p.category_id, p.picked_at, "
		+ "v.name AS vendor_name, vc.name_fr AS vendor_category, "
		+ "v.gamme AS vendor_gamme, v.prix_min AS vendor_prix_min, "
		+ "v.city AS vendor_city, v.phone AS vendor_phone, "
		+ "v.instagram AS vendor_instagram, v.description AS vendor_description "
		+ "FROM user_picks p "
		+ "JOIN vendors v ON p.vendor_id = v.id "
		+ "JOIN vendor_categories vc ON p.category_id = vc.id ";

	public UserPickDao() {}
}
