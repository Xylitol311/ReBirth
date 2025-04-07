package com.kkulmoo.rebirth.user.infrastrucutre.repository;

import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserCardBenefitEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.mapper.UserCardBenefitEntityMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCardBenefitRepositoryImpl implements UserCardBenefitRepository {
    public final UserCardBenefitJpaRepository userCardBenefitJpaRepository;
    public final UserCardBenefitEntityMapper userCardBenefitEntityMapper;

    @Override
    public UserCardBenefit findByUserIdAndBenefitTemplateIdAndYearAndMonth(Integer userId, Integer benefitId, int year, int month) {
        UserCardBenefitEntity entity = userCardBenefitJpaRepository
                .findByUserIdAndBenefitTemplateIdAndYearAndMonth(userId, benefitId, year, month)
                .orElseThrow(() -> new EntityNotFoundException(
                        "UserCardBenefit not found for userId " + userId +
                        ", benefitId " + benefitId +
                        ", year " + year + ", month " + month));
        return userCardBenefitEntityMapper.toUserCardBenefit(entity);
    }

    @Override
    public UserCardBenefit save(UserCardBenefit userCardBenefit) {
        UserCardBenefitEntity entity = userCardBenefitEntityMapper.toUserCardBenefitEntity(userCardBenefit);
        UserCardBenefitEntity savedEntity = userCardBenefitJpaRepository.save(entity);
        return userCardBenefitEntityMapper.toUserCardBenefit(savedEntity);
    }
}
