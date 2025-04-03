package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.payment.application.BenefitInfo;

import java.util.List;

public interface BenefitRepository {
    List<BenefitInfo> findBenefitsByMerchantFilter(int cardTemplateId, int categoryId, int subcategoryId, int merchantId);
}
