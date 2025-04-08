package com.kkulmoo.rebirth.analysis.presentation;

import com.kkulmoo.rebirth.analysis.application.service.MainPageService;
import com.kkulmoo.rebirth.analysis.domain.dto.response.*;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainPageController {


    private final MainPageService mainPageService;

    @GetMapping("/summary")
    public ResponseEntity<ResponseDTO> getSummary(@RequestParam Integer userId) {
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("메인페이지 요약 조회 완료");
        MainSummaryDTO mainSummary = mainPageService.getSummary(userId);
        result.setData(mainSummary);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/summary/card")
    public ResponseEntity<ResponseDTO> getSummaryCard(@RequestParam Integer userId) {
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("메인페이지 카드별 요약 조회 완료");
        List<MainCardSummaryDTO> mainCardSummaryList = mainPageService.getCardSummary(userId);
        result.setData(mainCardSummaryList);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/summary/category")
    public ResponseEntity<ResponseDTO> getSummaryCategory(@RequestParam Integer userId) {
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("메인페이지 카테고리별 요약 조회 완료");
        List<ReportCategoryDTO> mainCategorySummaryList = mainPageService.getCategorySummary(userId);
        result.setData(mainCategorySummaryList);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/prebenefit")
    public ResponseEntity<ResponseDTO> getPrebenefit(@RequestParam Integer userId) {
        // TODO: 직전 거래 혜택 정보 불러오기
        PreBenefitDto preBenefitDto = mainPageService.getPreBenefit(userId);

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("직전 거래 혜택 피드백 조회 완료(추천 카드로 결제한 경우 제외)");
        result.setData(preBenefitDto);
        return ResponseEntity.ok().body(result);
    }
}