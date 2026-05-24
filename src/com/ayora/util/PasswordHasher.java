package com.ayora.util;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashage de mots de passe via PBKDF2-HMAC-SHA-256 (stdlib Java, aucune
 * dependance externe).
 *
 * Format stocke en base : "pbkdf2$<iterations>$<saltBase64>$<hashBase64>"
 * Exemple :   pbkdf2$120000$qfX9aL...$Z7K2bP...
 *
 * Conforme aux recommandations OWASP 2023 pour PBKDF2 :
 *   - >= 120 000 iterations HMAC-SHA-256
 *   - salt aleatoire 16 octets
 *   - cle derivee 32 octets (256 bits)
 *
 * Methodes publiques :
 *   - hash(plain) -> String      : retourne le format encode complet
 *   - verify(plain, stored) -> boolean : compare en temps constant
 *   - isHashed(stored) -> boolean      : detecte si la valeur est deja hashee
 *                                        (utile pour migration progressive
 *                                        depuis l'ancienne base "passwords
 *                                        en clair").
 */
public final class PasswordHasher {

	private static final int ITERATIONS = 120_000;
	private static final int KEY_LENGTH_BITS = 256;
	private static final int SALT_LENGTH = 16;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final String PREFIX = "pbkdf2$";

	private PasswordHasher() { /* utility */ }

	/** Genere un hash auto-contenu (sel + iterations + hash). */
	public static String hash(String plain) {
		if (plain == null) throw new IllegalArgumentException("password null");
		byte[] salt = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(salt);
		byte[] hash = pbkdf2(plain.toCharArray(), salt, ITERATIONS);
		Base64.Encoder enc = Base64.getEncoder().withoutPadding();
		return PREFIX + ITERATIONS + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
	}

	/** Verifie un mot de passe contre une chaine au format ci-dessus. */
	public static boolean verify(String plain, String stored) {
		if (plain == null || stored == null) return false;
		if (!stored.startsWith(PREFIX)) return false;
		String[] parts = stored.split("\\$");
		if (parts.length != 4) return false;
		try {
			int iter = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			byte[] actual = pbkdf2(plain.toCharArray(), salt, iter);
			return constantTimeEquals(expected, actual);
		} catch (Exception e) {
			return false;
		}
	}

	/** Vrai si la chaine fournie est deja un hash PBKDF2 (sinon = clair). */
	public static boolean isHashed(String stored) {
		return stored != null && stored.startsWith(PREFIX);
	}

	// ------------------------------------------------------------------

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
		try {
			KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
			return skf.generateSecret(spec).getEncoded();
		} catch (Exception e) {
			throw new IllegalStateException("Erreur PBKDF2 : " + e.getMessage(), e);
		}
	}

	/** Comparaison en temps constant : prevention timing attacks. */
	private static boolean constantTimeEquals(byte[] a, byte[] b) {
		if (a == null || b == null) return false;
		if (a.length != b.length) return false;
		int diff = 0;
		for (int i = 0; i < a.length; i++) diff |= (a[i] ^ b[i]);
		return diff == 0;
	}
}
