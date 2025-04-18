package Util;

import java.security.MessageDigest;

public class PasswordUtils {
	public static String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(password.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b: bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static boolean verifyPassword(String inputPassword, String hashedPassword) {
		String hashedInput = hashPassword(inputPassword);
		return hashedInput.equals(hashedPassword);
	}
}
