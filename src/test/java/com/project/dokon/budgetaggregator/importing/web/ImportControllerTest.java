package com.project.dokon.budgetaggregator.importing.web;

import com.project.dokon.budgetaggregator.importing.api.ImportFacade;
import com.project.dokon.budgetaggregator.importing.enums.ImportStatus;
import com.project.dokon.budgetaggregator.importing.model.ImportJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportController.class)
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportFacade importFacade;

    @Test
    void shouldAcceptFileAndReturnJobId() throws Exception {
        String jobId = UUID.randomUUID().toString();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.csv",
                "text/csv",
                "header1,header2".getBytes()
        );

        when(importFacade.submitTransactionImport(any())).thenReturn(jobId);

        mockMvc.perform(multipart("/api/v1/imports")
                        .file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(jobId));
    }

    @Test
    void shouldReturnJobStatus() throws Exception {
        String jobId = "123-abc";
        ImportJob mockJob = new ImportJob(
                jobId,
                ImportStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                Collections.emptyList()
        );

        when(importFacade.getImportStatus(jobId)).thenReturn(mockJob);

        mockMvc.perform(get("/api/v1/imports/{jobId}", jobId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}