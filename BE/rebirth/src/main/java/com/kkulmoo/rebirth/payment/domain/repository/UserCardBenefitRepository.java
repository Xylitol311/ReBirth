package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;

public interface UserCardBenefitRepository {
    UserCardBenefit findByUserIdAndBenefitId(Integer userId, Integer benefitId);
}
