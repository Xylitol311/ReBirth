package com.kkulmoo.rebirth.transactions.application.dto;

import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CardTransactionResponse {

    private UserId userId;
    private String cardUniqueNumber;
    private Integer amount;
    private String benefitType;
    private Integer benefitAmount;
    private LocalDateTime createdAt;
    private String merchantName;
    private String approvalCode;
    private String merchantId;

    /**
     * userId 값을 설정하고 새로운 CardTransactionResponse 객체를 반환합니다.
     *
     * @param userId 설정할 새로운 userId 값
     * @return userId가 변경된 새로운 CardTransactionResponse 객체
     */
    public CardTransactionResponse withUserId(UserId userId) {
        return CardTransactionResponse.builder()
                .userId(userId)
                .cardUniqueNumber(this.cardUniqueNumber)
                .amount(this.amount)
                .benefitType(this.benefitType)
                .benefitAmount(this.benefitAmount)
                .createdAt(this.createdAt)
                .merchantName(this.merchantName)
                .approvalCode(this.approvalCode)
                .merchantId(this.merchantId)
                .build();
    }

    /**
     * merchantId 값을 설정하고 새로운 CardTransactionResponse 객체를 반환합니다.
     *
     * @param merchantId 설정할 새로운 merchantId 값
     * @return merchantId가 변경된 새로운 CardTransactionResponse 객체
     */
    public CardTransactionResponse withMerchantId(String merchantId) {
        return CardTransactionResponse.builder()
                .userId(this.userId)
                .cardUniqueNumber(this.cardUniqueNumber)
                .amount(this.amount)
                .benefitType(this.benefitType)
                .benefitAmount(this.benefitAmount)
                .createdAt(this.createdAt)
                .merchantName(this.merchantName)
                .approvalCode(this.approvalCode)
                .merchantId(merchantId)
                .build();
    }

    /**
     * userId와 merchantId 값을 동시에 설정하고 새로운 CardTransactionResponse 객체를 반환합니다.
     *
     * @param userId 설정할 새로운 userId 값
     * @param merchantId 설정할 새로운 merchantId 값
     * @return userId와 merchantId가 변경된 새로운 CardTransactionResponse 객체
     */
    public CardTransactionResponse withUserIdAndMerchantId(UserId userId, String merchantId) {
        return CardTransactionResponse.builder()
                .userId(userId)
                .cardUniqueNumber(this.cardUniqueNumber)
                .amount(this.amount)
                .benefitType(this.benefitType)
                .benefitAmount(this.benefitAmount)
                .createdAt(this.createdAt)
                .merchantName(this.merchantName)
                .approvalCode(this.approvalCode)
                .merchantId(merchantId)
                .build();
    }
}