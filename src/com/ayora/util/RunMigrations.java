package com.ayora.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runner de migrations idempotent.
 *
 * Approche :
 *   1) S'assure que la table _migrations existe (creee par migration_security_v1.sql,
 *      mais on la cree quand meme pour bootstrap).
 *   2) Liste tous les fichiers `sql/migration_*.sql` (ordre alphabetique).
 *   3) Pour chaque migration, calcule sa checksum SHA-256. Si elle est deja
 *      tracee avec la meme checksum -> skip. Sinon : applique chaque
 *      instruction (split sur ';') et enregistre.
 *
 * Securite : tous les inserts dans _migrations passent par PreparedStatement.
 * Le contenu SQL des migrations est evidemment execute via Statement (c'est
 * le but) -- ce sont des fichiers controles par le dev, pas des inputs user.
 */
public class RunMigrations {

	private static final Log log = Log.of(RunMigrations.class);

	public static void main(String[] args) throws Exception {
		String sqlDir = args.length > 0 ? args[0] : "sql";
		File dir = new File(sqlDir);
		if (!dir.isDirectory()) { log.error("Dossier introuvable : {0}", dir.getAbsolutePath()); System.exit(1); }

		MySQLDataSource ds = new MySQLDataSource("ayora_db", "root", "");
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(true);
			ensureMigrationsTable(c);
			Set<String> applied = loadApplied(c);

			File[] files = dir.listFiles((f, n) -> n.startsWith("migration_") && n.endsWith(".sql"));
			if (files == null) files = new File[0];
			Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));

			int ok = 0, skip = 0, fail = 0;
			for (File f : files) {
				String name = f.getName();
				String checksum = sha256(Files.readAllBytes(f.toPath()));
				if (applied.contains(name + "::" + checksum)) {
					log.info("[skip]  {0} (deja appliquee, checksum identique)", name);
					skip++;
					continue;
				}
				log.info("[apply] {0}", name);
				try {
					applyFile(c, f);
					recordApplied(c, name, checksum);
					ok++;
				} catch (Exception e) {
					log.error("[fail]  " + name, e);
					fail++;
				}
			}
			log.info("Migrations terminees : {0} OK, {1} skip, {2} fail.", ok, skip, fail);
			if (fail > 0) System.exit(2);
		}
	}

	private static void ensureMigrationsTable(Connection c) throws Exception {
		try (Statement st = c.createStatement()) {
			st.execute("CREATE TABLE IF NOT EXISTS _migrations ("
				+ "filename VARCHAR(190) PRIMARY KEY,"
				+ "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "checksum VARCHAR(64))");
		}
	}

	private static Set<String> loadApplied(Connection c) throws Exception {
		Set<String> out = new HashSet<>();
		try (PreparedStatement ps = c.prepareStatement("SELECT filename, checksum FROM _migrations");
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) out.add(rs.getString(1) + "::" + rs.getString(2));
		}
		return out;
	}

	private static void recordApplied(Connection c, String name, String checksum) throws Exception {
		try (PreparedStatement ps = c.prepareStatement(
				"INSERT INTO _migrations(filename, checksum) VALUES (?, ?) "
				+ "ON DUPLICATE KEY UPDATE checksum=VALUES(checksum), applied_at=CURRENT_TIMESTAMP")) {
			ps.setString(1, name);
			ps.setString(2, checksum);
			ps.executeUpdate();
		}
	}

	private static void applyFile(Connection c, File f) throws Exception {
		String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
		// Split tres simple sur ; en respectant les commentaires de ligne
		List<String> statements = splitStatements(content);
		try (Statement st = c.createStatement()) {
			for (String s : statements) {
				String trimmed = s.trim();
				if (trimmed.isEmpty()) continue;
				st.execute(trimmed);
			}
		}
	}

	private static List<String> splitStatements(String sql) {
		List<String> out = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		for (String line : sql.split("\n")) {
			String trimmed = line.trim();
			if (trimmed.startsWith("--")) continue;
			cur.append(line).append('\n');
			if (trimmed.endsWith(";")) {
				out.add(cur.toString());
				cur.setLength(0);
			}
		}
		if (cur.toString().trim().length() > 0) out.add(cur.toString());
		return out;
	}

	private static String sha256(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] h = md.digest(data);
		StringBuilder sb = new StringBuilder(64);
		for (byte b : h) sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
