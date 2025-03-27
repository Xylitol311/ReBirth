package com.kkulmoo.rebirth.payment.application.service;
import com.kkulmoo.rebirth.payment.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//List가 비어있을 경우에 대한 에러처리 해야함


@Service
public class PaymentService {

    private final CardsRepository cardsRepository;
    private final DisposableTokenRepository disposableTokenRepository;
    private final PaymentEncryption paymentEncryption;
    private final CardTemplateRepository cardTemplateRepository;

    public PaymentService(CardsRepository cardsRepository, DisposableTokenRepository disposableTokenRepository, PaymentEncryption paymentEncryption, CardTemplateRepository cardTemplateRepository) {
        this.cardsRepository = cardsRepository;
        this.disposableTokenRepository = disposableTokenRepository;
        this.paymentEncryption = paymentEncryption;
        this.cardTemplateRepository = cardTemplateRepository;
    }


    public List<String> getAllUsersPermanentToken(int userId){

        List<Cards> userCards = cardsRepository.findByUserId(userId);

        if(userCards.isEmpty()) return null;

        List<String> userPTs = new ArrayList<>();
        for(Cards cards : userCards){
            userPTs.add(cards.getPermanentToken());
        }

        return userPTs;

    }

    public List<String> createDisposableToken(List<String> permanentToken, int userId) throws Exception {

        // 사용자 정보로 부터 영구 토큰 받아서 옴
        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)

        if(permanentToken.isEmpty()) return null;

        List<String> disposableTokens = new ArrayList<>();
        for(String pt : permanentToken) {
            disposableTokens.add(paymentEncryption.generateOneTimeToken(pt,userId));
        }

        disposableTokens.add(paymentEncryption.generateOneTimeToken("rebirth", userId));

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

    //영구 토큰에 있는 카드 아이디 전달하고 카드 아이디에 있는 카드 템플릿 가져오기
    public CardTemplate getCardTemplate(String permanentToken){

        int cardTemplateId = cardsRepository.findCardTemplateIdByToken(permanentToken);
        CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(cardTemplateId);

        return cardTemplate;
    }


}
