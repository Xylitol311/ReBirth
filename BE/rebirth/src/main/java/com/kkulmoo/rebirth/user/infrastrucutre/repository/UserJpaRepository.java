package com.kkulmoo.rebirth.user.infrastrucutre.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    @Query("SELECT u " +
            "FROM UserEntity u " +
            "WHERE u.deletedAt is null")
    List<UserEntity> findAllAndDeletedAtIsNull();
    Optional<UserEntity> findFirstByPhoneSerialNumberOrderByUserIdDesc(String phoneSerialNumber);
    Optional<UserEntity> findByPhoneSerialNumber(String phoneSerialNumber);

    Optional<UserEntity> findByUserId(Integer userId);

    Optional<UserEntity> findByPhoneSerialNumberAndHashedPinNumber(String phoneSerialNumber, String hashedPinNumber);

}
