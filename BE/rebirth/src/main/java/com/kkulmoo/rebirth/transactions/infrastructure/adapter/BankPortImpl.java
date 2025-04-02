package com.kkulmoo.rebirth.transactions.infrastructure.adapter;

import com.kkulmoo.rebirth.transactions.application.BankPort;
import com.kkulmoo.rebirth.transactions.application.dto.BankTransactionRequest;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionSingleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankPortImpl implements BankPort {

    private final WebClient ssafyBankAPIClient;

    @Override
    public Mono<List<BankTransactionResponse>> getBankTransaction(BankTransactionRequest bankTransactionRequest) {
        String userCI = bankTransactionRequest.getUserCI();
        List<String> bankAccounts = bankTransactionRequest.getBankAccounts();
        LocalDateTime timestamp = bankTransactionRequest.getTimestamp();


        return Flux.fromIterable(bankAccounts)
                .flatMap(account -> {
                    BankTransactionSingleRequest singleRequest = BankTransactionSingleRequest.builder()
                            .userCI(userCI)
                            .accountNumber(account)
                            .timestamp(timestamp)
                            .build();

                return ssafyBankAPIClient.post()
                        .uri("/api/transactions/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(singleRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<BankTransactionResponse>>() {})
                        .doOnSuccess(txns -> log.info("사용자 CI: {}, 계좌번호: {}의 거래내역 {}건 조회 완료",
                                userCI, account, txns.size()))
                        .onErrorResume(e -> {
                            log.error("계좌 거래내역 조회 중 예외 발생 (계좌번호: {}): {}", account, e.getMessage(), e);
                            return Mono.just(Collections.emptyList());
                        })
                        .flatMapMany(Flux::fromIterable);
                })
                .collectList();
    }
}
