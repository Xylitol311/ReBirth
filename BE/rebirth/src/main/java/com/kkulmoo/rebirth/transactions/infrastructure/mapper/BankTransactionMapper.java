package com.kkulmoo.rebirth.transactions.infrastructure.mapper;

import com.kkulmoo.rebirth.transactions.domain.BankTransactions;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.BankTransactionsEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BankTransactionMapper {

    // Convert DTO to Entity
    public static BankTransactionsEntity toEntity(BankTransactions dto) {


        return BankTransactionsEntity.builder()
                .transactionId(dto.getTransactionsId())
                .cardCompanyId(dto.getCardCompanyId())
                .bankTransactionType(dto.getBankTransactionType())
                .accountNumber(dto.getAccountNumber())
                .build();
    }

    // Convert Entity to DTO
    public static BankTransactions toDto(BankTransactionsEntity entity) {

        return BankTransactions.builder()
                .TransactionsId(entity.getTransactionId())
                .cardCompanyId(entity.getCardCompanyId())
                .bankTransactionType(entity.getBankTransactionType())
                .accountNumber(entity.getAccountNumber())
                .build();
    }

    // Convert a list of DTOs to a list of Entities
    public static List<BankTransactionsEntity> toEntityList(List<BankTransactions> dtoList) {

        return dtoList.stream()
                .map(BankTransactionMapper::toEntity)
                .collect(Collectors.toList());
    }

    // Convert a list of Entities to a list of DTOs
    public static List<BankTransactions> toDtoList(List<BankTransactionsEntity> entityList) {

        return entityList.stream()
                .map(BankTransactionMapper::toDto)
                .collect(Collectors.toList());
    }
}
