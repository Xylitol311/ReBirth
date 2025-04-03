package com.kkulmoo.rebirth.card.infrastructure.adapter;

import com.kkulmoo.rebirth.card.application.CardPort;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardDataRequest;
import com.kkulmoo.rebirth.common.exception.CardFetchException;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionRequest;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionSingleRequest;
import com.kkulmoo.rebirth.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPortImpl implements CardPort {

    @Qualifier("cardIssuerAPIClient") // 또는 secondApiClient 중 적절한 것 선택
    private final WebClient cardIssuerAPIClient;

    @Value("${api.cardissuer.base-url}")
    private String firstApiBaseUrl;

    @Override
    public List<CardApiResponse> fetchCardData(User user) {
        try {

            String endpoint = firstApiBaseUrl + "/api/cards/list";
            log.info("카드 API 요청 URL: {}", endpoint);
            log.info("요청 바디: {}", CardDataRequest.builder().userCI(user.getUserCI()).toString());


            // WebClient로 API 호출 후 바로 List<CardApiResponse>로 반환
            List<CardApiResponse> cardList = cardIssuerAPIClient.post()
                    .uri("/api/cards/list")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(CardDataRequest.builder().userCI(user.getUserCI()).build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CardApiResponse>>() {
                    })
                    .block();

            log.info("사용자 {}의 카드 {}개를 가져왔습니다", user.getUserName(),
                    cardList != null ? cardList.size() : 0);

            return cardList != null ? cardList : Collections.emptyList();
        } catch (Exception e) {
            log.error("사용자 {}의 카드 데이터 가져오기 실패: {}", user.getUserName(), e.getMessage());
            throw new CardFetchException("카드 데이터를 가져오는데 실패했습니다", e);
        }
    }

    @Override
    public Mono<List<CardTransactionResponse>> getCardTransaction(CardTransactionRequest cardTransactionRequest) {
        String userCI = cardTransactionRequest.getUserCI();
        List<myCard> cards = cardTransactionRequest.getCards();
        return Flux.fromIterable((cards))
                .flatMap(card -> {
                    CardTransactionSingleRequest singleRequest = CardTransactionSingleRequest.builder()
                            .userCI(cardTransactionRequest.getUserCI())
                            .cardUniqueNumber(card.getCardUniqueNumber())
                            .fromDate(card.getLatestLoadDataAt())
                            .build();

                    return cardIssuerAPIClient.post()
                            .uri("/api/transactions/getMyData")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(singleRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<CardTransactionResponse>>() {
                            })
                            .defaultIfEmpty(Collections.emptyList())
                            .doOnSuccess(txns -> log.info("사용자 CI: {}, 카드번호: {}의 거래내역 {}건 조회 완료",
                                    userCI, card.getCardUniqueNumber(), txns.size()))
                            .onErrorResume(e -> {
                                log.error("거래내역 조회 중 예외 발생 (카드번호: {}): {}", card.getCardUniqueNumber(), e.getMessage(), e);
                                return Mono.just(Collections.emptyList());
                            })
                            .flatMapMany(Flux::fromIterable); // 각 리스트를 개별 항목으로 평면화
                })
                .collectList();
    }
}