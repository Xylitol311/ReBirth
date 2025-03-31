package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.application.service.*;
import com.kkulmoo.rebirth.payment.presentation.request.CardInfoDTO;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.request.OnlinePayDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*

추가 수정 필요
1) 프론트에 전달해야할 데이터 -> 카드 이름, 카드 이미지, 별 좌표
2) 결제 이후 DB 저장로직
3) cors 설정 다시

 */

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentOfflineEncryption paymentOfflineEncryption;
    private final SseService sseService;
    private final WebClientService webClientService;
    private final PaymentOnlineEncryption paymentOnlineEncryption;


    public PaymentController(PaymentService paymentService, PaymentOfflineEncryption paymentOfflineEncryption, SseService sseService, WebClientService webClientService, PaymentOnlineEncryption paymentOnlineEncryption) {
        this.paymentService = paymentService;
        this.paymentOfflineEncryption = paymentOfflineEncryption;
        this.sseService = sseService;
        this.webClientService = webClientService;
        this.paymentOnlineEncryption = paymentOnlineEncryption;
    }

    @PostMapping("/registpaymentcard")
    public ResponseEntity<?> registPaymentCard(@RequestBody CardInfoDTO cardInfoDTO){

        // 카드사에 넘겨주기
        // 1. userId로 userCI 검색해서 가져오기

        // 2.
        // 카드사에게 받은 데이터 토대로 사용자 카드 부분 update

        return ResponseEntity.ok("임시");
    }


    // 오프라인에서의 일회용 토큰 생성
    @GetMapping("/disposabletoken")
    public ResponseEntity<?> getDisposableToken(@RequestParam(value="userId") int userId) throws Exception {
    //1. 사용자 받아온 걸로 영구토큰 전부다 가져오기
        // 영구 토큰하고 카드 템플릿만 주는데, 나중에 카드 사진하고 카드 이름도 주는 걸로 바꾸기
    List<String[]> PTandUCN = paymentService.getAllUsersPermanentTokenAndTemplateId(userId);

    //2. 영구 토큰 싹다 일회용 토큰 처리
    List<PaymentTokenResponseDTO> disposableTokens = paymentService.createDisposableToken(PTandUCN,userId);
    ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"일회용 토큰 생성",disposableTokens);

    //3. 토큰 전달
    //3-1. sse 열기
        return ResponseEntity.ok(apiResponseDTO);

    }

    // 가맹점을 위한 QR 코드 생성
    @GetMapping("/generateqr")
    public ResponseEntity<?> generateQRforOnline(@RequestParam("merchantName") String merchantName, @RequestParam("amount") int amount) throws Exception {

        String QRcode = paymentOnlineEncryption.generateQRToken(merchantName, amount);

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"가맹점 QR용 토큰 생성",QRcode);
        return ResponseEntity.ok(apiResponseDTO);

    }

    // QR 찍은 후 프론트에 가맹점 정보 & 토큰 정보 반환
    @PostMapping("/onlinedisposabletoken")
    public ResponseEntity<?> generateOnlineDisposableToken(@RequestBody OnlinePayDTO onlinePay) throws Exception {

        // 받은 토큰을 까서 가맹점 & 가격 확인 0: 가맹점 이름 , 1: 가격정보
        String[] merchantInfo = paymentOnlineEncryption.validateQRToken(onlinePay.getToken());

        //1. 사용자 받아온 걸로 영구토큰하고 templateId 전부다 가져오기
        List<String[]> cardInfo = paymentService.getAllUsersPermanentTokenAndTemplateId(onlinePay.getUserId());

        //2. 영구 토큰 싹다 일회용 토큰 처리하고, 카드 정보 모아서 가져오기
        List<PaymentTokenResponseDTO> disposableTokens = paymentService.createOnlineDisposableToken(cardInfo,merchantInfo[0],
                Integer.parseInt(merchantInfo[1]));
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"일회용 토큰 생성",disposableTokens);

        return ResponseEntity.ok(apiResponseDTO);
    }

    // 토큰 받고 나서 결제 로직 작성
    // DB 업뎃 할때는 영구토큰으로 찾아서 바꾸기
    // 토큰 안에 있는 내용 -> 가맹점, 가격, 영구토큰
    @PostMapping("/onlineprogresspay")
    public ResponseEntity<?> onlineProgressPay(@RequestBody String token) throws Exception {

        // 받은 토큰을 까서 가맹점 & 가격 확인 0: 토큰, 1: 가맹점 이름 , 2: 가격정보
        String[] merchantInfo = paymentOnlineEncryption.validateOnlineToken(token);

        CreateTransactionRequestDTO dataToCardsa = CreateTransactionRequestDTO.builder().token(merchantInfo[0]).amount(Integer.parseInt(merchantInfo[2])).merchantName(merchantInfo[1]).build();
        CardTransactionDTO cardTransactionDTO = paymentService.transactionToCardsa(dataToCardsa);
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"일회용 토큰 생성",cardTransactionDTO);

        return ResponseEntity.ok(apiResponseDTO);

    }



}
