package com.ayora.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Facade JDBC simple, alignee sur la classe Database du cours
 * (com.esisa.jee.jdbc.Database — p01-jdbc du prof).
 *
 * Expose les memes operations basiques sur une connexion :
 *   executeSelect / selectAll / selectByKeyword / selectById / insert / delete
 *
 * Pour les requetes plus complexes (jointures, parametres typees), les DAO
 * du projet utilisent directement PreparedStatement via DatabaseConnection
 * (plus sur que la concatenation a la main du prof, qui reste pedagogique).
 */
public class Database {

	private DataSource dataSource;
	private Connection db;

	public Database(DataSource dataSource) {
		this.dataSource = dataSource;
		this.db = dataSource.getConnection();
	}

	public Connection getConnection() {
		return db;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public String[][] executeSelect(String query) {
		try {
			Statement sql = db.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY
			);
			ResultSet rs = sql.executeQuery(query);
			rs.last();
			int rows = rs.getRow();
			rs.beforeFirst();
			ResultSetMetaData rsm = rs.getMetaData();
			int cols = rsm.getColumnCount();
			String[][] data = new String[rows][cols];
			int row = 0;
			while (rs.next()) {
				for (int col = 0; col < cols; col++) {
					data[row][col] = rs.getString(col + 1);
				}
				row++;
			}
			rs.close();
			return data;
		} catch (Exception e) {
			System.out.println("## Erreur : " + e.getMessage());
			return null;
		}
	}

	public String[][] selectAll(String tableName) {
		return executeSelect("SELECT * FROM " + tableName);
	}

	public String[][] selectByKeyword(String tableName, String key, Object value) {
		String query = "SELECT * FROM " + tableName
				+ " WHERE " + key + " LIKE '%" + value + "%'";
		return executeSelect(query);
	}

	public String[][] selectById(String tableName, String id, Object value) {
		String query = "SELECT * FROM " + tableName
				+ " WHERE " + id + " = '" + value + "'";
		return executeSelect(query);
	}

	public int insert(String tableName, Object... row) {
		StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
		for (int i = 0; i < row.length; i++) {
			if (i > 0) query.append(", ");
			query.append("'").append(row[i]).append("'");
		}
		query.append(")");
		System.out.println(">> SQL QUERY : " + query);
		try {
			Statement sql = db.createStatement();
			return sql.executeUpdate(query.toString());
		} catch (SQLException e) {
			System.out.println("## Erreur d'insertion : " + e.getMessage());
			return 0;
		}
	}

	public int delete(String tableName, String id, Object value) {
		String query = "DELETE FROM " + tableName
				+ " WHERE " + id + " = '" + value + "'";
		System.out.println(">> SQL QUERY : " + query);
		try {
			Statement sql = db.createStatement();
			return sql.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("## Erreur de suppression : " + e.getMessage());
			return 0;
		}
	}
}
