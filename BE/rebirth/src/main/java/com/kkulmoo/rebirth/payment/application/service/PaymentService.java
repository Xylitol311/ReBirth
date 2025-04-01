package com.kkulmoo.rebirth.payment.application.service;
import com.kkulmoo.rebirth.payment.domain.*;
import com.kkulmoo.rebirth.payment.domain.repository.CardTemplateRepository;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.domain.repository.DisposableTokenRepository;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//
@Service
public class PaymentService {

    private final CardsRepository cardsRepository;
    private final DisposableTokenRepository disposableTokenRepository;
    private final PaymentOfflineEncryption paymentOfflineEncryption;
    private final CardTemplateRepository cardTemplateRepository;
    private final PaymentOnlineEncryption paymentOnlineEncryption;
    private final WebClientService webClientService;

    public PaymentService(CardsRepository cardsRepository, DisposableTokenRepository disposableTokenRepository, PaymentOfflineEncryption paymentOfflineEncryption, CardTemplateRepository cardTemplateRepository, PaymentOnlineEncryption paymentOnlineEncryption, WebClientService webClientService) {
        this.cardsRepository = cardsRepository;
        this.disposableTokenRepository = disposableTokenRepository;
        this.paymentOfflineEncryption = paymentOfflineEncryption;
        this.cardTemplateRepository = cardTemplateRepository;
        this.paymentOnlineEncryption = paymentOnlineEncryption;
        this.webClientService = webClientService;
    }


    public List<String[]> getAllUsersPermanentTokenAndTemplateId(int userId){

        List<paymentCard> userCards = cardsRepository.findByUserId(userId);

        if(userCards.isEmpty()) return null;

        List<String[]> userPTs = new ArrayList<>();
        for(paymentCard paymentCard : userCards){

            if(paymentCard.getPermanentToken()== null) continue;
            String[] tokenAndCUN = {String.valueOf(paymentCard.getCardTemplateId()), paymentCard.getPermanentToken()};
            userPTs.add(tokenAndCUN);
        }

        return userPTs;

    }

    // cardInfo(영구토큰과 카드 탬플릿 아이디) -> 0 : 카드 탬플릿 아이디 1: 영구토큰
    // disposableTokens -> 0: 고유번호 1:일회용 토큰

//    String token;
//    String cardName;
//    String cardImgUrl;
//    Json cardConstellationInfo;
    public List<PaymentTokenResponseDTO> createDisposableToken(List<String[]> cardInfo, int userId) throws Exception {

        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)

        if(cardInfo.isEmpty()) return null;

        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();


        // 추천카드의 고유번호는 000으로, 영구토큰 대신 rebirth로
        String realRecommendToken = paymentOfflineEncryption.generateOneTimeToken("rebirth", userId);
        String shortRecommendToken = realRecommendToken.substring(0,20);

        //추천 카드에 대해서도 따로 db에 저장해서 가져오기
        disposableTokensResponse.add((PaymentTokenResponseDTO.builder().
                token(shortRecommendToken).cardName("추천카드").
                cardConstellationInfo("추천카드").
                cardImgUrl("추천카드").build()));


        for(String[] pt : cardInfo) {

            // 영구 토큰 별로 일회용 토큰 생성
            String realToken = paymentOfflineEncryption.generateOneTimeToken(pt[1],userId);

            // 일회용 토큰을 20자로 줄이기
            String shortToken = realToken.substring(0,20);

            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(pt[0]));

            // 카드 이름, 카드 사진, 별자리, 일회용 토큰 넘기기
            disposableTokensResponse.add(PaymentTokenResponseDTO.builder().
                    token(shortToken).cardName(cardTemplate.getCardName()).
                    cardConstellationInfo(cardTemplate.getCardConstellationInfo()).
                    cardImgUrl(cardTemplate.getCardImgUrl()).build());

            //redis에 key: 일회용 토큰 / value : 진짜 토큰으로 저장
            disposableTokenRepository.saveToken(shortToken,realToken);
        }



        // 얘도 redis에 저장하기
        disposableTokenRepository.saveToken(shortRecommendToken,realRecommendToken);

        return disposableTokensResponse;
    }


    public List<PaymentTokenResponseDTO> createOnlineDisposableToken(List<String[]> cardInfo, String merchantName, int amount) throws Exception {

        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)
        if(cardInfo.isEmpty()) return null;

        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();

        // 추천카드의 고유번호는 000으로, 영구토큰 대신 rebirth로
        String realRecommendToken = paymentOnlineEncryption.generateOnlineToken(merchantName,amount,"rebirth");

        //추천 카드에 대해서도 따로 db에 저장해서 가져오기
        disposableTokensResponse.add((PaymentTokenResponseDTO.builder().
                token(realRecommendToken).cardName("추천카드").
                cardConstellationInfo("추천카드").
                cardImgUrl("추천카드").build()));
        for(String[] pt : cardInfo) {

            // 영구 토큰 별로 일회용 토큰 생성
            String realToken = paymentOnlineEncryption.generateOnlineToken(merchantName,amount,pt[1]);

            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(pt[0]));

            // 카드 고유 번호, 일회용 토큰, 카드 정보 넘기기
            disposableTokensResponse.add(PaymentTokenResponseDTO.builder().
                    token(realToken).cardName(cardTemplate.getCardName()).
                    cardConstellationInfo(cardTemplate.getCardConstellationInfo()).
                    cardImgUrl(cardTemplate.getCardImgUrl()).build());

        }



        return disposableTokensResponse;
    }

    // 잘린 토큰으로 전체 토큰 가져오기
    public String getRealDisposableToken(String shortDisposableTokens){
        if(shortDisposableTokens.isEmpty()) return null;

        return disposableTokenRepository.findById(shortDisposableTokens);
    }


    public CardTransactionDTO transactionToCardsa(CreateTransactionRequestDTO cardTransactionDTO){

        CardTransactionDTO cardTransaction = webClientService.checkPermanentToken(cardTransactionDTO).block();

        return cardTransaction;

    }


}
