package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.analysis.application.service.ReportService;
import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.payment.domain.PreBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.MerchantJoinRepository;
import com.kkulmoo.rebirth.payment.domain.repository.PreBenefitRepository;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestToCardsaDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.user.application.service.MyDataService;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionService {

    // 카드사 결제 요청을 위한 WebClient 서비스
    private final WebClientService webClientService;
    // 가맹점 정보를 조회하는 Repository
    private final MerchantJoinRepository merchantJoinRepository;
    // 카드 추천 및 혜택 계산을 담당하는 BenefitService
    private final BenefitService benefitService;
    // 토큰 관련 기능을 사용할 경우를 대비해 PaymentTokenService 주입
    private final PaymentTokenService paymentTokenService;
    // 결제 피드백 정보를 저장하기 위한 Repository
    private final PreBenefitRepository preBenefitRepository;
    // 카드 정보 조회를 위한 Repository
    private final CardRepository cardRepository;
    // 유저 데이터 조회
    private final UserRepository userRepository;
    // 마이데이터 호출
    private final MyDataService myDataService;
    private final ReportService reportService;

    // 결제 프로세스 전체를 처리하는 메서드
    public CardTransactionDTO processPayment(int userId, String requestToken, String merchantName, int amount) {
        // 가맹점 정보 조회
        MerchantJoinDto merchantJoinDto = merchantJoinRepository.findMerchantJoinDataByMerchantName(merchantName);
        // 추천 카드 혜택 정보 계산 (매 결제마다 추천 기록을 위해 호출)
        CalculatedBenefitDto recommendedBenefit = benefitService.recommendPaymentCard(userId, amount, merchantJoinDto);
        // 기본적으로 benefitType과 benefitAmount는 추천 혜택으로 설정
        BenefitType benefitType = (recommendedBenefit != null) ? recommendedBenefit.getBenefitType() : BenefitType.DISCOUNT;
        Integer benefitAmount = (recommendedBenefit != null) ? recommendedBenefit.getBenefitAmount() : 0;
        String permanentToken = (recommendedBenefit != null) ? recommendedBenefit.getPermanentToken() : requestToken;
        Integer benefitId = (recommendedBenefit != null) ? recommendedBenefit.getBenefitId() : null;

        CalculatedBenefitDto realBenefit = null; // 실제 카드 혜택 정보를 담을 객체

        // 추천 카드 결제가 아닌 경우 실제 카드의 혜택 계산 로직 수행
        if (!requestToken.equals("rebirth")) {
            // 실제 카드 혜택 계산을 위해 BenefitService의 calculateRealBenefit 호출
            realBenefit = benefitService.calculateRealBenefit(userId, requestToken, amount, merchantJoinDto);
            if (realBenefit != null) {
                // 실제 카드 혜택 정보를 적용
                benefitType = realBenefit.getBenefitType();
                benefitAmount = realBenefit.getBenefitAmount();
                permanentToken = realBenefit.getPermanentToken();
                // benefitId는 기본적으로 추천 혜택의 benefitId를 사용하되, 실제 혜택이 있으면(원래 로직과 동일) 업데이트할 수 있음
                benefitId = realBenefit.getBenefitId();
            }
        }

        // 카드사 결제 요청 데이터 구성 (혜택 정보가 null이면 기본값으로 전송)
        CreateTransactionRequestToCardsaDTO request = CreateTransactionRequestToCardsaDTO.builder()
                .permanentToken(permanentToken)
                .amount(amount)
                .merchantName(merchantName)
                .benefitId(benefitId) // 혜택 정보가 없으면 null
                .benefitType(benefitType.name()) // 혜택 정보가 없으면 "DISCOUNT"
                .benefitAmount(benefitAmount) // 혜택 정보가 없으면 0
                .createdAt(LocalDateTime.now())
                .build();

        // 카드사에 결제 요청 후 결과 수신
        CardTransactionDTO cardTransactionDTO = transactionToCardsa(request);

        // 추천 카드가 아닌 경우 결제 피드백 정보를 업데이트
        if (!requestToken.equals("rebirth")) {
            PreBenefit preBenefit = PreBenefit.builder()
                    .userId(userId)
                    // 실제 카드 혜택 정보가 없으면 추천 혜택 정보도 없을 수 있으므로, 추가 null 체크 필요함
                    .paymentCardId(realBenefit != null
                            ? realBenefit.getMyCardId()
                            : (recommendedBenefit != null ? recommendedBenefit.getMyCardId() : null))
                    .recommendedCardId(recommendedBenefit != null ? recommendedBenefit.getMyCardId() : null)
                    .amount(amount)
                    .ifBenefitType(recommendedBenefit != null ? recommendedBenefit.getBenefitType() : BenefitType.DISCOUNT)
                    .ifBenefitAmount(recommendedBenefit != null ? recommendedBenefit.getBenefitAmount() : 0)
                    .realBenefitType(realBenefit != null
                            ? realBenefit.getBenefitType()
                            : (recommendedBenefit != null ? recommendedBenefit.getBenefitType() : BenefitType.DISCOUNT))
                    .realBenefitAmount(realBenefit != null
                            ? realBenefit.getBenefitAmount()
                            : (recommendedBenefit != null ? recommendedBenefit.getBenefitAmount() : 0))
                    .merchantName(merchantName)
                    .build();
            savePreBenefit(preBenefit);
        }

        // 마이데이터 카드 내역 가져오기
        User user = userRepository.findByUserId(new UserId(userId));
        MyCard myCard = cardRepository.findById(realBenefit != null ? realBenefit.getMyCardId() : recommendedBenefit.getMyCardId()).get();
        List<MyCard> myCards = Arrays.asList(myCard);
        myDataService.loadMyTransactionByCards(user, myCards);

        // 혜택 현황 관련 테이블에 업데이트 하기
        // TODO: 테이블 수정 로직 추가
        benefitService.updateUserCardBenefit(userId, benefitId, benefitAmount);

        // 리포트 업데이트 하기
        reportService.updateMonthlyTransactionSummary(userId);

        return cardTransactionDTO;
    }

    // 카드사에 결제 요청하는 내부 메서드
    private CardTransactionDTO transactionToCardsa(CreateTransactionRequestToCardsaDTO request) {
        return webClientService.checkPermanentToken(request).block();
    }

    // 결제 피드백 정보를 저장하는 트랜잭션 처리 메서드
    @Transactional
    public PreBenefit savePreBenefit(PreBenefit preBenefit) {
        return preBenefitRepository.findByUserId(preBenefit.getUserId())
                .map(existing -> PreBenefit.builder()
                        .userId(existing.getUserId()) // 기존 사용자 ID 유지
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