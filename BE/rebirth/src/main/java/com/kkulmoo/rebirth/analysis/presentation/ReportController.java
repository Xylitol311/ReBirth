package com.kkulmoo.rebirth.analysis.presentation;

import com.kkulmoo.rebirth.analysis.application.service.ReportService;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCardDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportWithPatternDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ResponseDTO;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/frommydata")
    public ResponseEntity<ResponseDTO> getReportFromMyData(@JwtUserId Integer userId) {
        reportService.startWithMyData(userId);
        ResponseDTO responseDTO = ResponseDTO
                .builder()
                .success(true)
                .message("마이데이터 기반 리포트 생성 완료")
                .build();
        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> getReportWithPattern(@JwtUserId Integer userId,
                                                            @RequestParam int year, @RequestParam int month) {
        ReportWithPatternDTO report = reportService.getReportWithPattern(userId, year, month);
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("리포트 조회 완료");
        result.setData(report);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/card")
    public ResponseEntity<ResponseDTO> getReportCards(@JwtUserId Integer userId,
                                                      @RequestParam int year, @RequestParam int month) {

        List<ReportCardDTO> reportCards = reportService.getReportCards(userId, year, month);
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("카드별 조회 완료");
        result.setData(reportCards);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/category")
    public ResponseEntity<ResponseDTO> getReportCategories(@JwtUserId Integer userId,
                                                           @RequestParam int year, @RequestParam int month) {

        List<ReportCategoryDTO> reportCategories = reportService.getReportCategories(userId, year,month);
        Collections.sort(reportCategories, Comparator.comparing(ReportCategoryDTO::getAmount));

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("카테고리별 조회 완료");
        result.setData(reportCategories);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/test")
    public ResponseEntity<ResponseDTO> testTransaction(@JwtUserId Integer userId, @RequestParam LocalDateTime now) {
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        reportService.updateMonthlyTransactionSummary(userId);
        result.setMessage("트랜잭션 갱신 완료");


        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/test2")
    public ResponseEntity<ResponseDTO> testTransaction2(@JwtUserId Integer userId) {
        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("리포트 갱신 완료");
        reportService.updateWithMyData(userId);

        return ResponseEntity.ok().body(result);
    }
}
