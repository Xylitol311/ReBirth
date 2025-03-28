package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("api/payment/sse")
public class SseController {
    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    // 특정 유저 SSE 구독
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam("userId") int userId) {
        log.info("sse 구독 진행중~~~");
        return sseService.subscribe(userId);
    }
}
