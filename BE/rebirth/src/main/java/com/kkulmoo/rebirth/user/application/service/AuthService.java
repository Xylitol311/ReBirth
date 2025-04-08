package com.kkulmoo.rebirth.user.application.service;


import com.kkulmoo.rebirth.auth.AuthenticationResult;
import com.kkulmoo.rebirth.auth.jwt.JwtProvider;
import com.kkulmoo.rebirth.common.exception.UserCreationException;
import com.kkulmoo.rebirth.common.util.PasswordUtils;
import com.kkulmoo.rebirth.transactions.application.BankPort;
import com.kkulmoo.rebirth.user.application.command.CreateUserCommand;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BankPort bankPort;
    private final JwtProvider jwtProvider;


    public User createUser(CreateUserCommand command) {
        // PIN 번호 암호화
        String hashedPinNumber = PasswordUtils.encodePassword(command.getPinNumber());

        // 사용자 객체 생성
        User newUser = User.builder()
                .userName(command.getUserName())
                .userCI(command.getUserCI())
                .hashedPinNumber(hashedPinNumber)
                .phoneNumber(command.getPhoneNumber())
                .averageMonthlyIncome(Integer.parseInt(command.getAverageMonthlyIncome()))
                .phoneSerialNumber(command.getPhoneSerialNumber())
                .bankLatestLoadDataAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                .bankAccounts(new ArrayList<>())
                .build();

        System.out.println(newUser);
        // 사용자 저장
        User createdUser = userRepository.save(newUser);

        if (createdUser == null) {
            throw new UserCreationException("사용자 생성에 실패했습니다.");
        }

        return createdUser;
    }

    public void createPatternNum(int userId, String patternNumbers) {

        // Pattern 번호 암호화
        String hashedPatternNumber = PasswordUtils.encodePassword(patternNumbers);
        System.out.println("원본 : " + patternNumbers);
        System.out.println("바뀐거" + hashedPatternNumber);
        User user = userRepository.findByUserId(new UserId(userId));

        userRepository.update(User.builder()
                        .userId(user.getUserId())
                .userName(user.getUserName())
                .userCI(user.getUserCI())
                .hashedPinNumber(user.getHashedPinNumber())
                .phoneNumber(user.getPhoneNumber())
                .phoneSerialNumber(user.getPhoneSerialNumber())
                .bankLatestLoadDataAt(user.getBankLatestLoadDataAt())
                .bankAccounts(user.getBankAccounts())
                .hashedPatternNumber(hashedPatternNumber)
                .build());

    }

    public String getUserCI(UserCIRequest userCIRequest) {
        return Objects.requireNonNull(bankPort.getUserCI(userCIRequest).block()).getUserCI();
    }


    public AuthenticationResult validUser(String number, String type, String phoneSerialNumber) {
        User user = userRepository.findByPhoneSerialNumber(phoneSerialNumber);

        if (user == null) return AuthenticationResult.failure();

        //핀 일 경우
        if (type.equals("PIN")) {
            String hashedPinNumber = PasswordUtils.encodePassword(number);
            if (user.getHashedPinNumber().equals(hashedPinNumber)) {
                return AuthenticationResult.success(user);
            }
        } else if (type.equals("PATTERN")) {

            System.out.println("비교할 원본 암호 : " + number);


            String hashedPatternNumber = PasswordUtils.encodePassword(number);

            System.out.println("바뀐 암호 : " + hashedPatternNumber);

            System.out.println("패턴" + " " + hashedPatternNumber +"원래 유저거 > "+ user.getHashedPatternNumber()) ;
            if (user.getHashedPatternNumber().equals(hashedPatternNumber)) {
                return AuthenticationResult.success(user);
            }
        }
        return AuthenticationResult.failure();

    }

    public AuthenticationResult authenticateWithBiometric(String deviceId) {
        try {
            User user = userRepository.findByPhoneSerialNumber(deviceId);
            if (user != null) {
                return AuthenticationResult.success(user);
            }
        } catch (Exception e) {
            // 로깅 등 예외 처리
        }
        return AuthenticationResult.failure();
    }

    public String generateAccessToken(UserId userId) {
        return jwtProvider.generateAccessToken(userId);
    }
}
