package com.kkulmoo.rebirth.recommend.presentation;

import com.kkulmoo.rebirth.analysis.domain.dto.response.ResponseDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardCategoriesJpaRepository;
import com.kkulmoo.rebirth.recommend.application.RecommendService;
import com.kkulmoo.rebirth.recommend.domain.dto.response.AvgAmountByCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {


    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
    private final RecommendService recommendService;

    @GetMapping("/test")
    public ResponseEntity<ResponseDTO> testAPI(@RequestParam Integer userId) {

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("테스트 성공");
        List<AvgAmountByCategoryDTO> data = recommendService.calculateRecommendCardForAll(userId);
        result.setData(data);
        return ResponseEntity.ok().body(result);
    }
}
