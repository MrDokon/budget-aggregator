package com.project.dokon.budgetaggregator.importing.web;

import com.project.dokon.budgetaggregator.importing.api.ImportFacade;
import com.project.dokon.budgetaggregator.importing.api.dto.ImportIdResponse;
import com.project.dokon.budgetaggregator.importing.model.ImportJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

    private final ImportFacade importFacade;

    public ImportController(ImportFacade importFacade) {
        this.importFacade = importFacade;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ImportIdResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String jobId = importFacade.submitTransactionImport(file);
        return ResponseEntity.accepted().body(new ImportIdResponse(jobId));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ImportJob> checkStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(importFacade.getImportStatus(jobId));
    }

}