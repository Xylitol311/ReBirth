package com.cardissuer.cardissuer.cards;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

	// 사용자 ID로 카드 목록 조회
	List<CardEntity> findByUserId(Integer userId);

	// 사용자 ID로 카드 목록 조회 (삭제되지 않은 카드만)
	List<CardEntity> findByUserIdAndDeletedAtIsNull(Integer userId);

	// 카드 번호로 카드 조회
	Optional<CardEntity> findByCardNumber(String cardNumber);

	// 카드 고유 번호로 카드 조회
	Optional<CardEntity> findByCardUniqueNumber(Long cardUniqueNumber);
}
