package com.cardissuer.cardissuer.cards.infrastructure.mapper;

import org.springframework.stereotype.Component;

import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.infrastructure.PermanentTokenEntity;

@Component
public class PermanentTokenEntityMapper {

	public PermanentToken toDomain(PermanentTokenEntity entity) {
		if (entity == null) {
			return null;
		}
		return PermanentToken.builder()
			.cardUniqueNumber(entity.getCardUniqueNumber())
			.token(entity.getToken())
			.createdAt(entity.getCreatedAt())
			.isActive(entity.getIsActive())
			.build();
	}

	public PermanentTokenEntity toEntity(PermanentToken domain) {
		if (domain == null) {
			return null;
		}
		return PermanentTokenEntity.builder()
			.cardUniqueNumber(domain.getCardUniqueNumber())
			.token(domain.getToken())
			.createdAt(domain.getCreatedAt())
			.isActive(domain.getIsActive())
			.build();
	}
}
