package com.kkulmoo.rebirth.transactions.infrastructure.mapper;


import com.kkulmoo.rebirth.transactions.domain.Transactions;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import com.kkulmoo.rebirth.user.domain.UserId;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionMapper {

    // Convert DTO to Entity
    public static TransactionEntity toEntity(Transactions dto) {

        return TransactionEntity.builder()
                .transactionId(dto.getTransactionsId())
                .userId(dto.getUserId().getValue())
                .createdAt(dto.getCreatedAt())
                .approvalNumber(dto.getApprovalNumber())
                .build();
    }

    // Convert Entity to DTO
    public static Transactions toDto(TransactionEntity entity) {

        return Transactions.builder()
                .TransactionsId(entity.getTransactionId())
                .userId(new UserId(entity.getUserId()))
                .createdAt(entity.getCreatedAt())
                .ApprovalNumber(entity.getApprovalNumber())
                .build();
    }

    // Convert a list of DTOs to a list of Entities
    public static List<TransactionEntity> toEntityList(List<Transactions> dtoList) {

        return dtoList.stream()
                .map(TransactionMapper::toEntity)
                .collect(Collectors.toList());
    }

    // Convert a list of Entities to a list of DTOs
    public static List<Transactions> toDtoList(List<TransactionEntity> entityList) {

        return entityList.stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }
}