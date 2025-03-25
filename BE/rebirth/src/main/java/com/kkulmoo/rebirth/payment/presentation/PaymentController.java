package com.kkulmoo.rebirth.payment.presentation;

import com.kkulmoo.rebirth.payment.application.service.PaymentService;
import org.aspectj.weaver.AjAttribute;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/temporarytoken")
    public ResponseEntity<?> getTemporaryPaymentToken(@RequestParam(value="userId") int userId) throws Exception {

    //1. 사용자 받아온 걸로 영구토큰 전부다 가져오기
    List<String> permanentTokens = paymentService.getAllUsersPermanentToken(userId);

    //2. 영구 토큰 싹다 일회용 토큰 처리
    List<String> disposableTokens = paymentService.createDisposableToken(permanentTokens);

    //2-1. 추천 카드에 대한 바코드
    disposableTokens.add("rebirth|"+"userId");

    //3. 토큰 전달
    return ResponseEntity.ok(disposableTokens);

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
