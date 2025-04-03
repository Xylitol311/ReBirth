package com.kkulmoo.rebirth.user.presentation.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class UserLoginRequest {
    String type;
    String number;
    String phoneSerialNumber;

}
