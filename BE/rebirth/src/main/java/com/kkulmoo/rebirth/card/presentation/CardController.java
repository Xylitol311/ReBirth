package com.kkulmoo.rebirth.card.presentation;

import com.kkulmoo.rebirth.card.application.CardService;
import com.kkulmoo.rebirth.card.application.dto.CardDetailResponse;
import com.kkulmoo.rebirth.card.application.dto.CardResponse;
import com.kkulmoo.rebirth.card.presentation.dto.CardOrderRequest;
import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import com.kkulmoo.rebirth.user.domain.UserId;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
public class CardController {
    private final CardService cardService;

    @GetMapping("/detail/{cardId}/{year}/{month}")
    public ResponseEntity<ApiResponseDTO<CardDetailResponse>> getMyCardInfo(@PathVariable Integer cardId,
                                                                            @PathVariable Integer year,
                                                                            @PathVariable Integer month) {
        try {
            CardDetailResponse cardDetail = cardService.getCardDetail(new UserId(2), cardId, year, month);
            return ResponseEntity.ok(ApiResponseDTO.success("카드 상세 정보 조회 성공", cardDetail));
        } catch (EntityNotFoundException e) {
            log.error("Card not found error", e);  // Add logging here
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("카드를 찾을 수 없습니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error when retrieving card details", e);  // Add logging here
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDTO.error("카드 정보 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    @GetMapping()
    public ResponseEntity<ApiResponseDTO<List<CardResponse>>> getAllCards(@JwtUserId Integer userId) {
        try {
            //todo: 이름 나중에 바꾸기
            List<CardResponse> cards = cardService.findCardsAll(new UserId(2));
            ApiResponseDTO<List<CardResponse>> response = ApiResponseDTO.success("카드 목록 조회 성공", cards);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponseDTO<List<CardResponse>> response = ApiResponseDTO.error("카드 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<ApiResponseDTO<Void>> reorderCards(@JwtUserId Integer userId, @RequestBody List<CardOrderRequest> cardOrders) {
        try {
            //todo: 이름 나중에 바꾸기
            cardService.updateCardsOrder(new UserId(2), cardOrders);
            ApiResponseDTO<Void> response = ApiResponseDTO.success("카드 순서 변경 성공", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponseDTO<Void> response = ApiResponseDTO.error("카드 순서 변경 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}