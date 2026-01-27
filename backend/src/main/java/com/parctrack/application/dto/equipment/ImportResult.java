package com.parctrack.application.dto.equipment;

import java.util.List;

public record ImportResult(
        int totalRows,
        int successCount,
        int errorCount,
        List<ImportError> errors
) {
    public record ImportError(
            int row,
            String field,
            String message
    ) {}
}
