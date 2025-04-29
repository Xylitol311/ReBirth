package com.kkulmoo.rebirth.payment.presentation.response;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.util.Json;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentTokenResponseDTO {
    String token;
    String cardName;
    String cardImgUrl;
    String cardConstellationInfo;
}
