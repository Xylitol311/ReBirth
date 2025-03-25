package com.kkulmoo.rebirth.payment.application.service;
import com.kkulmoo.rebirth.payment.domain.Cards;
import com.kkulmoo.rebirth.payment.domain.CardsRepository;
import com.kkulmoo.rebirth.payment.domain.DisposableTokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

//List가 비어있을 경우에 대한 에러처리 해야함


@Service
public class PaymentService {

    private final CardsRepository cardsRepository;
    private final DisposableTokenRepository disposableTokenRepository;

    private final PaymentEncryption paymentEncryption;

    public PaymentService(CardsRepository cardsRepository, DisposableTokenRepository disposableTokenRepository, PaymentEncryption paymentEncryption) {
        this.cardsRepository = cardsRepository;
        this.disposableTokenRepository = disposableTokenRepository;
        this.paymentEncryption = paymentEncryption;
    }


    public List<String> getAllUsersPermanentToken(int userId){

        List<Cards> userCards = cardsRepository.findByUserId(userId);

        if(userCards.isEmpty()) return null;

        List<String> userPTs = null;
        for(Cards cards : userCards){
            userPTs.add(cards.getPermanentToken());
        }

        return userPTs;

    }

    public List<String> createDisposableToken(List<String> permanentToken) throws Exception {

        // 사용자 정보로 부터 영구 토큰 받아서 옴
        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)

        if(permanentToken.isEmpty()) return null;

        List<String> disposableTokens = null;
        for(String pt : permanentToken) {
            disposableTokens.add(paymentEncryption.generateOneTimeToken(pt));
        }

        // 각각 하나씩 결제 uuid 생성하기
        // 넘겨주면서 동시에 redis에 저장하기
        saveDisposableToken(disposableTokens);


        return disposableTokens;
    }


    // 일회용토큰 생성시 모든 토큰들을 redis에 저장하기
    public void saveDisposableToken(List<String> disposableTokens){
        if(disposableTokens.isEmpty()) return;

        for(String pt : disposableTokens) {
            String id =UUID.randomUUID().toString();
            disposableTokenRepository.saveToken(pt, id);
        }
    }

}
