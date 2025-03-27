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

		TransactionType transactionType = null;
		if (dto.getType() != null) {
			try {
				transactionType = TransactionType.valueOf(dto.getType());
			} catch (IllegalArgumentException e) {
				// 잘못된 type 문자열이 전달되었을 경우의 오류 처리
				// 로깅 또는 기본값 설정 등을 수행할 수 있습니다.
				// throw new IllegalArgumentException("Invalid transaction type: " + dto.getType());
			}
		}

		return TransactionEntity.builder()
			.accountNumber(dto.getAccountNumber())
			.amount(dto.getAmount())
			.type(transactionType)
			.description(dto.getDescription())
			.approvalCode(dto.getApprovalCode())
			.createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
			.build();
	}
}