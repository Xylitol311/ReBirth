package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.PreBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.PreBenefitRepository;
import com.kkulmoo.rebirth.payment.infrastructure.entity.PreBenefitEntity;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.PreBenefitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PreBenefitRepositoryImpl implements PreBenefitRepository {
    private final PreBenefitJpaRepository preBenefitJpaRepository;
    private final PreBenefitMapper preBenefitMapper;

    @Override
    public Optional<PreBenefit> findByUserId(Integer userId) {
        return preBenefitJpaRepository.findByUserId(userId).map(preBenefitMapper::toDomain);
    }

    @Override
    @Transactional
    public PreBenefit save(PreBenefit preBenefit) {
        PreBenefitEntity entity = preBenefitMapper.toEntity(preBenefit);
        PreBenefitEntity savedEntity = preBenefitJpaRepository.save(entity);
        return preBenefitMapper.toDomain(savedEntity);
    }
}
