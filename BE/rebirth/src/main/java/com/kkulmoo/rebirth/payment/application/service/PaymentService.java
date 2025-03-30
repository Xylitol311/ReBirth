package com.kkulmoo.rebirth.payment.application.service;
import com.kkulmoo.rebirth.payment.domain.*;
import com.kkulmoo.rebirth.payment.domain.repository.CardTemplateRepository;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.domain.repository.DisposableTokenRepository;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
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

            if(cards.getPermanentToken()== null) continue;
            String[] tokenAndCUN = {cards.getCardUniqueNumber(), cards.getPermanentToken()};
            userPTs.add(tokenAndCUN);
        }

        return userPTs;

    }

    // PTandUCN(영구토큰과 사용자 고유번호) -> 0 : 고유번호 1: 영구토큰
    // disposableTokens -> 0: 고유번호 1:일회용 토큰
    public List<PaymentTokenResponseDTO> createDisposableToken(List<String[]> PTandUCN, int userId) throws Exception {

        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)

        if(PTandUCN.isEmpty()) return null;

        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();
        for(String[] pt : PTandUCN) {

            // 영구 토큰 별로 일회용 토큰 생성
            String realToken = paymentEncryption.generateOneTimeToken(pt[1],userId);

            // 일회용 토큰을 20자로 줄이기
            String shortToken = realToken.substring(0,20);

            // 카드 고유 번호와 일회용 토큰 넘기기
            disposableTokensResponse.add(PaymentTokenResponseDTO.builder().token(shortToken).cardId(pt[0]).build());

            //redis에 key: 일회용 토큰 / value : 진짜 토큰으로 저장
            disposableTokenRepository.saveToken(shortToken,realToken);
        }

        // 추천카드의 고유번호는 000으로, 영구토큰 대신 rebirth로
        String realRecommendToken = paymentEncryption.generateOneTimeToken("rebirth", userId);
        String shortRecommendToken = realRecommendToken.substring(0,20);

        disposableTokensResponse.add(PaymentTokenResponseDTO.builder().token(shortRecommendToken).cardId("000").build());

        // 얘도 redis에 저장하기
        disposableTokenRepository.saveToken(shortRecommendToken,realRecommendToken);

        return disposableTokensResponse;
    }

    // 잘린 토큰으로 전체 토큰 가져오기
    public String getRealDisposableToken(String shortDisposableTokens){
        if(shortDisposableTokens.isEmpty()) return null;

        return disposableTokenRepository.findById(shortDisposableTokens);
    }

    //영구 토큰에 있는 카드 아이디 전달하고 카드 아이디에 있는 카드 템플릿 가져오기 ( 프론트 화면 용 )
    public CardTemplate getCardTemplate(String permanentToken){

        int cardTemplateId = cardsRepository.findCardTemplateIdByToken(permanentToken);
        CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(cardTemplateId);

        return cardTemplate;
    }


}
