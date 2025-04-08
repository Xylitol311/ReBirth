package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.application.service.PaymentTokenService;
import com.kkulmoo.rebirth.payment.application.service.PaymentTransactionService;
import com.kkulmoo.rebirth.payment.application.service.SseService;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/payment/sse")
@RequiredArgsConstructor
public class SseController {

    // SSE 구독 및 전송을 위한 서비스
    private final SseService sseService;
    // 결제 처리를 담당하는 서비스
    private final PaymentTransactionService paymentTransactionService;
    // 토큰 관련 기능을 제공하는 서비스
    private final PaymentTokenService paymentTokenService;

    // 특정 유저의 SSE 구독 엔드포인트
    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@RequestParam(value = "userId") int userId) {
        log.info("SSE 연결 요청 - userId: {}", userId);
        // SSE 구독 생성 - 서비스에서 이미 모든 핸들러 설정 및 관리를 담당
        SseEmitter emitter = sseService.subscribe(userId);
        return ResponseEntity.ok(emitter);
    }

    // 오프라인 결제(포스기) 진행 엔드포인트
    @PostMapping("/progresspay")
    public ResponseEntity<?> progressPay(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {
        // 클라이언트로부터 받은 짧은 토큰으로 실제 토큰 복원
        String realToken = paymentTokenService.getRealDisposableToken(createTransactionRequestDTO.getToken());
        // 복원된 토큰을 검증하여 결제 정보 추출
        String[] tokenInfo = paymentTokenService.validateOneTimeToken(realToken);
        String permanentToken = tokenInfo[0];
        int userId = Integer.parseInt(tokenInfo[1]);
        String merchantName = createTransactionRequestDTO.getMerchantName();
        int amount = createTransactionRequestDTO.getAmount();
        // 결제 시작 알림을 SSE를 통해 전송
        sseService.sendToUser(userId, "결제시작");
        // 결제 처리 서비스 호출
        CardTransactionDTO cardTransactionDTO = paymentTransactionService.processPayment(userId, permanentToken, merchantName, amount);
        // 결제 결과 알림을 SSE를 통해 전송
        sseService.sendToUser(userId, cardTransactionDTO.getApprovalCode());
        // 응답 객체 생성 후 반환
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "결제 응답", cardTransactionDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }

    // 오프라인 결제(포스기) 진행 엔드포인트
    @PostMapping("/insert-paydata")
    public ResponseEntity<?> insertPayData(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {
        // 클라이언트로부터 받은 짧은 토큰으로 실제 토큰 복원
        String realToken = paymentTokenService.getRealDisposableToken(createTransactionRequestDTO.getToken());
        // 복원된 토큰을 검증하여 결제 정보 추출
        String[] tokenInfo = paymentTokenService.validateOneTimeToken(realToken);
        String permanentToken = tokenInfo[0];
        int userId = Integer.parseInt(tokenInfo[1]);
        String merchantName = createTransactionRequestDTO.getMerchantName();
        int amount = createTransactionRequestDTO.getAmount();
        // 결제 처리 서비스 호출
        CardTransactionDTO cardTransactionDTO = paymentTransactionService.insertPayData(userId, permanentToken, merchantName, amount, createTransactionRequestDTO.getCreatedAt());
        // 응답 객체 생성 후 반환
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "결제 응답", cardTransactionDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }

    @PostMapping("/test")
    public ResponseEntity<?> insertPayDataa(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {

        Integer userId = 154;
        String merchantName = createTransactionRequestDTO.getMerchantName();
        int amount = createTransactionRequestDTO.getAmount();
        // 결제 처리 서비스 호출
        CardTransactionDTO cardTransactionDTO = paymentTransactionService.insertPayData(userId, createTransactionRequestDTO.getToken(), merchantName, amount, createTransactionRequestDTO.getCreatedAt());
        // 응답 객체 생성 후 반환
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "결제 응답", cardTransactionDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }
}