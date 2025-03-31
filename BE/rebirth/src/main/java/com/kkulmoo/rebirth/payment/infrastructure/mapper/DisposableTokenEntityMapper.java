package com.kkulmoo.rebirth.payment.infrastructure.mapper;

import com.kkulmoo.rebirth.payment.domain.DisposableToken;
import com.kkulmoo.rebirth.payment.infrastructure.entity.DisposableTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class DisposableTokenEntityMapper {

    public DisposableToken toDisposableToken(DisposableTokenEntity disposableTokenEntity){

        if(disposableTokenEntity == null) {
            return null;
        }
        return DisposableToken.builder().id(disposableTokenEntity.getId()).token(disposableTokenEntity.getToken()).build();
    }

}
