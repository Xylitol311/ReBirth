package com.kkulmoo.rebirth.analysis.presentation;

import com.kkulmoo.rebirth.analysis.application.service.ReportService;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCardDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportWithPatternDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ResponseDTO;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserEntity;
import com.kkulmoo.rebirth.user.infrastrucutre.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserJpaRepository userJpaRepository;

    @PostMapping("/frommydata")
    public ResponseEntity<ResponseDTO> getReportFromMyData(@AuthenticationPrincipal(expression = "userId") Integer userId) {
        UserEntity user = userJpaRepository.getById(userId);
        reportService.startWithMyData(user);
        ResponseDTO responseDTO = ResponseDTO
                .builder()
                .success(true)
                .message("마이데이터 기반 리포트 생성 완료")
                .build();
        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> getReportWithPattern(@AuthenticationPrincipal(expression = "userId") Integer userId,
                                                            @RequestParam int year, @RequestParam int month) {
        ReportWithPatternDTO report = reportService.getReportWithPattern(userId, year, month);
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("리포트 조회 완료");
        result.setData(report);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/card")
    public ResponseEntity<ResponseDTO> getReportCards(@AuthenticationPrincipal(expression = "userId") Integer userId,
                                                     @RequestParam int year, @RequestParam int month) {

        List<ReportCardDTO> reportCards = reportService.getReportCards(userId, year, month);
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("카드별 조회 완료");
        result.setData(reportCards);

        return ResponseEntity.ok().body(result);
    }

//    @GetMapping("/category")
//    public ResponseEntity<ResponseDTO> getReportCategories() {
//
//        List<>
//    }
}
