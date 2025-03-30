package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.PaymentOfflineEncryption;
import com.kkulmoo.rebirth.payment.application.service.PaymentService;
import com.kkulmoo.rebirth.payment.application.service.SseService;
import com.kkulmoo.rebirth.payment.application.service.WebClientService;
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
    private final WebClientService webClientService;

    public SseController(SseService sseService, PaymentService paymentService, PaymentOfflineEncryption paymentOfflineEncryption, WebClientService webClientService) {
        this.sseService = sseService;
        this.paymentService = paymentService;
        this.paymentOfflineEncryption = paymentOfflineEncryption;
        this.webClientService = webClientService;
    }

    // 특정 유저 SSE 구독
    @GetMapping(path="/subscribe",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@RequestParam(value="userId") int userId) {
        log.info("sse 구독 진행중~~~ userId: {}", userId);

        SseEmitter emitter = sseService.subscribe(userId);

        // 클라이언트가 연결을 끊으면 제거하는 이벤트 리스너 추가
        emitter.onCompletion(() -> log.info("SSE 연결 종료 - userId: {}", userId));
        emitter.onTimeout(() -> log.warn("SSE 타임아웃 - userId: {}", userId));

        return ResponseEntity.ok(emitter);
    }



    @PostMapping("/progresspay")
    public ResponseEntity<?> progressPay(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {


        //1. 받은 토큰을 redis에 가서 실제 값을 가져오기
        String realToken= paymentService.getRealDisposableToken(createTransactionRequestDTO.getToken());
        String[] tokenInfo = paymentOfflineEncryption.validateOneTimeToken(realToken);

        log.info(realToken);
        log.info(tokenInfo[0]);

        String permanentToken = tokenInfo[0];
        int userId = Integer.parseInt(tokenInfo[1]);

        sseService.sendToUser(userId, "결제시작");

        //1. 추천 카드 일경우 로직 작성
        if(permanentToken.equals("rebirth")){

        }

        //2. 추천 카드가 아닐 경우 해당 카드로 정보 검색
        // 영구토큰 까서 검색해서 카드 가져오기
        // CardTemplate cardTemplate = paymentService.getCardTemplate(permanentToken);

        //2-2. permanent는 웹 클라이언트로 카드사에 넘기기 & 값 받
        log.info(permanentToken);
        CreateTransactionRequestDTO dataToCardsa = CreateTransactionRequestDTO.builder().token(permanentToken).amount(createTransactionRequestDTO.getAmount()).merchantName(createTransactionRequestDTO.getMerchantName()).build();
        CardTransactionDTO cardTransactionDTO = webClientService.checkPermanentToken(dataToCardsa).block();

        //3. 받은 값으로 아래 데이터 갱신하기
        // 값 최종 업데이트 해주기 ( 이거 어디어디 해줘야 하는데... ) -> 나중으로 우선 미루기

        //4. 결제 결과 반환하기
        sseService.sendToUser(userId, cardTransactionDTO.getApprovalCode());

        return ResponseEntity.ok(cardTransactionDTO);
    }
}

