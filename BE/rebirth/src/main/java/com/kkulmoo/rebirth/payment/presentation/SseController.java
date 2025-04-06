package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.application.service.PaymentOfflineEncryption;
import com.kkulmoo.rebirth.payment.application.service.PaymentService;
import com.kkulmoo.rebirth.payment.application.service.SseService;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/payment/sse")
public class SseController {
    private final SseService sseService;
    private final PaymentService paymentService;
    private final PaymentOfflineEncryption paymentOfflineEncryption;


    public SseController(SseService sseService, PaymentService paymentService, PaymentOfflineEncryption paymentOfflineEncryption) {
        this.sseService = sseService;
        this.paymentService = paymentService;
        this.paymentOfflineEncryption = paymentOfflineEncryption;

    }

    // 특정 유저 SSE 구독
    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@RequestParam(value = "userId") int userId) {
        log.info("sse 구독 진행중~~~ userId: {}", userId);

        SseEmitter emitter = sseService.subscribe(userId);

        // 클라이언트가 연결을 끊으면 제거하는 이벤트 리스너 추가
        emitter.onCompletion(() -> log.info("SSE 연결 종료 - userId: {}", userId));
        emitter.onTimeout(() -> log.warn("SSE 타임아웃 - userId: {}", userId));

        return ResponseEntity.ok(emitter);
    }


    //오프라인 결제 (포스기)
    @PostMapping("/progresspay")
    public ResponseEntity<?> progressPay(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {
        // 토큰 검증 후 [영구토큰, userId] 추출
        String realToken = paymentService.getRealDisposableToken(createTransactionRequestDTO.getToken());
        String[] tokenInfo = paymentOfflineEncryption.validateOneTimeToken(realToken);

        log.info(realToken);
        log.info(tokenInfo[0]);

        String permanentToken = tokenInfo[0];
        int userId = Integer.parseInt(tokenInfo[1]);
        String merchantName = createTransactionRequestDTO.getMerchantName();
        int amount = createTransactionRequestDTO.getAmount();

        // 결제 시작 알림
        sseService.sendToUser(userId, "결제시작");

        // 공통 결제 처리 로직 호출
        CardTransactionDTO cardTransactionDTO = paymentService.processPayment(userId, permanentToken, merchantName, amount);

        // 결제 결과 알림
        sseService.sendToUser(userId, cardTransactionDTO.getApprovalCode());
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "결제 응답", cardTransactionDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }
}

