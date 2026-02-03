package com.project.dokon.budgetaggregator.importing.engine;

import com.project.dokon.budgetaggregator.core.model.Transaction;
import com.project.dokon.budgetaggregator.core.repository.TransactionRepository;
import com.project.dokon.budgetaggregator.importing.api.dto.TransactionSourceDto;
import com.project.dokon.budgetaggregator.importing.engine.mapper.TransactionMapper;
import com.project.dokon.budgetaggregator.importing.engine.parser.ParserFactory;
import com.project.dokon.budgetaggregator.importing.engine.parser.TransactionParser;
import com.project.dokon.budgetaggregator.importing.enums.ImportStatus;
import com.project.dokon.budgetaggregator.importing.event.FileUploadedMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportOrchestratorTest {

    @Mock private ImportJobService jobService;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private ParserFactory parserFactory;
    @Mock private TransactionParser transactionParser;
    @Mock private Validator validator;

    private ImportOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new ImportOrchestrator(
                jobService, transactionRepository, transactionMapper, validator, parserFactory
        );
    }

    @Test
    void shouldProcessValidFileAndCompleteJob() throws IOException {
        String jobId = "job-success";
        FileUploadedMessage message = new FileUploadedMessage(jobId, "test.csv");

        TransactionSourceDto dto1 = mock(TransactionSourceDto.class);
        when(dto1.getTransactionId()).thenReturn("tx1");

        when(parserFactory.getParser(anyString())).thenReturn(transactionParser);
        when(transactionParser.parse(any(Path.class))).thenReturn(Stream.of(dto1));
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(transactionMapper.toEntity(any(), any())).thenReturn(mock(Transaction.class));
        when(transactionRepository.findAllByTransactionIdIn(any())).thenReturn(Collections.emptyList());

        orchestrator.handleFileUploaded(message);

        verify(jobService).updateStatus(jobId, ImportStatus.PROCESSING);
        verify(transactionRepository, times(1)).saveAll(anyList());
        verify(jobService).completeJob(jobId);
    }

    @Test
    void shouldHandlePartialFailureAndCompleteWithErrors() throws IOException {
        String jobId = "job-partial";
        FileUploadedMessage message = new FileUploadedMessage(jobId, "test.csv");

        TransactionSourceDto validDto = mock(TransactionSourceDto.class);
        when(validDto.getTransactionId()).thenReturn("tx_valid");

        TransactionSourceDto invalidDto = mock(TransactionSourceDto.class);

        when(parserFactory.getParser(anyString())).thenReturn(transactionParser);
        when(transactionParser.parse(any(Path.class))).thenReturn(Stream.of(validDto, invalidDto));

        ConstraintViolation<TransactionSourceDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid data");

        when(validator.validate(validDto)).thenReturn(Collections.emptySet());
        when(validator.validate(invalidDto)).thenReturn(Set.of(violation));

        when(transactionMapper.toEntity(eq(validDto), any())).thenReturn(mock(Transaction.class));
        when(transactionRepository.findAllByTransactionIdIn(any())).thenReturn(Collections.emptyList());

        orchestrator.handleFileUploaded(message);

        verify(transactionRepository, times(1)).saveAll(anyList());

        verify(jobService).completeJobWithErrors(eq(jobId), argThat(errors ->
                errors.size() == 1 && errors.get(0).contains("Invalid data")
        ));
    }

    @Test
    void shouldFailJobWhenParserThrowsException() throws IOException {
        String jobId = "job-fail";
        FileUploadedMessage message = new FileUploadedMessage(jobId, "test.csv");

        when(parserFactory.getParser(anyString())).thenReturn(transactionParser);
        when(transactionParser.parse(any(Path.class))).thenThrow(new IOException("Corrupted file"));

        orchestrator.handleFileUploaded(message);

        verify(jobService).failJob(eq(jobId), anyList());
        verify(transactionRepository, never()).saveAll(any());
    }
}