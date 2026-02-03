package com.project.dokon.budgetaggregator.importing.engine.parser;

import com.opencsv.bean.CsvToBeanBuilder;
import com.project.dokon.budgetaggregator.importing.api.dto.TransactionCsvDto;
import com.project.dokon.budgetaggregator.importing.api.dto.TransactionSourceDto;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class CsvTransactionParser implements TransactionParser {

    @Override
    public boolean supports(String originalFileName) {
        return originalFileName != null && originalFileName.toLowerCase().endsWith(".csv");
    }

    @Override
    public Stream<TransactionSourceDto> parse(Path filePath) throws IOException {
        BufferedReader reader = Files.newBufferedReader(filePath);

        try {
            var csvIterator = new CsvToBeanBuilder<TransactionCsvDto>(reader)
                    .withType(TransactionCsvDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build()
                    .iterator();
            var spliterator = Spliterators.spliteratorUnknownSize(csvIterator, Spliterator.ORDERED | Spliterator.NONNULL);

            Stream<TransactionCsvDto> stream = StreamSupport.stream(spliterator, false);
            return stream.map(dto -> (TransactionSourceDto) dto)
                    .onClose(() -> {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            throw new RuntimeException("Error closing CSV reader", e);
                        }
                    });
        } catch (Exception e) {
            reader.close();
            throw new IOException("Failed to create CSV stream", e);
        }
    }
}