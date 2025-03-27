package com.kkulmoo.rebirth.analysis.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum StatusType {
    approved("승인"),
    rejected("거절"),
    canceled("취소");

    final private String value;

    StatusType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static StatusType fromValue(String value) {
        for (StatusType status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status_type value: " + value);
    }
}
