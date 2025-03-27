package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.infrastructure.entity.CardTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardTemplateJpaRespository extends JpaRepository<CardTemplateEntity, Integer> {
}
