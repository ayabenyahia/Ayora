package com.ayora.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Runner ponctuel : applique sql/migration_venue_type.sql.
 *
 * Usage : java -cp ...:WebContent/WEB-INF/lib/* com.ayora.util.RunVenueTypeMigration
 *
 * Idempotent : peut etre relance sans casser l'existant.
 */
public class RunVenueTypeMigration {
	public static void main(String[] args) throws Exception {
		String path = args.length > 0 ? args[0] : "sql/migration_venue_type.sql";
		String sql = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

		// On split sur les ';' en ignorant ceux dans des commentaires.
		MySQLDataSource ds = new MySQLDataSource("ayora_db", "root", "");
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(true);
			String[] stmts = sql.split(";");
			int ok = 0, skip = 0, fail = 0;
			for (String raw : stmts) {
				StringBuilder filtered = new StringBuilder();
				for (String line : raw.split("\n")) {
					String trimmed = line.trim();
					if (trimmed.startsWith("--")) continue;
					filtered.append(line).append("\n");
				}
				String s = filtered.toString().trim();
				if (s.isEmpty()) { skip++; continue; }
				try (Statement st = c.createStatement()) {
					boolean hasRs = st.execute(s);
					if (hasRs) {
						try (ResultSet rs = st.getResultSet()) {
							System.out.println("\n>>> Resultats :");
							int cols = rs.getMetaData().getColumnCount();
							while (rs.next()) {
								StringBuilder row = new StringBuilder("    ");
								for (int i = 1; i <= cols; i++) {
									if (i > 1) row.append(" | ");
									row.append(rs.getMetaData().getColumnLabel(i)).append("=").append(rs.getString(i));
								}
								System.out.println(row);
							}
						}
					}
					ok++;
					String snippet = s.length() > 70 ? s.substring(0, 70).replace('\n',' ') + "..." : s.replace('\n',' ');
					System.out.println("  [OK]   " + snippet);
				} catch (Exception e) {
					fail++;
					String snippet = s.length() > 70 ? s.substring(0, 70).replace('\n',' ') + "..." : s.replace('\n',' ');
					System.out.println("  [WARN] " + snippet + " -> " + e.getMessage());
				}
			}
			System.out.println("\nMigration terminee : " + ok + " OK, " + fail + " WARN, " + skip + " vides.");
		}
	}
}
