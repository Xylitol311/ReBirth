package com.cardissuer.cardissuer.cards.infrastructure.mapper;

import org.springframework.stereotype.Component;

import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.infrastructure.CardEntity;
import com.cardissuer.cardissuer.cards.infrastructure.PermanentTokenEntity;
@Component
public class PermanentTokenEntityMapper {

	private final CardEntityMapper cardMapper; // 카드 매퍼 주입

	public PermanentTokenEntityMapper(CardEntityMapper cardMapper) {
		this.cardMapper = cardMapper;
	}

	public PermanentToken toDomain(PermanentTokenEntity entity) {
		if (entity == null) {
			return null;
		}
		return PermanentToken.builder()
			.cardUniqueNumber(entity.getCardUniqueNumber())
			.token(entity.getToken())
			.createdAt(entity.getCreatedAt())
			.isActive(entity.getIsActive())
			.card(entity.getCard() != null ? cardMapper.toDomain(entity.getCard()) : null)
			.build();
	}

	public PermanentTokenEntity toEntity(PermanentToken domain) {
		if (domain == null) {
			return null;
		}

		// 여기서 card의 변환이 필요합니다
		CardEntity cardEntity = domain.getCard() != null ? cardMapper.toEntity(domain.getCard()) : null;

		return PermanentTokenEntity.builder()
			.token(domain.getToken())
			// cardUniqueNumber는 여기서 설정하지 않음 (insertable=false, updatable=false)
			.createdAt(domain.getCreatedAt())
			.isActive(domain.getIsActive())
			.card(cardEntity) // 변환된 카드 엔티티 설정
			.build();
	}
}