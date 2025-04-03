package com.kkulmoo.rebirth.analysis.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum BenefitType {

    DISCOUNT("할인"),
    MILEAGE("적립"),
    COUPON("쿠폰");

    final private String value;

    BenefitType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static BenefitType fromValue(String value) {
        for (BenefitType benefitType : values()) {
            if (benefitType.value.equals(value)) {
                return benefitType;
            }
        }
        throw new IllegalArgumentException("Unknown benefit_type value: " + value);
    }
}
