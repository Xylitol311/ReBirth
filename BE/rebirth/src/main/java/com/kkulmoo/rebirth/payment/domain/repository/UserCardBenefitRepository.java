package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCardBenefitRepository {
    UserCardBenefit findByUserIdAndBenefitIdAndYearAndMonth(Integer userId, Integer benefitId, int year, int month);
}
