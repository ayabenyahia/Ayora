package com.ayora.util;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

	// Parametres de connexion a la base ayora_db (phpMyAdmin / MySQL)
	private static final String DB_NAME = "ayora_db";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "";

	// On reutilise le pattern DataSource / MySQLDataSource du cours p01-jdbc
	private static final MySQLDataSource dataSource =
			new MySQLDataSource(DB_NAME, DB_USER, DB_PASSWORD);

	public DatabaseConnection() {
	}

	public static Connection getConnection() throws SQLException {
		Connection db = dataSource.getConnection();
		if (db == null) {
			throw new SQLException("## Erreur : impossible d'ouvrir la connexion a " + DB_NAME);
		}
		return db;
	}

	public static void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				System.out.println("## Erreur fermeture connexion : " + e.getMessage());
			}
		}
	}
}
