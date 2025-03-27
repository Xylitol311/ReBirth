package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.PaymentEncryption;
import com.kkulmoo.rebirth.payment.application.service.PaymentService;
import com.kkulmoo.rebirth.payment.application.service.SseService;
import com.kkulmoo.rebirth.payment.application.service.WebClientService;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentEncryption paymentEncryption;
    private final SseService sseService;
    private final WebClientService webClientService;


    public PaymentController(PaymentService paymentService, PaymentEncryption paymentEncryption, SseService sseService, WebClientService webClientService) {
        this.paymentService = paymentService;
        this.paymentEncryption = paymentEncryption;
        this.sseService = sseService;
        this.webClientService = webClientService;
    }

    @GetMapping("/disposabletoken")
    public ResponseEntity<?> getTemporaryPaymentToken(@RequestParam(value="userId") int userId) throws Exception {
    //1. 사용자 받아온 걸로 영구토큰 전부다 가져오기
    List<String[]> PTandUCN = paymentService.getAllUsersPermanentToken(userId);


    //2. 영구 토큰 싹다 일회용 토큰 처리
    List<String[]> disposableTokens = paymentService.createDisposableToken(PTandUCN,userId);
    ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true,"일회용 토큰 생성",disposableTokens);

    //3. 토큰 전달
    return ResponseEntity.ok(apiResponseDTO);

    }

    @PostMapping("/progresspay")
    public ResponseEntity<?> progressPay(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) throws Exception {

        System.out.println("이건 그냥 받은 토큰 " + createTransactionRequestDTO.getToken());

        //1. 받은 토큰을 redis에 가서 실제 값을 가져오기
        String realToken= paymentService.getRealDisposableToken(createTransactionRequestDTO.getToken());
        System.out.println("redis에서 겁색해서 받은 토큰" + realToken);
        String[] tokenInfo = paymentEncryption.validateOneTimeToken(realToken);

        String permanentToken = tokenInfo[0];
        int userId = Integer.parseInt(tokenInfo[1]);

        sseService.sendToUser(userId, "결제 진행 중");

        //1. 추천 카드 일경우 로직 작성
        if(permanentToken.equals("rebirth")){

        }

        //2. 추천 카드가 아닐 경우 해당 카드로 정보 검색
        // 영구토큰 까서 검색해서 카드 가져오기
        // CardTemplate cardTemplate = paymentService.getCardTemplate(permanentToken);

        //2-2. permanent는 웹 클라이언트로 카드사에 넘기기 & 값 받
        CreateTransactionRequestDTO dataToCardsa = CreateTransactionRequestDTO.builder().token(permanentToken).amount(createTransactionRequestDTO.getAmount()).merchantName(createTransactionRequestDTO.getMerchantName()).build();
        CardTransactionDTO cardTransactionDTO = webClientService.checkPermanentToken(dataToCardsa).block();

        //3. 받은 값으로 아래 데이터 갱신하기
        // 값 최종 업데이트 해주기 ( 이거 어디어디 해줘야 하는데... ) -> 나중으로 우선 미루기

        //4. 결제 결과 반환하기
        sseService.sendToUser(userId, cardTransactionDTO.getApprovalCode());

        return ResponseEntity.ok(cardTransactionDTO);
    }


    //선택해서 카드 결제 하는 경우
    //1. rebirth로 시작하는게 아닐 경우에

//    선택 결제 시 로직
//1. 일회용 토큰 복호화
//1-1. 영구 토큰에 해당하는 해당 카드에 대해서 SSE로 클라한테 보내주기(end point는 userId)(아래 비동기)
//            2. 영구 토큰을 카드사 에게 보내기
//3. 카드사한테 결제 결과를 받기
//4. 결제 결과를 DB에 저장하기
//5. 결제 되었다는 걸 SSE로 다시 쏘기, SSE 연결 끊기


    //2. rebirth로 시작 할 경우





    // 추천카드로 결제 할때 포스기에서 직접 선택을하던가
    // 바코드 값의 시작값을 바꾸던가


    //가맹점 QR 생성

    // 포스기 ui, 마켓 ui도 만들어야함
    // 포스기 ui는 api 그냥 연결 하나만 해두면 될듯 web에서
    //pc 용 ui도 하나 만들어 오기, 그냥 클론 코딩해서 payment 키기


    // 추천 결제 바코드에는 userId넣어주는데, 이건 프론트에서 해주기






}
