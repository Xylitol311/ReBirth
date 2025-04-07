package com.kkulmoo.rebirth.payment.application.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SseService {

    private final Map<Integer, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 70 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;
    private static final long HEARTBEAT_INTERVAL = 30 * 1000; // 30초

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // 30초마다 heartbeat를 전송하는 스케줄러 설정
        scheduler.scheduleAtFixedRate(this::sendHeartbeatToAll, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
    }

    public SseEmitter subscribe(int userId) {
        SseEmitter emitter = createEmitter();

        // SseEmitter를 userId와 매핑하여 emitterMap에 저장
        emitterMap.put(userId, emitter);

        // 연결 세션 timeout 이벤트 핸들러 등록
        emitter.onTimeout(() -> {
            log.info("server sent event timed out : userId={}", userId);
            emitterMap.remove(userId);
            emitter.complete();
        });

        // 에러 핸들러 등록
        emitter.onError(e -> {
            log.info("server sent event error occurred : userId={}, message={}", userId, e.getMessage());
            emitterMap.remove(userId);
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
            emitterMap.remove(userId); // 초기 메시지 전송 실패 시 emitter 제거
            emitter.complete();
        }

        return emitter;
    }

    public void sendToUser(int userId, String message) {
        log.info("유저한테 sse 보내는중: {}", userId);
        SseEmitter emitter = emitterMap.get(userId);
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
                emitterMap.remove(userId); // 메시지 전송 실패 시 emitter 제거
                emitter.complete();
            }
        } else {
            log.warn("해당 userId에 대한 emitter를 찾을 수 없음: {}", userId);
        }
    }

    /**
     * 모든 연결된 클라이언트에게 heartbeat 메시지를 전송
     */
    private void sendHeartbeatToAll() {
        log.debug("Sending heartbeat to all connected clients. Current connections: {}", emitterMap.size());

        // ConcurrentModificationException 방지를 위해 복사본 사용
        List<Integer> deadEmitters = new ArrayList<>();

        emitterMap.forEach((userId, emitter) -> {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name("heartbeat")
                        .data("ping")
                        .reconnectTime(RECONNECTION_TIMEOUT);
                emitter.send(event);
                log.debug("Heartbeat sent to userId: {}", userId);
            } catch (IOException e) {
                log.warn("Failed to send heartbeat to userId: {}. Marking for removal.", userId);
                deadEmitters.add(userId);
            }
        });

        // 실패한 emitter 정리
        for (Integer userId : deadEmitters) {
            SseEmitter emitter = emitterMap.remove(userId);
            if (emitter != null) {
                emitter.complete();
                log.info("Removed dead emitter for userId: {}", userId);
            }
        }
    }

    /**
     * 현재 연결된 클라이언트 수 반환
     */
    public int getActiveConnectionCount() {
        return emitterMap.size();
    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);
    }
}
