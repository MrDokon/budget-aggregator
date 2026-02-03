package com.project.dokon.budgetaggregator.statistics.engine;

import com.project.dokon.budgetaggregator.statistics.api.StatisticsFacade;
import com.project.dokon.budgetaggregator.statistics.api.dto.MonthlyStatisticsResponse;
import org.springframework.stereotype.Service;

@Service
public class StatisticsFacadeImpl implements StatisticsFacade {

    private final StatisticsService statisticsService;

    public StatisticsFacadeImpl(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public MonthlyStatisticsResponse getMonthlyStatistics(int year, int month, String iban) {
        return statisticsService.getMonthlyStatistics(year, month, iban);
    }
}