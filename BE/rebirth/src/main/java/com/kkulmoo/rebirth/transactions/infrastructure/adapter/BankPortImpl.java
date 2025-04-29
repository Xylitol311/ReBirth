package com.kkulmoo.rebirth.transactions.infrastructure.adapter;

import com.kkulmoo.rebirth.transactions.application.BankPort;
import com.kkulmoo.rebirth.transactions.application.dto.BankTransactionRequest;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionSingleRequest;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.UserCIDTO;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
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
                            .bodyToMono(new ParameterizedTypeReference<List<BankTransactionResponse>>() {
                            })
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

    @Override
    public Mono<List<String>> getAccountNumbersByUserCI(String userCI) {
        return ssafyBankAPIClient.get()
                .uri("/api/accounts/user/ci/{userCI}/accountNumbers", userCI)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                })
                .doOnSuccess(accountNumbers -> log.info("사용자 CI: {}의 계좌번호 {}개 조회 완료",
                        userCI, accountNumbers.size()))
                .onErrorResume(e -> {
                    log.error("사용자 계좌번호 조회 중 예외 발생 (사용자 CI: {}): {}", userCI, e.getMessage(), e);
                    return Mono.just(Collections.emptyList());
                });
    }

    @Override
    public Mono<UserCIDTO> getUserCI(UserCIRequest userCIRequest) {
        System.out.println(userCIRequest.toString());
        return ssafyBankAPIClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/userci")
                        .queryParam("userName", userCIRequest.getUserName())
                        .queryParam("birth", userCIRequest.getBirth())
                        .build())
                .retrieve()
                .bodyToMono(UserCIDTO.class)  // ApiResponseDTO 아닌 직접 UserCIDTO로 변환
                .doOnNext(dto -> log.info("사용자 CI 조회 성공: {}", dto))
                .doOnError(e -> log.error("사용자 CI 조회 중 오류 발생: {}", e.getMessage(), e))
                .onErrorResume(e -> {
                    log.error("사용자 CI 조회 실패 (userName: {}, birth: {}): {}",
                            userCIRequest.getUserName(), userCIRequest.getBirth(), e.getMessage());
                    return Mono.empty();
                });
    }
}
