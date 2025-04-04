package com.kkulmoo.rebirth.payment.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentOnlineEncryption {
    private final EncryptionUtils encryptionUtils;


    @Value("${token.secret.key}")
    private String secretKey; // 서명용 키 (HMAC)

    @Value("${aes.key}")
    private String aesKey; // AES 암호화 키

    private static final long EXPIRATION_TIME = 5 * 60 * 1000; // 5분


    // 가맹점하고 가격 정보 담아서 토큰 생성 ( AES 처리만 하기 )
    public String generateQRToken(String merchantName, int amount) throws Exception {
        long expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        String iv = encryptionUtils.generateIV();

        String encryptedData = encryptionUtils.encryptAES(merchantName + "|" + amount, aesKey, iv);

        String data = encryptedData + "|" + iv + "|" + expiration;
        String signature = encryptionUtils.generateHMAC(data, secretKey);

        return Base64.getUrlEncoder().withoutPadding().encodeToString((data + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }


    // 0 : 가맹점 정보, 1 : 가격 던지기
    public String[] validateQRToken(String token) throws Exception {
        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split("\\|");

        if (parts.length != 4) return null;

        String encryptedData = parts[0];
        String iv = parts[1];
        long expiration = Long.parseLong(parts[2]);
        String signature = parts[3];

        //만료 처리로 에러 던지기
        if (System.currentTimeMillis() > expiration) return null;

        String data = encryptedData + "|" + iv + "|" + expiration;
        String expectedSignature = encryptionUtils.generateHMAC(data, secretKey);

        if (!expectedSignature.equals(signature)) return null;

        // 복호화 후 가맹점 이름하고 가격 던지기
        String decryptedData = encryptionUtils.decryptAES(encryptedData, aesKey, iv);
        String[] decryptedParts = decryptedData.split("\\|");

        if (decryptedParts.length != 2) return null;

        return decryptedParts;
    }

    // 유저 정보, 가맹점, 가격, 영구 토큰 담아서 온라인용 토큰 생성
    public String generateOnlineToken(String merchantName, int amount, String permanentToken, int userId) throws Exception {
        long expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        String iv = encryptionUtils.generateIV();

        String dataToEncrypt = userId + "|" + permanentToken + "|" + merchantName + "|" + amount;
        String encryptedData = encryptionUtils.encryptAES(dataToEncrypt, aesKey, iv);

        String data = encryptedData + "|" + iv + "|" + expiration;
        String signature = encryptionUtils.generateHMAC(data, secretKey);

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((data + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    // 온라인 토큰 검증 메서드
    // 복호화 결과 : [userId, permanentToken, merchantName, amount]
    public String[] validateOnlineToken(String token) throws Exception {
        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split("\\|");

        if (parts.length != 4) return null;

        String encryptedData = parts[0];
        String iv = parts[1];
        long expiration = Long.parseLong(parts[2]);
        String signature = parts[3];

        //만료 처리로 에러 던지기
        if (System.currentTimeMillis() > expiration) return null;

        String data = encryptedData + "|" + iv + "|" + expiration;
        String expectedSignature = encryptionUtils.generateHMAC(data, secretKey);

        if (!expectedSignature.equals(signature)) return null;

        // 복호화 후 가맹점 이름하고 가격하고 토큰 던지기
        String decryptedData = encryptionUtils.decryptAES(encryptedData, aesKey, iv);
        String[] decryptedParts = decryptedData.split("\\|");

        if (decryptedParts.length != 4) return null;

        return decryptedParts;
    }
}
