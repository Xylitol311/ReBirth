package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// 카드 템플릿
public interface CardTemplateJpaRepository extends JpaRepository<CardTemplateEntity, Integer> {
}
