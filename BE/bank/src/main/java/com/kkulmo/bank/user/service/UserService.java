package com.kkulmo.bank.user.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kkulmo.bank.user.dto.CardIssuerRequest;
import com.kkulmo.bank.user.dto.User;
import com.kkulmo.bank.user.dto.UserDTO;
import com.kkulmo.bank.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final cardissuerAPI cardissuerAPI;

	public UserDTO createUser(UserDTO userDTO) {
		validateMonthdayBirth(userDTO.getMonthdaybirth());

		// 이름으로 사용자 찾기
		Optional<User> existingUser = userRepository.findByNameAndMonthdaybirth(
			userDTO.getName(),
			userDTO.getMonthdaybirth()
		);

		// 이미 존재하는 사용자가 있으면 그 사용자 정보 반환
		if (existingUser.isPresent()) {
			return convertToDTO(existingUser.get());
		}

		// 새 사용자 생성 (Builder 패턴 사용)
		User user = User.builder()
			.userId(UUID.randomUUID().toString())
			.name(userDTO.getName())
			.monthdaybirth(userDTO.getMonthdaybirth())
			.createdAt(userDTO.getCreatedAt() != null ? userDTO.getCreatedAt() : LocalDateTime.now())
			.build();

		// 저장 후 DTO 변환하여 반환
		User savedUser = userRepository.save(user);

		cardissuerAPI.createCardIssuerUser(CardIssuerRequest.builder()
			.userName(user.getName())
			.userCI(user.getUserId())
			.createdAt(user.getCreatedAt())
			.build());

		return convertToDTO(savedUser);
	}

	// Entity -> DTO 변환
	private UserDTO convertToDTO(User user) {
		return UserDTO.builder()
			.name(user.getName())
			.userId(user.getUserId())
			.monthdaybirth(user.getMonthdaybirth())
			.createdAt(user.getCreatedAt()) // createdAt 필드도 DTO에 포함
			.build();
	}

	public void deleteUser(String id) {
		userRepository.deleteById(id);
	}

	private void validateMonthdayBirth(String monthdaybirth) {
		if (monthdaybirth == null || monthdaybirth.length() != 6) {
			throw new IllegalArgumentException("생년월일은 6자리로 입력해야 합니다.");
		}

		// 숫자 형식 확인
		try {
			Integer.parseInt(monthdaybirth);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("생년월일은 숫자만 입력 가능합니다.");
		}
	}

	// 더 이상 사용하지 않으므로 제거 (직접 Builder 패턴 사용)
}