package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CardJpaRepository extends JpaRepository<CardEntity,Integer> {

	List<CardEntity> findByUserId(Integer userId);

	@Query("SELECT c FROM CardEntity c WHERE c.cardUniqueNumber IN :cardUniqueNumbers")
	List<CardEntity> findByCardUniqueNumberIn(List<String> cardUniqueNumbers);

	List<CardEntity> findByUserIdAndCardIdIn(Integer userId, List<Integer> cardIds);

	Optional<CardEntity> findByCardUniqueNumber(String cardUniqueNumber);

	Integer countByUserId(Integer userId);
}
