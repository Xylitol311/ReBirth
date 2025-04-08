package com.kkulmoo.rebirth.user.application.service;

import lombok.RequiredArgsConstructor;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CoolSmsService {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.api.number}")
    private String fromPhoneNumber;

    // todo: 이름바꾸기
    private final RedisTemplate<String, String> redisTemplate;

    private static final long VERIFICATION_EXPIRE_TIME = 5;
    private static final String SMS_PREFIX = "SMS:VERIFICATION:";



    public String sendSms(String to) throws CoolsmsException {
        try {
            // 랜덤한 4자리 인증번호 생성
            String verificationCode  = generateRandomNumber();

            Message coolsms = new Message(apiKey, apiSecret); // 생성자를 통해 API 키와 API 시크릿 전달

            HashMap<String, String> params = new HashMap<>();
            params.put("to", to);    // 수신 전화번호
            params.put("from", fromPhoneNumber);    // 발신 전화번호
            params.put("type", "sms");
            params.put("text", "[RE:BIRTH]의 인증번호는 [" + verificationCode + "] 입니다.");

            // 메시지 전송
            coolsms.send(params);

            String redisKey = SMS_PREFIX + to;
            redisTemplate.opsForValue().set(redisKey, verificationCode, VERIFICATION_EXPIRE_TIME, TimeUnit.MINUTES);

            return verificationCode; // 생성된 인증번호 반환

        } catch (Exception e) {
            throw new CoolsmsException("SMS 전송 실패: " + e.getMessage(), -1);
        }
    }


    public boolean verifyCode(String phoneNumber, String code) {
        String redisKey = SMS_PREFIX + phoneNumber;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        // 저장된 코드가 없거나 불일치하는 경우
        if (savedCode == null || !savedCode.equals(code)) {
            return false;
        }

        // 검증 성공 시 Redis에서 삭제 (재사용 방지)
        redisTemplate.delete(redisKey);
        return true;
    }

    private String generateRandomNumber() {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            numStr.append(rand.nextInt(10));
        }
        return numStr.toString();
    }

}
