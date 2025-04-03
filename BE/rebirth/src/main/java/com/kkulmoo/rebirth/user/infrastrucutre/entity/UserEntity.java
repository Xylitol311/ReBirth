package com.kkulmoo.rebirth.user.infrastrucutre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
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

	@Column(name = "hashed_pattern_number", length = 64)
	private String hashedPatternNumber;

	@Column(name = "birth" , length = 6)
	private String monthdaybirth;

	@Column(name = "phone_number", nullable = false, length = 15)
	private String phoneNumber;

	@Column(name = "phone_serial_number", length = 100)
	private String phoneSerialNumber;

	@Column(name = "user_ci", nullable = false)
	private String userCI;

	@Column(name = "bank_accounts", columnDefinition = "text[]")
	@JdbcTypeCode(Types.ARRAY)
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
	private Integer averageMonthlyIncome;

}