package com.project.dokon.budgetaggregator.statistics.engine;

import com.project.dokon.budgetaggregator.core.model.Transaction;
import com.project.dokon.budgetaggregator.statistics.api.dto.CategoryStatDto;
import com.project.dokon.budgetaggregator.statistics.api.dto.MonthlyStatisticsResponse;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void shouldCreateCorrectAggregationForGivenMonth() {
        int year = 2024;
        int month = 1;
        String iban = null;

        CategoryStatDto stat1 = new CategoryStatDto("Food", "PLN", new BigDecimal("100.00"), 5);
        CategoryStatDto stat2 = new CategoryStatDto("Fun", "PLN", new BigDecimal("50.00"), 2);

        AggregationResults<CategoryStatDto> mockResults = new AggregationResults<>(
                List.of(stat1, stat2),
                new Document()
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Transaction.class), eq(CategoryStatDto.class)))
                .thenReturn(mockResults);

        MonthlyStatisticsResponse response = statisticsService.getMonthlyStatistics(year, month, iban);

        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.stats()).hasSize(2);
        assertThat(response.stats()).extracting("category").contains("Food", "Fun");

        ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(aggregationCaptor.capture(), eq(Transaction.class), eq(CategoryStatDto.class));

        Aggregation capturedAggregation = aggregationCaptor.getValue();
        String aggregationString = capturedAggregation.toString();

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        assertThat(aggregationString).contains(start.toString());
        assertThat(aggregationString).contains(end.toString());
        assertThat(aggregationString).doesNotContain("iban");
    }

    @Test
    void shouldAddIbanFilterWhenProvided() {
        int year = 2024;
        int month = 2;
        String iban = "PL123456789";

        AggregationResults<CategoryStatDto> emptyResults = new AggregationResults<>(List.of(), new Document());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Transaction.class), eq(CategoryStatDto.class)))
                .thenReturn(emptyResults);

        statisticsService.getMonthlyStatistics(year, month, iban);

        ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(aggregationCaptor.capture(), eq(Transaction.class), eq(CategoryStatDto.class));

        String aggregationString = aggregationCaptor.getValue().toString();

        assertThat(aggregationString).contains("iban");
        assertThat(aggregationString).contains(iban);
    }
}