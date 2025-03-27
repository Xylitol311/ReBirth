package com.kkulmoo.rebirth.payment.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ApiResponseDTO {
    private final Boolean success;
    private final String message;
    private final Object data;

    @JsonCreator
    public ApiResponseDTO(
            @JsonProperty("success") Boolean success,
            @JsonProperty("message") String message,
            @JsonProperty("data") Object data
    ) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}

