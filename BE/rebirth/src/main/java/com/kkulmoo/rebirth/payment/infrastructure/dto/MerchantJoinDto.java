package com.kkulmoo.rebirth.payment.infrastructure.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class MerchantJoinDto {
    private Integer merchantId;
    private Integer subCategoryId;
    private Integer categoryId;
}
