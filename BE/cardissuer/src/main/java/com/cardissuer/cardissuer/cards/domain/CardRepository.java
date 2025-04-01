package com.cardissuer.cardissuer.cards.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.cards.domain.Card;

@Repository
public interface CardRepository {

	// 사용자 ID로 카드 목록 조회 (삭제되지 않은 카드만)
	List<Card> findByUserCIAndDeletedAtIsNull(String userId);

	Optional<Card> findByCardUniqueNumber(CardUniqueNumber cardUniqueNumber);

	void updateCard(Card card);

	Optional<PermanentToken> findTokenByCardUniqueNumber(String cardUniqueNumber);

	Optional<PermanentToken> findTokenByToken(String token);

	void updateToken(PermanentToken permanentToken);

	Card save(Card card);

	Boolean existsByCardUniqueNumberAndUserCI(String cardUniqueNumber, String UserCI);
}
