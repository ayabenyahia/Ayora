package com.ayora.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapper fonctionnel : transforme la ligne courante d'un ResultSet en un POJO.
 *
 * Utilise par Database.queryList / queryOne. Permet aux DAO de decrire
 * uniquement leur mapping ligne -> objet, sans gerer le boilerplate
 * connection / try-finally / close.
 *
 * Exemple :
 *   db.queryList("SELECT * FROM guests WHERE user_id=?", this::mapGuest, userId);
 *
 *   private Guest mapGuest(ResultSet rs) throws SQLException {
 *       Guest g = new Guest();
 *       g.setId(rs.getInt("id"));
 *       g.setUserId(rs.getInt("user_id"));
 *       ...
 *       return g;
 *   }
 */
@FunctionalInterface
public interface RowMapper<T> {
	T map(ResultSet rs) throws SQLException;
}
