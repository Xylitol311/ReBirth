package com.kkulmoo.rebirth.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

	private PasswordUtils() {
		// 인스턴스화 방지
	}

	/**
	 * 비밀번호를 SHA-256으로 해시화합니다.
	 *
	 * @param password 해시화할 원본 비밀번호
	 * @return 해시화된 비밀번호 (16진수 문자열)
	 */
	public static String encodePassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedHash = digest.digest(
				password.getBytes(StandardCharsets.UTF_8));

			return bytesToHex(encodedHash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
		}
	}

	/**
	 * 비밀번호가 저장된 해시와 일치하는지 확인합니다.
	 *
	 * @param rawPassword 확인할 원본 비밀번호
	 * @param storedHash 저장된 해시값
	 * @return 일치 여부
	 */
	public static boolean matchPassword(String rawPassword, String storedHash) {
		String inputHash = encodePassword(rawPassword);
		return inputHash.equals(storedHash);
	}

	/**
	 * 바이트 배열을 16진수 문자열로 변환합니다.
	 */
	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
}