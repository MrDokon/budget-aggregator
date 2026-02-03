package com.project.dokon.budgetaggregator.importing.engine;

import com.project.dokon.budgetaggregator.importing.model.ImportJob;
import com.project.dokon.budgetaggregator.importing.enums.ImportStatus;
import com.project.dokon.budgetaggregator.importing.repository.ImportJobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class ImportJobService {

    private final ImportJobRepository repository;

    public ImportJobService(ImportJobRepository repository) {
        this.repository = repository;
    }

    public void createJob(String jobId) {
        ImportJob job = new ImportJob(
                jobId,
                ImportStatus.PENDING,
                Instant.now(),
                null,
                Collections.emptyList()
        );
        repository.save(job);
    }

    public ImportJob getJob(String jobId) {
        return repository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }

    public void updateStatus(String jobId, ImportStatus status) {
        repository.findById(jobId).ifPresent(job ->
                repository.save(job.withStatus(status))
        );
    }

    public void completeJob(String jobId) {
        updateStatus(jobId, ImportStatus.COMPLETED);
    }

    public void completeJobWithErrors(String jobId, List<String> errors) {
        repository.findById(jobId).ifPresent(job -> {
            ImportJob updatedJob = job.withError(errors)
                    .withStatus(ImportStatus.COMPLETED_WITH_ERRORS);
            repository.save(updatedJob);
        });
    }

    public void failJob(String jobId, List<String> errors) {
        repository.findById(jobId).ifPresent(job ->
                repository.save(job.withError(errors))
        );
    }
}