package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.SseService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("api/payment/sse")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    // 특정 유저 SSE 구독
    // userId가 노출되는데 괜찮나??
    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable int userId) {
        return sseService.subscribe(userId);
    }


}