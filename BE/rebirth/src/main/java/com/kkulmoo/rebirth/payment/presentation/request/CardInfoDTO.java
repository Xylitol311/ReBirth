package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CardInfoDTO {
    String userId;
    String cardPassword;
    String cvc;
    Timestamp expiryDate;
    String cardNumber;

}
