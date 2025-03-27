package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.TransactionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsJpaRepository extends JpaRepository<TransactionsEntity, Integer> {

    @Query("SELECT c.categoryId, cd.cardId, " +
            "SUM(t.amount) AS totalSpending, " +
            "SUM(ct.benefitAmount) AS totalBenefit, " +
            "COUNT(t.transactionId) AS transactionCount " +
            "FROM TransactionsEntity t " +
            "JOIN CardTransactionsEntity ct ON t.transactionId = ct.transactionId " +
            "JOIN Cards cd ON ct.cardUniqueNumber = cd.cardUniqueNumber " +
            "JOIN Merchant m ON ct.merchantId = m.merchantId " +
            "JOIN Subcategory sc ON m.subcategoryId = sc.subcategoryId " +
            "JOIN Category c ON sc.categoryId = c.categoryId " +
            "WHERE t.userId = :userId " +
            "AND FUNCTION('YEAR', t.createdAt) = :year " +
            "AND FUNCTION('MONTH', t.createdAt) = :month " +
            "AND ct.status = '승인' " +
            "GROUP BY c.categoryId, cd.cardId")
    List<Object[]> getMonthlySpendingByCategoryAndCard(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month);
}
