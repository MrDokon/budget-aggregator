package com.project.dokon.budgetaggregator.importing.engine;

import com.project.dokon.budgetaggregator.importing.api.ImportFacade;
import com.project.dokon.budgetaggregator.importing.event.FileUploadedMessage;
import com.project.dokon.budgetaggregator.importing.model.ImportJob;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImportFacadeImpl implements ImportFacade {

    private final ImportJobService jobService;
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public ImportFacadeImpl(ImportJobService jobService,
                            RabbitTemplate rabbitTemplate,
                            @Value("${app.rabbitmq.queue.imports}") String queueName) {
        this.jobService = jobService;
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    @Override
    public String submitTransactionImport(MultipartFile file) throws IOException {
        String jobId = UUID.randomUUID().toString();
        jobService.createJob(jobId);

        Path tempFile = Files.createTempFile("import-", ".csv");
        file.transferTo(tempFile.toFile());

        FileUploadedMessage message = new FileUploadedMessage(jobId, tempFile.toString());
        rabbitTemplate.convertAndSend(queueName, message);

        return jobId;
    }

    @Override
    public ImportJob getImportStatus(String jobId) {
        return jobService.getJob(jobId);
    }
}