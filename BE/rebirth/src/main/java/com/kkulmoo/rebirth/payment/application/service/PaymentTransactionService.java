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
import com.kkulmoo.rebirth.user.application.service.UserCardBenefitService;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserCardBenefitRepository userCardBenefitRepository;
    private final UserCardBenefitService userCardBenefitService;

    public CardTransactionDTO processPayment(int userId, String requestToken, String merchantName, int amount) {
        log.info("processPayment 시작 - userId: {}, merchantName: {}, amount: {}, requestToken: {}",
                userId, merchantName, amount, requestToken);

        MerchantJoinDto merchantJoinDto = merchantJoinRepository.findMerchantJoinDataByMerchantName(merchantName);
        log.info("가맹점 정보 - categoryId: {}, subCategoryId: {}, merchantId: {}",
                merchantJoinDto.getCategoryId(),
                merchantJoinDto.getSubCategoryId(),
                merchantJoinDto.getMerchantId());
        LocalDateTime createdAt = LocalDateTime.now();

        // 추천 카드 혜택 정보 계산
        CalculatedBenefitDto recommendedBenefit = benefitService.recommendPaymentCard(userId, amount, merchantJoinDto, createdAt);
        if (recommendedBenefit != null) {
            log.info("추천 혜택 - myCardId: {}, permanentToken: {}, benefitId: {}, benefitAmount: {}, benefitType: {}",
                    recommendedBenefit.getMyCardId(), recommendedBenefit.getPermanentToken(),
                    recommendedBenefit.getBenefitId(), recommendedBenefit.getBenefitAmount(), recommendedBenefit.getBenefitType());
        } else {
            log.warn("추천 혜택 정보가 없음.");
        }

        // 기본 혜택 정보 적용

        MyCard myCardDto = (recommendedBenefit != null) ?
                cardRepository.findByPermanentToken(recommendedBenefit.getPermanentToken())
                        .orElseThrow(() -> new EntityNotFoundException("해당 카드를 찾을 수 없습니다.")) : null;
        BenefitType benefitType = (recommendedBenefit != null) ? recommendedBenefit.getBenefitType() : BenefitType.DISCOUNT;
        Integer benefitAmount = (recommendedBenefit != null) ? recommendedBenefit.getBenefitAmount() : 0;
        String permanentToken = (recommendedBenefit != null) ? recommendedBenefit.getPermanentToken() : requestToken;
        Integer benefitId = (recommendedBenefit != null) ? recommendedBenefit.getBenefitId() : null;

        // 추천 카드 결제가 아닌 경우 실제 카드 혜택 계산
        CalculatedBenefitDto realBenefit = null;
        if (!"rebirth".equals(requestToken)) {
            myCardDto = cardRepository.findByPermanentToken(requestToken)
                    .orElseThrow(() -> new EntityNotFoundException("해당 카드를 찾을 수 없습니다."));
            realBenefit = benefitService.calculateRealBenefit(userId, amount, merchantJoinDto, myCardDto, createdAt);
            if (realBenefit != null) {
                log.info("실제 혜택 - myCardId: {}, permanentToken: {}, benefitId: {}, benefitAmount: {}, benefitType: {}",
                        realBenefit.getMyCardId(), realBenefit.getPermanentToken(),
                        realBenefit.getBenefitId(), realBenefit.getBenefitAmount(), realBenefit.getBenefitType());
                benefitType = realBenefit.getBenefitType();
                benefitAmount = realBenefit.getBenefitAmount();
                permanentToken = realBenefit.getPermanentToken();
                benefitId = realBenefit.getBenefitId();
            } else {
                log.warn("실제 혜택 정보가 없음. 추천 혜택 사용.");
            }
        }

        // 최종 요청 페이로드 구성
        log.info("카드사 요청 페이로드 - permanentToken: {}", permanentToken);
        log.info("카드사 요청 페이로드 - amount: {}", amount);
        log.info("카드사 요청 페이로드 - merchantName: {}", merchantName);
        log.info("카드사 요청 페이로드 - benefitId: {}", benefitId);
        log.info("카드사 요청 페이로드 - benefitType: {}", benefitType.name());
        log.info("카드사 요청 페이로드 - benefitAmount: {}", benefitAmount);
        log.info("카드사 요청 페이로드 - createdAt: {}", createdAt);
        CreateTransactionRequestToCardsaDTO transactionRequest = CreateTransactionRequestToCardsaDTO.builder()
                .token(permanentToken)
                .amount(amount)
                .merchantName(merchantName)
                .benefitId(benefitId)
                .benefitType(benefitType.name())
                .benefitAmount(benefitAmount)
                .createdAt(createdAt)
                .build();

        // 카드사에 결제 요청 후 결과 수신
        CardTransactionDTO cardTransactionDTO = transactionToCardsa(transactionRequest);
        if (cardTransactionDTO != null) {
            log.info("카드사 응답 - transactionId: {}, status: {}",
                    cardTransactionDTO.getApprovalCode(), cardTransactionDTO.getCreatedAt());
        } else {
            log.warn("카드사 응답이 null입니다.");
        }

        // 혜택 현황 업데이트
        if (benefitId != null) {
            UserCardBenefit userCardBenefit = userCardBenefitService.getUserCardBenefit(userId, benefitId, myCardDto.getCardId(), createdAt);

            userCardBenefitRepository.save(
                    userCardBenefit.toBuilder()
                            .benefitCount((short) (userCardBenefit.getBenefitCount() + 1))
                            .benefitAmount(userCardBenefit.getBenefitAmount() + benefitAmount)
                            .updateDate(createdAt)
                            .build()
            );
        }

        // 결제 피드백 정보 업데이트
        PreBenefit preBenefit = PreBenefit.builder()
                .userId(userId)
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

        // 마이데이터 호출 및 혜택 현황 업데이트
        User user = userRepository.findByUserId(new UserId(userId));
        log.info("유저 정보 - userId: {}, userName: {}", user.getUserId(), user.getUserName());
        List<MyCard> myCards = Arrays.asList(myCardDto);
        myDataService.loadMyTransactionByCardsForPayment(user, myCards);
        reportService.updateMonthlyTransactionSummary(userId, LocalDateTime.now());

        return cardTransactionDTO;
    }


    // 카드사에 결제 요청하는 내부 메서드
    private CardTransactionDTO transactionToCardsa(CreateTransactionRequestToCardsaDTO request) {
        return webClientService.checkPermanentToken(request).block();
    }

    // 결제 피드백 정보를 저장하는 트랜잭션 처리 메서드
    @Transactional
    public PreBenefit savePreBenefit(PreBenefit preBenefit) {
        log.info("결제 피드백 저장 - userId: {}, paymentCardId: {}, recommendedCardId: {}, amount: {}, ifBenefitType: {}, ifBenefitAmount: {}, realBenefitType: {}, realBenefitAmount: {}, merchantName: {}",
                preBenefit.getUserId(), preBenefit.getPaymentCardId(), preBenefit.getRecommendedCardId(), preBenefit.getAmount(),
                preBenefit.getIfBenefitType(), preBenefit.getIfBenefitAmount(), preBenefit.getRealBenefitType(), preBenefit.getRealBenefitAmount(),
                preBenefit.getMerchantName());

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

    public CardTransactionDTO insertPayData(int userId, String requestToken, String merchantName, int amount, LocalDateTime createdAt) {
        log.info("InsertPayData 시작 - userId: {}, merchantName: {}, amount: {}, requestToken: {}",
                userId, merchantName, amount, requestToken);

        MerchantJoinDto merchantJoinDto = merchantJoinRepository.findMerchantJoinDataByMerchantName(merchantName);
        log.info("가맹점 정보 - categoryId: {}, subCategoryId: {}, merchantId: {}",
                merchantJoinDto.getCategoryId(),
                merchantJoinDto.getSubCategoryId(),
                merchantJoinDto.getMerchantId());

        // 추천 카드 혜택 정보 계산
        CalculatedBenefitDto recommendedBenefit = benefitService.recommendPaymentCard(userId, amount, merchantJoinDto, createdAt);
        if (recommendedBenefit != null) {
            log.info("추천 혜택 - myCardId: {}, permanentToken: {}, benefitId: {}, benefitAmount: {}, benefitType: {}",
                    recommendedBenefit.getMyCardId(), recommendedBenefit.getPermanentToken(),
                    recommendedBenefit.getBenefitId(), recommendedBenefit.getBenefitAmount(), recommendedBenefit.getBenefitType());
        } else {
            log.warn("추천 혜택 정보가 없음.");
        }

        // 기본 혜택 정보 적용
        MyCard myCardDto = (recommendedBenefit != null) ?
                cardRepository.findByPermanentToken(recommendedBenefit.getPermanentToken())
                        .orElseThrow(() -> new EntityNotFoundException("해당 카드를 찾을 수 없습니다.")) : null;
        BenefitType benefitType = (recommendedBenefit != null) ? recommendedBenefit.getBenefitType() : BenefitType.DISCOUNT;
        Integer benefitAmount = (recommendedBenefit != null) ? recommendedBenefit.getBenefitAmount() : 0;
        String permanentToken = (recommendedBenefit != null) ? recommendedBenefit.getPermanentToken() : requestToken;
        Integer benefitId = (recommendedBenefit != null) ? recommendedBenefit.getBenefitId() : null;

        // 추천 카드 결제가 아닌 경우 실제 카드 혜택 계산
        CalculatedBenefitDto realBenefit = null;
        if (!"rebirth".equals(requestToken)) {
            myCardDto = cardRepository.findByPermanentToken(requestToken)
                    .orElseThrow(() -> new EntityNotFoundException("해당 카드를 찾을 수 없습니다."));
            realBenefit = benefitService.calculateRealBenefit(userId, amount, merchantJoinDto, myCardDto, createdAt);
            if (realBenefit != null) {
                log.info("실제 혜택 - myCardId: {}, permanentToken: {}, benefitId: {}, benefitAmount: {}, benefitType: {}",
                        realBenefit.getMyCardId(), realBenefit.getPermanentToken(),
                        realBenefit.getBenefitId(), realBenefit.getBenefitAmount(), realBenefit.getBenefitType());
                benefitType = realBenefit.getBenefitType();
                benefitAmount = realBenefit.getBenefitAmount();
                permanentToken = realBenefit.getPermanentToken();
                benefitId = realBenefit.getBenefitId();
            } else {
                log.warn("실제 혜택 정보가 없음. 추천 혜택 사용.");
            }
        }

        // 최종 요청 페이로드 구성
        log.info("카드사 요청 페이로드 - permanentToken: {}", permanentToken);
        log.info("카드사 요청 페이로드 - amount: {}", amount);
        log.info("카드사 요청 페이로드 - merchantName: {}", merchantName);
        log.info("카드사 요청 페이로드 - benefitId: {}", benefitId);
        log.info("카드사 요청 페이로드 - benefitType: {}", benefitType.name());
        log.info("카드사 요청 페이로드 - benefitAmount: {}", benefitAmount);
        log.info("카드사 요청 페이로드 - createdAt: {}", createdAt);
        CreateTransactionRequestToCardsaDTO transactionRequest = CreateTransactionRequestToCardsaDTO.builder()
                .token(permanentToken)
                .amount(amount)
                .merchantName(merchantName)
                .benefitId(benefitId)
                .benefitType(benefitType.name())
                .benefitAmount(benefitAmount)
                .createdAt(createdAt)
                .build();

        // 카드사에 결제 요청 후 결과 수신
        CardTransactionDTO cardTransactionDTO = transactionToCardsa(transactionRequest);
        if (cardTransactionDTO != null) {
            log.info("카드사 응답 - transactionId: {}, status: {}",
                    cardTransactionDTO.getApprovalCode(), cardTransactionDTO.getCreatedAt());
        } else {
            log.warn("카드사 응답이 null입니다.");
        }

        // 혜택 현황 업데이트
        if (benefitId != null) {
            UserCardBenefit userCardBenefit = userCardBenefitService.getUserCardBenefit(userId, benefitId, myCardDto.getCardId(), createdAt);

            userCardBenefitRepository.save(
                    userCardBenefit.toBuilder()
                            .benefitCount((short) (userCardBenefit.getBenefitCount() + 1))
                            .benefitAmount(userCardBenefit.getBenefitAmount() + benefitAmount)
                            .updateDate(createdAt)
                            .build()
            );
        }

        // 마이데이터 호출 및 혜택 현황 업데이트
        User user = userRepository.findByUserId(new UserId(userId));
        log.info("유저 정보 - userId: {}, userName: {}", user.getUserId(), user.getUserName());
        List<MyCard> myCards = Arrays.asList(myCardDto);

        // 마이데이터 가져오기 호출
        log.info("유저 정보 - userId: {} + 마이데이터 호출하기직전 ++++++ ", user.getUserId());
        myDataService.loadMyTransactionByCardsForPayment(user, myCards);

        // 리포트 업데이트(이것도 현재 기준이라 없애야 할듯?)
        reportService.updateMonthlyTransactionSummary(userId, createdAt);

        return cardTransactionDTO;
    }


}