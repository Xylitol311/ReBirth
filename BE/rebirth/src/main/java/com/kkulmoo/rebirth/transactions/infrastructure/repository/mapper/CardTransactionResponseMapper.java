package com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.domain.Status;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.CardTransactionEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardTransactionResponseMapper {

    // CardTransactionResponse -> TransactionEntity 변환
    public TransactionEntity toTransactionEntity(CardTransactionResponse response) {
        return TransactionEntity.builder()
                .userId(response.getUserId().getValue())
                .createdAt(response.getCreatedAt())
                .approvalNumber(response.getApprovalCode())
                .amount(response.getAmount())
                .build();
    }

    // CardTransactionResponse -> CardTransactionEntity 변환
    public CardTransactionEntity toCardTransactionEntity(CardTransactionResponse response, Integer transactionId) {
        return CardTransactionEntity.builder()
                .transactionId(transactionId)
                .cardUniqueNumber(response.getCardUniqueNumber())
                .status(Status.fromApprovalCode(response.getApprovalCode()))
                .cardBenefitType(response.getBenefitType() != null ?
                        BenefitType.valueOf(response.getBenefitType()) : null)
                .benefitAmount(response.getBenefitAmount())
                .merchantId(response.getMerchantId())
                .build();
    }

    // TransactionResponse 리스트 -> TransactionEntity 리스트 변환
    public List<TransactionEntity> toTransactionEntities(List<CardTransactionResponse> responses) {
        return responses.stream()
                .map(this::toTransactionEntity)
                .collect(Collectors.toList());
    }

    // TransactionResponse 리스트 + 저장된 TransactionEntity 리스트 -> CardTransactionEntity 리스트 변환
    public List<CardTransactionEntity> toCardTransactionEntities(
            List<CardTransactionResponse> responses,
            List<TransactionEntity> savedTransactions) {

        Map<String, TransactionEntity> transactionEntityMap = savedTransactions.stream()
                .collect(Collectors.toMap(
                        TransactionEntity::getApprovalNumber,
                        transaction -> transaction
                ));

        return responses.stream()
                .map(response -> {
                    TransactionEntity matchedTransaction  = transactionEntityMap.get(response.getApprovalCode());
                    if(matchedTransaction!= null){
                        return toCardTransactionEntity(response, matchedTransaction.getTransactionId());
                    }else {
                        log.warn("매칭되는 트랜잭션을 찾을 수 없음: {}", response.getApprovalCode());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }
}
