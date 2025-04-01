package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.card.application.CardService;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.transactions.application.TransactionService;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDataService {

    private final UserRepository userRepository;
    private final CardService cardService;
    private final TransactionService transactionService;

    @Transactional
    public void loadMyCard(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));
        // todo : 카드 가져오기
        List<myCard> cardData = cardService.getCardData(user);

        // todo : 카드에 해당하는 거래내역 가져오기
        loadMyTransactionByCards(user, cardData);
    }

    @Transactional
    public void getMyTransactionData(Integer userId) {
        cardService.findByUserId(new UserId(userId));
    }


    @Transactional
    public void loadMyTransactionByCards(User user, List<myCard> cards){
        List<String> cardUniqueNumbers = cards.stream()
                .map(myCard::getCardUniqueNumber)
                .collect(Collectors.toList());

        // 추출한 카드 고유 번호 리스트를 이용해 거래내역 가져오기
        transactionService.getCardTransactionByMyData(user, cardUniqueNumbers);

    }

    @Transactional
    public void loadMyBankTransaction() {
        // todo: 은행 거래내역 가져오기
        // 그러면 어떤 계좌를 부를것인가를 고민해야한다.
        // card에서 가져온 계좌? 아니면 모든 계좌를 업데이트?
        // 계좌만 업데이트됐는데... 카드 거래내역이 업데이트 안되면 어떡해?
    }

}
