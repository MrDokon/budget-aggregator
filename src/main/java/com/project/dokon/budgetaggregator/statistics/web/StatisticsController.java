package com.project.dokon.budgetaggregator.statistics.web;

import com.project.dokon.budgetaggregator.statistics.api.StatisticsFacade;
import com.project.dokon.budgetaggregator.statistics.api.dto.MonthlyStatisticsResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
@Validated
public class StatisticsController {

    private final StatisticsFacade statisticsFacade;

    public StatisticsController(StatisticsFacade statisticsFacade) {
        this.statisticsFacade = statisticsFacade;
    }

    @GetMapping
    public ResponseEntity<MonthlyStatisticsResponse> getStatistics(
            @RequestParam("year") int year,
            @RequestParam("month") @Min(1) @Max(12) int month,
            @RequestParam(value = "iban", required = false) String iban
    ) {
        MonthlyStatisticsResponse response = statisticsFacade.getMonthlyStatistics(year, month, iban);
        return ResponseEntity.ok(response);
    }
}