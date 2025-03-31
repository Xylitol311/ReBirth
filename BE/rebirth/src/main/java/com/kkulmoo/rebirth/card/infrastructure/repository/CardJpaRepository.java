package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardJpaRepository extends JpaRepository<CardsEntity,Integer> {
	List<CardsEntity> findByUserId(Integer userId);
}
