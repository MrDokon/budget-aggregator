package com.project.dokon.budgetaggregator.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "transaction")
public record Transaction(
        @Id
        String id,

        @Indexed(unique = true)
        String transactionId,

        @Indexed
        String iban,

        @Indexed
        LocalDate date,

        BigDecimal amount,
        String currency,

        @Indexed
        String category,

        String importJobId
        ) {
}
