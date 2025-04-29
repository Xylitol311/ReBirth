package com.kkulmoo.rebirth.user.presentation.requestDTO;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserCIRequest {
    String userName;
    String birth;
}
