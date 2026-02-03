package com.project.dokon.budgetaggregator.importing.model;

import com.project.dokon.budgetaggregator.importing.enums.ImportStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "import_jobs")
public record ImportJob(
        @Id
        String id,
        ImportStatus status,
        Instant createdAt,
        Instant finishedAt,
        List<String> errorReport) {

        public ImportJob withStatus(ImportStatus status) {
                return new ImportJob(id, status, createdAt, Instant.now(), errorReport);
        }

        public ImportJob withError(List<String> errors) {
                return new ImportJob(id, ImportStatus.FAILED, createdAt, Instant.now(), errors);
        }

}
