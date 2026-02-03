package com.project.dokon.budgetaggregator.importing.event;

public record FileUploadedMessage(
        String importJobId,
        String tempFilePath
) {}