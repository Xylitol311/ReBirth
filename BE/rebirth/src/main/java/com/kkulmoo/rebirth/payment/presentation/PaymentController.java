package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import com.kkulmoo.rebirth.payment.application.service.PaymentTokenService;
import com.kkulmoo.rebirth.payment.application.service.PaymentTransactionService;
import com.kkulmoo.rebirth.payment.presentation.request.OnlinePayDTO;
import com.kkulmoo.rebirth.payment.presentation.request.PermanentTokenRequestToCardsaDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.OnlinePayResponseDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    // 토큰 관련 기능을 제공하는 서비스
    private final PaymentTokenService paymentTokenService;
    // 결제 처리를 담당하는 서비스
    private final PaymentTransactionService paymentTransactionService;

    // 카드 등록 엔드포인트 (추후 상세 구현)
    @PostMapping("/registpaymentcard")
    public ResponseEntity<?> registPaymentCard(@JwtUserId Integer userId, @RequestBody PermanentTokenRequestToCardsaDTO permanentTokenRequest) {

        // 카드 등록하고 영구토큰 가져오기
        paymentTokenService.getPermanentTokenFromCardsa(userId, permanentTokenRequest);
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "카드 등록 완료", null);
        return ResponseEntity.ok(apiResponseDTO);
    }

    // 오프라인 일회용 토큰 생성 엔드포인트
    @GetMapping("/disposabletoken")
    public ResponseEntity<?> getDisposableToken(@JwtUserId Integer userId) throws Exception {
        // 사용자 보유 카드의 영구토큰과 템플릿 ID 조회
        List<String[]> cardInfo = paymentTokenService.getAllUsersPermanentTokenAndTemplateId(userId);
        // 오프라인 일회용 토큰 생성 및 DB 저장
        List<PaymentTokenResponseDTO> disposableTokens = paymentTokenService.createDisposableToken(cardInfo, userId);
        // 응답 객체 생성 후 반환
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "일회용 토큰 생성", disposableTokens);
        return ResponseEntity.ok(apiResponseDTO);
    }

    // 온라인 가맹점용 QR 토큰 생성 엔드포인트
    @GetMapping("/generateqr")
    public ResponseEntity<?> generateQRforOnline(@RequestParam("merchantName") String merchantName,
                                                 @RequestParam("amount") int amount) throws Exception {
        // QR 토큰 생성을 위해 PaymentTokenService 호출
        String QRcode = paymentTokenService.generateQRToken(merchantName, amount);
        // 응답 객체 생성 후 반환
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "가맹점 QR용 토큰 생성", QRcode);
        return ResponseEntity.ok(apiResponseDTO);
    }

    // 온라인 일회용 토큰 생성 엔드포인트
    @PostMapping("/onlinedisposabletoken")
    public ResponseEntity<?> generateOnlineDisposableToken(@RequestBody OnlinePayDTO onlinePay) throws Exception {
        // QR 토큰 검증을 통해 가맹점 정보 추출
        String[] merchantInfo = paymentTokenService.validateQRToken(onlinePay.getToken());
        // 사용자 보유 카드 정보 조회
        List<String[]> cardInfo = paymentTokenService.getAllUsersPermanentTokenAndTemplateId(onlinePay.getUserId());
        // 온라인 일회용 토큰 생성 및 DB 저장
        List<PaymentTokenResponseDTO> disposableTokens = paymentTokenService.createOnlineDisposableToken(
                cardInfo,
                merchantInfo[0],
                Integer.parseInt(merchantInfo[1]),
                onlinePay.getUserId()
        );
        // 온라인 결제 응답 DTO 구성
        OnlinePayResponseDTO onlineResponse = OnlinePayResponseDTO.builder()
                .paymentTokenResponseDTO(disposableTokens)
                .merchantName(merchantInfo[0])
                .amount(Integer.parseInt(merchantInfo[1]))
                .build();
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "일회용 토큰 생성", onlineResponse);
        return ResponseEntity.ok(apiResponseDTO);
    }

    // 온라인 결제 진행 엔드포인트
    @PostMapping("/onlineprogresspay")
    public ResponseEntity<?> onlineProgressPay(@RequestBody String token) throws Exception {
        // 온라인 토큰 검증 후 결제 정보(사용자 ID, 영구토큰, 가맹점, 금액) 추출
        String[] tokenData = paymentTokenService.validateOnlineToken(token);
        int userId = Integer.parseInt(tokenData[0]);
        String permanentToken = tokenData[1];
        String merchantName = tokenData[2];
        int amount = Integer.parseInt(tokenData[3]);
        // 결제 처리 서비스 호출
        CardTransactionDTO cardTransactionDTO = paymentTransactionService.processPayment(userId, permanentToken, merchantName, amount);
        // 응답 객체 생성 후 반환
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "결제 응답", cardTransactionDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }




}