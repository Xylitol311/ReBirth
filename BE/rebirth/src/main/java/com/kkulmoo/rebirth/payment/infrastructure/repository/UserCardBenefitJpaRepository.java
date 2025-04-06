package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.infrastructure.entity.UserCardBenefitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCardBenefitJpaRepository extends JpaRepository<UserCardBenefitEntity, Integer> {
    Optional<UserCardBenefitEntity> findByUserIdAndBenefitTemplateId(
            Integer userId,
            Integer benefitTemplateId,
            int year,
            int month
    );
}
