package com.kkulmoo.rebirth.user.infrastrucutre.mapper;

import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserEntityMapper {

    public User toUser(UserEntity userEntity) {

        if (userEntity == null) {
            return null;
        }

        return User.builder()
                .userId(new UserId(userEntity.getUserId()))
                .consumptionPatternId(userEntity.getConsumptionPatternId())
                .userName(userEntity.getUserName())
                .hashedPinNumber(userEntity.getHashedPinNumber())
                .phoneNumber(userEntity.getPhoneNumber())
                .phoneSerialNumber(userEntity.getPhoneSerialNumber())
                .userApiKey(userEntity.getUserApiKey())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .deletedAt(userEntity.getDeletedAt())
                .bankLatestLoadDataAt(userEntity.getBankLatestLoadDataAt())
                .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserEntity.builder()
                .userId(user.getUserId().getValue())
                .consumptionPatternId(user.getConsumptionPatternId())
                .userName(user.getUserName())
                .hashedPinNumber(user.getHashedPinNumber())
                .phoneNumber(user.getPhoneNumber())
                .phoneSerialNumber(user.getPhoneSerialNumber())
                .userApiKey(user.getUserApiKey())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .bankLatestLoadDataAt(user.getBankLatestLoadDataAt())
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