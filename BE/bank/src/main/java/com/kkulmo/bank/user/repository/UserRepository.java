package com.kkulmo.bank.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkulmo.bank.user.dto.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByName(String name);
	Optional<User> findByNameAndMonthdaybirth (String name,String monthdayBirth);
}
