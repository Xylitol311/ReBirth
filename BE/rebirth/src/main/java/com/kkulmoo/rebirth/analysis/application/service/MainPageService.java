package com.kkulmoo.rebirth.analysis.application.service;

import com.kkulmoo.rebirth.analysis.domain.dto.response.MainCardSummaryDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MainSummaryDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.PreBenefitDto;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.MonthlyTransactionSummaryEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.MonthlyTransactionSummaryJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardCategoriesJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.payment.domain.repository.PreBenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final MonthlyTransactionSummaryJpaRepository monthlyTransactionSummaryJpaRepository;
    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
    private final ReportCardsJpaRepository reportCardsJpaRepository;
    private final PreBenefitRepository preBenefitRepository;

    public MainSummaryDTO getSummary(Integer userId) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        MonthlyTransactionSummaryEntity monthlyTransactionSummary =
                monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(userId, year, month);

        List<ReportCategoryDTO> reportCategoryList =
                reportCardCategoriesJpaRepository.getTotalSpendingByCategoryNameAndUser(userId, year, month);

        // 안전한 정렬 (Stream 활용)
        reportCategoryList.sort(Comparator
                .comparingInt(ReportCategoryDTO::getBenefit).reversed()
                .thenComparingInt(ReportCategoryDTO::getAmount));

        // 상위 3개 항목 추출 (Stream 활용)
        List<ReportCategoryDTO> goodList = reportCategoryList.stream()
                .filter(reportCategory -> reportCategory.getBenefit() > 0)
                .limit(3)
                .collect(Collectors.toList());

        List<ReportCategoryDTO> badList = reportCategoryList.stream()
                .filter(reportCategory -> reportCategory.getBenefit() == 0)
                .limit(3)
                .collect(Collectors.toList());

        // NPE 방지 (null 체크 후 기본값 설정)
        MainSummaryDTO mainSummary = MainSummaryDTO
                .builder()
                .totalSpendingAmount(monthlyTransactionSummary != null ? Math.abs(monthlyTransactionSummary.getTotalSpending()) : 0)
                .totalBenefitAmount(monthlyTransactionSummary != null ? monthlyTransactionSummary.getReceivedBenefitAmount() : 0)
                .goodList(goodList)
                .badList(badList)
                .build();

        return mainSummary;
    }

    public List<MainCardSummaryDTO> getCardSummary(Integer userId) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        return reportCardsJpaRepository.getByUserIdAndYearAndMonth(userId, year, month);
    }

    public List<ReportCategoryDTO> getCategorySummary(Integer userId) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        return reportCardCategoriesJpaRepository.getTotalSpendingByCategoryNameAndUser(userId, year, month);
    }

    public PreBenefitDto getPreBenefit(Integer userId){
        preBenefitRepository.findByUserId(userId);

        return null;
    }
}
