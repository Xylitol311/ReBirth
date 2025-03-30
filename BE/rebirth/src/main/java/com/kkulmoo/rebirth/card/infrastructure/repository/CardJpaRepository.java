package com.kkulmoo.rebirth.card.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkulmoo.rebirth.shared.entity.CardEntity;

public interface CardJpaRepository extends JpaRepository<CardEntity,Integer> {
	List<CardEntity> findByUserId(Integer userId);
}
