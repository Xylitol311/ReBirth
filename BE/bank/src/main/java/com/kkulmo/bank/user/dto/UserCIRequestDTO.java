package com.kkulmo.bank.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCIRequestDTO {

    private String userName;
    private String birth;

}
