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
                .userCI(userEntity.getUserCI())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .deletedAt(userEntity.getDeletedAt())
                .hashedPatternNumber(userEntity.getHashedPatternNumber())
                .averageMonthlyIncome(userEntity.getAverageMonthlyIncome())
                .bankLatestLoadDataAt(userEntity.getBankLatestLoadDataAt())
                .bankAccounts(userEntity.getBankAccounts())
                .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        UserEntity.UserEntityBuilder builder = UserEntity.builder()
                .consumptionPatternId(user.getConsumptionPatternId())
                .userName(user.getUserName())
                .hashedPinNumber(user.getHashedPinNumber())
                .phoneNumber(user.getPhoneNumber())
                .phoneSerialNumber(user.getPhoneSerialNumber())
                .userCI(user.getUserCI())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .hashedPatternNumber(user.getHashedPatternNumber())
                .averageMonthlyIncome(user.getAverageMonthlyIncome())
                .bankLatestLoadDataAt(user.getBankLatestLoadDataAt())
                .bankAccounts(user.getBankAccounts());

        // userId가 null이 아닌 경우에만 설정
        if (user.getUserId() != null) {
            builder.userId(user.getUserId().getValue());
        }

        return builder.build();
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