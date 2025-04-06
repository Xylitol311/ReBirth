package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.payment.domain.PreBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.PreBenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreBenefitService {

    private final PreBenefitRepository preBenefitRepository; // PreBenefit 저장소

    // PreBenefit 저장 (업데이트 또는 인서트)
    @Transactional
    public PreBenefit savePreBenefit(PreBenefit preBenefit) {
        return preBenefitRepository.findByUserId(preBenefit.getUserId())
                .map(existing -> PreBenefit.builder()
                        .userId(existing.getUserId())          // 기존 userId 유지
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