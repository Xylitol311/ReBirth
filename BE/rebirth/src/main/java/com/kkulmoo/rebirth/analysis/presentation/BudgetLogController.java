package com.kkulmoo.rebirth.analysis.presentation;

import com.kkulmoo.rebirth.analysis.application.service.BudgetLogService;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.DailyTransactionsDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.MonthlyLogInfoDTO;
import com.kkulmoo.rebirth.analysis.domain.dto.response.ResponseDTO;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budgetlog")
@RequiredArgsConstructor
public class BudgetLogController {

    private final BudgetLogService budgetLogService;

    @GetMapping("/log")
    public ResponseEntity<ResponseDTO> getMonthlyLog(@JwtUserId Integer userId,
                                                     @RequestParam("year") int year,
                                                     @RequestParam("month") int month) {

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("월별 가계부 내역 조회 완료");
        List<MonthlyLogDTO> monthlyLogs = budgetLogService.getMonthlyLog(userId, year, month);
        result.setData(monthlyLogs);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/transaction")
    public ResponseEntity<ResponseDTO> getMonthlyTransactions(@JwtUserId Integer userId,
                                                            @RequestParam("year") int year,
                                                            @RequestParam("month") int month) {

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("월별 거래 내역 조회 완료");
        List<DailyTransactionsDTO> monthlyLogs = budgetLogService.getMonthlyTransactions(userId, year, month);
        result.setData(monthlyLogs);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseDTO> getMonthlyBudgetLogInfo(@JwtUserId Integer userId,
                                                               @RequestParam("year") int year,
                                                               @RequestParam("month") int month) {

        ResponseDTO result = new ResponseDTO();
        result.setSuccess(true);
        result.setMessage("당월 거래 현황 조회 완료");
        MonthlyLogInfoDTO monthlyLogInfo = budgetLogService.getMonthlyLogInfo(userId, year, month);
        result.setData(monthlyLogInfo);
        return ResponseEntity.ok().body(result);
    }
}
