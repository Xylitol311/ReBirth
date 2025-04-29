package com.kkulmoo.rebirth.transactions.domain;

import lombok.Getter;

@Getter
public enum Status {
    APPROVED("승인"),
    REJECTED("거절"),
    CANCELED("취소");

    private final String koreanDescription;

    Status(String koreanDescription) {
        this.koreanDescription = koreanDescription;
    }

    /**
     * 승인 코드(approvalCode)로부터 Status를 결정하는 정적 메서드
     *
     * @param approvalCode 카드 승인 코드 (예: "TXN17430804010037519")
     * @return 해당하는 Status 열거형 값
     */
    public static Status fromApprovalCode(String approvalCode) {
        if (approvalCode == null || approvalCode.length() < 3) {
            return REJECTED; // 기본값 또는 예외 처리
        }

        String prefix = approvalCode.substring(0, 3);

        switch (prefix) {
            case "TXN":
                return APPROVED;
            case "REJ":
                return REJECTED;
            case "CAN":
                return CANCELED;
            default:
                return REJECTED; // 알 수 없는 코드는 거절로 처리
        }
    }
}