package com.kkulmoo.rebirth.user.infrastrucutre.repository;

import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserCardBenefitEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.mapper.UserCardBenefitEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCardBenefitRepositoryImpl implements UserCardBenefitRepository {
    public final UserCardBenefitJpaRepository userCardBenefitJpaRepository;
    public final UserCardBenefitEntityMapper userCardBenefitEntityMapper;

    @Override
    public Optional<UserCardBenefit> findByUserIdAndBenefitTemplateIdAndYearAndMonth(Integer userId, Integer benefitId, int year, int month) {
        Optional<UserCardBenefitEntity> entityOptional = userCardBenefitJpaRepository
                .findByUserIdAndBenefitTemplateIdAndYearAndMonth(userId, benefitId, year, month);

        return entityOptional.map(userCardBenefitEntityMapper::toUserCardBenefit);
    }
    @Override
    public UserCardBenefit save(UserCardBenefit userCardBenefit) {
        UserCardBenefitEntity entity = userCardBenefitEntityMapper.toUserCardBenefitEntity(userCardBenefit);
        UserCardBenefitEntity savedEntity = userCardBenefitJpaRepository.save(entity);
        return userCardBenefitEntityMapper.toUserCardBenefit(savedEntity);
    }

    @Override
    public void saveAll(List<UserCardBenefit> userCardBenefits) {
        List<UserCardBenefitEntity> entities = userCardBenefits.stream()
                .map(userCardBenefitEntityMapper::toUserCardBenefitEntity)
                .toList();
        userCardBenefitJpaRepository.saveAll(entities);
    }
}
