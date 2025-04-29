package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.PreBenefit;

import java.util.Optional;

public interface PreBenefitRepository {
    Optional<PreBenefit> findByUserId(Integer userId);
    PreBenefit save(PreBenefit preBenefit);
}
