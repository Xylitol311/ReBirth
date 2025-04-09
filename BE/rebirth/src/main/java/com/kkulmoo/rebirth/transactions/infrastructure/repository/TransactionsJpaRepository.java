package com.kkulmoo.rebirth.transactions.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.dto.response.DailyTransactionsDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogInfoDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlySpendingByCategoryAndCardDTO;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import com.kkulmoo.rebirth.transactions.presentation.TransactionHistoryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionsJpaRepository extends JpaRepository<TransactionEntity, Integer> {


    @Query("SELECT new com.kkulmoo.rebirth.transactions.presentation.TransactionHistoryDto(" +
            "t.createdAt, " +                     // 거래일시
            "c.categoryName, " +                  // 가맹점 카테고리 이름
            "t.amount, " +                         // 거래금액
            "m.merchantName, " +                  // 가맹점 이름
            "ct.benefitAmount) " +                // 혜택금액
            "FROM TransactionEntity t " +
            "LEFT JOIN CardTransactionEntity ct ON t.transactionId = ct.transactionId " +
            "LEFT JOIN MerchantEntity m ON ct.merchantId = m.merchantId " +
            "LEFT JOIN m.subcategory s " +        // subcategory 조인
            "LEFT JOIN s.category c " +           // category 조인
            "WHERE t.userId = :userId " +
            "AND EXTRACT(YEAR FROM t.createdAt) = :year AND EXTRACT(MONTH FROM t.createdAt) = :month " +
            "AND ct.cardUniqueNumber = :cardUniqueNumber "+
            "ORDER BY t.createdAt DESC")
    Slice<TransactionHistoryDto> findTransactionsByUserIdYearMonth(
            @Param("userId") Integer userId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("cardUniqueNumber") String cardUniqueNumber,
            Pageable pageable);


    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlySpendingByCategoryAndCardDTO(c.categoryId, cd.cardId, " +
            "CAST(SUM(t.amount) AS int), " +
            "CAST(SUM(ct.benefitAmount) AS int), " +
            "CAST(COUNT(t.transactionId) AS int)) " +
            "FROM TransactionEntity t " +
            "JOIN CardTransactionEntity ct ON t.transactionId = ct.transactionId " +
            "JOIN CardEntity cd ON ct.cardUniqueNumber = cd.cardUniqueNumber " +
            "JOIN MerchantEntity m ON ct.merchantId = m.merchantId " +
            "JOIN SubcategoryEntity sc ON m.subcategory.subcategoryId = sc.subcategoryId " +
            "JOIN CategoryEntity c ON sc.category.categoryId = c.categoryId " +
            "WHERE t.userId = :userId " +
            "AND YEAR(t.createdAt) = :year " +
            "AND MONTH(t.createdAt) = :month " +
            "AND ct.status = 'APPROVED' " +
            "GROUP BY c.categoryId, cd.cardId")
    List<MonthlySpendingByCategoryAndCardDTO> getMonthlySpendingByCategoryAndCard(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month);

    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogDTO( " +
            "DAY(t.createdAt), " +
            "CAST(COALESCE(SUM(CASE WHEN ct.transactionId IS NOT NULL THEN t.amount ELSE 0 END), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN bt.transactionId IS NOT NULL THEN t.amount ELSE 0 END), 0) AS int)) " +
            "FROM TransactionEntity t " +
            "LEFT JOIN CardTransactionEntity ct ON t.transactionId = ct.transactionId " +
            "LEFT JOIN BankTransactionEntity bt ON t.transactionId = bt.transactionId AND bt.bankTransactionType = 'DEPOSIT' " +
            "WHERE YEAR(t.createdAt) = :year " +
            "AND MONTH(t.createdAt) = :month " +
            "AND t.userId = :userId " +
            "GROUP BY DAY(t.createdAt)")
    List<MonthlyLogDTO> getMonthlyLogs(@Param("userId") int userId, @Param("year") int year, @Param("month") int month);


    @Query("""
            SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.DailyTransactionsDTO(
                t.createdAt,
                c.categoryName,
                m.merchantName,
                t.amount,
                cardTemplate.cardName
                )
                FROM TransactionEntity t
                JOIN CardTransactionEntity ct ON t.transactionId = ct.transactionId
                JOIN CardEntity card ON ct.cardUniqueNumber = card.cardUniqueNumber
                JOIN MerchantEntity m ON ct.merchantId = m.merchantId
                JOIN SubcategoryEntity sc ON m.subcategory.subcategoryId = sc.subcategoryId
                JOIN CategoryEntity c ON sc.category.categoryId = c.categoryId
                JOIN CardTemplateEntity cardTemplate ON card.cardTemplateId = cardTemplate.cardTemplateId
                WHERE t.userId = :userId
                AND YEAR(t.createdAt) = :year
                AND MONTH(t.createdAt) = :month
                AND ct.status = 'APPROVED'
                ORDER BY t.createdAt DESC
            """)
    List<DailyTransactionsDTO> getMonthlyTransactions(int userId, int year, int month);


    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogInfoDTO(" +
            "COALESCE(SUM(t.amount), 0), " +  // 1. 당월 총 소비 금액
            "(" +
            "SELECT c.categoryName " +  // 2. 가장 많이 소비한 카테고리
            "FROM TransactionEntity t2 " +
            "JOIN CardTransactionEntity ct2 ON t2.transactionId = ct2.transactionId " +
            "JOIN MerchantEntity m2 ON ct2.merchantId = m2.merchantId " +
            "JOIN SubcategoryEntity sc2 ON m2.subcategory.subcategoryId = sc2.subcategoryId " +
            "JOIN CategoryEntity c ON sc2.category.categoryId = c.categoryId " +
            "WHERE t2.userId = :userId " +
            "AND t2.createdAt BETWEEN :startDateTime AND :endDateTime " +
            "AND ct2.status = 'APPROVED' " +
            "GROUP BY c.categoryId " +
            "ORDER BY SUM(t2.amount) ASC " +
            "LIMIT 1 " +
            "), " +
            "(" +
            "COALESCE(" +
            "(SELECT SUM(t3.amount) " +  // 3. 이전 월 총 소비 금액
            "FROM TransactionEntity t3 " +
            "JOIN CardTransactionEntity ct3 ON t3.transactionId = ct3.transactionId " +
            "WHERE t3.userId = :userId " +
            "AND t3.createdAt BETWEEN :prevStartDateTime AND :prevEndDateTime " +
            "AND ct3.status = 'APPROVED' " +
            "), 0) - COALESCE(SUM(t.amount), 0) " +  // 소비 차이
            ")" +
            ") " +
            "FROM TransactionEntity t " +
            "JOIN CardTransactionEntity ct ON t.transactionId = ct.transactionId " +
            "WHERE t.userId = :userId " +
            "AND t.createdAt BETWEEN :startDateTime AND :endDateTime " +
            "AND ct.status = 'APPROVED'"
    )
    MonthlyLogInfoDTO getMonthlyLogInfo(int userId, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime prevStartDateTime, LocalDateTime prevEndDateTime);
}
