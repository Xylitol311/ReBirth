package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.infrastructure.entity.CardTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTemplateJpaRepository extends JpaRepository<CardTemplateEntity, Integer> {
}
