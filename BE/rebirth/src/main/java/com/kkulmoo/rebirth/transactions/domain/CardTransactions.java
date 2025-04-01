package com.kkulmoo.rebirth.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactions {
    Integer TransactionsId;
    Short cardCompanyId;
    Integer merchantId;
    Integer cardUniqueNumber;
    // 승인, 거절, 취소
    Status status;
    // 받은 혜택
    CardBenefitType cardBenefitType;
    // 받은 혜택 금액
    Integer benefitAmount;
}
