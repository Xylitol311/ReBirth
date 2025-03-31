package com.kkulmoo.rebirth.analysis.application.service;

import com.kkulmoo.rebirth.analysis.application.scheduler.MonthlyTransactionScheduler;
import com.kkulmoo.rebirth.analysis.domain.dto.response.CardCategoryDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCardDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportWithPatternDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.*;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.*;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardsJpaRepository;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MonthlyTransactionSummaryJpaRepository monthlyTransactionSummaryJpaRepository;
    private final TransactionsJpaRepository transactionsJpaRepository;
    private final ReportCardsJpaRepository reportCardsJpaRepository;
    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
    private final CardsJpaRepository cardsJpaRepository;
    private final MonthlyTransactionScheduler monthlyTransactionScheduler;
    private final CategoryJpaRepository categoryJpaRepository;
    private final MonthlyConsumptionReportJpaRepository monthlyConsumptionReportJpaRepository;
    private final ConsumptionPatternJpaRepository consumptionPatternJpaRepository;

    @Transactional
    public void updateMonthlyTransactionSummary(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);
        List<Object[]> monthlyTransactions = transactionsJpaRepository.getMonthlySpendingByCategoryAndCard(user.getUserId(), year, month);

        Map<Integer, int[]> countByCard = new HashMap<>();
        for (Object[] monthlyTransaction : monthlyTransactions) {
            ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), (Integer) monthlyTransaction[1]);
            if (reportCard == null) {
                ReportCardsEntity newReportCard = ReportCardsEntity
                        .builder()
                        .reportId(report.getReportId())
                        .cardId((Integer) monthlyTransaction[1])
                        .monthBenefitAmount(0)
                        .monthSpendingAmount(0)
                        .createdAt(now)
                        .build();
                int reportCardId = reportCardsJpaRepository.save(newReportCard).getReportCardId();
                reportCard = reportCardsJpaRepository.getById(reportCardId);


//                // 보유카드의 카드 템플릿을 확인하여, 실적구간을 고려한 혜택 템플릿을 모두 가져옴
//                CardEntity card = cardsJpaRepository.findById(monthlyTransaction[1]);
//                List<BenefitTemplateEntity> benefitTemplates = benefitTemplateJpaRepository.getByCardTemplateIdAndSpendingTier(card.getCardTemplateId, card.getSpendingTier);
//                for(BenefitTemplateEntity benefit : benefitTemplates) {
//                    ReportCardCategoriesEntity reportCardCategories = monthlyTransactionScheduler.createReportCardCategories(reportCard,benefit);
//                }
            }

            // 그냥 결제 단건 혜택만 고려
            ReportCardCategoriesEntity reportCardCategory = reportCardCategoriesJpaRepository.getByReportCardIdAndCategoryId(reportCard.getReportCardId(), (Integer) monthlyTransaction[0]);

            if (reportCardCategory == null) {
                ReportCardCategoriesEntity newReportCardCategory = ReportCardCategoriesEntity
                        .builder()
                        .reportCardId(reportCard.getReportCardId())
                        .categoryId((Integer) monthlyTransaction[0])
                        .amount(0)
                        .receivedBenefitAmount(0)
                        .count(0)
                        .createdAt(now)
                        .build();

                int reportCardCategoryId = reportCardCategoriesJpaRepository.save(newReportCardCategory).getReportCardId();
                reportCardCategory = reportCardCategoriesJpaRepository.getById(reportCardCategoryId);
            }

            reportCardCategory.setAmount((Integer) monthlyTransaction[2]); // 결제 금액
            reportCardCategory.setReceivedBenefitAmount((Integer) monthlyTransaction[3]); // 혜택 금액
            reportCardCategory.setCount((Integer) monthlyTransaction[4]); // 결제 횟수

            if (!countByCard.containsKey((Integer) monthlyTransaction[1])) {
                countByCard.put((Integer) monthlyTransaction[1], new int[]{(Integer) monthlyTransaction[2], (Integer) monthlyTransaction[3], (Integer) monthlyTransaction[4]});
            } else {
                int[] chk = countByCard.get((Integer) monthlyTransaction[1]);
                chk[0] += (Integer) monthlyTransaction[2];
                chk[1] += (Integer) monthlyTransaction[3];
                chk[2] += (Integer) monthlyTransaction[4];

                countByCard.put((Integer) monthlyTransaction[1], chk);
            }
        }

        int[] total = new int[2];
        for (Map.Entry<Integer, int[]> entry : countByCard.entrySet()) {
            int cardId = entry.getKey();
            int[] count = entry.getValue();

            ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), cardId);
            reportCard.setMonthSpendingAmount(count[0]);
            reportCard.setMonthBenefitAmount(count[1]);

            total[0] += count[0];
            total[1] += count[1];
        }

        report.setTotalSpending(total[0]);
        report.setReceivedBenefitAmount(total[1]);
    }

    @Transactional
    public void startWithMyData(UserEntity user) {
        for (int i = 0; i < 6; i++) {
            LocalDateTime now = LocalDateTime.now().minusMonths(i);
            int year = now.getYear();
            int month = now.getMonthValue();
            MonthlyTransactionSummaryEntity monthlyTransactionSummary = MonthlyTransactionSummaryEntity
                    .builder()
                    .userId(user.getUserId())
                    .year(year)
                    .month(month)
                    .totalSpending(0)
                    .receivedBenefitAmount(0)
                    .build();

            int reportId = monthlyTransactionSummaryJpaRepository.save(monthlyTransactionSummary).getReportId();
            MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getById(reportId);
            List<CardsEntity> cards = cardsJpaRepository.findByUserId(user.getUserId());
            for (CardsEntity card : cards) {

                ReportCardsEntity reportCardsEntity = ReportCardsEntity
                        .builder()
                        .cardId(card.getCardId())
                        .reportId(report.getReportId())
                        .monthSpendingAmount(0)
                        .monthBenefitAmount(0)
                        .createdAt(report.getCreatedAt())
                        .spendingTier(card.getSpendingTier())
                        .build();
                int reportCardId = reportCardsJpaRepository.save(reportCardsEntity).getReportCardId();
                ReportCardsEntity reportCard = reportCardsJpaRepository.getById(reportCardId);

            }

            List<Object[]> monthlyTransactions = transactionsJpaRepository.getMonthlySpendingByCategoryAndCard(user.getUserId(), year, month);

            Map<Integer, int[]> countByCard = new HashMap<>();
            for (Object[] monthlyTransaction : monthlyTransactions) {
                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), (Integer) monthlyTransaction[1]);
                if (reportCard == null) {
                    ReportCardsEntity newReportCard = ReportCardsEntity
                            .builder()
                            .reportId(report.getReportId())
                            .cardId((Integer) monthlyTransaction[1])
                            .monthBenefitAmount(0)
                            .monthSpendingAmount(0)
                            .createdAt(now)
                            .build();
                    int reportCardId = reportCardsJpaRepository.save(newReportCard).getReportCardId();
                    reportCard = reportCardsJpaRepository.getById(reportCardId);

                }

                // 그냥 결제 단건 혜택만 고려
                ReportCardCategoriesEntity reportCardCategory = reportCardCategoriesJpaRepository.getByReportCardIdAndCategoryId(reportCard.getReportCardId(), (Integer) monthlyTransaction[0]);

                if (reportCardCategory == null) {
                    ReportCardCategoriesEntity newReportCardCategory = ReportCardCategoriesEntity
                            .builder()
                            .reportCardId(reportCard.getReportCardId())
                            .categoryId((Integer) monthlyTransaction[0])
                            .amount(0)
                            .receivedBenefitAmount(0)
                            .count(0)
                            .createdAt(now)
                            .build();

                    int reportCardCategoryId = reportCardCategoriesJpaRepository.save(newReportCardCategory).getReportCardId();
                    reportCardCategory = reportCardCategoriesJpaRepository.getById(reportCardCategoryId);
                }

                reportCardCategory.setAmount((Integer) monthlyTransaction[2]); // 결제 금액
                reportCardCategory.setReceivedBenefitAmount((Integer) monthlyTransaction[3]); // 혜택 금액
                reportCardCategory.setCount((Integer) monthlyTransaction[4]); // 결제 횟수

                if (!countByCard.containsKey((Integer) monthlyTransaction[1])) {
                    countByCard.put((Integer) monthlyTransaction[1], new int[]{(Integer) monthlyTransaction[2], (Integer) monthlyTransaction[3], (Integer) monthlyTransaction[4]});
                } else {
                    int[] chk = countByCard.get((Integer) monthlyTransaction[1]);
                    chk[0] += (Integer) monthlyTransaction[2];
                    chk[1] += (Integer) monthlyTransaction[3];
                    chk[2] += (Integer) monthlyTransaction[4];

                    countByCard.put((Integer) monthlyTransaction[1], chk);
                }
            }

            int[] total = new int[2];
            for (Map.Entry<Integer, int[]> entry : countByCard.entrySet()) {
                int cardId = entry.getKey();
                int[] count = entry.getValue();

                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), cardId);
                reportCard.setMonthSpendingAmount(count[0]);
                reportCard.setMonthBenefitAmount(count[1]);

                total[0] += count[0];
                total[1] += count[1];
            }

            report.setTotalSpending(total[0]);
            report.setReceivedBenefitAmount(total[1]);
        }

        // 월별 요약
        for (int i = 1; i < 6; i++) {
            LocalDateTime now = LocalDateTime.now().minusMonths(i);
            int year = now.getYear();
            int month = now.getMonthValue();
            MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);

            // 소비패턴 계산
            int[] pattern = calculateSpendingPattern(user, now);

            // AI 요약
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .baseUrl("http://langchain4j.dev/demo/openai/v1")
                    .apiKey("demo")
                    .modelName("gpt-4o-mini")
                    .build();

            String question = "다음은 " + user.getUserName() + "님의 한달간 소비 내역이야. 요약해줘.\n";

            List<ReportCategoryDTO> spendingByCategory = getTotalSpendingByCategory(user, year, month);
            for (ReportCategoryDTO row : spendingByCategory) {
                String category = row.getCategory();
                question = question.concat(category + "카테고리 지출 : " + row.getAmount() + "원\n");
            }
            question = question.concat("과소비성향 : " + pattern[0] + "\n");
            question = question.concat("소비변동성 : " + pattern[1] + "\n");
            question = question.concat("소비외향성 : " + pattern[2] + "\n");
            question = question.concat("hint: 과소비 성향은 수입 대비 소비정도를, 소비 변동성은 직전달 대비 소비의 변동성, 소비 외향성은 소비카테고리 기준 외향적 소비 비율을 의미해.");


            String answer = model.chat(question);
            String consumptionPatternId = "";
            if (pattern[2] > 0.5) consumptionPatternId = consumptionPatternId.concat("E"); // 외향형
            else consumptionPatternId = consumptionPatternId.concat("I"); // 내향헝
            if (pattern[0] > 0.5) consumptionPatternId = consumptionPatternId.concat("O"); // 과소비형
            else consumptionPatternId = consumptionPatternId.concat("C"); // 절약형
            if (pattern[1] > 0.5) consumptionPatternId = consumptionPatternId.concat("V"); // 변동형
            else consumptionPatternId = consumptionPatternId.concat("S"); // 일관형
            MonthlyConsumptionReportEntity monthlyConsumptionReport = MonthlyConsumptionReportEntity
                    .builder()
                    .reportId(report.getReportId())
                    .consumptionPatternId(consumptionPatternId)
                    .overConsumption(pattern[0])
                    .variation(pattern[1])
                    .extrovert(pattern[2])
                    .reportDescription(answer)
                    .build();

            monthlyConsumptionReportJpaRepository.save(monthlyConsumptionReport);
        }

    }

    @Transactional
    public int[] calculateSpendingPattern(UserEntity user, LocalDateTime now) {
        // 필요한 정보 - 월평균 수입, 월 총 지출, 카테고리별 지출, 전 월 지출
        int monthlyIncome = user.getAverageMonthlyIncome();
        int year = now.getYear();
        int month = now.getMonthValue();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);
        // 전 월 총지출 가져오기
        LocalDateTime lastMonth = now.minusMonths(1);

        int preYear = lastMonth.getYear();
        int preMonth = lastMonth.getMonthValue();
        MonthlyTransactionSummaryEntity preReport = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), preYear, preMonth);

        // 과소비 계산
        int overConsumption = Math.min(100, 50 * report.getTotalSpending() / monthlyIncome);
        // 변동성 계산
        int variation = Math.abs((preReport.getTotalSpending() - report.getTotalSpending()) / (report.getTotalSpending()));
        variation = Math.min(100, variation);

        // 외향성 계산
        Map<String, Integer> extrovertCategories = new HashMap<>();
        extrovertCategories.put("공항라운지", 1);
        extrovertCategories.put("항공", 1);
        extrovertCategories.put("교통", 1);
        extrovertCategories.put("대형마트", 1);
        extrovertCategories.put("숙박", 1);
        extrovertCategories.put("스포츠관련", 1);
        extrovertCategories.put("여행/숙박", 1);
        extrovertCategories.put("영화관", 1);
        extrovertCategories.put("외식", 1);
        extrovertCategories.put("자동차", 1);
        extrovertCategories.put("주유소", 1);
        extrovertCategories.put("택시", 1);

        // 카테고리별 지출 가져오기
        List<ReportCategoryDTO> spendingByCategory = getTotalSpendingByCategory(user, year, month);
        int extrovertSpendAmount = 0;
        for (ReportCategoryDTO spending : spendingByCategory) {
            if (extrovertCategories.containsKey(spending.getCategory())) extrovertSpendAmount += spending.getAmount();
        }
        int extrovert = extrovertSpendAmount / report.getTotalSpending();

        int[] result = new int[3];

        result[0] = overConsumption;
        result[1] = variation;
        result[2] = extrovert;

        return result;
    }

    public List<ReportCategoryDTO> getTotalSpendingByCategory(UserEntity user, int year, int month) {
        List<ReportCategoryDTO> totalSpendingByCategory = reportCardCategoriesJpaRepository.getTotalSpendingByCategoryNameAndUser(user.getUserId(), year, month);
        return totalSpendingByCategory;
    }

    public ReportWithPatternDTO getReportWithPattern(Integer userId, int year, int month) {
        MonthlyTransactionSummaryEntity summary = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(userId, year, month);
        MonthlyConsumptionReportEntity report = monthlyConsumptionReportJpaRepository.getById(summary.getReportId());
        ConsumptionPatternEntity pattern = consumptionPatternJpaRepository.getById(report.getConsumptionPatternId());

        int preYear = year;
        int preMonth = month;
        preMonth--;
        if (preMonth == 0) {
            preMonth = 12;
            preYear--;
        }
        MonthlyTransactionSummaryEntity preSummary = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(userId, preYear, preMonth);
        int minSpending = 0;
        int maxSpending = 0;
        if (summary.getTotalSpending() >= 0 && summary.getTotalSpending() <= 500000) {
            maxSpending = 500000;
        } else if (summary.getTotalSpending() > 500001 && summary.getTotalSpending() <= 1000000) {
            minSpending = 500001;
            maxSpending = 1000000;
        } else if (summary.getTotalSpending() > 1000001 && summary.getTotalSpending() <= 1500000) {
            minSpending = 1000001;
            maxSpending = 1500000;
        } else if (summary.getTotalSpending() > 1500001 && summary.getTotalSpending() <= 2000000) {
            minSpending = 1500001;
            maxSpending = 2000000;
        } else if (summary.getTotalSpending() > 2000001 && summary.getTotalSpending() <= 2500000) {
            minSpending = 2000001;
            maxSpending = 2500000;
        } else if (summary.getTotalSpending() > 2500001 && summary.getTotalSpending() <= 3000000) {
            minSpending = 2500001;
            maxSpending = 3000000;
        } else {
            minSpending = 3000001;
            maxSpending = 2100000000;
        }

        double totalGroupBenefitAverage = monthlyTransactionSummaryJpaRepository.getGroupBenefitAverage(minSpending, maxSpending);

        ReportWithPatternDTO result = ReportWithPatternDTO
                .builder()
                .totalSpendingAmount(summary.getTotalSpending())
                .totalBenefitAmount(summary.getReceivedBenefitAmount())
                .totalGroupBenefitAverage((int) totalGroupBenefitAverage)
                .preTotalSpendingAmount(preSummary.getTotalSpending())
                .reportDescription(report.getReportDescription())
                .consumptionPattern(pattern)
                .build();

        return result;
    }


    public List<ReportCardDTO> getReportCards(Integer userId, int year, int month) {
        List<ReportCardDTO> result = new ArrayList<>();
        MonthlyTransactionSummaryEntity monthlyTransactionSummary = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(userId, year, month);
        List<ReportCardsEntity> reportCards = reportCardsJpaRepository.getByReportId(monthlyTransactionSummary.getReportId());
        for (ReportCardsEntity reportCard : reportCards) {
            List<CardCategoryDTO> cardCategories = new ArrayList<>();
            int totalCount = 0;
            List<ReportCardCategoriesEntity> reportCardCategories = reportCardCategoriesJpaRepository.getByReportCardId(reportCard.getReportCardId());
            for (ReportCardCategoriesEntity reportCardCategory : reportCardCategories) {
                CategoryEntity category = categoryJpaRepository.getById(reportCardCategory.getCategoryId());
                CardCategoryDTO cardCategory = CardCategoryDTO
                        .builder()
                        .category(category.getCategoryName())
                        .amount(reportCardCategory.getAmount())
                        .benefit(reportCardCategory.getReceivedBenefitAmount())
                        .count(reportCardCategory.getCount())
                        .build();
                totalCount += reportCardCategory.getCount();
                cardCategories.add(cardCategory);
            }

            CardTemplateEntity card = cardsJpaRepository.findCardNameByCardId(reportCard.getCardId());
            ReportCardDTO reportCardDTO = ReportCardDTO
                    .builder()
                    .name(card.getCardName())
                    .totalCount(totalCount)
                    .totalAmount(reportCard.getMonthSpendingAmount())
                    .totalBenefit(reportCard.getMonthBenefitAmount())
                    .categories(cardCategories)
                    .build();
            result.add(reportCardDTO);
        }
        return result;
    }

    public List<ReportCategoryDTO> getReportCategories(Integer userId, int year, int month) {

        List<ReportCategoryDTO> reportCategories = reportCardCategoriesJpaRepository.getTotalSpendingByCategoryNameAndUser(userId, year, month);
        return reportCategories;
    }
}
