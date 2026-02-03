package com.project.dokon.budgetaggregator.core.repository;

import com.project.dokon.budgetaggregator.core.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findAllByTransactionIdIn(Collection<String> transactionIds);
}
