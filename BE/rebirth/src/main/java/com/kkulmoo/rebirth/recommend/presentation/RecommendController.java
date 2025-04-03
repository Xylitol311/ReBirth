package com.kkulmoo.rebirth.recommend.presentation;

import com.kkulmoo.rebirth.analysis.domain.dto.response.ResponseDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardCategoriesJpaRepository;
import com.kkulmoo.rebirth.recommend.application.RecommendService;
import com.kkulmoo.rebirth.recommend.domain.dto.response.RecommendCardDTO;
import com.kkulmoo.rebirth.recommend.domain.dto.response.RecommendCardForCategoryDTO;
import com.kkulmoo.rebirth.recommend.domain.dto.response.Top3CardDTO;
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

    private final RecommendService recommendService;

    @GetMapping("/top3")
    public ResponseEntity<ResponseDTO> top3ForAll(@RequestParam Integer userId) {

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("전체 top3 카드 추천");
        Top3CardDTO data = recommendService.calculateRecommendCardForAll(userId);

        result.setData(data);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/category")
    public ResponseEntity<ResponseDTO> top3ForCategory(@RequestParam Integer userId) {

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("카테고리별 top3 카드 추천");
        List<RecommendCardForCategoryDTO> data = recommendService.calculateRecommendCardForCategory(userId);
        result.setData(data);
        return ResponseEntity.ok().body(result);
    }
}
