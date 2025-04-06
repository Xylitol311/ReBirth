package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.UserCardBenefitRepository;
import com.kkulmoo.rebirth.payment.infrastructure.entity.UserCardBenefitEntity;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.UserCardBenefitEntityMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCardBenefitRepositoryImpl implements UserCardBenefitRepository {
    public final UserCardBenefitJpaRepository userCardBenefitJpaRepository;
    public final UserCardBenefitEntityMapper userCardBenefitEntityMapper;

    @Override
    public UserCardBenefit findByUserIdAndBenefitIdAndYearAndMonth(Integer userId, Integer benefitId, int year, int month) {
        UserCardBenefitEntity entity = userCardBenefitJpaRepository
                .findByUserIdAndBenefitTemplateId(userId, benefitId, year, month)
                .orElseThrow(() -> new EntityNotFoundException(
                        "UserCardBenefit not found for userId " + userId +
                        ", benefitId " + benefitId +
                        ", year " + year + ", month " + month));
        return userCardBenefitEntityMapper.toUserCardBenefit(entity);
    }
}
