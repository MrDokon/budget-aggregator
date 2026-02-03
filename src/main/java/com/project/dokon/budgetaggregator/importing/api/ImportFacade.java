package com.project.dokon.budgetaggregator.importing.api;

import com.project.dokon.budgetaggregator.importing.model.ImportJob;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportFacade {
    String submitTransactionImport(MultipartFile file) throws IOException;
    ImportJob getImportStatus(String jobId);
}