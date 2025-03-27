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

    //밑에거는 왜 필요한거지?
//    // 특정 유저에게 메시지 전송
//    //user.Id를 어디서 받지? 헤더에 담아서 주나? 아니면 그냥 넘겨주나?
//    @PostMapping("/send/{userId}")
//    public String sendMessage(@RequestBody RequestDTO request) {
//        sseService.sendToUser(request.userId, request.message);
//        return "Message sent to user " + request.userId;
//    }

}