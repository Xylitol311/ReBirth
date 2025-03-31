package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.DailyTransactionsDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogInfoDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.TransactionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionsJpaRepository extends JpaRepository<TransactionsEntity, Integer> {

    @Query("SELECT c.categoryId, cd.cardId, " +
            "SUM(t.amount) AS totalSpending, " +
            "SUM(ct.benefitAmount) AS totalBenefit, " +
            "COUNT(t.transactionId) AS transactionCount " +
            "FROM TransactionsEntity t " +
            "JOIN CardTransactionsEntity ct ON t.transactionId = ct.transactionId " +
            "JOIN CardsEntity cd ON ct.cardUniqueNumber = cd.cardUniqueNumber " +
            "JOIN MerchantEntity m ON ct.merchantId = m.merchantId " +
            "JOIN SubcategoryEntity sc ON m.subcategory.subcategoryId = sc.subcategoryId " +
            "JOIN CategoryEntity c ON sc.category.categoryId = c.categoryId " +
            "WHERE t.userId = :userId " +
            "AND FUNCTION('YEAR', t.createdAt) = :year " +
            "AND FUNCTION('MONTH', t.createdAt) = :month " +
            "AND ct.status = '승인' " +
            "GROUP BY c.categoryId, cd.cardId")
    List<Object[]> getMonthlySpendingByCategoryAndCard(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month);

    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogDTO( " +
            "DAY(t.createdAt), " +
            "CAST(COALESCE(SUM(CASE WHEN ct.transactionId IS NOT NULL THEN t.amount ELSE 0 END), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN bt.transactionId IS NOT NULL THEN t.amount ELSE 0 END), 0) AS int)) " +
            "FROM TransactionsEntity t " +
            "LEFT JOIN CardTransactionsEntity ct ON t.transactionId = ct.transactionId " +
            "LEFT JOIN BankTransactionsEntity bt ON t.transactionId = bt.transactionId AND bt.transactionType = 'DEPOSIT' " +
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
        FROM TransactionsEntity t
        JOIN CardTransactionsEntity ct ON t.transactionId = ct.transactionId
        JOIN CardsEntity card ON ct.cardUniqueNumber = card.cardUniqueNumber
        JOIN MerchantEntity m ON ct.merchantId = m.merchantId
        JOIN SubcategoryEntity sc ON m.subcategory.subcategoryId = sc.subcategoryId
        JOIN CategoryEntity c ON sc.category.categoryId = c.categoryId
        JOIN CardTemplateEntity cardTemplate ON card.cardTemplateId = cardTemplate.cardTemplateId
        WHERE t.userId = :userId
        AND YEAR(t.createdAt) = :year
        AND MONTH(t.createdAt) = :month
        AND DAY(t.createdAt) = :day
        AND ct.status = '승인'
        ORDER BY t.createdAt DESC
    """)
    List<DailyTransactionsDTO> getDailyTransactions(int userId, int year, int month, int day);


    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogInfoDTO(" +
            "COALESCE(SUM(t.amount), 0), " +  // 1. 당월 총 소비 금액
            "(" +
            "SELECT c.categoryName " +  // 2. 가장 많이 소비한 카테고리
            "FROM TransactionsEntity t2 " +
            "JOIN CardTransactionsEntity ct2 ON t2.transactionId = ct2.transactionId " +
            "JOIN MerchantEntity m2 ON ct2.merchantId = m2.merchantId " +
            "JOIN SubcategoryEntity sc2 ON m2.subcategory.subcategoryId = sc2.subcategoryId " +
            "JOIN CategoryEntity c ON sc2.category.categoryId = c.categoryId " +
            "WHERE t2.userId = :userId " +
            "AND t2.createdAt BETWEEN :startDateTime AND :endDateTime " +
            "AND ct2.status = '승인' " +
            "GROUP BY c.categoryId " +
            "ORDER BY SUM(t2.amount) DESC " +
            "LIMIT 1 " +
            "), " +
            "(" +
            "COALESCE(" +
            "(SELECT SUM(t3.amount) " +  // 3. 이전 월 총 소비 금액
            "FROM TransactionsEntity t3 " +
            "JOIN CardTransactionsEntity ct3 ON t3.transactionId = ct3.transactionId " +
            "WHERE t3.userId = :userId " +
            "AND t3.createdAt BETWEEN :prevStartDateTime AND :prevEndDateTime " +
            "AND ct3.status = '승인' " +
            "), 0) - COALESCE(SUM(t.amount), 0) " +  // 소비 차이
            ")" +
            ") " +
            "FROM TransactionsEntity t " +
            "JOIN CardTransactionsEntity ct ON t.transactionId = ct.transactionId " +
            "WHERE t.userId = :userId " +
            "AND t.createdAt BETWEEN :startDateTime AND :endDateTime " +
            "AND ct.status = '승인'"
    )
    MonthlyLogInfoDTO getMonthlyLogInfo(int userId, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime prevStartDateTime, LocalDateTime prevEndDateTime);
}
