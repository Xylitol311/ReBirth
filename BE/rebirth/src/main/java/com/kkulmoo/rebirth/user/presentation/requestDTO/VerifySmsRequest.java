package com.kkulmoo.rebirth.user.presentation.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VerifySmsRequest {
    String phoneNumber;
    String code;
}
