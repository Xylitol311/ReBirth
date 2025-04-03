package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.*;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

//
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final CardsRepository cardsRepository;
    private final DisposableTokenRepository disposableTokenRepository;
    private final PaymentOfflineEncryption paymentOfflineEncryption;
    private final CardTemplateRepository cardTemplateRepository;
    private final PaymentOnlineEncryption paymentOnlineEncryption;
    private final WebClientService webClientService;
    private final MerchantJoinRepository merchantJoinRepository;
    private final CardJoinRepository cardJoinRepository;
    private final BenefitRepository benefitRepository;
    private final UserCardBenefitRepository userCardBenefitRepository;

    public List<String[]> getAllUsersPermanentTokenAndTemplateId(int userId) {

        List<PaymentCard> userCards = cardsRepository.findByUserId(userId);

        if (userCards.isEmpty()) return null;

        List<String[]> userPTs = new ArrayList<>();
        for (PaymentCard paymentCard : userCards) {

            if (paymentCard.getPermanentToken() == null) continue;
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

        if (cardInfo.isEmpty()) return null;

        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();


        // 추천카드의 고유번호는 000으로, 영구토큰 대신 rebirth로
        String realRecommendToken = paymentOfflineEncryption.generateOneTimeToken("rebirth", userId);
        String shortRecommendToken = realRecommendToken.substring(0, 20);

        //추천 카드에 대해서도 따로 db에 저장해서 가져오기
        disposableTokensResponse.add((PaymentTokenResponseDTO.builder().
                token(shortRecommendToken).cardName("추천카드").
                cardConstellationInfo("추천카드").
                cardImgUrl("추천카드").build()));


        for (String[] pt : cardInfo) {

            // 영구 토큰 별로 일회용 토큰 생성
            String realToken = paymentOfflineEncryption.generateOneTimeToken(pt[1], userId);

            // 일회용 토큰을 20자로 줄이기
            String shortToken = realToken.substring(0, 20);

            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(pt[0]));

            // 카드 이름, 카드 사진, 별자리, 일회용 토큰 넘기기
            disposableTokensResponse.add(PaymentTokenResponseDTO.builder().
                    token(shortToken).cardName(cardTemplate.getCardName()).
                    cardConstellationInfo(cardTemplate.getCardConstellationInfo()).
                    cardImgUrl(cardTemplate.getCardImgUrl()).build());

            //redis에 key: 일회용 토큰 / value : 진짜 토큰으로 저장
            disposableTokenRepository.saveToken(shortToken, realToken);
        }


        // 얘도 redis에 저장하기
        disposableTokenRepository.saveToken(shortRecommendToken, realRecommendToken);

        return disposableTokensResponse;
    }


    public List<PaymentTokenResponseDTO> createOnlineDisposableToken(List<String[]> cardInfo, String merchantName, int amount) throws Exception {

        // 일회용 토큰 : 복호화 가능한 key, 영구토큰, 만료시간, 서명(HMAC)
        if (cardInfo.isEmpty()) return null;

        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();

        // 추천카드의 고유번호는 000으로, 영구토큰 대신 rebirth로
        String realRecommendToken = paymentOnlineEncryption.generateOnlineToken(merchantName, amount, "rebirth");

        //추천 카드에 대해서도 따로 db에 저장해서 가져오기
        disposableTokensResponse.add((PaymentTokenResponseDTO.builder().
                token(realRecommendToken).cardName("추천카드").
                cardConstellationInfo("추천카드").
                cardImgUrl("추천카드").build()));
        for (String[] pt : cardInfo) {

            // 영구 토큰 별로 일회용 토큰 생성
            String realToken = paymentOnlineEncryption.generateOnlineToken(merchantName, amount, pt[1]);

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
    public String getRealDisposableToken(String shortDisposableTokens) {
        if (shortDisposableTokens.isEmpty()) return null;

        return disposableTokenRepository.findById(shortDisposableTokens);
    }


    public CardTransactionDTO transactionToCardsa(CreateTransactionRequestDTO cardTransactionDTO) {

        CardTransactionDTO cardTransaction = webClientService.checkPermanentToken(cardTransactionDTO).block();

        return cardTransaction;

    }

    /**
     * 결제 카드 추천 로직
     * 1. 가맹점 이름으로 가맹점 id, 카테고리 대분류, 소분류 id 가져오기
     * - Transaction 패키지에서 Merchant 엔티티, Category 엔티티 통해서 가져오기
     * - 현재 결제 정보 객체 만들어서 여기에 저장
     * 2. 가맹점 카테고리 기준 해당하는 혜택 가져오기
     * - Shared 패키지에서 Card 엔티티 통해서 보유한 카드 ID와 카드 템플릿 ID, 영구 토큰 가져오기
     * - 보유 카드 목록 객체 리스트 만들어서 저장.
     * - 카드 템플릿 ID 기반으로 Card 패키지에서 BenefitTemplate 조회해서 특정 가맹점, 특정 카테고리가 일치하거나 전체 가맹점인 혜택 전부 가져오기
     * - 혜택 객체 리스트 만들어서 저장
     * 3. 가져온 혜택 리스트 하나씩 순회하면서 실적 계산
     * 3-0. userId, 혜택id 이용해서 카드 헤택별 받은 현황 가져오기 -> payment 패키지 내 userCardBenefit 엔티티 이용해서 가져오기
     * 3-1. 혜택 계산 구분 방식이 1,3인 경우, userCardBenefit에서 가져온 실적 구간 정보 확인해서 해당하는 혜택 계산. => 최종 적용되는 혜택 금액 계산
     * 3-2. 제한 한도 확인 -> 최종 혜택량 구하기.
     * 3-2-1. 이미 한도를 다 채운 경우 => 0
     * 3-2-2. 한도가 얼마 안 남아서 이번에 혜택에서 일부를 빼야 하는 경우 => 뺀 금액
     * 3-2-3. 한도가 여유가 있어서 한번에 다 받을 수 있는 경우 => 3-1 금액
     * 3-3. 구해진 혜택량과 혜택id를 포함한 객체를 우선순위 큐에 저장.(정렬은 혜택량 기준)
     * 4. 가장 혜택이 큰 혜택 id를 통해 보유 카드의 영구 토큰을 반환
     */
    public CalculatedBenefitDto recommendPaymentCard(Integer userId, int amount, String merchantName) {
        // 1. 가맹점 이름으로 가맹점 id, 카테고리 대분류, 소분류 id 가져오기
        MerchantJoinDto merchantJoinData = merchantJoinRepository.findMerchantJoinDataByMerchantName(merchantName);

        // 2. userId를 기반으로 갖고 있는 카드 목록과 카드별 카드 템플릿 ID, 실적 구간, 영구토큰 가져오기
        List<MyCardDto> myCardDtos = cardJoinRepository.findMyCardsIdAndTemplateIdsByUserId(userId);

        Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(
                Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed()
        );

        for (MyCardDto myCardDto : myCardDtos) {
            // 유저가 가진 카드의 혜택 정보 가져오기
            List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                    myCardDto.getCardTemplateId(),
                    merchantJoinData.getCategoryId(),
                    merchantJoinData.getSubCategoryId(),
                    merchantJoinData.getMerchantId()
            );
            for (BenefitInfo benefitInfo : benefitInfos) {
                // 쿠폰 혜택의 경우 서비스에서 계산하지 않으므로 통과
                if (benefitInfo.getBenefitType().toString().equals("쿠폰")) continue;

                // 유저가 받은 해당 혜택 현황 가져오기
                UserCardBenefit userCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitId(userId, benefitInfo.getBenefitId());

                // 해당 혜택의 적용 금액 계산
                int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);

                // 계산된 결과를 객체로 생성(카드 영구토큰, 혜택 금액, 혜택 id)
                CalculatedBenefitDto calculatedBenefit = CalculatedBenefitDto.builder()
                        .myCardId(myCardDto.getCardId())
                        .permanentToken(myCardDto.getPermanentToken())
                        .benefitId(benefitInfo.getBenefitId())
                        .benefitAmount(discountAmount)
                        .benefitType(benefitInfo.getBenefitType().toString())
                        .build();

                // 우선순위 큐에 추가
                benefitQueue.add(calculatedBenefit);
            }
        }

        // 우선순위 큐에서 혜택 금액이 가장 큰 항목의 permanentToken 반환
        if (!benefitQueue.isEmpty()) {
            return benefitQueue.poll();
        }

        // 예외 처리 필요
        log.error("혜택 계산 실패. 로직이 끝났으나 결과값 없음");
        return null;
    }

    // 혜택 금액 계산 메서드
    private int calculateBenefitAmount(BenefitInfo benefitInfo, int amount, UserCardBenefit userCardBenefit) {
        // 최소 실적조차 못 채운 경우
        if (userCardBenefit.getSpendingTier() == 0)
            return 0;

        Double benefit = 0.0;
        int result;
        int spendingTier = userCardBenefit.getSpendingTier(); // 현재 혜택의 실적 구간

        // <<혜택 계산 (실적 단일/구간의 경우 userCardBenefit에 있는 실적 구간으로 혜택 파악 가능)>>
        // 조건이 없거나 실적 조건인 경우
        if (benefitInfo.getBenefitConditionType() == 4 ||
                benefitInfo.getBenefitConditionType() == 1) {
            // benefitInfo.getBenefitConditionType() <= 실적 구간을 이용해 혜택 계산
            benefit = benefitInfo.getBenefitsBySection().get(spendingTier - 1);
        }

        // 건당 결제 금액 조건
        if (benefitInfo.getBenefitConditionType() == 2) {
            benefit = calculateBenefit(benefitInfo, amount);
        }

        // 복합 (전월 실적 먼저 확인하고 결제 구간 계산)
        if (benefitInfo.getBenefitConditionType() == 3 && spendingTier >= 1) {
            benefit = calculateBenefit(benefitInfo, amount);
        }

        // 받을 수 있는 헤택이 없는 경우
        if (benefit == 0) return 0;

        // <<제한 한도 확인 -> 최종 혜택량 구하기>>
        // 횟수 체크
        if (userCardBenefit.getBenefitCount() >=
                benefitInfo.getBenefitUsageLimit().get(spendingTier - 1))
            return 0;

        // 금액 체크
        // 이미 받을 수 있는 혜택을 다 받은 경우
        int totalAbleBenefitAmout = benefitInfo.getBenefitUsageAmount().get(spendingTier - 1);// 받을 수 있는 총 혜택
        if (userCardBenefit.getBenefitAmount() >= totalAbleBenefitAmout)
            return 0;
        // 남은 받을 수 있는 혜택과 이번에 받게 될 혜택 중 더 작은 값을 받을 수 있음.
        result = Math.min((int)(amount * benefit), totalAbleBenefitAmout - userCardBenefit.getBenefitAmount());

        return result;
    }

    // 결제 구간 기준 혜택 금액 계산
    private double calculateBenefit(BenefitInfo benefitInfo, int amount) {
        double benefit = 0.0;
        int rangeIdx = 0; // 결제 금액 구간을 저장할 Index 변수
        for (int idx = 0; idx < benefitInfo.getPaymentRange().size(); idx++) {
            // 결제 금액이 구간 시작 기준 금액 이상이면 해당 구간 인덱스 저장.
            if (benefitInfo.getPaymentRange().get(idx) < amount) {
                rangeIdx = ++idx;
                break;
            }
        }

        // 결제 기준 금액을 달성한 경우
        if (rangeIdx != 0)
            benefit = benefitInfo.getBenefitsBySection().get(rangeIdx - 1);

        return benefit;
    }
}
