package com.kkulmoo.rebirth.payment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

        return emitter;
    }


    public void sendToUser(int userId, String message) {
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
