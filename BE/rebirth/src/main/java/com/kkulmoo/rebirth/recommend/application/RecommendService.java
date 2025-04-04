package com.kkulmoo.rebirth.recommend.application;

import com.kkulmoo.rebirth.analysis.infrastructure.repository.BenefitTemplateJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardCategoriesJpaRepository;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import com.kkulmoo.rebirth.payment.domain.repository.CardTemplateRepository;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateJpaRepository;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateRepositoryImpl;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardsJpaRepository;
import com.kkulmoo.rebirth.recommend.domain.dto.request.SearchParameterDTO;
import com.kkulmoo.rebirth.recommend.domain.dto.response.AvgAmountByCategoryDTO;
import com.kkulmoo.rebirth.recommend.domain.dto.response.RecommendCardDTO;
import com.kkulmoo.rebirth.recommend.domain.dto.response.RecommendCardForCategoryDTO;
import com.kkulmoo.rebirth.recommend.domain.dto.response.Top3CardDTO;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendService {


    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
    private final BenefitTemplateJpaRepository benefitTemplateJpaRepository;
    private final CardsJpaRepository cardsJpaRepository;
    private final CardTemplateJpaRepository cardTemplateJpaRepository;
    private final CardTemplateRepository cardTemplateRepository;

    public Top3CardDTO calculateRecommendCardForAll(Integer userId) {

        // 전체 카드 중 사용자에게 맞는 카드 추천
        // 1. 우선 사용자가 가장 많이 사용하는 카테고리 5개에 대한 소비 내역을 들고 오자.
        //  - 3개월 기반이니까 3개월 카테고리별 평균으로 가져오면 좋을 듯
        List<AvgAmountByCategoryDTO> avgAmountByCategoryList = reportCardCategoriesJpaRepository.getCategorySpendingLast3Months(userId);
        int totalSpending = 0;
        // 2. 가져온 카테고리에 해당하는 혜택 템플릿 다 가져오자. 보유 여부 관계 없이
        Map<Integer,Double> scoreForCards = new HashMap<>();
        for(AvgAmountByCategoryDTO avgAmountByCategory : avgAmountByCategoryList) {
           List<BenefitTemplateEntity> benefitsForCategory = benefitTemplateJpaRepository.findByCategoryId(avgAmountByCategory.getCategoryId());
           totalSpending += avgAmountByCategory.getTotalSpending();
           // 3. 템플릿 하나하나 보면서 카테고리별 점수 계산, 맵에다 카드별로 점수 합산
           for(BenefitTemplateEntity benefitForCategory : benefitsForCategory) {
               double score = 0;
               if(benefitForCategory.getDiscountType().equals(DiscountType.PERCENT)) {
                   score = avgAmountByCategory.getAvgTotalSpending()*benefitForCategory.getBenefitsBySection().get(0)/100.0;
                   if(benefitForCategory.getBenefitUsageAmount()!=null) {
                       score = Math.min(score,benefitForCategory.getBenefitUsageAmount().get(0));
                   }
               } else {
                   double percent = benefitForCategory.getBenefitsBySection().get(0)/benefitForCategory.getPaymentRange().get(0)/100.0;
                   score = avgAmountByCategory.getAvgTotalSpending()*percent;
                   if(benefitForCategory.getBenefitUsageAmount()!=null) {
                       score = Math.min(score,benefitForCategory.getBenefitUsageAmount().get(0));
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
        List<RecommendCardDTO> cards = new ArrayList<>();
        // 4. 총점이 높은 순으로 카드 추천
        for(Map.Entry<Integer, Double> entry : scoreForCards.entrySet()) {
            int cardId = entry.getKey();
            double score = entry.getValue();
            CardTemplateEntity cardTemplate = cardTemplateJpaRepository.getReferenceById(cardId);
            RecommendCardDTO card = RecommendCardDTO
                    .builder()
                    .cardId(cardTemplate.getCardTemplateId())
                    .cardName(cardTemplate.getCardName())
                    .cardInfo(cardTemplate.getCardDetailInfo())
                    .imgUrl(cardTemplate.getCardImgUrl())
                    .constellation(cardTemplate.getCardConstellationInfo())
                    .score((int) Math.round(score))
                    .build();

            cards.add(card);
        }

        Collections.sort(cards, Comparator.comparing(RecommendCardDTO::getScore).reversed());
        List<RecommendCardDTO> subList = cards.subList(0, Math.min(3, cards.size()));
        Top3CardDTO top3CardDTO = Top3CardDTO
                .builder()
                .amount(totalSpending)
                .recommendCards(subList)
                .build();

        return top3CardDTO;
    }

    public List<RecommendCardForCategoryDTO> calculateRecommendCardForCategory(Integer userId) {
        // 1. 우선 사용자가 가장 많이 사용하는 카테고리 5개에 대한 소비 내역
        List<AvgAmountByCategoryDTO> avgAmountByCategoryList = reportCardCategoriesJpaRepository.getCategorySpendingLast3Months(userId);

        List<RecommendCardForCategoryDTO> recommendCardForCategoryList = new ArrayList<>();
        // 2. 가져온 카테고리에 해당하는 혜택 템플릿 다 가져오자. 보유 여부 관계 없이
        for(AvgAmountByCategoryDTO avgAmountByCategory : avgAmountByCategoryList) {
            Map<Integer,Double> scoreForCards = new HashMap<>();
            List<BenefitTemplateEntity> benefitsForCategory = benefitTemplateJpaRepository.findByCategoryId(avgAmountByCategory.getCategoryId());

            // 3. 템플릿 하나하나 보면서 카테고리별 점수 계산, 맵에다 카드별로 점수 합산
            for(BenefitTemplateEntity benefitForCategory : benefitsForCategory) {
                double score = 0;
                if(benefitForCategory.getDiscountType().equals(DiscountType.PERCENT)) {
                    score = avgAmountByCategory.getAvgTotalSpending()*benefitForCategory.getBenefitsBySection().get(0)/100.0;
                    if(benefitForCategory.getBenefitUsageAmount()!=null) {
                        score = Math.min(score,benefitForCategory.getBenefitUsageAmount().get(0));
                    }
                } else {
                    double percent = benefitForCategory.getBenefitsBySection().get(0)/benefitForCategory.getPaymentRange().get(0)/100.0;
                    score = avgAmountByCategory.getAvgTotalSpending()*percent;
                    if(benefitForCategory.getBenefitUsageAmount()!=null) {
                        score = Math.min(score,benefitForCategory.getBenefitUsageAmount().get(0));
                    }
                }
                int cardId = benefitForCategory.getCardTemplate().getCardTemplateId();
                if(!scoreForCards.containsKey(cardId)) {
                    scoreForCards.put(cardId, score);
                } else {
                    scoreForCards.put(cardId, scoreForCards.get(cardId)+score);
                }
            }
            List<RecommendCardDTO> cards = new ArrayList<>();
            // 4. 총점이 높은 순으로 카드 추천
            for(Map.Entry<Integer, Double> entry : scoreForCards.entrySet()) {
                int cardId = entry.getKey();
                double score = entry.getValue();
                CardTemplateEntity cardTemplate = cardTemplateJpaRepository.getReferenceById(cardId);
                RecommendCardDTO card = RecommendCardDTO
                        .builder()
                        .cardId(cardTemplate.getCardTemplateId())
                        .cardName(cardTemplate.getCardName())
                        .cardInfo(cardTemplate.getCardDetailInfo())
                        .imgUrl(cardTemplate.getCardImgUrl())
                        .constellation(cardTemplate.getCardConstellationInfo())
                        .score((int) Math.round(score))
                        .build();

                cards.add(card);
            }
            Collections.sort(cards, Comparator.comparing(RecommendCardDTO::getScore).reversed());
            List<RecommendCardDTO> recommendCards = cards.subList(0, Math.min(3, cards.size()));
            RecommendCardForCategoryDTO recommendCardForCategory = RecommendCardForCategoryDTO
                    .builder()
                    .categoryId(avgAmountByCategory.getCategoryId())
                    .categoryName(avgAmountByCategory.getCategoryName())
                    .amount(avgAmountByCategory.getTotalSpending())
                    .recommendCards(recommendCards)
                    .build();

            recommendCardForCategoryList.add(recommendCardForCategory);
        }
        return recommendCardForCategoryList;
    }

    public List<CardTemplateEntity> searchByParameter(SearchParameterDTO parameter) {

        return cardTemplateRepository.searchCard(parameter);
    }
}
