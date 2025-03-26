package com.cardissuer.cardissuer.transaction.infrastrucuture;

import org.mapstruct.Mapper;

import com.cardissuer.cardissuer.transaction.domain.CardTransaction;


@Mapper(componentModel = "spring")
public interface CardTransactionMapper {
	CardTransaction toDomain(CardTransactionEntity entity);
	CardTransactionEntity toEntity(CardTransaction domain);
}