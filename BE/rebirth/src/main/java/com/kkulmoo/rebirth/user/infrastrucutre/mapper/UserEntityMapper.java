package com.kkulmoo.rebirth.user.infrastrucutre.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;

@Component
public class UserEntityMapper {

	public User toUser(UserEntity userEntity) {

		if (userEntity == null) {
			return null;
		}

		return User.builder()
			.userId(new UserId(userEntity.getUserId().intValue()))
			.consumptionPatternId(userEntity.getConsumptionPatternId())
			.userName(userEntity.getUserName())
			.hashedPinNumber(userEntity.getHashedPinNumber())
			.phoneNumber(userEntity.getPhoneNumber())
			.phoneSerialNumber(userEntity.getPhoneSerialNumber())
			.userApiKey(userEntity.getUserApiKey())
			.createdAt(userEntity.getCreatedAt())
			.updatedAt(userEntity.getUpdatedAt())
			.deletedAt(userEntity.getDeletedAt())
			.latestLoadDataAt(userEntity.getLatestLoadDataAt())
			.build();
	}

	public UserEntity toEntity(User user) {
		if (user == null) {
			return null;
		}

		return UserEntity.builder()
			.userId(user.getUserId() != null ? Long.valueOf(user.getUserId().getValue()) : null)
			.consumptionPatternId(user.getConsumptionPatternId())
			.userName(user.getUserName())
			.hashedPinNumber(user.getHashedPinNumber())
			.phoneNumber(user.getPhoneNumber())
			.phoneSerialNumber(user.getPhoneSerialNumber())
			.userApiKey(user.getUserApiKey())
			.createdAt(user.getCreatedAt())
			.updatedAt(user.getUpdatedAt())
			.deletedAt(user.getDeletedAt())
			.latestLoadDataAt(user.getLatestLoadDataAt())
			.build();
	}

	public List<User> toUserList(List<UserEntity> userEntities) {
		if (userEntities == null) {
			return Collections.emptyList();
		}

		return userEntities.stream()
			.map(this::toUser)
			.collect(Collectors.toList());
	}
}