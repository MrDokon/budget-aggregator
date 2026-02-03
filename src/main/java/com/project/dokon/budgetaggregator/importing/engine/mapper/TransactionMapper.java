package com.project.dokon.budgetaggregator.importing.engine.mapper;

import com.project.dokon.budgetaggregator.core.model.Transaction;
import com.project.dokon.budgetaggregator.importing.api.dto.TransactionSourceDto;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionSourceDto dto, String importJobId) {
        return new Transaction(
                null,
                dto.getTransactionId(),
                dto.getIban(),
                dto.getParsedDate(),
                dto.getParsedAmount(),
                dto.getCurrency(),
                dto.getCategory(),
                importJobId
        );
    }
}