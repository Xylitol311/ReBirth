package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.infrastructure.entity.UserCardBenefitEntity;
import com.kkulmoo.rebirth.payment.infrastructure.entity.UserCardBenefitId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCardBenefitJpaRepository extends JpaRepository<UserCardBenefitEntity, UserCardBenefitId> {
    Optional<UserCardBenefitEntity> findById_UserIdAndId_BenefitTemplateId(Integer userId, Integer benefitTemplateId);
}
