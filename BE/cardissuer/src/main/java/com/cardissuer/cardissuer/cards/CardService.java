package com.cardissuer.cardissuer.cards;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

	private final CardRepository cardRepository;

	/**
	 * 새로운 카드를 등록합니다.
	 *
	 * @param card 등록할 카드 정보
	 * @return 등록된 카드 정보
	 */
	@Transactional
	public CardEntity createCard(CardEntity card) {
		return cardRepository.save(card);
	}

	/**
	 * 특정 사용자의 모든 카드를 조회합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 사용자의 모든 카드 목록
	 */
	@Transactional(readOnly = true)
	public List<CardEntity> getCardsByUserId(Integer userId) {
		return cardRepository.findByUserIdAndDeletedAtIsNull(userId);
	}


}