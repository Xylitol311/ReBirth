package com.cardissuer.cardissuer.user;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	/**
	 * 새로운 사용자를 생성합니다.
	 *
	 * @param user 생성할 사용자 정보
	 * @return 생성된 사용자 정보
	 */
	@Transactional
	public User createUser(User user) {
		// 필요한 경우 비즈니스 검증 로직을 여기에 추가할 수 있습니다
		// 예: 중복 사용자 체크, 데이터 유효성 검사 등

		return userRepository.save(user);
	}
}