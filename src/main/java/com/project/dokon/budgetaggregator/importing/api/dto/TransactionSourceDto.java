package com.project.dokon.budgetaggregator.importing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionSourceDto {
    String getTransactionId();
    String getIban();
    String getCurrency();
    String getCategory();
    LocalDate getParsedDate();
    BigDecimal getParsedAmount();
}