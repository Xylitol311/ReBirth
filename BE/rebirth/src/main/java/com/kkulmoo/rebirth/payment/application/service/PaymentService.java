package com.kkulmoo.rebirth.payment.application.service;
import com.kkulmoo.rebirth.payment.domain.*;
import com.kkulmoo.rebirth.payment.domain.repository.CardTemplateRepository;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.domain.repository.DisposableTokenRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//
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


    public List<String[]> getAllUsersPermanentToken(int userId){

        List<Cards> userCards = cardsRepository.findByUserId(userId);

        if(userCards.isEmpty()) return null;

        List<String[]> userPTs = new ArrayList<>();
        for(Cards cards : userCards){

            String[] tokenAndCUN = {cards.getCardUniqueNumber(), cards.getPermanentToken()};
            userPTs.add(tokenAndCUN);
        }

        return userPTs;

    }

    // PTandUCN(영구토큰과 사용자 고유번호) -> 0 : 고유번호 1: 영구토큰
    // disposableTokens -> 0: 고유번호 1:일회용 토큰
    public List<String[]> createDisposableToken(List<String[]> PTandUCN, int userId) throws Exception {

        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)

        if(PTandUCN.isEmpty()) return null;

        List<String[]> disposableTokensResponse = new ArrayList<>();
        for(String[] pt : PTandUCN) {
            disposableTokensResponse.add(new String[]{pt[0],paymentEncryption.generateOneTimeToken(pt[1],userId)});

        }

        // 추천카드의 고유번호는 000으로
        disposableTokensResponse.add(new String[]{"000",paymentEncryption.generateOneTimeToken("rebirth", userId)});


//        // 넘겨주면서 동시에 redis에 저장하기
//        saveDisposableToken(disposableTokens);


        return disposableTokensResponse;
    }

    // 일회용토큰 생성시 모든 토큰들을 redis에 저장하기
    public void saveDisposableToken(List<String> disposableTokens){
        if(disposableTokens.isEmpty()) return;

        for(String pt : disposableTokens) {
            String id =UUID.randomUUID().toString();
            disposableTokenRepository.saveToken(pt, id);
        }
    }

    //영구 토큰에 있는 카드 아이디 전달하고 카드 아이디에 있는 카드 템플릿 가져오기 ( 프론트 화면 용 )
    public CardTemplate getCardTemplate(String permanentToken){

        int cardTemplateId = cardsRepository.findCardTemplateIdByToken(permanentToken);
        CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(cardTemplateId);

        return cardTemplate;
    }


}
