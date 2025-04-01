package com.kkulmoo.rebirth.analysis.application.service;

import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.DailyTransactionsDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogInfoDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.TransactionsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetLogService {


    private final TransactionsJpaRepository transactionsJpaRepository;

    public List<MonthlyLogDTO> getMonthlyLog(int userId, int year, int month) {
        List<MonthlyLogDTO> monthlyLogs = transactionsJpaRepository.getMonthlyLogs(userId, year, month);
        return monthlyLogs;
    }

    public List<DailyTransactionsDTO> getDailyTransactions(int userId, int year, int month, int day) {
        List<DailyTransactionsDTO> monthlyTransactions = transactionsJpaRepository.getDailyTransactions(userId, year, month, day);
        return monthlyTransactions;
    }

    public MonthlyLogInfoDTO getMonthlyLogInfo(int userId, int year, int month) {
        // 현재 날짜
        LocalDate today = LocalDate.now();
        boolean isCurrentMonth = (year == today.getYear() && month == today.getMonthValue());

        // 조회 기간 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = isCurrentMonth
                ? today // 현재월이면 오늘까지
                : YearMonth.of(year, month).atEndOfMonth(); // 과거월이면 해당 월의 마지막 날

        // 전월 기간 계산
        LocalDate prevStartDate = startDate.minusMonths(1); // 전월 1일
        LocalDate prevEndDate = isCurrentMonth
                ? today.minusMonths(1) // 현재월이면 1달 전 같은 날까지 비교
                : startDate.minusDays(1); // 과거월이면 전월 마지막 날

        // LocalDate → LocalDateTime 변환
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        LocalDateTime prevStartDateTime = prevStartDate.atStartOfDay();
        LocalDateTime prevEndDateTime = prevEndDate.atTime(23, 59, 59);
        MonthlyLogInfoDTO monthlyLogInfo = transactionsJpaRepository.getMonthlyLogInfo(userId, startDateTime, endDateTime, prevStartDateTime, prevEndDateTime);
        return monthlyLogInfo;
    }
}
