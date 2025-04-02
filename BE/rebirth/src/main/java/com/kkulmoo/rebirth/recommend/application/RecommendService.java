package com.kkulmoo.rebirth.recommend.application;

import com.kkulmoo.rebirth.analysis.infrastructure.repository.BenefitTemplateJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardCategoriesJpaRepository;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateJpaRepository;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardsJpaRepository;
import com.kkulmoo.rebirth.recommend.domain.dto.response.AvgAmountByCategoryDTO;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendService {


    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
    private final BenefitTemplateJpaRepository benefitTemplateJpaRepository;
    private final CardsJpaRepository cardsJpaRepository;
    private final CardTemplateJpaRepository cardTemplateJpaRepository;

    public List<AvgAmountByCategoryDTO> calculateRecommendCardForAll(Integer userId) {

        // 전체 카드 중 사용자에게 맞는 카드 추천
        // 1. 우선 사용자가 가장 많이 사용하는 카테고리 3개에 대한 소비 내역을 들고 오자.
        //  - 3개월 기반이니까 3개월 카테고리별 평균으로 가져오면 좋을 듯
        List<AvgAmountByCategoryDTO> avgAmountByCategoryList = reportCardCategoriesJpaRepository.getCategorySpendingLast3Months(userId);

        // 2. 가져온 카테고리에 해당하는 혜택 템플릿 다 가져오자. 보유 여부 관계 없이
        Map<Integer,Double> scoreForCards = new HashMap<>();
        for(AvgAmountByCategoryDTO avgAmountByCategory : avgAmountByCategoryList) {
            System.out.println("엥?? "+avgAmountByCategory);
           List<BenefitTemplateEntity> benefitsForCategory = benefitTemplateJpaRepository.findByCategoryId(avgAmountByCategory.getCategoryId());

           // 3. 템플릿 하나하나 보면서 카테고리별 점수 계산, 맵에다 카드별로 점수 합산
           for(BenefitTemplateEntity benefitForCategory : benefitsForCategory) {
               System.out.println("으잉? "+benefitForCategory);
               double score = 0;
               if(benefitForCategory.getDiscountType().equals(DiscountType.percent)) {
                   score = avgAmountByCategory.getAvgTotalSpending()*benefitForCategory.getBenefitsBySection()[0]/100.0;
                   if(benefitForCategory.getBenefitUsageAmount()!=null) {
                       score = Math.min(score,benefitForCategory.getBenefitUsageAmount()[0]);
                   }
               } else {
                   double percent = benefitForCategory.getBenefitsBySection()[0]/benefitForCategory.getPaymentRange()[0]/100.0;
                   score = avgAmountByCategory.getAvgTotalSpending()*percent;
                   if(benefitForCategory.getBenefitUsageAmount()!=null) {
                       score = Math.min(score,benefitForCategory.getBenefitUsageAmount()[0]);
                   }
               }
               int cardId = benefitForCategory.getCardTemplate().getCardTemplateId();
               if(!scoreForCards.containsKey(cardId)) {
                   scoreForCards.put(cardId, score);
               } else {
                   scoreForCards.put(cardId, scoreForCards.get(cardId)+score);
               }
           }
        }
        // 4. 총점이 높은 순으로 카드 추천
        for(Map.Entry<Integer, Double> entry : scoreForCards.entrySet()) {
            int cardId = entry.getKey();
            double score = entry.getValue();
            CardTemplateEntity cardTemplate = cardTemplateJpaRepository.getReferenceById(cardId);
            System.out.println(cardTemplate.getCardName()+" "+score);
        }

        return avgAmountByCategoryList;
    }

}
