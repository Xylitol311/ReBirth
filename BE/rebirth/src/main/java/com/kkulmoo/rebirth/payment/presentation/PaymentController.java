package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.*;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.request.OnlinePayDTO;
import com.kkulmoo.rebirth.payment.presentation.response.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;

import lombok.extern.slf4j.Slf4j;

/*

추가 수정 필요
1) 프론트에 전달해야할 데이터 -> 카드 이름, 카드 이미지, 별 좌표
2) 결제 이후 DB 저장로직
 */


@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
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

    // 오프라인에서의 일회용 토큰 생성
    @GetMapping("/disposabletoken")
    public ResponseEntity<?> getDisposableToken(@RequestParam(value="userId") int userId) throws Exception {
    //1. 사용자 받아온 걸로 영구토큰 전부다 가져오기
        // 영구 토큰하고 카드 고유 번호만 주는데, 나중에 카드 사진하고 카드 이름도 주는 걸로 바꾸기
    List<String[]> PTandUCN = paymentService.getAllUsersPermanentToken(userId);


    //2. 영구 토큰 싹다 일회용 토큰 처리
    List<PaymentTokenResponseDTO> disposableTokens = paymentService.createDisposableToken(PTandUCN,userId);
    ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"일회용 토큰 생성",disposableTokens);

    //3. 토큰 전달
    //3-1. sse 열기
        return ResponseEntity.ok(apiResponseDTO);

    }

//    // 오프라인에서의 결제 진행
//    @PostMapping("/progresspay")
//    public ResponseEntity<?> progressPay(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {
//
//        //1. 받은 토큰을 redis에 가서 실제 값을 가져오기
//        String realToken= paymentService.getRealDisposableToken(createTransactionRequestDTO.getToken());
//        String[] tokenInfo = paymentOfflineEncryption.validateOneTimeToken(realToken);
//
//        log.info(realToken);
//        log.info(tokenInfo[0]);
//
//        String permanentToken = tokenInfo[0];
//        int userId = Integer.parseInt(tokenInfo[1]);
//
//        sseService.sendToUser(userId, "결제시작");
//
//        //1. 추천 카드 일경우 로직 작성
//        if(permanentToken.equals("rebirth")){
//
//        }
//
//        //2. 추천 카드가 아닐 경우 해당 카드로 정보 검색
//        // 영구토큰 까서 검색해서 카드 가져오기
//        // CardTemplate cardTemplate = paymentService.getCardTemplate(permanentToken);
//
//        //2-2. permanent는 웹 클라이언트로 카드사에 넘기기 & 값 받
//        log.info(permanentToken);
//        CreateTransactionRequestDTO dataToCardsa = CreateTransactionRequestDTO.builder().token(permanentToken).amount(createTransactionRequestDTO.getAmount()).merchantName(createTransactionRequestDTO.getMerchantName()).build();
//        CardTransactionDTO cardTransactionDTO = webClientService.checkPermanentToken(dataToCardsa).block();
//
//        //3. 받은 값으로 아래 데이터 갱신하기
//        // 값 최종 업데이트 해주기 ( 이거 어디어디 해줘야 하는데... ) -> 나중으로 우선 미루기
//
//        //4. 결제 결과 반환하기
//        sseService.sendToUser(userId, cardTransactionDTO.getApprovalCode());
//
//        return ResponseEntity.ok(cardTransactionDTO);
//    }


    // 가맹점을 위한 QR 코드 생성
    @GetMapping("/generateqr")
    public ResponseEntity<?> generateQRforOnline(@RequestParam("merchantName") String merchantName, @RequestParam("amount") int amount) throws Exception {

        String QRcode = paymentOnlineEncryption.generateQRToken(merchantName, amount);

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"가맹점 QRd용 토큰 생성",QRcode);
        return ResponseEntity.ok(apiResponseDTO);

    }

    // 반환해야하는 데이터 확인
    @PostMapping("/onlinedisposabletoken")
    public ResponseEntity<?> generateOnlineDisposableToken(@RequestBody OnlinePayDTO onlinePay) throws Exception {

        // 받은 토큰을 까서 가맹점 & 가격 확인 0: 가맹점 이름 , 1: 가격정보
        String[] merchantInfo = paymentOnlineEncryption.validateQRToken(onlinePay.getToken());

        //1. 사용자 받아온 걸로 영구토큰 전부다 가져오기
        List<String[]> PTandUCN = paymentService.getAllUsersPermanentToken(onlinePay.getUserId());

        //2. 영구 토큰 싹다 일회용 토큰 처리
        List<PaymentTokenResponseDTO> disposableTokens = paymentService.createOnlineDisposableToken(PTandUCN,merchantInfo[0],
                Integer.parseInt(merchantInfo[1]));
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"일회용 토큰 생성",disposableTokens);



        return ResponseEntity.ok(apiResponseDTO);
    }




}
