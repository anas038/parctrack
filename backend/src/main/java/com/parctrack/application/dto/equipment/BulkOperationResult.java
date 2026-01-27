package com.parctrack.application.dto.equipment;

public record BulkOperationResult(
        int successCount,
        int failureCount,
        String message
) {}
