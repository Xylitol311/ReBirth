package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 카드 템플릿
public interface CardTemplateJpaRepository extends JpaRepository<CardTemplateEntity, Integer> {
    Optional<CardTemplateEntity> findByCardName(String cardName);
}
