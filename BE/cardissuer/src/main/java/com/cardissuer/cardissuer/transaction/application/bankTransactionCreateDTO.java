package com.cardissuer.cardissuer.transaction.application;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class bankTransactionCreateDTO {
	private String accountNumber;
	private Integer amount;
	private String userCI;
	private String type;
	private String description;
	private LocalDateTime createdAt;
}
