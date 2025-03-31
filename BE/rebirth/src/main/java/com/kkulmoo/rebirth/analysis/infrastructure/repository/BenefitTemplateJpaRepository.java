package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.BenefitTemplate;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitTemplateJpaRepository extends JpaRepository<BenefitTemplateEntity, Integer> {
}
