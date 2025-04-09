package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.analysis.application.service.ReportService;
import com.kkulmoo.rebirth.card.application.CardService;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.transactions.application.BankPort;
import com.kkulmoo.rebirth.transactions.application.TransactionService;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDataService {
    private final UserRepository userRepository;
    private final CardService cardService;
    private final TransactionService transactionService;
    private final UserCardBenefitService userCardBenefitService;
    private final BankPort bankPort;
    private final ReportService reportService;
    private final CardRepository cardRepository;

    // 카드 내 자산 불러오기
    @Transactional
    public void loadMyCard(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));
        // todo : 카드 가져오기
        List<MyCard> cardData = cardService.getCardData(user);

        // todo : 카드에 해당하는 거래내역 가져오기
        loadMyTransactionByCards(user, cardData);
    }

    //내 카드의 전체 거래내역 데이터 불러오기
    @Transactional
    public void getMyCardTransactionData(Integer userId) {
        User user = userRepository.findByUserId(new UserId(userId));

        List<MyCard> myCardList = cardService.findByUserId(new UserId(userId));

        loadMyTransactionByCards(user, myCardList);
    }

    //카드 거래내역 불러오기 (선택한 카드들만 불러온다.)
    @Transactional
    public void loadMyTransactionByCards(User user, List<MyCard> cards) {
        List<String> cardUniqueNumbers = cards.stream()
                .map(MyCard::getCardUniqueNumber)
                .collect(Collectors.toList());

        // 추출한 카드 고유 번호 리스트를 이용해 거래내역 가져오기
        List<CardTransactionResponse> transactionResponses = transactionService.getCardTransactionByMyData(user, cardUniqueNumbers);
        updateCardsLastLoadTimeFromTransactions(cards, transactionResponses);


        reportService.startWithMyData(user.getUserId().getValue());

        userCardBenefitService.updateUseCardBenefit(transactionResponses, cards);
    }

    public void updateCardsLastLoadTimeFromTransactions(List<MyCard> myCards, List<CardTransactionResponse> transactionResponses) {
        // 카드별 최신 거래일시를 맵으로 저장
        Map<String, LocalDateTime> latestTransactionTimeByCard = transactionResponses.stream()
                .collect(Collectors.groupingBy(
                        CardTransactionResponse::getCardUniqueNumber,
                        Collectors.mapping(
                                CardTransactionResponse::getCreatedAt,
                                Collectors.maxBy(LocalDateTime::compareTo)
                        )
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                ));

        // 각 카드의 lastLoadedAt 업데이트
        List<MyCard> updatedCards = myCards.stream()
                .filter(card -> latestTransactionTimeByCard.containsKey(card.getCardUniqueNumber()))
                .map(card -> card.updateLatestLoadDataAt(latestTransactionTimeByCard.get(card.getCardUniqueNumber())))
                .collect(Collectors.toList());

        // 변경된 카드만 저장
        if (!updatedCards.isEmpty()) {
            cardRepository.saveAll(updatedCards);
        }
    }


//    @Transactional
//    public void loadMyTransactionByCardsV2(User user, List<MyCard> cards) {
//        List<String> cardUniqueNumbers = cards.stream()
//                .map(MyCard::getCardUniqueNumber)
//                .collect(Collectors.toList());
//
//        // 추출한 카드 고유 번호 리스트를 이용해 거래내역 가져오기
//        List<CardTransactionResponse> transactionResponses = transactionService.getCardTransactionByMyData(user, cardUniqueNumbers);
//
//
//        cardService.updateCardsLastLoadTime(cards);
//
//        userCardBenefitService.updateUseCardBenefit(transactionResponses, cards);
//
//        // 혜택 현황 업데이트
//    }

    // 내 은행 계좌 가져오기
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

    //은행 거래내역 불러오기
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