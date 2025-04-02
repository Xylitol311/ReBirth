package com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper;

/**
 * Mapper class to convert entities to BankTransactionResponse
 */

import com.kkulmoo.rebirth.transactions.domain.BankTransactionType;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.BankTransactionEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
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

        List<BankTransactionEntity> result = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            BankTransactionEntity bankEntity = toBankTransactionEntity(responses.get(i));
            // 저장된 TransactionEntity의 ID를 설정
            bankEntity.setTransactionId(savedTransactions.get(i).getTransactionId());
            result.add(bankEntity);
        }

        return result;
    }

    public BankTransactionEntity toBankTransactionEntity(BankTransactionResponse response) {
        return BankTransactionEntity.builder()
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