package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.payment.presentation.response.PermanentTokenResponseByCardsaDTO;

import javax.smartcardio.Card;
import java.util.List;

public interface CardsRepository {
    List<PaymentCard> findByUserId(int userId);
//    PaymentCard findByCardUniqueNumber(String CardUniqueNumber);

    void savePermanentToken(Integer userId, PermanentTokenResponseByCardsaDTO permanentTokenResponseByCardsaDTO , PaymentCard paymentCard);

    PaymentCard findByUserIdAndCardUniqueNumber(Integer userId, String cardUniqueNumber);
}
