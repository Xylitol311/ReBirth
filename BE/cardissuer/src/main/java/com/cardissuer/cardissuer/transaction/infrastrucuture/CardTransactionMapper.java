package com.cardissuer.cardissuer.transaction.infrastrucuture;

import org.mapstruct.Mapper;

import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;


@Mapper(componentModel = "spring")
public interface CardTransactionMapper {
	CardTransaction toDomain(CardTransactionEntity entity);
	CardTransactionEntity toEntity(CardTransaction domain);
	default CardUniqueNumber map(String value) {
		if (value == null) {
			return null;
		}
		return new CardUniqueNumber(value);
	}

	// Add a method to convert CardUniqueNumber to String
	default String map(CardUniqueNumber value) {
		if (value == null) {
			return null;
		}
		return value.getValue(); // Assuming CardUniqueNumber has a getValue() method
	}
}
