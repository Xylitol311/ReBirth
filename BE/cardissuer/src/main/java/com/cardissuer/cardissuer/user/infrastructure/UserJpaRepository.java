package com.cardissuer.cardissuer.user.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {
	Optional<UserEntity> findByUserApiKey(String userApiKey);
}