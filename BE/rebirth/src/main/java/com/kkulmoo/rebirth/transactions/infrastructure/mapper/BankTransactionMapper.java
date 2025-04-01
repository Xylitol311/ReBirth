package com.kkulmoo.rebirth.transactions.infrastructure.mapper;

import com.kkulmoo.rebirth.transactions.domain.BankTransactions;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.BankTransactionEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BankTransactionMapper {

    // Convert DTO to Entity
    public static BankTransactionEntity toEntity(BankTransactions dto) {


        return BankTransactionEntity.builder()
                .transactionId(dto.getTransactionsId())
                .cardCompanyId(dto.getCardCompanyId())
                .bankTransactionType(dto.getBankTransactionType())
                .accountNumber(dto.getAccountNumber())
                .build();
    }

    // Convert Entity to DTO
    public static BankTransactions toDto(BankTransactionEntity entity) {

        return BankTransactions.builder()
                .TransactionsId(entity.getTransactionId())
                .cardCompanyId(entity.getCardCompanyId())
                .bankTransactionType(entity.getBankTransactionType())
                .accountNumber(entity.getAccountNumber())
                .build();
    }

    // Convert a list of DTOs to a list of Entities
    public static List<BankTransactionEntity> toEntityList(List<BankTransactions> dtoList) {

        return dtoList.stream()
                .map(BankTransactionMapper::toEntity)
                .collect(Collectors.toList());
    }

    // Convert a list of Entities to a list of DTOs
    public static List<BankTransactions> toDtoList(List<BankTransactionEntity> entityList) {

        return entityList.stream()
                .map(BankTransactionMapper::toDto)
                .collect(Collectors.toList());
    }
}
