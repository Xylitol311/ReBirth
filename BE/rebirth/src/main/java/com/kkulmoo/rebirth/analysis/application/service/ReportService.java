package com.kkulmoo.rebirth.analysis.application.service;

import com.kkulmoo.rebirth.analysis.application.scheduler.MonthlyTransactionScheduler;
import com.kkulmoo.rebirth.analysis.domain.dto.response.*;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.*;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.*;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateJpaRepository;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardsJpaRepository;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.CategoryEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.repository.TransactionsJpaRepository;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.repository.UserJpaRepository;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private final UserJpaRepository userJpaRepository;
    private final CardTemplateJpaRepository cardTemplateJpaRepository;

    @Transactional
    public void updateMonthlyTransactionSummary(Integer userId) {
        UserEntity user = userJpaRepository.getReferenceById(userId);
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);
        List<MonthlySpendingByCategoryAndCardDTO> monthlyTransactions = transactionsJpaRepository.getMonthlySpendingByCategoryAndCard(user.getUserId(), year, month);

        Map<Integer, int[]> countByCard = new HashMap<>();
        for (MonthlySpendingByCategoryAndCardDTO monthlyTransaction : monthlyTransactions) {
            ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), monthlyTransaction.getCardId());
            if (reportCard == null) {
                ReportCardsEntity newReportCard = ReportCardsEntity
                        .builder()
                        .reportId(report.getReportId())
                        .cardId(monthlyTransaction.getCardId())
                        .monthBenefitAmount(0)
                        .monthSpendingAmount(0)
                        .createdAt(now)
                        .build();
                int reportCardId = reportCardsJpaRepository.save(newReportCard).getReportCardId();
                reportCard = reportCardsJpaRepository.getReferenceById(reportCardId);


            }

            // 그냥 결제 단건 혜택만 고려
            ReportCardCategoriesEntity reportCardCategory = reportCardCategoriesJpaRepository.getByReportCardAndCategoryId(reportCard, monthlyTransaction.getCategoryId());

            if (reportCardCategory == null) {
                ReportCardCategoriesEntity newReportCardCategory = ReportCardCategoriesEntity
                        .builder()
                        .reportCard(reportCard)
                        .categoryId(monthlyTransaction.getCategoryId())
                        .amount(0)
                        .receivedBenefitAmount(0)
                        .count(0)
                        .createdAt(now)
                        .build();

                int reportCardCategoryId = reportCardCategoriesJpaRepository.save(newReportCardCategory).getReportCategoryId();


                reportCardCategory = reportCardCategoriesJpaRepository.findById(reportCardCategoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Entity not found after saving: " + newReportCardCategory.getReportCard().getReportCardId()));
            }

            reportCardCategory.setAmount(monthlyTransaction.getTotalSpending()); // 결제 금액
            reportCardCategory.setReceivedBenefitAmount(monthlyTransaction.getTotalBenefit()); // 혜택 금액
            reportCardCategory.setCount(monthlyTransaction.getTransactionCount()); // 결제 횟수

            if (!countByCard.containsKey(monthlyTransaction.getCardId())) {
                countByCard.put(monthlyTransaction.getCardId(), new int[]{monthlyTransaction.getTotalSpending(), monthlyTransaction.getTotalBenefit(), monthlyTransaction.getTransactionCount()});
            } else {
                int[] chk = countByCard.get(monthlyTransaction.getCardId());
                chk[0] += monthlyTransaction.getTotalSpending();
                chk[1] += monthlyTransaction.getTotalBenefit();
                chk[2] += monthlyTransaction.getTransactionCount();

                countByCard.put(monthlyTransaction.getCardId(), chk);
            }
        }

        int[] total = new int[2];
        for (Map.Entry<Integer, int[]> entry : countByCard.entrySet()) {
            int cardId = entry.getKey();
            int[] count = entry.getValue();

            CardEntity card = cardsJpaRepository.getReferenceById(cardId);
            CardTemplateEntity cardTemplate = cardTemplateJpaRepository.getReferenceById(card.getCardTemplateId());
            short myTierForCard = 0;
            if(cardTemplate.getPerformanceRange()!=null) {
                for(int point: cardTemplate.getPerformanceRange()) {
                    if(Math.abs(count[0])<point) {
                        break;
                    }
                    myTierForCard++;
                }
            }

            ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), cardId);
            reportCard.setMonthSpendingAmount(count[0]);
            reportCard.setMonthBenefitAmount(count[1]);
            reportCard.setSpendingTier(myTierForCard);

            reportCardsJpaRepository.save(reportCard);
            total[0] += count[0];
            total[1] += count[1];
        }

        report.setTotalSpending(total[0]);
        report.setReceivedBenefitAmount(total[1]);
        monthlyTransactionSummaryJpaRepository.save(report);
    }

    @Transactional
    public void startWithMyData(Integer userId) {
        UserEntity user = userJpaRepository.getReferenceById(userId);
        for (int i = 5; i >= 0; i--) {
            LocalDateTime now = LocalDateTime.now().minusMonths(i);
            int year = now.getYear();
            int month = now.getMonthValue();
            MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);

            if (report == null) {
                MonthlyTransactionSummaryEntity monthlyTransactionSummary = MonthlyTransactionSummaryEntity
                        .builder()
                        .userId(user.getUserId())
                        .year(year)
                        .month(month)
                        .totalSpending(0)
                        .receivedBenefitAmount(0)
                        .build();

                int reportId = monthlyTransactionSummaryJpaRepository.save(monthlyTransactionSummary).getReportId();
                report = monthlyTransactionSummaryJpaRepository.getReferenceById(reportId);

            } else {
                report.setTotalSpending(0);
                report.setReceivedBenefitAmount(0);

                report = monthlyTransactionSummaryJpaRepository.save(report);
            }
            List<CardEntity> cards = cardsJpaRepository.findByUserId(user.getUserId());
            for (CardEntity card : cards) {
                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), card.getCardId());
                ReportCardsEntity reportCardsEntity = ReportCardsEntity
                        .builder()
                        .cardId(card.getCardId())
                        .reportId(report.getReportId())
                        .monthSpendingAmount(0)
                        .monthBenefitAmount(0)
                        .createdAt(report.getCreatedAt())
                        .spendingTier((short) 0)
                        .build();
                if (reportCard == null) {
                    int reportCardId = reportCardsJpaRepository.save(reportCardsEntity).getReportCardId();
                    reportCard = reportCardsJpaRepository.getReferenceById(reportCardId);

                } else {
                    reportCard.setMonthSpendingAmount(0);
                    reportCard.setMonthBenefitAmount(0);
                    reportCard.setSpendingTier((short) 0);

                    reportCard = reportCardsJpaRepository.save(reportCard);

                }

            }

            List<MonthlySpendingByCategoryAndCardDTO> monthlyTransactions = transactionsJpaRepository.getMonthlySpendingByCategoryAndCard(user.getUserId(), year, month);

            Map<Integer, int[]> countByCard = new HashMap<>();
            for (MonthlySpendingByCategoryAndCardDTO monthlyTransaction : monthlyTransactions) {
                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), monthlyTransaction.getCardId());
                if (reportCard == null) {
                    ReportCardsEntity newReportCard = ReportCardsEntity
                            .builder()
                            .reportId(report.getReportId())
                            .cardId(monthlyTransaction.getCardId())
                            .monthBenefitAmount(0)
                            .monthSpendingAmount(0)
                            .spendingTier((short) 0)
                            .createdAt(now)
                            .build();
                    int reportCardId = reportCardsJpaRepository.save(newReportCard).getReportCardId();
                    reportCard = reportCardsJpaRepository.getReferenceById(reportCardId);

                } else {
                    reportCard.setMonthSpendingAmount(0);
                    reportCard.setMonthBenefitAmount(0);
                    reportCard.setSpendingTier((short) 0);

                    reportCard = reportCardsJpaRepository.save(reportCard);
                }

                // 그냥 결제 단건 혜택만 고려
                ReportCardCategoriesEntity reportCardCategory = reportCardCategoriesJpaRepository.getByReportCardAndCategoryId(reportCard, monthlyTransaction.getCategoryId());

                if (reportCardCategory == null) {
                    ReportCardCategoriesEntity newReportCardCategory = ReportCardCategoriesEntity
                            .builder()
                            .reportCard(reportCard)
                            .categoryId(monthlyTransaction.getCategoryId())
                            .amount(0)
                            .receivedBenefitAmount(0)
                            .count(0)
                            .createdAt(now)
                            .build();

                    int reportCardCategoryId = reportCardCategoriesJpaRepository.save(newReportCardCategory).getReportCategoryId();

//                    reportCardCategory = reportCardCategoriesJpaRepository.getReferenceById(reportCardCategoryId);
                    reportCardCategory = reportCardCategoriesJpaRepository.findById(reportCardCategoryId)
                            .orElseThrow(() -> new EntityNotFoundException("Entity not found after saving: " + newReportCardCategory.getReportCard().getReportCardId()));
                } else {
                    reportCardCategory.setAmount(0);
                    reportCardCategory.setReceivedBenefitAmount(0);
                    reportCardCategory.setCount(0);

                    reportCardCategory = reportCardCategoriesJpaRepository.save(reportCardCategory);
                }

                reportCardCategory.setAmount(monthlyTransaction.getTotalSpending()); // 결제 금액
                reportCardCategory.setReceivedBenefitAmount(monthlyTransaction.getTotalBenefit()); // 혜택 금액
                reportCardCategory.setCount(monthlyTransaction.getTransactionCount()); // 결제 횟수

                if (!countByCard.containsKey(monthlyTransaction.getCardId())) {
                    countByCard.put(monthlyTransaction.getCardId(), new int[]{ monthlyTransaction.getTotalSpending(), monthlyTransaction.getTotalBenefit(), monthlyTransaction.getTransactionCount()});
                } else {
                    int[] chk = countByCard.get(monthlyTransaction.getCardId());
                    chk[0] += monthlyTransaction.getTotalSpending();
                    chk[1] += monthlyTransaction.getTotalBenefit();
                    chk[2] += monthlyTransaction.getTransactionCount();

                    countByCard.put(monthlyTransaction.getCardId(), chk);
                }
                reportCardCategoriesJpaRepository.save(reportCardCategory);
            }

            int[] total = new int[2];
            for (Map.Entry<Integer, int[]> entry : countByCard.entrySet()) {
                int cardId = entry.getKey();
                int[] count = entry.getValue();
                CardEntity card = cardsJpaRepository.getReferenceById(cardId);
                CardTemplateEntity cardTemplate = cardTemplateJpaRepository.getReferenceById(card.getCardTemplateId());
                short myTierForCard = 0;
                if(cardTemplate.getPerformanceRange()!=null) {
                    for(int point: cardTemplate.getPerformanceRange()) {
                        System.out.println(card.getCardName()+" "+count[0]+" 이거 넘나? "+point);
                        if(Math.abs(count[0])<point) {
                            break;
                        }
                        myTierForCard++;
                    }
                }
                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), cardId);
                reportCard.setMonthSpendingAmount(count[0]);
                reportCard.setMonthBenefitAmount(count[1]);
                reportCard.setSpendingTier(myTierForCard);
                reportCardsJpaRepository.save(reportCard);

                total[0] += count[0];
                total[1] += count[1];
            }

            if(i==1) {
                for(CardEntity card: cards) {
                    ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), card.getCardId());
                    CardEntity newCard = card.toBuilder().spendingTier(reportCard.getSpendingTier()).build();
                    cardsJpaRepository.save(newCard);

                }
            }

            report.setTotalSpending(total[0]);
            report.setReceivedBenefitAmount(total[1]);
            monthlyTransactionSummaryJpaRepository.save(report);
        }

        // 월별 요약(최근 두 달만)
        for(int i=2 ; i>=1 ; i--) {
            LocalDateTime now = LocalDateTime.now().minusMonths(i);
            createReport(userId,now);

        }
        LocalDateTime now = LocalDateTime.now().minusMonths(1);
        int year = now.getYear();
        int month = now.getMonthValue();

        MonthlyTransactionSummaryEntity mts = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(),year, month);
        MonthlyConsumptionReportEntity mcr = monthlyConsumptionReportJpaRepository.getByReport(mts);
        UserEntity nowUser = userJpaRepository.getReferenceById(user.getUserId());
        nowUser.setConsumptionPatternId(mcr.getConsumptionPatternId());
        nowUser = userJpaRepository.save(nowUser);
    }

    @Transactional
    public void reportWithMyDataAfterStart(Integer userId) {
        // 월별 요약
        for(int i=5 ; i>=1 ; i--) {
            LocalDateTime now = LocalDateTime.now().minusMonths(i);
            createReport(userId,now);
            updateReport(userId,now);
        }
    }

    @Transactional
    public void createReport(Integer userId, LocalDateTime now) {
        UserEntity user = userJpaRepository.getReferenceById(userId);
        int year = now.getYear();
        int month = now.getMonthValue();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);

        // 소비패턴 계산
        int[] pattern = calculateSpendingPattern(user, now);

        String answer = "";
        String consumptionPatternId = "";
        if (pattern[2] > 50) consumptionPatternId = consumptionPatternId.concat("E"); // 외향형
        else consumptionPatternId = consumptionPatternId.concat("I"); // 내향헝
        if (pattern[0] > 50) consumptionPatternId = consumptionPatternId.concat("B"); // 과소비형
        else consumptionPatternId = consumptionPatternId.concat("M"); // 절약형
        if (pattern[1] > 50) consumptionPatternId = consumptionPatternId.concat("V"); // 변동형
        else consumptionPatternId = consumptionPatternId.concat("S"); // 일관형
        MonthlyConsumptionReportEntity mcr = monthlyConsumptionReportJpaRepository.getByReport(report);
        System.out.println(mcr);
        if (mcr == null) {

            MonthlyConsumptionReportEntity monthlyConsumptionReport = MonthlyConsumptionReportEntity
                    .builder()
                    .report(report)
                    .consumptionPatternId(consumptionPatternId)
                    .overConsumption(pattern[0])
                    .variation(pattern[1])
                    .extrovert(pattern[2])
                    .reportDescription(answer)
                    .build();

            monthlyConsumptionReportJpaRepository.save(monthlyConsumptionReport);
        } else {
            mcr.setReport(report);
            mcr.setConsumptionPatternId(consumptionPatternId);
            mcr.setOverConsumption(pattern[0]);
            mcr.setVariation(pattern[1]);
            mcr.setExtrovert(pattern[2]);
            mcr.setReportDescription(answer);

            mcr = monthlyConsumptionReportJpaRepository.save(mcr);
        }
    }

    @Transactional
    public void updateReport(Integer userId, LocalDateTime now) {
        // GPT 답변 생성
        UserEntity user = userJpaRepository.getReferenceById(userId);
        int year = now.getYear();
        int month = now.getMonthValue();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);
        MonthlyConsumptionReportEntity mcr = monthlyConsumptionReportJpaRepository.getByReport(report);
        int[] pattern = new int[] { mcr.getOverConsumption() , mcr.getVariation(), mcr.getExtrovert() };

        // AI 요약
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                // .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();

        String question = "다음은 " + user.getUserName() + "님의 한달간 소비 내역이야. 요약해줘. 감성적인 말투로 부탁해.\n";

        List<ReportCategoryDTO> spendingByCategory = getTotalSpendingByCategory(user, year, month);
        for (ReportCategoryDTO row : spendingByCategory) {
            String category = row.getCategory();
            question = question.concat(category + "카테고리 지출 : " + row.getAmount() + "원\n");
        }
        question = question.concat("과소비성향 : " + pattern[0] + "\n");
        question = question.concat("소비변동성 : " + pattern[1] + "\n");
        question = question.concat("소비외향성 : " + pattern[2] + "\n");
        question = question.concat("hint: 과소비 성향은 수입 대비 소비정도를, 소비 변동성은 직전달 대비 소비의 변동성, 소비 외향성은 소비카테고리 기준 외향적 소비 비율을 의미해. 모든 값은 50을 기준으로 생각하고 평가해줘");


        String answer = model.chat(question);
        System.out.println(mcr);
        mcr.setReportDescription(answer);

        mcr = monthlyConsumptionReportJpaRepository.save(mcr);

    }

    @Transactional
    public void updateWithMyData(Integer userId) {
        UserEntity user = userJpaRepository.getReferenceById(userId);
//        List<MonthlyTransactionSummaryEntity> mtsList = monthlyTransactionSummaryJpaRepository.findByUserId(userId);
//        mtsList.sort(Comparator
//                .comparing(MonthlyTransactionSummaryEntity::getYear)
//                .thenComparing(MonthlyTransactionSummaryEntity::getMonth));
//        for (MonthlyTransactionSummaryEntity report : mtsList) {
        for (int i = 3; i>=0 ; i--) {
            LocalDateTime now = LocalDateTime.now().minusMonths(i);
            int year = now.getYear();
            int month = now.getMonthValue();
//            int year = report.getYear();
//            int month = report.getMonth();
            MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(userId, year, month);
            report.setTotalSpending(0);
            report.setReceivedBenefitAmount(0);

            report = monthlyTransactionSummaryJpaRepository.save(report);

            List<MonthlySpendingByCategoryAndCardDTO> monthlyTransactions = transactionsJpaRepository.getMonthlySpendingByCategoryAndCard(user.getUserId(), year, month);

            Map<Integer, int[]> countByCard = new HashMap<>();
            for (MonthlySpendingByCategoryAndCardDTO monthlyTransaction : monthlyTransactions) {
                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), monthlyTransaction.getCardId());
                if (reportCard == null) {
                    ReportCardsEntity newReportCard = ReportCardsEntity
                            .builder()
                            .reportId(report.getReportId())
                            .cardId(monthlyTransaction.getCardId())
                            .monthBenefitAmount(0)
                            .monthSpendingAmount(0)
                            .spendingTier((short) 0)
                            .createdAt(LocalDateTime.now())
                            .build();
                    int reportCardId = reportCardsJpaRepository.save(newReportCard).getReportCardId();
                    reportCard = reportCardsJpaRepository.getReferenceById(reportCardId);

                } else {
                    reportCard.setMonthSpendingAmount(0);
                    reportCard.setMonthBenefitAmount(0);
                    reportCard.setSpendingTier((short) 0);

                    reportCard = reportCardsJpaRepository.save(reportCard);
                }

                // 그냥 결제 단건 혜택만 고려
                ReportCardCategoriesEntity reportCardCategory = reportCardCategoriesJpaRepository.getByReportCardAndCategoryId(reportCard, monthlyTransaction.getCategoryId());

                if (reportCardCategory == null) {
                    ReportCardCategoriesEntity newReportCardCategory = ReportCardCategoriesEntity
                            .builder()
                            .reportCard(reportCard)
                            .categoryId(monthlyTransaction.getCategoryId())
                            .amount(0)
                            .receivedBenefitAmount(0)
                            .count(0)
                            .createdAt(LocalDateTime.now())
                            .build();

                    int reportCardCategoryId = reportCardCategoriesJpaRepository.save(newReportCardCategory).getReportCategoryId();

//                    reportCardCategory = reportCardCategoriesJpaRepository.getReferenceById(reportCardCategoryId);
                    reportCardCategory = reportCardCategoriesJpaRepository.findById(reportCardCategoryId)
                            .orElseThrow(() -> new EntityNotFoundException("Entity not found after saving: " + newReportCardCategory.getReportCard().getReportCardId()));
                } else {
                    reportCardCategory.setAmount(0);
                    reportCardCategory.setReceivedBenefitAmount(0);
                    reportCardCategory.setCount(0);

                    reportCardCategory = reportCardCategoriesJpaRepository.save(reportCardCategory);
                }

                reportCardCategory.setAmount(monthlyTransaction.getTotalSpending()); // 결제 금액
                reportCardCategory.setReceivedBenefitAmount(monthlyTransaction.getTotalBenefit()); // 혜택 금액
                reportCardCategory.setCount(monthlyTransaction.getTransactionCount()); // 결제 횟수

                if (!countByCard.containsKey(monthlyTransaction.getCardId())) {
                    countByCard.put(monthlyTransaction.getCardId(), new int[]{ monthlyTransaction.getTotalSpending(), monthlyTransaction.getTotalBenefit(), monthlyTransaction.getTransactionCount()});
                } else {
                    int[] chk = countByCard.get(monthlyTransaction.getCardId());
                    chk[0] += monthlyTransaction.getTotalSpending();
                    chk[1] += monthlyTransaction.getTotalBenefit();
                    chk[2] += monthlyTransaction.getTransactionCount();

                    countByCard.put(monthlyTransaction.getCardId(), chk);
                }
                reportCardCategoriesJpaRepository.save(reportCardCategory);
            }

            int[] total = new int[2];
            for (Map.Entry<Integer, int[]> entry : countByCard.entrySet()) {
                int cardId = entry.getKey();
                int[] count = entry.getValue();
                CardEntity card = cardsJpaRepository.getReferenceById(cardId);
                CardTemplateEntity cardTemplate = cardTemplateJpaRepository.getReferenceById(card.getCardTemplateId());
                short myTierForCard = 0;
                if(cardTemplate.getPerformanceRange()!=null) {
                    for(int point: cardTemplate.getPerformanceRange()) {
                        System.out.println(card.getCardName()+" "+count[0]+" 이거 넘나? "+point);
                        if(Math.abs(count[0])<point) {
                            break;
                        }
                        myTierForCard++;
                    }
                }
                ReportCardsEntity reportCard = reportCardsJpaRepository.getByReportIdAndCardId(report.getReportId(), cardId);
                reportCard.setMonthSpendingAmount(count[0]);
                reportCard.setMonthBenefitAmount(count[1]);
                reportCard.setSpendingTier(myTierForCard);

                reportCardsJpaRepository.save(reportCard);
                total[0] += count[0];
                total[1] += count[1];
            }

            report.setTotalSpending(total[0]);
            report.setReceivedBenefitAmount(total[1]);
            monthlyTransactionSummaryJpaRepository.save(report);
        }

    }

    @Transactional
    public int[] calculateSpendingPattern(UserEntity user, LocalDateTime now) {
        // 필요한 정보 - 월평균 수입, 월 총 지출, 카테고리별 지출, 전 월 지출
        int monthlyIncome = user.getAverageMonthlyIncome()+1;
        int year = now.getYear();
        int month = now.getMonthValue();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), year, month);
        // 전 월 총지출 가져오기
        LocalDateTime lastMonth = now.minusMonths(1);

        int preYear = lastMonth.getYear();
        int preMonth = lastMonth.getMonthValue();
        MonthlyTransactionSummaryEntity preReport = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), preYear, preMonth);
        if(preReport == null) {
            preReport = MonthlyTransactionSummaryEntity
                    .builder()
                    .year(preYear)
                    .month(preMonth)
                    .receivedBenefitAmount(0)
                    .totalSpending(0)
                    .build();
        }
        // 과소비 계산
        int overConsumption = Math.min(100, Math.abs(100 * report.getTotalSpending()) / monthlyIncome);
        // 변동성 계산
        int variation = 50;
        if(report.getTotalSpending() != 0 && preReport != null) {
        variation = Math.abs(100*(preReport.getTotalSpending() - report.getTotalSpending()) / (report.getTotalSpending()));
        }
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

        int extrovert = 0;
        if(report.getTotalSpending() != 0) {
            extrovert = Math.abs(100*extrovertSpendAmount / report.getTotalSpending());
        }

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
        if(summary == null) { return null; }
        MonthlyConsumptionReportEntity report = monthlyConsumptionReportJpaRepository.getReferenceById(summary.getReportId());
        ConsumptionPatternEntity pattern = consumptionPatternJpaRepository.getReferenceById(report.getConsumptionPatternId());

        int preYear = year;
        int preMonth = month;
        preMonth--;
        if (preMonth == 0) {
            preMonth = 12;
            preYear--;
        }
        MonthlyTransactionSummaryEntity preSummary = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(userId, preYear, preMonth);

        if(preSummary == null) {
            preSummary = MonthlyTransactionSummaryEntity
                    .builder()
                    .year(preYear)
                    .month(preMonth)
                    .receivedBenefitAmount(0)
                    .totalSpending(0)
                    .build();
        }

        int minSpending = 0;
        int maxSpending = 0;
        String groupName = "";
        if (Math.abs(summary.getTotalSpending()) >= 0 && Math.abs(summary.getTotalSpending()) <= 500000) {
            maxSpending = 500000;
            groupName = "50만원 이하";
        } else if (Math.abs(summary.getTotalSpending()) > 500001 && Math.abs(summary.getTotalSpending()) <= 1000000) {
            minSpending = 500001;
            maxSpending = 1000000;
            groupName = "50만원 ~ 100만원";
        } else if (Math.abs(summary.getTotalSpending()) > 1000001 && Math.abs(summary.getTotalSpending()) <= 1500000) {
            minSpending = 1000001;
            maxSpending = 1500000;
            groupName = "100만원 ~ 150만원";
        } else if (Math.abs(summary.getTotalSpending()) > 1500001 && Math.abs(summary.getTotalSpending()) <= 2000000) {
            minSpending = 1500001;
            maxSpending = 2000000;
            groupName = "150만원 ~ 200만원";
        } else if (Math.abs(summary.getTotalSpending()) > 2000001 && Math.abs(summary.getTotalSpending()) <= 2500000) {
            minSpending = 2000001;
            maxSpending = 2500000;
            groupName = "200만원 ~ 250만원";
        } else if (Math.abs(summary.getTotalSpending())  > 2500001 && Math.abs(summary.getTotalSpending()) <= 3000000) {
            minSpending = 2500001;
            maxSpending = 3000000;
            groupName = "250만원 ~ 300만원";
        } else {
            minSpending = 3000001;
            maxSpending = 2100000000;
            groupName = "300만원 이상";
        }

        double totalGroupBenefitAverage = monthlyTransactionSummaryJpaRepository.getGroupBenefitAverage(year, month, minSpending, maxSpending);

        ReportWithPatternDTO result = ReportWithPatternDTO
                .builder()
                .totalSpendingAmount(summary.getTotalSpending())
                .totalBenefitAmount(summary.getReceivedBenefitAmount())
                .totalGroupBenefitAverage((int) totalGroupBenefitAverage)
                .preTotalSpendingAmount(preSummary.getTotalSpending())
                .groupName(groupName)
                .reportDescription(report.getReportDescription())
                .overConsumption(report.getOverConsumption())
                .variation(report.getVariation())
                .extrovert(report.getExtrovert())
                .consumptionPattern(new ConsumptionPatternDTO(pattern))
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
            List<ReportCardCategoriesEntity> reportCardCategories = reportCardCategoriesJpaRepository.getByReportCard(reportCard);
            for (ReportCardCategoriesEntity reportCardCategory : reportCardCategories) {
                CategoryEntity category = categoryJpaRepository.getReferenceById(reportCardCategory.getCategoryId());
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
