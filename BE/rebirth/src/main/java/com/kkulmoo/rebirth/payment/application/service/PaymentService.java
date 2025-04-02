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

        List<PaymentCard> userCards = cardsRepository.findByUserId(userId);

        if(userCards.isEmpty()) return null;

        List<String[]> userPTs = new ArrayList<>();
        for(PaymentCard paymentCard : userCards){

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

/**
 * 결제 카드 추천 로직
1. 가맹점 이름으로 가맹점 id, 카테고리 대분류, 소분류 id 가져오기
  - Transaction 패키지에서 Merchant 엔티티, Category 엔티티 통해서 가져오기
  - 현재 결제 정보 객체 만들어서 여기에 저장
2. 가맹점 카테고리 기준 해당하는 혜택 가져오기
  - Shared 패키지에서 Card 엔티티 통해서 보유한 카드 ID와 카드 템플릿 ID, 영구 토큰 가져오기
    - 보유 카드 목록 객체 리스트 만들어서 저장.
  - 카드 템플릿 ID 기반으로 Card 패키지에서 BenefitTemplate 조회해서 특정 가맹점, 특정 카테고리가 일치하거나 전체 가맹점인 혜택 전부 가져오기
    - 혜택 객체 리스트 만들어서 저장
3. 가져온 혜택 리스트 하나씩 순회하면서 실적 계산
  3-0. userId, 혜택id 이용해서 카드 헤택별 받은 현황 가져오기 -> payment 패키지 내 userCardBenefit 엔티티 이용해서 가져오기
  3-1. 혜택 계산 구분 방식이 1,3인 경우, userCardBenefit에서 가져온 실적 구간 정보 확인해서 해당하는 혜택 계산. => 최종 적용되는 혜택 금액 계산
  3-2. 제한 한도 확인 -> 최종 혜택량 구하기.
    3-2-1. 이미 한도를 다 채운 경우 => 0
    3-2-2. 한도가 얼마 안 남아서 이번에 혜택에서 일부를 빼야 하는 경우 => 뺀 금액
    3-2-3. 한도가 여유가 있어서 한번에 다 받을 수 있는 경우 => 3-1 금액
  3-3. 구해진 혜택량과 혜택id를 포함한 객체를 우선순위 큐에 저장.(정렬은 혜택량 기준)
4. 가장 혜택이 큰 혜택 id를 통해 보유 카드의 영구 토큰을 반환
*/
    public String recommendPaymentCard(Integer userId, int amount, String merchantName){
        String permanentToken = "";

        return permanentToken;
    }
}
