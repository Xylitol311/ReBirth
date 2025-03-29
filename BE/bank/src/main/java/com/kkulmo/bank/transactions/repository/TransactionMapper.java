package com.kkulmo.bank.transactions.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.kkulmo.bank.transactions.dto.TransactionDTO;
import com.kkulmo.bank.transactions.dto.TransactionType;

@Component
public class TransactionMapper {

	/**
	 * Convert a TransactionEntity to TransactionDTO
	 *
	 * @param transaction the entity to convert
	 * @return the converted DTO
	 */
	public TransactionDTO toDTO(TransactionEntity transaction) {
		if (transaction == null) {
			return null;
		}
		return TransactionDTO.builder()
			.accountNumber(transaction.getAccountNumber())
			.amount(transaction.getAmount())
			.createdAt(transaction.getCreatedAt())
			.type(transaction.getType() != null ? transaction.getType().name() : null)
			.description(transaction.getDescription())
			.approvalCode(transaction.getApprovalCode())
			.build();
	}

	/**
	 * Convert a TransactionDTO to TransactionEntity
	 *
	 * @param dto the DTO to convert
	 * @return the converted entity
	 */
	public TransactionEntity toEntity(TransactionDTO dto) {
		if (dto == null) {
			return null;
		}

		return TransactionEntity.builder()
			.accountNumber(dto.getAccountNumber())
			.amount(dto.getAmount())
			.type(TransactionType.valueOf(dto.getType()))
			.description(dto.getDescription())
			.approvalCode(dto.getApprovalCode())
			.createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
			.build();
	}
}