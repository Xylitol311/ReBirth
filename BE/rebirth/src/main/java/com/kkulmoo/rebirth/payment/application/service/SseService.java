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

    private final Map<Integer, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;

    public SseEmitter subscribe(int userId) {
        SseEmitter emitter = createEmitter();

        // SseEmitter를 userId와 매핑하여 emitterMap에 저장
        emitterMap.put(userId, emitter);

        // 연결 세션 timeout 이벤트 핸들러 등록
        emitter.onTimeout(() -> {
            log.info("server sent event timed out : userId={}", userId);
            emitter.complete();
        });

        // 에러 핸들러 등록
        emitter.onError(e -> {
            log.info("server sent event error occurred : userId={}, message={}", userId, e.getMessage());
            emitter.complete();
        });

        // SSE complete 핸들러 등록
        emitter.onCompletion(() -> {
            if (emitterMap.remove(userId) != null) {
                log.info("server sent event removed in emitter cache: userId={}", userId);
            }

            log.info("disconnected by completed server sent event: userId={}", userId);
        });

        // 초기 연결시에 응답 데이터를 전송할 수도 있다.
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("결제이벤트")
                    .id(String.valueOf("id-1"))
                    .data("SSE 최초 연결 서버에서 보내는 메시지")
                    .reconnectTime(RECONNECTION_TIMEOUT);
            emitter.send(event);
        } catch (IOException e) {
            log.error("failure send media position data, userId={}, {}", userId, e.getMessage());
        }

        return emitter;
    }


    public void sendToUser(int userId, String message) {
        log.info("유저한테 sse 보내는중");
        SseEmitter emitter = emitterMap.get(userId); // 이 부분에서 userId로 emitter를 찾음.
        if (emitter != null) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name("결제중")
                        .id(String.valueOf("id-2"))
                        .data(message)
                        .reconnectTime(RECONNECTION_TIMEOUT);
                emitter.send(event);
            } catch (IOException e) {
                log.error("failure send media position data, userId={}, {}", userId, e.getMessage());
            }
        } else {
            log.warn("해당 userId에 대한 emitter를 찾을 수 없음: {}", userId);
        }
    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);
    }
}
