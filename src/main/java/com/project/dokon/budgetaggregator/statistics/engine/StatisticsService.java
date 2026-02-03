package com.project.dokon.budgetaggregator.statistics.engine;

import com.project.dokon.budgetaggregator.statistics.api.dto.CategoryStatDto;
import com.project.dokon.budgetaggregator.statistics.api.dto.MonthlyStatisticsResponse;
import com.project.dokon.budgetaggregator.core.model.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class StatisticsService {

    private final MongoTemplate mongoTemplate;

    public StatisticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public MonthlyStatisticsResponse getMonthlyStatistics(int year, int month, String iban) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Criteria criteria = Criteria.where("date").gte(startDate).lte(endDate);

        if (iban != null && !iban.isBlank()) {
            criteria.and("iban").is(iban);
        }

        Aggregation aggregation = newAggregation(
                match(criteria),
                group("category", "currency")
                        .sum("amount").as("totalAmount")
                        .count().as("transactionCount"),
                project("totalAmount", "transactionCount")
                        .and("_id.category").as("category")
                        .and("_id.currency").as("currency"),
                sort(Sort.Direction.ASC, "category")
        );

        AggregationResults<CategoryStatDto> results = mongoTemplate.aggregate(
                aggregation,
                Transaction.class,
                CategoryStatDto.class
        );

        return new MonthlyStatisticsResponse(year, month, iban, results.getMappedResults());
    }

}
