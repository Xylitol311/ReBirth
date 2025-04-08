package com.kkulmoo.rebirth.user.presentation.requestDTO;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SendSmsRequest {
    private final String phoneNumber;
}
