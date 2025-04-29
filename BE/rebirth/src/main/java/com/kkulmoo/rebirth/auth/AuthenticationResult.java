package com.kkulmoo.rebirth.auth;

import com.kkulmoo.rebirth.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResult {
    private final Boolean isSuccess;
    private final User user;

    public static AuthenticationResult success(User user) {
        return new AuthenticationResult(true, user);
    }

    public static AuthenticationResult failure() {
        return new AuthenticationResult(false, null);
    }
}
