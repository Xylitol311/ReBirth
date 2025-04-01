package com.kkulmoo.rebirth.transactions.application;

import com.kkulmoo.rebirth.card.application.CardPort;
import com.kkulmoo.rebirth.card.application.CardService;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionRequest;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.domain.MerchantCache;
import com.kkulmoo.rebirth.transactions.domain.TransactionRepository;
import com.kkulmoo.rebirth.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardPort cardPort;
    private final CardService cardService;
    private final MerchantCache merchantCache;

    // card내역 가져오기.
    // 카드사에게 이사람이 누구인지 이사람의 어떤 카드의 결제내역을 얻고 싶은지 요청을해야한다.
    public void getCardTransactionByMyData(User user, List<String> CardUniqueNumbers){

        // CardUniqueNumbers
        List<myCard> myCardList = cardService.getMyCardListByCardUniqueNumbers(CardUniqueNumbers);

        Mono<List<CardTransactionResponse>> cardTransaction = cardPort.getCardTransaction(CardTransactionRequest.builder()
                .cards(myCardList)
                .userCI(user.getUserApiKey())
                .build());

        cardTransaction.subscribe(transactions -> {
            if (!transactions.isEmpty()) {
                List<CardTransactionResponse> transactionsWithUserId = transactions.stream()
                        .map(transaction ->
                                        transaction.withUserIdAndMerchantId(user.getUserId()
                                        ,transaction.getMerchantName())
                                )
                        .toList();
                // 한 번에 모든 트랜잭션 저장
                transactionRepository.saveAllCardTransactions(transactionsWithUserId);
                log.info("사용자 {}의 {}개 거래내역 일괄 저장 완료", user.getUserId(), transactions.size());
            }
        }, error -> {
            log.error("카드 거래내역 처리 중 오류 발생: {}", error.getMessage(), error);
        });
    }

    // bank내역 가져오기.
}
