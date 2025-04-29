package com.cardissuer.cardissuer.cards.application;

import com.cardissuer.cardissuer.cards.domain.Card;
import com.cardissuer.cardissuer.cards.domain.CardRepository;
import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.presentation.CardCreateRequest;
import com.cardissuer.cardissuer.common.exception.NotFoundException;
import com.cardissuer.cardissuer.common.exception.UnauthorizedException;
import com.cardissuer.cardissuer.common.exception.UserNotFoundException;
import com.cardissuer.cardissuer.transaction.presentation.PermanentTokenRequest;
import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    // 이렇게 의존성 주입하는건 싫지만 카드사는 귀찮아서.. 그냥 갑니다..
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public Card createCard(CardCreateRequest request) {
        // Card 도메인 객체 생성
        Card card = Card.builder()
                .cardUniqueNumber(CardUniqueNumber.of(null))
                .userCI(request.getUserCI())
                .accountNumber(request.getAccountNumber())
                .cardNumber(request.getCardNumber())
                .cardName(request.getCardName())
                .expiryDate(request.getExpiryDate())
                .cvc(request.getCvc())
                .cardPassword(request.getCardPassword())
                .createdAt(request.getCreatedAt())
                .build();

        System.out.println(request.getAccountNumber());
        // 엔티티로 변환하여 저장하고 다시 도메인 객체로 변환
        Card save = cardRepository.save(card);

        createNewPermanentToken(card);

        return save;
    }


    @Transactional
    public PermanentToken getPermanentToken(
            String userCI,
            PermanentTokenRequest permanentTokenRequest) {

        //User꺼내기
        Optional<User> optionalUser = userRepository.findByUserCI(userCI);
        Optional<Card> optionalCard = cardRepository.findByCardNumber(
                permanentTokenRequest.getCardNumber()
        );

        if (!optionalUser.isPresent()) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        if (!optionalCard.isPresent()) {
            throw new NotFoundException("카드를 찾을 수 없습니다.");
        }

        User user = optionalUser.get();
        Card card = optionalCard.get();

        //먼저 해당 유저의 카드가 맞는지.
        if (!user.getUserCI().equals(card.getUserCI())) {
            throw new UnauthorizedException("해당 카드에 접근 권한이 없습니다.");
        }

        Optional<PermanentToken> existingToken = cardRepository.findTokenByCardUniqueNumber(
                card.getCardUniqueNumber().getValue());

        return existingToken.get();
    }

    public void createNewPermanentToken(Card card) {
        String tokenValue = generateUniqueTokenValue();

        PermanentToken newToken = PermanentToken.builder()
                .cardUniqueNumber(card.getCardUniqueNumber().getValue())
                .token(tokenValue)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .card(card)
                .build();

        // 토큰 저장
        cardRepository.updateToken(newToken);
    }

    private String generateUniqueTokenValue() {
        return UUID.randomUUID().toString();
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByUserCI(String userCI) {
        Optional<User> optionalUser = userRepository.findByUserCI(userCI);

        // Optional에서 User를 안전하게 꺼내서 사용
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<Card> cards = cardRepository.findByUserCIAndDeletedAtIsNull(user.getUserCI());

            // Card 객체들을 CardResponse로 변환
            return cards.stream()
                    .map(card -> {
                        CardResponse response = new CardResponse();
                        response.setCardUniqueNumber(card.getCardUniqueNumber().getValue());
                        response.setCardName(card.getCardName());
                        return response;
                    })
                    .collect(Collectors.toList());
        } else {
            throw new UserNotFoundException("User not found with API key: " + userCI);
        }
    }
}