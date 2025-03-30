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
public class PaymentOnlineEncryption {


    @Value("${token.secret.key}")
    private String secretKey; // 서명용 키 (HMAC)

    @Value("${aes.key}")
    private String aesKey; // AES 암호화 키

    private static final long EXPIRATION_TIME = 5 * 60 * 1000; // 5분


    // 가맹점하고 가격 정보 담아서 토큰 생성 ( AES 처리만 하기 )
    public String generateQRToken(String merchantName, int amount) throws Exception {
        long expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        String iv = generateIV();

        String encryptedData = encryptAES(merchantName + "|" + Integer.toString(amount), aesKey, iv);

        String data = encryptedData + "|" + iv + "|" + expiration;
        String signature = generateHMAC(data, secretKey);

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
        String expectedSignature = generateHMAC(data, secretKey);

        if (!expectedSignature.equals(signature)) return null;

        // 복호화 후 가맹점 이름하고 가격 던지기
        String decryptedData = decryptAES(encryptedData, aesKey, iv);
        String[] decryptedParts = decryptedData.split("\\|");

        if (decryptedParts.length != 2) return null;


        return decryptedParts;

    }

    //가맹점, 가격, 영구 토큰 담아서 생성해야함
// 가맹점하고 가격 정보 담아서 토큰 생성 ( AES 처리만 하기 )
    public String generateOnlineToken(String merchantName, int amount, String token) throws Exception {
        long expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        String iv = generateIV();

        String encryptedData = encryptAES(token + "|" + merchantName + "|" + Integer.toString(amount), aesKey, iv);

        String data = encryptedData + "|" + iv + "|" + expiration;
        String signature = generateHMAC(data, secretKey);

        return Base64.getUrlEncoder().withoutPadding().encodeToString((data + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    // 0 : 가맹점 정보, 1 : 가격 던지기
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
        String expectedSignature = generateHMAC(data, secretKey);

        if (!expectedSignature.equals(signature)) return null;

        // 복호화 후 가맹점 이름하고 가격하고 토큰 던지기
        String decryptedData = decryptAES(encryptedData, aesKey, iv);
        String[] decryptedParts = decryptedData.split("\\|");

        if (decryptedParts.length != 3) return null;


        return decryptedParts;

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
