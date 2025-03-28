package com.kkulmoo.rebirth.payment.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Service
public class SseService {

    // 사용자별 SseEmitter 저장
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(int userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        // 클라이언트가 연결을 끊으면 삭제
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        sendToUser(userId, "start Connecting...");

        log.info("연결진행");

        return emitter;
    }


    public void sendToUser(int userId, String message) {

        log.info("유저한테 sse 보내는중");
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(userId +"에 보내는").data(message));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(userId);
            }
        }
    }
}
