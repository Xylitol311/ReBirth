package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.infrastructure.entity.PreBenefitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreBenefitJpaRepository extends JpaRepository<PreBenefitEntity, Integer> {
    Optional<PreBenefitEntity> findByUserId(Integer userId);
}
