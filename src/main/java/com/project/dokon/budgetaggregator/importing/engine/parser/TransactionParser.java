package com.project.dokon.budgetaggregator.importing.engine.parser;

import com.project.dokon.budgetaggregator.importing.api.dto.TransactionSourceDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface TransactionParser {
    boolean supports(String fileName);
    Stream<TransactionSourceDto> parse(Path filePath) throws IOException;
}