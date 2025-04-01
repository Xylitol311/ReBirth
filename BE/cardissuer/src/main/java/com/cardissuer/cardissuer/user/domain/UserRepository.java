package com.cardissuer.cardissuer.user.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.infrastructure.UserEntity;

public interface UserRepository {
	Optional<User> findByUserCI(String userCI);
	User save(User user);
	boolean existsByUserCI(String userCI);

}
