package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardJpaRepository extends JpaRepository<CardEntity,Integer> {

	List<CardEntity> findByUserId(Integer userId);

	@Query("SELECT c FROM CardEntity c WHERE c.cardUniqueNumber IN :cardUniqueNumbers AND c.userId = :userId")
	List<CardEntity> findByCardUniqueNumberInAndUserId(List<String> cardUniqueNumbers, Integer userId);

	List<CardEntity> findByUserIdAndCardIdIn(Integer userId, List<Integer> cardIds);

	Optional<CardEntity> findByCardUniqueNumber(String cardUniqueNumber);

	Integer countByUserId(Integer userId);

	Optional<CardEntity> findByPermanentToken(String permanentToken);

	Optional<CardEntity> findByPermanentTokenAndUserId(String permanentToken, Integer userId);

	@Query("SELECT ct.cardImgUrl FROM CardEntity c JOIN CardTemplateEntity ct ON c.cardTemplateId = ct.cardTemplateId " +
			"WHERE c.cardId = :cardId")
	Optional<String> findCardImgUrlByCardId(@Param("cardId") Integer cardId);
}
