package com.kkulmoo.rebirth.card.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class CardBenefit {
    List<String> benefitCategory;
    Integer receivedBenefitAmount;
    //todo: 나중에 Integer로 고치기 수홍이형이 고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏고쳐야해해해해해햏
    Integer remainingBenefitAmount;
}
