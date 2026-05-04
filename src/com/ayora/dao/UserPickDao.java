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

	public UserPickDao() {}
}
