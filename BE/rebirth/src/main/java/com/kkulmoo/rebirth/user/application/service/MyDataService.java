package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.card.application.CardService;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.transactions.application.BankPort;
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
    private final BankPort bankPort;

    @Transactional
    public void loadMyCard(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));
        // todo : 카드 가져오기
        List<myCard> cardData = cardService.getCardData(user);

        // todo : 카드에 해당하는 거래내역 가져오기
        loadMyTransactionByCards(user, cardData);
    }

    @Transactional
    public void getMyCardTransactionData(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));

        List<myCard> myCardsList = cardService.findByUserId(new UserId(userId));

        loadMyTransactionByCards(user, myCardsList);
    }


    @Transactional
    public void loadMyTransactionByCards(User user, List<myCard> cards) {
        List<String> cardUniqueNumbers = cards.stream()
                .map(myCard::getCardUniqueNumber)
                .collect(Collectors.toList());

        // 추출한 카드 고유 번호 리스트를 이용해 거래내역 가져오기
        transactionService.getCardTransactionByMyData(user, cardUniqueNumbers);

        cardService.updateCardsLastLoadTime(cards);
    }

    @Transactional
    public void loadMyBankAccount(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));

        // 비동기 호출을 블로킹하여 계좌 목록 가져오기
        List<String> newAccounts = bankPort.getAccountNumbersByUserCI(user.getUserCI())
                .block(); // 실제 환경에서는 적절한 타임아웃 설정 필요

        // 계좌 목록 업데이트
        user.updateBankAccounts(newAccounts);

        // 변경된 사용자 정보 저장
        userRepository.save(user);
    }

    @Transactional
    public void loadMyBankTransaction(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));

        transactionService.getBankTransactionByMyData(user, user.getBankLatestLoadDataAt());


        user.updateLatestLoadDataAtNow();
        System.out.println(
                user.toString());
        userRepository.save(user);

    }
}