package com.ayora.util;

/**
 * Demo CLI de la facade Database.
 *
 * Permet de tester rapidement la connexion et les operations basiques
 * sur la base ayora_db sans demarrer Tomcat. A executer avec :
 *
 *   javac --release 17 -cp WebContent/WEB-INF/lib/* -d bin src/com/ayora/util/*.java
 *   java -cp 'bin;WebContent/WEB-INF/lib/*' com.ayora.util.Examples
 */
public class Examples {

	private Database db;

	public Examples() {
		init();
		exp01();
		exp02();
	}

	void init() {
		DataSource ds = new MySQLDataSource("ayora_db");
		db = new Database(ds);
	}

	void print(String[][] data, int rows) {
		if (data == null) {
			System.out.println("## Aucune donnee.");
			return;
		}
		for (int row = 0; row < rows; row++) {
			System.out.print(data[row][0]);
			for (int col = 1; col < data[row].length; col++) {
				System.out.print(" ; " + data[row][col]);
			}
			System.out.println();
		}
		System.out.println(" ==> Nombre total de lignes : " + data.length);
	}

	void print(String[][] data) {
		print(data, data.length);
	}

	/** Exemple 1 : lister les 5 premieres categories. */
	void exp01() {
		System.out.println("\n=== exp01 : 5 premieres categories ===");
		String[][] data = db.selectAll("vendor_categories");
		if (data != null) print(data, Math.min(5, data.length));
	}

	/** Exemple 2 : chercher les prestataires contenant 'Festin'. */
	void exp02() {
		System.out.println("\n=== exp02 : prestataires Festin (via PreparedStatement) ===");
		// Anciennement : selectByKeyword (supprime - vecteur d'injection SQL).
		// Maintenant : queryList + PreparedStatement, securise.
		java.util.List<String[]> rows = db.queryList(
			"SELECT id, name, city FROM vendors WHERE LOWER(name) LIKE ? LIMIT 5",
			rs -> new String[]{ String.valueOf(rs.getInt(1)), rs.getString(2), rs.getString(3) },
			"%festin%");
		for (String[] r : rows) System.out.println("  " + java.util.Arrays.toString(r));
	}

	public static void main(String[] args) {
		new Examples();
	}
}
