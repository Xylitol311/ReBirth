package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.application.service.*;
import com.kkulmoo.rebirth.payment.presentation.request.CardInfoDTO;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.request.OnlinePayDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.OnlinePayResponseDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentOnlineEncryption paymentOnlineEncryption;

    @PostMapping("/registpaymentcard")
    public ResponseEntity<?> registPaymentCard(@RequestBody CardInfoDTO cardInfoDTO) {

        // 카드사에 넘겨주기
        // 1. userId로 userCI 검색해서 가져오기

        // 2.
        // 카드사에게 받은 데이터 토대로 사용자 카드 부분 update

        return ResponseEntity.ok("임시");
    }


    // 오프라인에서의 일회용 토큰 생성
    @GetMapping("/disposabletoken")
    public ResponseEntity<?> getDisposableToken(@RequestParam(value = "userId") int userId) throws Exception {
        //1. 사용자 받아온 걸로 영구토큰 전부다 가져오기
        List<String[]> PTandUCN = paymentService.getAllUsersPermanentTokenAndTemplateId(userId);

        //2. 영구 토큰 싹다 일회용 토큰 처리
        List<PaymentTokenResponseDTO> disposableTokens = paymentService.createDisposableToken(PTandUCN, userId);
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "일회용 토큰 생성", disposableTokens);

        //3. 토큰 전달
        return ResponseEntity.ok(apiResponseDTO);
    }

    // 온라인 가맹점을 위한 QR 코드 생성
    @GetMapping("/generateqr")
    public ResponseEntity<?> generateQRforOnline(@RequestParam("merchantName") String merchantName, @RequestParam("amount") int amount) throws Exception {

        String QRcode = paymentOnlineEncryption.generateQRToken(merchantName, amount);

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "가맹점 QR용 토큰 생성", QRcode);
        return ResponseEntity.ok(apiResponseDTO);

    }

    // QR 찍은 후 클라이언트에게(고객) 가맹점 정보 & 토큰 정보 반환
    @PostMapping("/onlinedisposabletoken")
    public ResponseEntity<?> generateOnlineDisposableToken(@RequestBody OnlinePayDTO onlinePay) throws Exception {

        // QR 토큰 검증: [가맹점명, 금액]
        String[] merchantInfo = paymentOnlineEncryption.validateQRToken(onlinePay.getToken());

        // 사용자 보유 카드 정보 조회
        List<String[]> cardInfo = paymentService.getAllUsersPermanentTokenAndTemplateId(onlinePay.getUserId());

        // 온라인 일회용 토큰 생성 시 userId를 함께 전달
        List<PaymentTokenResponseDTO> disposableTokens = paymentService.createOnlineDisposableToken(
                cardInfo,
                merchantInfo[0],
                Integer.parseInt(merchantInfo[1]),
                onlinePay.getUserId()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "일회용 토큰 생성",
                OnlinePayResponseDTO.builder()
                        .paymentTokenResponseDTO(disposableTokens)
                        .merchantName(merchantInfo[0])
                        .amount(Integer.parseInt(merchantInfo[1]))
                        .build());
        return ResponseEntity.ok(apiResponseDTO);
    }

    /**
     * 토큰 받고 나서 결제 로직 작성
     * DB 업뎃 할때는 영구토큰으로 찾아서 바꾸기
     * 토큰 안에 있는 내용 -> 가맹점, 가격, 영구토큰
     */
    @PostMapping("/onlineprogresspay")
    public ResponseEntity<?> onlineProgressPay(@RequestBody String token) throws Exception {
        // 토큰 검증 후 [userId, 영구토큰, 가맹점명, 금액] 추출
        String[] tokenData = paymentOnlineEncryption.validateOnlineToken(token);
        int userId = Integer.parseInt(tokenData[0]);
        String permanentToken = tokenData[1];
        String merchantName = tokenData[2];
        int amount = Integer.parseInt(tokenData[3]);

        // 매 결제마다 추천 카드 로직 호출 (추천 기록 저장 및 혜택 비교용)
        CalculatedBenefitDto recommendedBenefit = paymentService.recommendPaymentCard(userId, amount, merchantName);

        // 요청 토큰의 영구토큰이 "rebirth"인 경우 추천 카드의 영구토큰을 사용
        if (permanentToken.equals("rebirth")) {
            if (recommendedBenefit != null && recommendedBenefit.getPermanentToken() != null) {
                permanentToken = recommendedBenefit.getPermanentToken();
            }
        }

        CreateTransactionRequestDTO dataToCardsa = CreateTransactionRequestDTO.builder()
                .token(permanentToken)
                .amount(amount)
                .merchantName(merchantName)
                .build();
        CardTransactionDTO cardTransactionDTO = paymentService.transactionToCardsa(dataToCardsa);
        ApiResponseDTO apiResponseDTO = new ApiResponseDTO(true, "결제 응답", cardTransactionDTO);

        return ResponseEntity.ok(apiResponseDTO);
    }
}
