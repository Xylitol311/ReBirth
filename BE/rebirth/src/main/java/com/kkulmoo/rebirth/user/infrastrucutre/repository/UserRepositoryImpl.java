package com.kkulmoo.rebirth.user.infrastrucutre.repository;

import org.springframework.stereotype.Repository;

import com.kkulmoo.rebirth.user.domain.UserRepository;
import com.kkulmoo.rebirth.user.infrastrucutre.mapper.UserEntityMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
	private final UserJpaRepository userJpaRepository;
	private final UserEntityMapper userEntityMapper;

}
