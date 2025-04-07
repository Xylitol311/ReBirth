package com.kkulmoo.rebirth.user.domain.repository;

import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCardBenefitRepository {
    Optional<UserCardBenefit> findByUserIdAndBenefitTemplateIdAndYearAndMonth(Integer userId, Integer benefitId, int year, int month);
    UserCardBenefit save(UserCardBenefit userCardBenefit);
}
