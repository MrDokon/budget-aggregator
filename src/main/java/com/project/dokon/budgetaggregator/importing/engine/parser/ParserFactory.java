package com.project.dokon.budgetaggregator.importing.engine.parser;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParserFactory {

    private final List<TransactionParser> parsers;

    public ParserFactory(List<TransactionParser> parsers) {
        this.parsers = parsers;
    }

    public TransactionParser getParser(String fileName) {
        return parsers.stream()
                .filter(parser -> parser.supports(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parser found for file: " + fileName));
    }

}