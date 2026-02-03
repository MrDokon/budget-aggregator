package com.project.dokon.budgetaggregator.importing.engine.parser;

import com.project.dokon.budgetaggregator.importing.api.dto.TransactionSourceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CsvTransactionParserTest {
    private final CsvTransactionParser parser = new CsvTransactionParser();

    @TempDir
    Path tempDir;

    @Test
    void shouldParseCsvToStreamOfDtos() throws IOException {
        Path csvFile = tempDir.resolve("test-data.csv");
        String content = """
                transactionId,iban,amount,currency,date,category
                tx1,PL00001,100.50,PLN,2024-01-01,Food
                tx2,PL00002,200.00,USD,2024-01-02,Entertainment
                """;
        Files.writeString(csvFile, content);

        try (Stream<TransactionSourceDto> stream = parser.parse(csvFile)) {
            List<TransactionSourceDto> result = stream.toList();

            assertThat(result).hasSize(2);

            TransactionSourceDto first = result.getFirst();
            assertThat(first.getTransactionId()).isEqualTo("tx1");
            assertThat(first.getCategory()).isEqualTo("Food");
            assertThat(first.getParsedAmount()).isEqualByComparingTo("100.50");
        }
    }

    @Test
    void shouldSupportCsvFiles() {
        assertThat(parser.supports("file.csv")).isTrue();
        assertThat(parser.supports("file.CSV")).isTrue();
        assertThat(parser.supports("file.txt")).isFalse();
    }
}
