package com.kkulmoo.rebirth.user.domain.repository;

import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCardBenefitRepository {
    UserCardBenefit findByUserIdAndBenefitTemplateIdAndYearAndMonth(Integer userId, Integer benefitId, int year, int month);
    UserCardBenefit save(UserCardBenefit userCardBenefit);
}
