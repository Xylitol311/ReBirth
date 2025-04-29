package com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper;

/**
 * Mapper class to convert entities to BankTransactionResponse
 */

import com.kkulmoo.rebirth.transactions.domain.BankTransactionType;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.BankTransactionEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BankTransactionMapper {

    public List<TransactionEntity> toTransactionEntities(List<BankTransactionResponse> responses) {
        return responses.stream()
                .map(this::toTransactionEntity)
                .collect(Collectors.toList());
    }
    public TransactionEntity toTransactionEntity(BankTransactionResponse response) {
        Integer userId = null;
        if (response.getUserId() != null) {
            userId = response.getUserId().getValue(); // UserId 클래스에 getValue() 메서드가 있다고 가정
        }

        return TransactionEntity.builder()                                                            
                .userId(userId)
                .amount(response.getAmount() != null ? response.getAmount().intValue() : null)
                .createdAt(response.getCreatedAt())
                .approvalNumber(response.getApprovalCode())
                .build();
    }
    public List<BankTransactionEntity> toBankTransactionEntities(
            List<BankTransactionResponse> responses,
            List<TransactionEntity> savedTransactions) {

        Map<String, TransactionEntity> transactionEntityMap = savedTransactions.stream()
                .collect(Collectors.toMap(
                        TransactionEntity::getApprovalNumber,
                        transaction -> transaction,
                        // 중복된 approvalNumber가 있을 경우 첫 번째 값 유지
                        (existing, replacement) -> existing
                ));

        return responses.stream()
                .map(response -> {
                    TransactionEntity matchedTransaction = transactionEntityMap.get(response.getApprovalCode());
                    if (matchedTransaction != null) {
                        return toBankTransactionEntity(response, matchedTransaction.getTransactionId());
                    } else {
                        log.warn("매칭되는 트랜잭션을 찾을 수 없음: {}", response.getApprovalCode());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public BankTransactionEntity toBankTransactionEntity(BankTransactionResponse response, Integer transactionId) {
        return BankTransactionEntity.builder()
                .transactionId(transactionId)
                .accountNumber(response.getAccountNumber())
                .bankTransactionType(mapTransactionType(response.getType()))
                .build();
    }

    private BankTransactionType mapTransactionType(String type) {
        if (type == null) {
            return null;
        }

        try {
            return BankTransactionType.valueOf(type);
        } catch (IllegalArgumentException e) {
            // 기본값 반환 또는 예외 처리
            return BankTransactionType.DEPOSIT; // 또는 다른 기본값
        }
    }
}