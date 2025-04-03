package com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.domain.Status;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.CardTransactionEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CardTransactionResponseMapper {

    // CardTransactionResponse -> TransactionEntity 변환
    public TransactionEntity toTransactionEntity(CardTransactionResponse response) {
        return TransactionEntity.builder()
                .userId(response.getUserId().getValue())
                .createdAt(response.getCreatedAt())
                .approvalNumber(response.getApprovalCode())
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

        List<CardTransactionEntity> cardEntities = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            cardEntities.add(toCardTransactionEntity(
                    responses.get(i),
                    savedTransactions.get(i).getTransactionId()
            ));
        }

        return cardEntities;
    }
}
