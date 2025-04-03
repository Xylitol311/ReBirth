package com.kkulmoo.rebirth.payment.application.service;


import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.*;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private MerchantJoinRepository merchantJoinRepository;

    @Mock
    private CardJoinRepository cardJoinRepository;

    @Mock
    private BenefitRepository benefitRepository;

    @Mock
    private UserCardBenefitRepository userCardBenefitRepository;

    @InjectMocks
    private PaymentService paymentService;

    private final Integer userId = 1;
    private final int amount = 1000;
    private final String merchantName = "TestMerchant";

    @BeforeEach
    public void setUp() {
        // 1. MerchantJoinRepository 더미 데이터
        MerchantJoinDto merchantJoinDto = MerchantJoinDto.builder()
                .merchantId(10)
                .categoryId(20)
                .subCategoryId(30)
                .build();
        when(merchantJoinRepository.findMerchantJoinDataByMerchantName(eq(merchantName)))
                .thenReturn(merchantJoinDto);

        // 2. CardJoinRepository 더미 데이터
        MyCardDto myCardDto = MyCardDto.builder()
                .cardId(100)
                .cardTemplateId(200)
                .permanentToken("PERM_TOKEN_100")
                .spendingTier((short) 1)
                .build();
        when(cardJoinRepository.findMyCardsIdAndTemplateIdsByUserId(eq(userId)))
                .thenReturn(Collections.singletonList(myCardDto));

        // 3. BenefitRepository 더미 데이터
        BenefitInfo benefitInfo = BenefitInfo.builder()
                .benefitId(300)
                .cardTemplateId(200)
                // 카테고리
                // 서브카테고리
                .benefitType(BenefitType.DISCOUNT)
                .merchantFilterType((short) 1)
                .benefitConditionType((short) 1)
                .performanceRange(Arrays.asList(300, 500))
                .benefitsBySection(Arrays.asList(0.1, 0.2))
                // merchantInfo
                // merchatList
                .paymentRange(List.of(100))
                .benefitUsageLimit(Arrays.asList((short)5, (short)20))
                .benefitUsageAmount(Arrays.asList((short)1500, (short)2500))
                .discountType(DiscountType.PERCENT)
                .build();
        when(benefitRepository.findBenefitsByMerchantFilter(
                eq(myCardDto.getCardTemplateId()),
                eq(merchantJoinDto.getCategoryId()),
                eq(merchantJoinDto.getSubCategoryId()),
                eq(merchantJoinDto.getMerchantId())
        )).thenReturn(Collections.singletonList(benefitInfo));

        // 4. UserCardBenefitRepository 더미 데이터
        UserCardBenefit userCardBenefit = UserCardBenefit.builder()
                .userId(userId)
                .benefitTemplateId(benefitInfo.getBenefitId())
                .spendingTier((short) 1)
                .benefitCount((short) 0)
                .benefitAmount(0)
                .build();
        when(userCardBenefitRepository.findByUserIdAndBenefitId(eq(userId), eq(benefitInfo.getBenefitId())))
                .thenReturn(userCardBenefit);

        // calculateBenefitAmount에서 사용되는 로직에 따라,
        // 예를 들어, benefitInfo의 benefitsBySection의 첫 번째 값(0.1)을 곱하면,
        // amount가 1000이면 할인액은 100이 될 것으로 예상할 수 있음.
    }

    @Test
    public void testRecommendPaymentCard() {
        // when
        CalculatedBenefitDto result = paymentService.recommendPaymentCard(userId, amount, merchantName);

        // then
        assertThat(result).isNotNull();
        // myCardDto의 permanentToken이 반환되어야 함
        assertThat(result.getPermanentToken()).isEqualTo("PERM_TOKEN_100");
        // 할인액(benefitAmount)이 예상 값 100과 일치하는지 검증 (테스트 로직에 따라 달라질 수 있음)
        assertThat(result.getBenefitAmount()).isEqualTo(100);
        // benefitId 검증
        assertThat(result.getBenefitId()).isEqualTo(300);
    }
}