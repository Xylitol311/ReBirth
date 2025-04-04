package com.kkulmoo.rebirth.user.infrastrucutre.repository;

import org.springframework.stereotype.Repository;

import com.kkulmoo.rebirth.common.exception.UserNotFoundException;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.mapper.UserEntityMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
	private final UserJpaRepository userJpaRepository;
	private final UserEntityMapper userEntityMapper;

	@Override
	public User findByUserId(UserId userId) {
		UserEntity userEntity = userJpaRepository.findById(userId.getValue())
			.orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId.getValue()));
		return userEntityMapper.toUser(userEntity);
	}

	@Override
	public boolean update(User user) {
		UserEntity userEntity = userEntityMapper.toEntity(user);
		userJpaRepository.save(userEntity);
		return true;
	}

	@Override
	public User save(User user) {
		System.out.println(user.getUserName());
		UserEntity userEntity = userEntityMapper.toEntity(user);
		UserEntity savedEntity = userJpaRepository.save(userEntity);
		return userEntityMapper.toUser(savedEntity);
	}


	@Override
	public User findByPhoneSerialNumber(String phoneSerialNumber) {
		UserEntity userEntity = userJpaRepository.findByPhoneSerialNumber(phoneSerialNumber)
				.orElseThrow(() -> new UserNotFoundException("핸드폰시리얼 넘버를 찾을 수 없습니다. ID: " + phoneSerialNumber));
		return userEntityMapper.toUser(userEntity);
	}


}
