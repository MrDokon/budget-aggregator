package com.project.dokon.budgetaggregator.statistics.api;

import com.project.dokon.budgetaggregator.statistics.api.dto.MonthlyStatisticsResponse;

public interface StatisticsFacade {
    MonthlyStatisticsResponse getMonthlyStatistics(int year, int month, String iban);
}