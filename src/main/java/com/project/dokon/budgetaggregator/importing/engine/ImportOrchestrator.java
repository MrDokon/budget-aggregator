package com.project.dokon.budgetaggregator.importing.engine;

import com.mongodb.DuplicateKeyException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImportOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ImportOrchestrator.class);
    private static final int BATCH_SIZE = 500;
    private static final int MAX_ERROR_COUNT = 1000;

    private final ImportJobService jobService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final Validator validator;
    private final ParserFactory parserFactory;

    public ImportOrchestrator(ImportJobService jobService,
                              TransactionRepository transactionRepository,
                              TransactionMapper transactionMapper,
                              Validator validator,
                              ParserFactory parserFactory) {
        this.jobService = jobService;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.validator = validator;
        this.parserFactory = parserFactory;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.imports}")
    public void handleFileUploaded(FileUploadedMessage message) {
        String jobId = message.importJobId();
        Path filePath = Paths.get(message.tempFilePath());
        List<String> processingErrors = new ArrayList<>();

        try {
            log.info("Received import job from RabbitMQ: {}", jobId);
            jobService.updateStatus(jobId, ImportStatus.PROCESSING);

            TransactionParser parser = parserFactory.getParser(filePath.toString());

            int successCount = processStream(filePath, parser, jobId, processingErrors);

            finalizeJob(jobId, successCount, processingErrors);

        } catch (Exception e) {
            log.error("Critical error processing job {}", jobId, e);
            addErrorSafe(processingErrors, "Critical system error: " + e.getMessage());
            jobService.failJob(jobId, processingErrors);
        } finally {
            deleteTempFile(filePath);
        }
    }

    private int processStream(Path filePath, TransactionParser parser, String jobId, List<String> globalErrors) throws IOException {
        var rowCounter = new AtomicInteger(0);
        var totalSaved = new AtomicInteger(0);

        List<Transaction> batchBuffer = new ArrayList<>(BATCH_SIZE);
        Set<String> seenIdsInFile = new HashSet<>();

        try (Stream<TransactionSourceDto> stream = parser.parse(filePath)) {

            stream.forEach(dto -> {
                int currentRow = rowCounter.incrementAndGet();

                processSingleRow(dto, currentRow, jobId, seenIdsInFile, globalErrors)
                        .ifPresent(transaction -> {
                            batchBuffer.add(transaction);

                            if (batchBuffer.size() >= BATCH_SIZE) {
                                int saved = saveBatch(batchBuffer, globalErrors);
                                totalSaved.addAndGet(saved);
                                batchBuffer.clear();
                            }
                        });
            });

            if (!batchBuffer.isEmpty()) {
                totalSaved.addAndGet(saveBatch(batchBuffer, globalErrors));
            }
        }
        return totalSaved.get();
    }

    private Optional<Transaction> processSingleRow(TransactionSourceDto dto,
                                                   int rowNumber,
                                                   String jobId,
                                                   Set<String> seenIdsInFile,
                                                   List<String> globalErrors) {

        Set<ConstraintViolation<TransactionSourceDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<TransactionSourceDto> v : violations) {
                addErrorSafe(globalErrors, "Row " + rowNumber + ": " + v.getMessage());
            }
            return Optional.empty();
        }

        if (!seenIdsInFile.add(dto.getTransactionId())) {
            addErrorSafe(globalErrors, "Row " + rowNumber + ": Duplicate transaction ID inside file: " + dto.getTransactionId());
            return Optional.empty();
        }

        try {
            Transaction transaction = transactionMapper.toEntity(dto, jobId);
            return Optional.of(transaction);
        } catch (Exception e) {
            addErrorSafe(globalErrors, "Row " + rowNumber + ": Mapping error - " + e.getMessage());
            return Optional.empty();
        }
    }

    private int saveBatch(List<Transaction> batch, List<String> globalErrors) {
        if (batch.isEmpty()) return 0;

        Set<String> incomingIds = batch.stream()
                .map(Transaction::transactionId)
                .collect(Collectors.toSet());

        List<Transaction> existingTransactions = transactionRepository.findAllByTransactionIdIn(incomingIds);
        Set<String> existingIds = existingTransactions.stream()
                .map(Transaction::transactionId)
                .collect(Collectors.toSet());

        List<Transaction> toSave = new ArrayList<>();
        for (Transaction tx : batch) {
            if (existingIds.contains(tx.transactionId())) {
                addErrorSafe(globalErrors, "Duplicate skipped (DB): " + tx.transactionId());
            } else {
                toSave.add(tx);
            }
        }

        if (!toSave.isEmpty()) {
            try {
                transactionRepository.saveAll(toSave);
                return toSave.size();
            } catch (DuplicateKeyException e) {
                addErrorSafe(globalErrors, "Duplicate key error during batch insert");
                return 0;
            }
        }
        return 0;
    }

    private void finalizeJob(String jobId, int successCount, List<String> processingErrors) {
        if (processingErrors.isEmpty()) {
            jobService.completeJob(jobId);
        } else if (successCount > 0) {
            jobService.completeJobWithErrors(jobId, processingErrors);
        } else {
            jobService.failJob(jobId, processingErrors);
        }
    }

    private void addErrorSafe(List<String> errors, String message) {
        if (errors.size() < MAX_ERROR_COUNT) {
            errors.add(message);
        } else if (errors.size() == MAX_ERROR_COUNT) {
            errors.add("... too many errors, truncation occurred.");
        }
    }

    private void deleteTempFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}", path, e);
        }
    }
}