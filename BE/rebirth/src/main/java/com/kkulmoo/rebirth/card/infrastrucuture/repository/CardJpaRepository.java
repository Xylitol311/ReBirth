package com.kkulmoo.rebirth.card.infrastrucuture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkulmoo.rebirth.card.infrastrucuture.entity.CardEntity;

public interface CardJpaRepository extends JpaRepository<CardEntity,Integer> {
	List<CardEntity> findByUserId(Integer userId);
}
