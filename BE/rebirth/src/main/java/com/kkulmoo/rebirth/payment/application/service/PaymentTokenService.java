package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.domain.repository.CardTemplateRepository;
import com.kkulmoo.rebirth.payment.domain.repository.DisposableTokenRepository;
import com.kkulmoo.rebirth.payment.presentation.request.PermanentTokenRequestToCardsaDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PermanentTokenResponseByCardsaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTokenService {

    // Repository를 통해 사용자 카드 정보를 조회
    private final CardsRepository cardsRepository;
    // 카드 템플릿 정보를 조회
    private final CardTemplateRepository cardTemplateRepository;
    // 일회용 토큰 저장소 Repository
    private final DisposableTokenRepository disposableTokenRepository;
    // 오프라인 토큰 암호화 및 검증 서비스
    private final PaymentOfflineEncryption paymentOfflineEncryption;
    // 온라인 토큰 암호화 및 검증 서비스
    private final PaymentOnlineEncryption paymentOnlineEncryption;
    // 카드사로 부터 응답
    private final WebClientService webClientService;

    // 결제 카드 등록
    public void getPermanetTokenFromCardsa(PermanentTokenRequestToCardsaDTO permanentTokenRequestToCardsaDTO){

        // 카드사로 부터 카드 정보 가져오기
        PermanentTokenResponseByCardsaDTO permanentTokenResponseByCardsaDTO = webClientService.createCard(permanentTokenRequestToCardsaDTO).block();

        // 리버스 DB에 결제 카드 정보 등록하기


    }


    // 사용자 보유 카드의 영구토큰과 템플릿 ID 목록 조회
    public List<String[]> getAllUsersPermanentTokenAndTemplateId(int userId) {
        // 사용자 카드 리스트 조회
        List<PaymentCard> userCards = cardsRepository.findByUserId(userId);
        if (userCards.isEmpty()) return null; // 카드가 없으면 null 반환
        List<String[]> userTokens = new ArrayList<>();
        // 각 카드에서 템플릿 ID와 영구토큰 추출
        for (PaymentCard card : userCards) {
            if (card.getPermanentToken() == null) continue; // 토큰이 없는 카드는 건너뜀
            userTokens.add(new String[]{String.valueOf(card.getCardTemplateId()), card.getPermanentToken()});
        }
        return userTokens;
    }

    // 오프라인 일회용 토큰 생성 및 DB 저장
    public List<PaymentTokenResponseDTO> createDisposableToken(List<String[]> cardInfo, int userId) throws Exception {
        if (cardInfo == null || cardInfo.isEmpty()) return null; // 카드 정보 없으면 null 반환
        List<PaymentTokenResponseDTO> tokensResponse = new ArrayList<>();
        // 추천 카드용 토큰 생성 (임시 값 "rebirth" 사용)
        String realRecommendToken = paymentOfflineEncryption.generateOneTimeToken("rebirth", userId);
        String shortRecommendToken = realRecommendToken.substring(0, 20); // 단축 토큰 생성
        // DB에 추천 토큰 저장
        disposableTokenRepository.saveToken(shortRecommendToken, realRecommendToken);
        tokensResponse.add(PaymentTokenResponseDTO.builder()
                .token(shortRecommendToken)
                .cardName("추천카드")
                .cardConstellationInfo("추천카드")
                .cardImgUrl("추천카드")
                .build());
        // 각 실제 카드별 토큰 생성
        for (String[] info : cardInfo) {
            String templateId = info[0];
            String permanentToken = info[1];
            String realToken = paymentOfflineEncryption.generateOneTimeToken(permanentToken, userId);
            String shortToken = realToken.substring(0, 20); // 단축 토큰 생성
            // 카드 템플릿 정보 조회
            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(templateId));
            tokensResponse.add(PaymentTokenResponseDTO.builder()
                    .token(shortToken)
                    .cardName(cardTemplate.getCardName())
                    .cardConstellationInfo(cardTemplate.getCardConstellationInfo())
                    .cardImgUrl(cardTemplate.getCardImgUrl())
                    .build());
            // DB에 생성된 토큰 저장
            disposableTokenRepository.saveToken(shortToken, realToken);
        }
        return tokensResponse;
    }

    // 온라인 일회용 토큰 생성 및 DB 저장
    public List<PaymentTokenResponseDTO> createOnlineDisposableToken(List<String[]> cardInfo, String merchantName, int amount, int userId) throws Exception {
        if (cardInfo == null || cardInfo.isEmpty()) return null; // 카드 정보 없으면 null 반환
        List<PaymentTokenResponseDTO> tokensResponse = new ArrayList<>();
        // 추천 카드용 온라인 토큰 생성 (임시 값 "rebirth" 사용)
        String realRecommendToken = paymentOnlineEncryption.generateOnlineToken(merchantName, amount, "rebirth", userId);
        tokensResponse.add(PaymentTokenResponseDTO.builder()
                .token(realRecommendToken)
                .cardName("추천카드")
                .cardConstellationInfo("추천카드")
                .cardImgUrl("추천카드")
                .build());
        // 각 실제 카드별 온라인 토큰 생성
        for (String[] info : cardInfo) {
            String templateId = info[0];
            String permanentToken = info[1];
            String realToken = paymentOnlineEncryption.generateOnlineToken(merchantName, amount, permanentToken, userId);
            // 카드 템플릿 정보 조회
            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(templateId));
            tokensResponse.add(PaymentTokenResponseDTO.builder()
                    .token(realToken)
                    .cardName(cardTemplate.getCardName())
                    .cardConstellationInfo(cardTemplate.getCardConstellationInfo())
                    .cardImgUrl(cardTemplate.getCardImgUrl())
                    .build());
        }
        return tokensResponse;
    }

    // 짧은 토큰을 받아 원래의 토큰 복원
    public String getRealDisposableToken(String shortToken) {
        if (shortToken == null || shortToken.isEmpty()) return null; // 유효성 검사
        return disposableTokenRepository.findById(shortToken);
    }

    // 온라인 결제용 QR 토큰 생성
    public String generateQRToken(String merchantName, int amount) throws Exception {
        return paymentOnlineEncryption.generateQRToken(merchantName, amount);
    }

    // QR 토큰 검증 후 가맹점 정보 반환
    public String[] validateQRToken(String token) throws Exception {
        return paymentOnlineEncryption.validateQRToken(token);
    }

    // 온라인 결제 진행용 토큰 검증 후 정보 추출
    public String[] validateOnlineToken(String token) throws Exception {
        return paymentOnlineEncryption.validateOnlineToken(token);
    }

    // 오프라인 결제(포스기)용 토큰 검증 후 정보 추출
    public String[] validateOneTimeToken(String token) throws Exception {
        return paymentOfflineEncryption.validateOneTimeToken(token);
    }
}