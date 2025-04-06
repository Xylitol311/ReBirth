package com.kkulmoo.rebirth.payment.application.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

//일회용 토큰 생성 및 디코드
@Service
@RequiredArgsConstructor
public class PaymentOfflineEncryption {
    private static final long EXPIRATION_TIME = 5 * 60 * 1000; // 5분
    private final EncryptionUtils encryptionUtils;
    @Value("${token.secret.key}")
    private String secretKey; // 서명용 키 (HMAC)
    @Value("${aes.key}")
    private String aesKey; // AES 암호화 키

    @PostConstruct
    private void validateKeys() {
        if (aesKey.length() != 16 && aesKey.length() != 24 && aesKey.length() != 32) {
            throw new IllegalArgumentException("AES_KEY는 16, 24, 32바이트여야 합니다.(128, 192 또는 256비트)");
        }
    }

    public String generateOneTimeToken(String permanentToken, int userId) throws Exception {
        long expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        String iv = encryptionUtils.generateIV();

        // permanentToken과 userId를 함께 암호화
        String encryptedData = encryptionUtils.encryptAES(permanentToken + "|" + userId, aesKey, iv);

        String data = encryptedData + "|" + iv + "|" + expiration;
        String signature = encryptionUtils.generateHMAC(data, secretKey);

        return Base64.getUrlEncoder().withoutPadding().encodeToString((data + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public String[] validateOneTimeToken(String token) throws Exception {

        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split("\\|");

        if (parts.length != 4) return null;

        String encryptedData = parts[0];
        String iv = parts[1];
        long expiration = Long.parseLong(parts[2]);
        String signature = parts[3];

        if (System.currentTimeMillis() > expiration) return null;

        String data = encryptedData + "|" + iv + "|" + expiration;
        String expectedSignature = encryptionUtils.generateHMAC(data, secretKey);


        if (!expectedSignature.equals(signature)) return null;

        // 복호화 후 permanentToken, userId 추출
        String decryptedData = encryptionUtils.decryptAES(encryptedData, aesKey, iv);
        String[] decryptedParts = decryptedData.split("\\|");

        if (decryptedParts.length != 2) return null;


        return decryptedParts;
    }
}
