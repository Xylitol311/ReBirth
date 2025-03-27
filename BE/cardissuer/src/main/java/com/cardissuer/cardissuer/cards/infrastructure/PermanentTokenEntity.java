package com.cardissuer.cardissuer.cards.infrastructure;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PermanentTokenEntity {

	@Id
	@Column(name = "token", nullable = false, length = 100)
	private String token;

	// 읽기 전용으로 설정 (insertable=false, updatable=false)
	@Column(name = "card_unique_number", nullable = false, length = 40, insertable = false, updatable = false)
	private String cardUniqueNumber;  // VARCHAR(40) 타입의 카드 고유 번호

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_unique_number", referencedColumnName = "card_unique_number", nullable = false)
	private CardEntity card;  // 카드 엔티티와의 연관관계
}
