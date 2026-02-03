package com.project.dokon.budgetaggregator.statistics.api.dto;

import java.math.BigDecimal;

public record CategoryStatDto(
        String category,
        String currency,
        BigDecimal totalAmount,
        long transactionCount
) {
}
