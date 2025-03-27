package com.kkulmo.bank.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {
	// 특정 사용자의 계좌 목록 조회
	List<AccountEntity> findByUserId(String userId);
	Optional<AccountEntity> findByUserIdAndAccountNumber(String userId, String accountNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM AccountEntity a WHERE a.userId = :userId AND a.accountNumber = :accountNumber")
	Optional<AccountEntity> findByUserIdAndAccountNumberWithPessimisticLock(
		@Param("userId") String userId,
		@Param("accountNumber") String accountNumber);
}
