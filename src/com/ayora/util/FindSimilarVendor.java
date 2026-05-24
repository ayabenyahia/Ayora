package com.ayora.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FindSimilarVendor {
	public static void main(String[] args) throws Exception {
		MySQLDataSource ds = new MySQLDataSource("ayora_db", "root", "");
		String[] needles = {"cacao", "kiko", "majda", "benjelloun", "tenquif"};
		try (Connection c = ds.getConnection()) {
			for (String n : needles) {
				System.out.println("\n=== Recherche : '" + n + "' ===");
				try (PreparedStatement ps = c.prepareStatement(
						"SELECT id, name FROM vendors WHERE LOWER(name) LIKE ? ORDER BY name")) {
					ps.setString(1, "%" + n.toLowerCase() + "%");
					try (ResultSet rs = ps.executeQuery()) {
						boolean any = false;
						while (rs.next()) {
							System.out.println("  id=" + rs.getInt(1) + " | '" + rs.getString(2) + "'");
							any = true;
						}
						if (!any) System.out.println("  (aucun)");
					}
				}
			}
		}
	}
}
