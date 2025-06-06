package com.kkulmoo.rebirth.transactions.application;

import com.kkulmoo.rebirth.card.application.CardPort;
import com.kkulmoo.rebirth.card.application.CardService;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.transactions.application.dto.*;
import com.kkulmoo.rebirth.transactions.application.mapper.TransactionHistoryMapper;
import com.kkulmoo.rebirth.transactions.domain.MerchantCache;
import com.kkulmoo.rebirth.transactions.domain.TransactionRepository;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.presentation.CardHistoryTransactionRequest;
import com.kkulmoo.rebirth.transactions.presentation.TransactionHistoryDto;
import com.kkulmoo.rebirth.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardPort cardPort;
    private final BankPort bankPort;
    private final CardService cardService;
    private final TransactionHistoryMapper mapper;
    private final MerchantCache merchantCache;
    private final CardRepository cardRepository;

    public TransactionHistoryResponseData getCardTransactionHistory(Integer userId, CardHistoryTransactionRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getPageSize());

        MyCard myCard = cardRepository.findById(request.getCardId()).get();

        CardTransactionQueryParams params = new CardTransactionQueryParams(
                myCard.getCardUniqueNumber(), request.getYear(), request.getMonth(), pageable
        );

        Slice<TransactionHistoryDto> transactions =
                transactionRepository.getCardTransactionHistoryByCardId(userId,params);

        return mapper.toResponseData(transactions);
    }

    // 입력할 때부터 어느 시간 기준으로 넣을 것인지 고민을 해야한다.
    public void getBankTransactionByMyData(User user, LocalDateTime fromDate) {
        System.out.println(fromDate);
        Mono<List<BankTransactionResponse>> bankTransaction = bankPort.getBankTransaction(
                BankTransactionRequest.builder()
                        .userCI(user.getUserCI())
                        .bankAccounts(user.getBankAccounts())
                        .timestamp(fromDate)
                        .build()
        );

        bankTransaction
                .map(transactions -> transactions.stream()
                        .peek(transaction -> transaction.setUserId(user.getUserId()))
                        .collect(Collectors.toList()))
                .subscribe(transactionRepository::saveAllBankTransactions);
    }

    // card내역 가져오기.
    // 카드사에게 이사람이 누구인지 이사람의 어떤 카드의 결제내역을 얻고 싶은지 요청을해야한다.
    public List<CardTransactionResponse> getCardTransactionByMyData(User user, List<String> CardUniqueNumbers) {
        // CardUniqueNumbers
        List<MyCard> myCardList = cardService.getMyCardListByCardUniqueNumbers(CardUniqueNumbers, user.getUserId().getValue());

        for(MyCard myCadrd : myCardList){
            System.out.println(myCadrd.getCardName());
        }
        try {
            List<CardTransactionResponse> transactions = cardPort.getCardTransaction(CardTransactionRequest.builder()
                    .cards(myCardList)
                    .userCI(user.getUserCI())
                    .build()).block();
            if (transactions != null && !transactions.isEmpty()) {
                List<CardTransactionResponse> transactionsWithUserId = transactions.stream()
                        .peek(transaction -> {
                            // 각 개별 트랜잭션 로깅
                            log.info("개별 트랜잭션: {}", transaction.toString());
                        })
                        .map(transaction ->
                                transaction.withUserIdAndMerchantNameAndMerchantId(user.getUserId()
                                        , transaction.getMerchantName()
                                        , merchantCache.getMerchantIdByName(transaction.getMerchantName()))
                        )
                        .toList();
                // 한 번에 모든 트랜잭션 저장
                transactionRepository.saveAllCardTransactions(transactionsWithUserId);
                log.info("사용자 {}의 {}개 거래내역 일괄 저장 완료", user.getUserId(), transactions.size());

                return transactionsWithUserId;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("카드 거래내역 처리 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
