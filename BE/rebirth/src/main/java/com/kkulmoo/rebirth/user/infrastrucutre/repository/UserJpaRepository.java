package com.kkulmoo.rebirth.user.infrastrucutre.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

}
