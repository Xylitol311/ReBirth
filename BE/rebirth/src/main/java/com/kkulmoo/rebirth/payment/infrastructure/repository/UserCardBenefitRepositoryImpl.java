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
    UserCardBenefitJpaRepository userCardBenefitJpaRepository;
    UserCardBenefitEntityMapper userCardBenefitEntityMapper;

    @Override
    public UserCardBenefit findByUserIdAndBenefitId(Integer userId, Integer benefitId) {
        UserCardBenefitEntity entity = userCardBenefitJpaRepository
                .findById_UserIdAndId_BenefitTemplateId(userId, benefitId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "UserCardBenefit not found for userId " + userId + " and benefitId " + benefitId));
        return userCardBenefitEntityMapper.toUserCardBenefit(entity);
    }
}
