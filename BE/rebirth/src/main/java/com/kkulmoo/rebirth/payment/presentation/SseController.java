package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/payment/sse")
public class SseController {
    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    // 특정 유저 SSE 구독
    @GetMapping("/subscribe")
    public ResponseEntity<SseEmitter> subscribe(@RequestParam(value="userId") int userId) {
        log.info("sse 구독 진행중~~~ userId: {}", userId);

        SseEmitter emitter = sseService.subscribe(userId);

        // 클라이언트가 연결을 끊으면 제거하는 이벤트 리스너 추가
        emitter.onCompletion(() -> log.info("SSE 연결 종료 - userId: {}", userId));
        emitter.onTimeout(() -> log.warn("SSE 타임아웃 - userId: {}", userId));

        return ResponseEntity.ok(emitter);
    }
}

