package com.parctrack.infrastructure.web;

import com.parctrack.application.dto.equipment.ImportResult;
import com.parctrack.application.equipment.ImportExportService;
import com.parctrack.domain.equipment.AgreementStatus;
import com.parctrack.domain.equipment.ServiceCycle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/equipment")
@Tag(name = "Import/Export", description = "Equipment import and export endpoints")
public class ImportExportController {

    private final ImportExportService importExportService;

    public ImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @PostMapping("/import")
    @Operation(summary = "Import equipment from Excel/CSV file")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ImportResult> importEquipment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean upsert) throws IOException {
        return ResponseEntity.ok(importExportService.importFromFile(file, upsert));
    }

    @GetMapping("/export")
    @Operation(summary = "Export equipment to Excel or PDF")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<byte[]> exportEquipment(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) AgreementStatus agreementStatus,
            @RequestParam(required = false) ServiceCycle serviceCycle,
            @RequestParam(required = false) String searchQuery) throws IOException {

        byte[] data;
        String contentType;
        String filename;

        if ("pdf".equalsIgnoreCase(format)) {
            data = importExportService.exportToPdf(agreementStatus, serviceCycle, searchQuery);
            contentType = MediaType.APPLICATION_PDF_VALUE;
            filename = "equipment.pdf";
        } else {
            data = importExportService.exportToExcel(agreementStatus, serviceCycle, searchQuery);
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "equipment.xlsx";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
