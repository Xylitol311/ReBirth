package com.kkulmoo.rebirth.user.infrastrucutre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "consumption_pattern_id")
	private String consumptionPatternId;

	@Column(name = "user_name", nullable = false, length = 10)
	private String userName;

	@Column(name = "hashed_pin_number", nullable = false, length = 64)
	private String hashedPinNumber;

	@Column(name = "phone_number", nullable = false, length = 15)
	private String phoneNumber;

	@Column(name = "phone_serial_number", length = 100)
	private String phoneSerialNumber;

	@Column(name = "user_api_key", length = 40)
	private String userApiKey;

	@Column(name = "bank_accounts")
	private List<String> bankAccounts;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "bank_latest_load_data_at")
	private LocalDateTime bankLatestLoadDataAt;

	@Column(name = "average_monthly_income")
	private int averageMonthlyIncome;

}