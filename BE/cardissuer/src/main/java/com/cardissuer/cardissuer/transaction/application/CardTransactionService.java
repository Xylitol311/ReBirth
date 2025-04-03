package com.cardissuer.cardissuer.transaction.application;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.cardissuer.cardissuer.common.exception.CardNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.cardissuer.cardissuer.cards.domain.Card;
import com.cardissuer.cardissuer.cards.domain.CardRepository;
import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.common.exception.UserNotFoundException;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.domain.CardTransactionRepository;
import com.cardissuer.cardissuer.transaction.infrastrucuture.BankAPI;
import com.cardissuer.cardissuer.transaction.infrastrucuture.BankTransactionResponseDTO;
import com.cardissuer.cardissuer.transaction.presentation.CreateTransactionRequest;
import com.cardissuer.cardissuer.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardTransactionService {
    private final CardTransactionRepository cardTransactionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final BankAPI bankAPI;

    @Transactional
    public BankTransactionResponseDTO createTransaction(CreateTransactionRequest createTransactionRequest) {

        System.out.println("서비스에서 토큰");
        System.out.println(createTransactionRequest.getToken());
        // TODO: 토큰을 가지고 사용자 보유카드 가져오기.

        PermanentToken token = cardRepository.findTokenByToken(createTransactionRequest.getToken())
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

        Card card = cardRepository.findByCardUniqueNumber(CardUniqueNumber.of(token.getCardUniqueNumber()))
                .orElseThrow(() -> new RuntimeException("카드 정보를 찾을 수 없습니다."));


        Integer transactionAmount = createTransactionRequest.getAmount();
        if ("discount".equals(createTransactionRequest.getBenefitType())) {
            transactionAmount -= createTransactionRequest.getBenefitAmount();
        }

        BankTransactionResponseDTO bankResult = bankAPI.createTransaction(bankTransactionCreateDTO.builder()
                .accountNumber(card.getAccountNumber())
                .amount(transactionAmount)
                .userId(card.getUserCI())
                .type("TXN")
                .createdAt(createTransactionRequest.getCreatedAt())
                .build());

        if (bankResult.getApprovalCode().contains("TXN")) {
            cardTransactionRepository.save(
                    CardTransaction.builder()
                            .cardUniqueNumber(card.getCardUniqueNumber().getValue())
                            .amount(transactionAmount)
                            .createdAt(bankResult.getCreatedAt())
                            .merchantName(createTransactionRequest.getMerchantName())
                            .approvalCode(bankResult.getApprovalCode())
                            .build());
        }
        return bankResult;
    }

    @Transactional(readOnly = true)
    public List<CardTransaction> getTransactionsByUserCIAndCardUniqueNumberAfterTimestamp(
            String userCI,
            String cardUniqueNumber,
            Timestamp timestamp) {

        // 사용자 존재 여부 먼저 확인
        boolean userExists = userRepository.existsByUserCI(userCI);
        if (!userExists) {
            throw new UserNotFoundException("User not found with CI: " + userCI);
        }

        // 카드 존재 여부 확인 (사용자의 카드인지도 검증)
        boolean cardExists = cardRepository.existsByCardUniqueNumberAndUserCI(cardUniqueNumber, userCI);
        if (!cardExists) {
            throw new CardNotFoundException("Card not found or doesn't belong to this user. Card unique number: " + cardUniqueNumber);
        }

        // 조건에 맞는 거래내역 조회 (cardUniqueNumber와 timestamp 기준)
        return cardTransactionRepository.findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
                cardUniqueNumber,
                timestamp
        );
    }
}