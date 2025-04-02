package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CardJpaRepository extends JpaRepository<CardEntity,Integer> {

	List<CardEntity> findByUserId(Integer userId);

	@Query("SELECT c FROM CardEntity c WHERE c.cardUniqueNumber IN :cardUniqueNumbers")
	List<CardEntity> findByCardUniqueNumberIn(List<String> cardUniqueNumbers);
}
