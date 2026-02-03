package com.project.dokon.budgetaggregator.statistics.api.dto;

import java.util.List;

public record MonthlyStatisticsResponse(
        int year,
        int month,
        String iban,
        List<CategoryStatDto> stats
) {
}
