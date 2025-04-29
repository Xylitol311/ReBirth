package com.kkulmoo.rebirth.user.infrastrucutre.repository;


import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserCardBenefitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCardBenefitJpaRepository extends JpaRepository<UserCardBenefitEntity, Integer> {
    Optional<UserCardBenefitEntity> findByUserIdAndBenefitTemplateIdAndYearAndMonth(
            Integer userId,
            Integer benefitTemplateId,
            int year,
            int month
    );
}
