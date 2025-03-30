package com.kkulmoo.rebirth.user.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkulmoo.rebirth.common.exception.UserAlreadyDeletedException;
import com.kkulmoo.rebirth.common.exception.UserDeletionException;
import com.kkulmoo.rebirth.common.exception.UserNotFoundException;
import com.kkulmoo.rebirth.user.application.eventDTO.MyDataEvent;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final ApplicationEventPublisher eventPublisher;
	private final UserRepository userRepository;

	@Transactional
	public void loadMyData(Integer userId) {
		User user = userRepository.findByUserId(new UserId(userId));
		eventPublisher.publishEvent(new MyDataEvent(user));



	}

	public void deleteUser(Integer userId) {
		UserId userIdObj = new UserId(userId);

		// 사용자 조회
		User user = userRepository.findByUserId(userIdObj);

		// 사용자가 존재하지 않는 경우 예외 처리
		if (user == null) {
			throw new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
		}

		// 이미 삭제된 사용자인지 확인
		if (user.isDeleted()) {
			throw new UserAlreadyDeletedException("이미 삭제된 사용자입니다. ID: " + userId);
		}

		// 사용자에 삭제 표시
		user.delete();

		// 사용자 정보 업데이트
		boolean isUpdated = userRepository.update(user);

		// 업데이트 실패 시 예외 AuthService처리
		if (!isUpdated) {
			throw new UserDeletionException("사용자 삭제에 실패하였습니다. ID: " + userId);
		}
	}
}
