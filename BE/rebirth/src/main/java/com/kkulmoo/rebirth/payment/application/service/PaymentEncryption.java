package com.kkulmoo.rebirth.payment.application.service;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PaymentEncryption {

    public PaymentEncryption(){

    }

    // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)
    //5분 만료시간 넣고 | 로 쪼갬, 영구 토큰 받아서 바로 aes로 암호화 함

    @Value("${token.secret.key}")
    private String secretKey; // 서명용 키 (HMAC)

    @Value("${aes.key}")
    private String aesKey; // AES 암호화 키

    private static final long EXPIRATION_TIME = 5 * 60 * 1000; // 5분

    // 키 확인용 코드 , 클래스가 service를 수행하기 전에 발생
    @PostConstruct
    private void validateKeys() {
        if (aesKey.length() != 16 && aesKey.length() != 24 && aesKey.length() != 32) {
            throw new IllegalArgumentException("AES_KEY는 16, 24, 32바이트여야 합니다.(128, 192 또는 256비트)");
        }
    }

    public String generateOneTimeToken(String permanentToken) throws Exception {
        long expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        String iv = generateIV(); // CBC 모드에서는 IV 필요
        String encryptedToken = encryptAES(permanentToken, aesKey, iv);

        String data = encryptedToken + "|" + iv + "|" + expiration;
        String signature = generateHMAC(data, secretKey);

        return Base64.getUrlEncoder().withoutPadding().encodeToString((data + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateOneTimeToken(String token) throws Exception {
        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split("\\|");

        if (parts.length != 4) return false;

        String encryptedToken = parts[0];
        String iv = parts[1];
        long expiration = Long.parseLong(parts[2]);
        String signature = parts[3];

        if (System.currentTimeMillis() > expiration) return false; // 만료 확인

        String data = encryptedToken + "|" + iv + "|" + expiration;
        String expectedSignature = generateHMAC(data, secretKey);

        return expectedSignature.equals(signature);
    }

    private String encryptAES(String data, String key, String iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(Base64.getDecoder().decode(iv));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String decryptAES(String encryptedData, String key, String iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(Base64.getDecoder().decode(iv));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
    }

    private String generateHMAC(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

}
