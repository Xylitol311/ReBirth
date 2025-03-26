package com.cardissuer.cardissuer.user.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
	private final UserEntityMapper userEntityMapper;
	private final UserJpaRepository userJpaRepository;

	@Override
	public Optional<User> findByUserApiKey(String userApiKey) {
		return userJpaRepository.findByUserApiKey(userApiKey)
			.map(userEntityMapper::toDomain);
	}

	@Override
	public User save(User user) {
		UserEntity userEntity = userEntityMapper.toEntity(user);
		UserEntity savedEntity = userJpaRepository.save(userEntity);
		return userEntityMapper.toDomain(savedEntity);
	}
}