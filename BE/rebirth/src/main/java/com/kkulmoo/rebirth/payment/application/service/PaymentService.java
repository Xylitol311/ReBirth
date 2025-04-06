package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.payment.domain.PreBenefit;
import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.*;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestToCardsaDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PaymentTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private final CardRepository cardRepository;
    private final BenefitRepository benefitRepository;
    private final UserCardBenefitRepository userCardBenefitRepository;
    private final PreBenefitRepository preBenefitRepository;

    // 사용자 ID 기반으로 보유 카드의 영구토큰과 템플릿 ID 목록 조회
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


    // 오프라인 일회용 토큰 생성 및 DB 저장 (추천 카드 포함)
    public List<PaymentTokenResponseDTO> createDisposableToken(List<String[]> cardInfo, int userId) throws Exception {
        // cardInfo가 없는 경우 예외 처리
        if (cardInfo.isEmpty()) return null;

        // 추천 카드용 토큰 생성 및 Redis 저장
        String realRecommendToken = paymentOfflineEncryption.generateOneTimeToken("rebirth", userId);
        String shortRecommendToken = realRecommendToken.substring(0, 20);
        disposableTokenRepository.saveToken(shortRecommendToken, realRecommendToken);

        // 토큰 DTO 목록 생성 및 추천 토큰 저장
        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();
        disposableTokensResponse.add(PaymentTokenResponseDTO.builder()
                .token(shortRecommendToken)
                .cardName("추천카드")
                .cardConstellationInfo("추천카드")
                .cardImgUrl("추천카드")
                .build());

        // 실제 카드 토큰 생성
        for (String[] pt : cardInfo) {
            String realToken = paymentOfflineEncryption.generateOneTimeToken(pt[1], userId);
            String shortToken = realToken.substring(0, 20);
            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(pt[0]));
            disposableTokensResponse.add(PaymentTokenResponseDTO.builder()
                    .token(shortToken)
                    .cardName(cardTemplate.getCardName())
                    .cardConstellationInfo(cardTemplate.getCardConstellationInfo())
                    .cardImgUrl(cardTemplate.getCardImgUrl())
                    .build());
            disposableTokenRepository.saveToken(shortToken, realToken);
        }
        return disposableTokensResponse;
    }

    // 온라인 일회용 토큰 생성 및 DB 저장 (추천 카드 포함)
    public List<PaymentTokenResponseDTO> createOnlineDisposableToken(List<String[]> cardInfo, String merchantName, int amount, int userId) throws Exception {
        // 없는 경우 예외 처리
        if (cardInfo.isEmpty()) return null;

        // 추천 토큰 생성
        String realRecommendToken = paymentOnlineEncryption.generateOnlineToken(merchantName, amount, "rebirth", userId);

        List<PaymentTokenResponseDTO> disposableTokensResponse = new ArrayList<>();
        disposableTokensResponse.add(PaymentTokenResponseDTO.builder()
                .token(realRecommendToken)
                .cardName("추천카드")
                .cardConstellationInfo("추천카드")
                .cardImgUrl("추천카드")
                .build());

        for (String[] pt : cardInfo) {
            String realToken = paymentOnlineEncryption.generateOnlineToken(merchantName, amount, pt[1], userId);
            CardTemplate cardTemplate = cardTemplateRepository.getCardTemplate(Integer.parseInt(pt[0]));
            disposableTokensResponse.add(PaymentTokenResponseDTO.builder()
                    .token(realToken)
                    .cardName(cardTemplate.getCardName())
                    .cardConstellationInfo(cardTemplate.getCardConstellationInfo())
                    .cardImgUrl(cardTemplate.getCardImgUrl())
                    .build());
        }
        return disposableTokensResponse;
    }

    // 짧은 일회용 토큰을 받아 원래의 토큰 반환 (복호화 가능한 토큰)
    public String getRealDisposableToken(String shortDisposableTokens) {
        if (shortDisposableTokens.isEmpty()) return null;
        return disposableTokenRepository.findById(shortDisposableTokens);
    }

    // 공통 결제 처리 메서드
    // - 추천 카드 로직 호출, 추천 카드 적용 여부 판단, 카드사 결제 요청 처리
    public CardTransactionDTO processPayment(int userId, String requestToken, String merchantName, int amount) {
        // 가맹점 정보 조회
        MerchantJoinDto merchantJoinDto = merchantJoinRepository.findMerchantJoinDataByMerchantName(merchantName);

        // 추천 카드 정보 조회 (매 결제마다 호출하여 추천 기록 저장)
        CalculatedBenefitDto recommendedBenefit = recommendPaymentCard(userId, amount, merchantJoinDto);
        BenefitType benefitType = recommendedBenefit.getBenefitType();
        Integer benefitAmount = recommendedBenefit.getBenefitAmount();
        String permanentToken = requestToken;

        CalculatedBenefitDto realBenefit = new CalculatedBenefitDto(); // 실제 혜택을 저장할 객체

        // 만약 영구토큰이 "rebirth"이면 추천 카드의 영구토큰으로 대체
        if (permanentToken.equals("rebirth") && recommendedBenefit != null && recommendedBenefit.getPermanentToken() != null) {
            permanentToken = recommendedBenefit.getPermanentToken();
        }

        // 만약 추천 카드가 아닌 경우 실제 카드 혜택 계산
        else {
            MyCardDto myCardDto = cardRepository.findMyCardIdAndTemplateIdByPermanentToken(permanentToken);
            if (myCardDto.getPermanentToken() == null)
                // TODO: 카드 데이터 없을 때 에러 처리 필요
                log.error("카드 데이터 조회 실패");
            Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(
                    Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed()
            );
            // 해당 카드의 혜택 정보 조회
            List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                    myCardDto.getCardTemplateId(),
                    merchantJoinDto.getCategoryId(),
                    merchantJoinDto.getSubCategoryId(),
                    merchantJoinDto.getMerchantId()
            );
            // 각 혜택에 대해 할인 금액 계산
            for (BenefitInfo benefitInfo : benefitInfos) {
                if (benefitInfo.getBenefitType().toString().equals("쿠폰")) continue;
                UserCardBenefit userCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitId(userId, benefitInfo.getBenefitId());
                int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);
                CalculatedBenefitDto calculatedBenefit = CalculatedBenefitDto.builder()
                        .myCardId(myCardDto.getCardId())
                        .permanentToken(myCardDto.getPermanentToken())
                        .benefitId(benefitInfo.getBenefitId())
                        .benefitAmount(discountAmount)
                        .benefitType(benefitInfo.getBenefitType())
                        .build();
                benefitQueue.add(calculatedBenefit);
            }
            if (!benefitQueue.isEmpty()) {
                // TODO: 혜택 데이터 없을 때 에러 처리 필요
            }
            realBenefit = benefitQueue.poll();

            // 실제 혜택 기준으로 혜택 유형과 혜택량 변경
            benefitType = realBenefit.getBenefitType();
            benefitAmount = realBenefit.getBenefitAmount();
            permanentToken = realBenefit.getPermanentToken();
        }

        // 카드사 결제 요청 데이터 구성
        CreateTransactionRequestToCardsaDTO dataToCardsa = CreateTransactionRequestToCardsaDTO.builder()
                .permanentToken(permanentToken)
                .amount(amount)
                .merchantName(merchantName)
                .benefitType(benefitType.name())
                .benefitAmount(benefitAmount)
                .createdAt(LocalDateTime.now())
                .build();

        // 카드사에 결제 요청 후 결과 수신
        CardTransactionDTO cardTransactionDTO = transactionToCardsa(dataToCardsa);

        // 추천 카드로 결제하지 않은 경우, 직전 거래 피드백 업데이트
        if (!requestToken.equals("rebirth")) {
            // 실제 혜택과 유형

            // 추천 혜택과 유형
            // 가맹점, 금액, 유저 id
            PreBenefit preBenefit = PreBenefit.builder()
                    .userId(userId)
                    .paymentCardId(realBenefit.getMyCardId())
                    .recommendedCardId(recommendedBenefit.getMyCardId())
                    .amount(amount)
                    .ifBenefitType(recommendedBenefit.getBenefitType())
                    .ifBenefitAmount(recommendedBenefit.getBenefitAmount())
                    .realBenefitType(realBenefit.getBenefitType())
                    .realBenefitAmount(realBenefit.getBenefitAmount())
                    .merchantName(merchantName)
                    .build();

            savePreBenefit(preBenefit);
        }

        // 마이데이터 카드 거래내역 가져오기(단건)

        // 사용자 혜택 현황 업데이트

        // 리포트 업데이트 호출


        return cardTransactionDTO;
    }

    // 카드사 결제 요청 (WebClientService 이용)
    public CardTransactionDTO transactionToCardsa(CreateTransactionRequestToCardsaDTO cardTransactionDTO) {
        CardTransactionDTO cardTransaction = webClientService.checkPermanentToken(cardTransactionDTO).block();
        return cardTransaction;
    }

    // 결제 카드 추천 로직
    // - 사용자 보유 카드별로 해당 가맹점에서 적용 가능한 혜택을 계산 후 가장 혜택이 큰 카드 선택
    public CalculatedBenefitDto recommendPaymentCard(Integer userId, int amount, MerchantJoinDto merchantJoinDto) {

        // 사용자 보유 카드 목록 조회
        List<MyCardDto> myCardDtos = cardRepository.findMyCardsIdAndTemplateIdsByUserId(userId);
        Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(
                Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed()
        );
        for (MyCardDto myCardDto : myCardDtos) {
            if (myCardDto.getPermanentToken() == null) continue;
            // 해당 카드의 혜택 정보 조회
            List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                    myCardDto.getCardTemplateId(),
                    merchantJoinDto.getCategoryId(),
                    merchantJoinDto.getSubCategoryId(),
                    merchantJoinDto.getMerchantId()
            );
            // 각 혜택에 대해 할인 금액 계산
            for (BenefitInfo benefitInfo : benefitInfos) {
                if (benefitInfo.getBenefitType().toString().equals("쿠폰")) continue;
                UserCardBenefit userCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitId(userId, benefitInfo.getBenefitId());
                int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);
                CalculatedBenefitDto calculatedBenefit = CalculatedBenefitDto.builder()
                        .myCardId(myCardDto.getCardId())
                        .permanentToken(myCardDto.getPermanentToken())
                        .benefitId(benefitInfo.getBenefitId())
                        .benefitAmount(discountAmount)
                        .benefitType(benefitInfo.getBenefitType())
                        .build();
                benefitQueue.add(calculatedBenefit);
            }
        }
        if (!benefitQueue.isEmpty()) {
            return benefitQueue.poll();
        }
        log.error("혜택 계산 실패. 로직이 끝났으나 결과값 없음");
        return null;
    }


    // 혜택 금액 계산 메서드 (benefitTemplate 필드에 대한 null 체크 포함)
    private int calculateBenefitAmount(BenefitInfo benefitInfo, int amount, UserCardBenefit userCardBenefit) {
        // 최소 실적을 못 채운 경우
        if (userCardBenefit.getSpendingTier() == 0)
            return 0;

        double benefit = 0.0;
        int result;
        int spendingTier = userCardBenefit.getSpendingTier();

        // 실적 조건 (단일 또는 구간)
        if (benefitInfo.getBenefitConditionType() == 4 || benefitInfo.getBenefitConditionType() == 1) {
            if (benefitInfo.getBenefitsBySection() != null && benefitInfo.getBenefitsBySection().size() >= spendingTier) {
                benefit = benefitInfo.getBenefitsBySection().get(spendingTier - 1);
            }
        }

        // 건당 결제 금액 조건
        if (benefitInfo.getBenefitConditionType() == 2) {
            benefit = calculateBenefit(benefitInfo, amount);
        }

        // 복합 조건 (전월 실적 후 결제 구간 계산)
        if (benefitInfo.getBenefitConditionType() == 3 && spendingTier >= 1) {
            benefit = calculateBenefit(benefitInfo, amount);
        }
        if (benefit == 0) return 0;

        // 제한 한도 - 횟수 체크 (benefitUsageLimit이 null이면 조건 건너뜀)
        if (benefitInfo.getBenefitUsageLimit() != null && benefitInfo.getBenefitUsageLimit().size() >= spendingTier) {
            if (userCardBenefit.getBenefitCount() >= benefitInfo.getBenefitUsageLimit().get(spendingTier - 1))
                return 0;
        }

        // 제한 한도 - 금액 체크 (benefitUsageAmount가 null이면 제한 없음)
        int totalAbleBenefitAmount = Integer.MAX_VALUE;
        if (benefitInfo.getBenefitUsageAmount() != null && benefitInfo.getBenefitUsageAmount().size() >= spendingTier) {
            totalAbleBenefitAmount = benefitInfo.getBenefitUsageAmount().get(spendingTier - 1);
        }
        if (userCardBenefit.getBenefitAmount() >= totalAbleBenefitAmount)
            return 0;

        // 할인 타입에 따라 최종 할인 금액 산출
        if (benefitInfo.getDiscountType() == DiscountType.AMOUNT) {
            result = Math.min((int) benefit, totalAbleBenefitAmount - userCardBenefit.getBenefitAmount());
        } else {
            result = Math.min((int) (amount * benefit), totalAbleBenefitAmount - userCardBenefit.getBenefitAmount());
        }
        return result;
    }

    // 결제 구간 기준 할인 금액 계산 (benefitTemplate 필드에 대한 null 체크 포함)
    private double calculateBenefit(BenefitInfo benefitInfo, int amount) {
        double benefit = 0.0;
        int rangeIdx = 0;
        if (benefitInfo.getPaymentRange() != null && benefitInfo.getBenefitsBySection() != null) {
            for (int idx = 0; idx < benefitInfo.getPaymentRange().size(); idx++) {
                if (benefitInfo.getPaymentRange().get(idx) < amount) {
                    rangeIdx = idx + 1;
                    break;
                }
            }
            if (rangeIdx != 0 && benefitInfo.getBenefitsBySection().size() >= rangeIdx) {
                benefit = benefitInfo.getBenefitsBySection().get(rangeIdx - 1);
            }
        }
        return benefit;
    }

    // userId를 기준으로 기존 데이터가 있으면 업데이트, 없으면 insert 처리
    @Transactional
    public PreBenefit savePreBenefit(PreBenefit preBenefit) {
        return preBenefitRepository.findByUserId(preBenefit.getUserId())
                .map(existing -> PreBenefit.builder()
                        .userId(existing.getUserId())
                        .paymentCardId(preBenefit.getPaymentCardId())
                        .recommendedCardId(preBenefit.getRecommendedCardId())
                        .amount(preBenefit.getAmount())
                        .ifBenefitType(preBenefit.getIfBenefitType())
                        .ifBenefitAmount(preBenefit.getIfBenefitAmount())
                        .realBenefitType(preBenefit.getRealBenefitType())
                        .realBenefitAmount(preBenefit.getRealBenefitAmount())
                        .merchantName(preBenefit.getMerchantName())
                        .build())
                .map(updated -> preBenefitRepository.save(updated))
                .orElseGet(() -> preBenefitRepository.save(preBenefit));
    }
}