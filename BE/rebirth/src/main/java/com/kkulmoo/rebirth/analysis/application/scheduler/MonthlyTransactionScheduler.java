package com.kkulmoo.rebirth.analysis.application.scheduler;

import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO;
import com.kkulmoo.rebirth.analysis.domain.repository.MonthlyTransactionSummaryRepository;
import com.kkulmoo.rebirth.analysis.domain.repository.ReportCardCategoriesRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.MonthlyConsumptionReportEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.MonthlyTransactionSummaryEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardCategoriesEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.MonthlyConsumptionReportJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.MonthlyTransactionSummaryJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardCategoriesJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardsJpaRepository;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.repository.UserJpaRepository;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthlyTransactionScheduler {

    private final UserJpaRepository userJpaRepository;
    private final ReportCardsJpaRepository reportCardsJpaRepository;
    private final ReportCardCategoriesRepository reportCardCategoriesRepository;
    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
    private final CardsJpaRepository cardsJpaRepository;
    private final BenefitTemplateJpaRepository benefitTemplateJpaRepository;
    private final MonthlyConsumptionReportJpaRepository monthlyConsumptionReportJpaRepository;
    private final MonthlyTransactionSummaryRepository monthlyTransactionSummaryRepository;
    private final MonthlyTransactionSummaryJpaRepository monthlyTransactionSummaryJpaRepository;


    @Scheduled(cron = "0 0 0 1 * ?")
    public void createMonthlyTransaction() {
        List<UserEntity> users = userJpaRepository.findAll(); // 나중에 deleted_at 있는건 안가져오게 수정할 것
        for(UserEntity user : users) {

            createMonthlyTransactionSummary(user);

        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void endMonthlyTransaction() {
        List<UserEntity> users = userJpaRepository.findAll(); // 나중에 deleted_at 있는건 안가져오게 수정할 것
        for(UserEntity user : users) {
            makeMonthlyConsumptionReport(user);
        }

    }

    @Transactional
    public void createMonthlyTransactionSummary(UserEntity user) {
        // 월 시작될 때 리포트 틀 만들어주기. 년, 월 정도 넣어주고 혜택이나 금액은 0
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        MonthlyTransactionSummaryEntity monthlyTransactionSummary = MonthlyTransactionSummaryEntity
                .builder()
                .userId(user.getUserId())
                .year(year)
                .month(month)
                .totalSpending(0)
                .receivedBenefitAmount(0)
                .build();

        int reportId = monthlyTransactionSummaryJpaRepository.save(monthlyTransactionSummary).getReportId();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getReferenceById(reportId);
        List<CardsEntity> cards = cardsJpaRepository.getByUserId(user.getUserId());
        for(CardsEntity card : cards) {

            createReportCards(card, report);

        }

    }

    @Transactional
    public void createReportCards(CardsEntity card, MonthlyTransactionSummaryEntity report) {
        // 카드별 혜택, 사용금액 0,
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
        ReportCardsEntity reportCard = reportCardsJpaRepository.getReferenceById(reportCardId);
//        // 보유카드의 카드 템플릿을 확인하여, 실적구간을 고려한 혜택 템플릿을 모두 가져옴
//        List<BenefitTemplateEntity> benefitTemplates = benefitTemplateJpaRepository.getByCardTemplateIdAndSpendingTier(card.getCardTemplateId, card.getSpendingTier);
//        for(BenefitTemplateEntity benefit : benefitTemplates) {
//            ReportCardCategoriesEntity reportCardCategories = createReportCardCategories(reportCard,benefit);
//        }
    }

    @Transactional
    public ReportCardCategoriesEntity createReportCardCategories(ReportCardsEntity reportCard, BenefitTemplateEntity benefit) {
        ReportCardCategoriesEntity reportCardCategoriesEntity = ReportCardCategoriesEntity
                .builder()
                .reportCardId(reportCard.getReportCardId())
                .categoryId(benefit.getCategory().getCategoryId())
//                .merchantId(benefit.getMerchantId())
                .amount(0)
                .receivedBenefitAmount(0)
                .count(0)
                .createdAt(reportCard.getCreatedAt())
                .build();
        int reportCardCategoryId = reportCardCategoriesJpaRepository.save(reportCardCategoriesEntity).getReportCategoryId();
        return reportCardCategoriesJpaRepository.getReferenceById(reportCardCategoryId);
    }

    @Transactional
    public void makeMonthlyConsumptionReport(UserEntity user) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        int year = yesterday.getYear();
        int month = yesterday.getMonthValue();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(),year, month);

        // 소비패턴 계산
        int[] pattern = calculateSpendingPattern(user, year, month);

        // AI 요약
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();

        String question = "다음은 " + user.getUserName() + "님의 한달간 소비 내역이야. 요약해줘.\n";

        List<ReportCategoryDTO> spendingByCategory = getTotalSpendingByCategory(user, year, month);
        for(ReportCategoryDTO row : spendingByCategory) {
            String category = row.getCategory();
            question = question.concat(category+"카테고리 지출 : "+row.getAmount()+"원\n");
        }
        question = question.concat("과소비성향 : "+pattern[0]+"\n");
        question = question.concat("소비변동성 : "+pattern[1]+"\n");
        question = question.concat("소비외향성 : "+pattern[2]+"\n");
        question = question.concat("hint: 과소비 성향은 수입 대비 소비정도를, 소비 변동성은 직전달 대비 소비의 변동성, 소비 외향성은 소비카테고리 기준 외향적 소비 비율을 의미해.");


        String answer = model.chat(question);
        String consumptionPatternId = "";
        if(pattern[2]>50) consumptionPatternId = consumptionPatternId.concat("E"); // 외향형
        else consumptionPatternId = consumptionPatternId.concat("I"); // 내향헝
        if(pattern[0]>50) consumptionPatternId = consumptionPatternId.concat("B"); // 과소비형
        else consumptionPatternId = consumptionPatternId.concat("M"); // 절약형
        if(pattern[1]>50) consumptionPatternId = consumptionPatternId.concat("V"); // 변동형
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

    @Transactional
    public int[] calculateSpendingPattern(UserEntity user, int year, int month) {
        // 필요한 정보 - 월평균 수입, 월 총 지출, 카테고리별 지출, 전 월 지출
        int monthlyIncome = user.getAverageMonthlyIncome();
        MonthlyTransactionSummaryEntity report = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(),year, month);

        // 전 월 총지출 가져오기
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int preYear = lastMonth.getYear();
        int preMonth = lastMonth.getMonthValue();
        MonthlyTransactionSummaryEntity preReport = monthlyTransactionSummaryJpaRepository.getByUserIdAndYearMonth(user.getUserId(), preYear, preMonth);

        // 과소비 계산
        int overConsumption = (int) Math.min(100,50*report.getTotalSpending()/(float)monthlyIncome);
        // 변동성 계산
        int variation = (int) Math.abs((preReport.getTotalSpending() - report.getTotalSpending())/(float)(report.getTotalSpending()));
        variation = Math.min(100,100*variation);

        // 외향성 계산
        Map<String,Integer> extrovertCategories = new HashMap<>();
        extrovertCategories.put("공항라운지",1);
        extrovertCategories.put("항공",1);
        extrovertCategories.put("교통",1);
        extrovertCategories.put("대형마트",1);
        extrovertCategories.put("숙박",1);
        extrovertCategories.put("스포츠관련",1);
        extrovertCategories.put("여행/숙박",1);
        extrovertCategories.put("영화관",1);
        extrovertCategories.put("외식",1);
        extrovertCategories.put("자동차",1);
        extrovertCategories.put("주유소",1);
        extrovertCategories.put("택시",1);

        // 카테고리별 지출 가져오기
        List<ReportCategoryDTO> spendingByCategory = getTotalSpendingByCategory(user, year, month);
        int extrovertSpendAmount = 0;
        for(ReportCategoryDTO spending : spendingByCategory) {
            if(extrovertCategories.containsKey(spending.getCategory())) extrovertSpendAmount += spending.getAmount();
        }
        int extrovert = (int)(100* ((float)extrovertSpendAmount) / report.getTotalSpending());

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
}
