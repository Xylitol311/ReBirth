package com.kkulmoo.rebirth.user.infrastrucutre.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private Long userId;

	@Column(name = "consumption_pattern_id")
	private Byte consumptionPatternId;

	@Column(name = "user_email")
	private String userEmail;

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

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "latest_load_data_at")
	private LocalDateTime latestLoadDataAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}