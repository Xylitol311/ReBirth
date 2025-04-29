package com.cardissuer.cardissuer.user.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cardissuer.cardissuer.user.domain.User;

/**
 * UserEntity와 User DTO 간의 변환을 담당하는 매퍼 클래스
 */
@Component // Spring 빈으로 등록
public class UserEntityMapper {

	/**
	 * UserEntity를 User 도메인 모델로 변환합니다.
	 *
	 * @param entity 변환할 엔티티
	 * @return 변환된 도메인 모델
	 */
	public User toDomain(UserEntity entity) {
		if (entity == null) {
			return null;
		}

		return User.builder()
			.userCI(entity.getUserCI())
			.userName(entity.getUserName())
			.createdAt(entity.getCreatedAt())
			.build();
	}

	/**
	 * User 도메인 모델을 UserEntity로 변환합니다.
	 *
	 * @param domain 변환할 도메인 모델
	 * @return 변환된 엔티티
	 */
	public UserEntity toEntity(User domain) {
		if (domain == null) {
			return null;
		}
		return UserEntity.builder()
			.userCI(domain.getUserCI())
			.userName(domain.getUserName())
			.createdAt(domain.getCreatedAt())
			.build();
	}

	/**
	 * UserEntity 목록을 User 도메인 모델 목록으로 변환합니다.
	 *
	 * @param entities 변환할 엔티티 목록
	 * @return 변환된 도메인 모델 목록
	 */
	public List<User> toDomainList(List<UserEntity> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}

		return entities.stream()
			.map(this::toDomain) // this 참조 사용
			.collect(Collectors.toList());
	}

	/**
	 * User 도메인 모델 목록을 UserEntity 목록으로 변환합니다.
	 *
	 * @param domains 변환할 도메인 모델 목록
	 * @return 변환된 엔티티 목록
	 */
	public List<UserEntity> toEntityList(List<User> domains) {
		if (domains == null) {
			return Collections.emptyList();
		}

		return domains.stream()
			.map(this::toEntity) // this 참조 사용
			.collect(Collectors.toList());
	}
}