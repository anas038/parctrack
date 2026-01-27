package com.parctrack.application.equipment;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.equipment.ImportResult;
import com.parctrack.domain.equipment.*;
import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.organization.OrganizationRepository;
import com.parctrack.infrastructure.security.TenantContext;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.ResourceNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ImportExportService {

    private final EquipmentRepository equipmentRepository;
    private final OrganizationRepository organizationRepository;
    private final StoplightService stoplightService;
    private final AuditService auditService;

    public ImportExportService(
            EquipmentRepository equipmentRepository,
            OrganizationRepository organizationRepository,
            StoplightService stoplightService,
            AuditService auditService) {
        this.equipmentRepository = equipmentRepository;
        this.organizationRepository = organizationRepository;
        this.stoplightService = stoplightService;
        this.auditService = auditService;
    }

    @Transactional
    public ImportResult importFromFile(MultipartFile file, boolean upsertMode) throws IOException {
        UUID orgId = TenantContext.getCurrentTenant();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        String filename = file.getOriginalFilename();
        if (filename != null && filename.endsWith(".csv")) {
            return importFromCsv(file, organization, upsertMode);
        } else {
            return importFromExcel(file, organization, upsertMode);
        }
    }

    private ImportResult importFromCsv(MultipartFile file, Organization organization, boolean upsertMode) throws IOException {
        List<ImportResult.ImportError> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                return new ImportResult(0, 0, 0, errors);
            }

            String[] headers = rows.get(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].toLowerCase().trim(), i);
            }

            for (int i = 1; i < rows.size(); i++) {
                totalRows++;
                String[] row = rows.get(i);
                try {
                    Equipment equipment = parseRow(headerMap, row, organization, upsertMode);
                    if (equipment != null) {
                        equipmentRepository.save(equipment);
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add(new ImportResult.ImportError(i + 1, null, e.getMessage()));
                }
            }
        } catch (CsvException e) {
            throw new IOException("Failed to parse CSV file", e);
        }

        auditService.logAction("EQUIPMENT_IMPORT", "Equipment", null,
                "Imported " + successCount + "/" + totalRows + " rows");

        return new ImportResult(totalRows, successCount, errors.size(), errors);
    }

    private ImportResult importFromExcel(MultipartFile file, Organization organization, boolean upsertMode) throws IOException {
        List<ImportResult.ImportError> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                return new ImportResult(0, 0, 0, errors);
            }

            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                headerMap.put(getCellStringValue(cell).toLowerCase().trim(), cell.getColumnIndex());
            }

            while (rowIterator.hasNext()) {
                totalRows++;
                Row row = rowIterator.next();
                try {
                    Equipment equipment = parseExcelRow(headerMap, row, organization, upsertMode);
                    if (equipment != null) {
                        equipmentRepository.save(equipment);
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add(new ImportResult.ImportError(row.getRowNum() + 1, null, e.getMessage()));
                }
            }
        }

        auditService.logAction("EQUIPMENT_IMPORT", "Equipment", null,
                "Imported " + successCount + "/" + totalRows + " rows");

        return new ImportResult(totalRows, successCount, errors.size(), errors);
    }

    private Equipment parseRow(Map<String, Integer> headerMap, String[] row, Organization organization, boolean upsertMode) {
        String serialNumber = getColumnValue(headerMap, row, "serial_number", "serialnumber");
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number is required");
        }

        Optional<Equipment> existingOpt = equipmentRepository.findBySerialNumberAndOrganizationId(serialNumber, organization.getId());

        if (existingOpt.isPresent() && !upsertMode) {
            throw new IllegalArgumentException("Equipment already exists: " + serialNumber);
        }

        Equipment equipment = existingOpt.orElseGet(() -> new Equipment(
                organization,
                serialNumber,
                AgreementStatus.PENDING,
                ServiceCycle.QUARTERLY
        ));

        String custAssetId = getColumnValue(headerMap, row, "cust_asset_id", "custassetid", "asset_id");
        if (custAssetId != null) {
            equipment.setCustAssetId(custAssetId);
        }

        String agreementStatusStr = getColumnValue(headerMap, row, "agreement_status", "agreementstatus", "status");
        if (agreementStatusStr != null) {
            try {
                equipment.setAgreementStatus(AgreementStatus.valueOf(agreementStatusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid agreement status: " + agreementStatusStr);
            }
        }

        String serviceCycleStr = getColumnValue(headerMap, row, "service_cycle", "servicecycle", "cycle");
        if (serviceCycleStr != null) {
            try {
                equipment.setServiceCycle(ServiceCycle.valueOf(serviceCycleStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid service cycle: " + serviceCycleStr);
            }
        }

        String nextServiceStr = getColumnValue(headerMap, row, "next_service", "nextservice");
        if (nextServiceStr != null && !nextServiceStr.isBlank()) {
            try {
                equipment.setNextService(LocalDate.parse(nextServiceStr, DateTimeFormatter.ISO_DATE));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format for next_service: " + nextServiceStr);
            }
        }

        return equipment;
    }

    private Equipment parseExcelRow(Map<String, Integer> headerMap, Row row, Organization organization, boolean upsertMode) {
        String serialNumberValue = getCellStringValue(row, headerMap.get("serial_number"));
        if (serialNumberValue == null) {
            serialNumberValue = getCellStringValue(row, headerMap.get("serialnumber"));
        }
        if (serialNumberValue == null || serialNumberValue.isBlank()) {
            throw new IllegalArgumentException("Serial number is required");
        }
        final String serialNumber = serialNumberValue;

        Optional<Equipment> existingOpt = equipmentRepository.findBySerialNumberAndOrganizationId(serialNumber, organization.getId());

        if (existingOpt.isPresent() && !upsertMode) {
            throw new IllegalArgumentException("Equipment already exists: " + serialNumber);
        }

        Equipment equipment = existingOpt.orElseGet(() -> new Equipment(
                organization,
                serialNumber,
                AgreementStatus.PENDING,
                ServiceCycle.QUARTERLY
        ));

        String custAssetId = getCellStringValue(row, headerMap.get("cust_asset_id"));
        if (custAssetId == null) custAssetId = getCellStringValue(row, headerMap.get("custassetid"));
        if (custAssetId != null && !custAssetId.isBlank()) {
            equipment.setCustAssetId(custAssetId);
        }

        String agreementStatus = getCellStringValue(row, headerMap.get("agreement_status"));
        if (agreementStatus == null) agreementStatus = getCellStringValue(row, headerMap.get("agreementstatus"));
        if (agreementStatus != null && !agreementStatus.isBlank()) {
            try {
                equipment.setAgreementStatus(AgreementStatus.valueOf(agreementStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid agreement status: " + agreementStatus);
            }
        }

        String serviceCycle = getCellStringValue(row, headerMap.get("service_cycle"));
        if (serviceCycle == null) serviceCycle = getCellStringValue(row, headerMap.get("servicecycle"));
        if (serviceCycle != null && !serviceCycle.isBlank()) {
            try {
                equipment.setServiceCycle(ServiceCycle.valueOf(serviceCycle.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid service cycle: " + serviceCycle);
            }
        }

        return equipment;
    }

    private String getColumnValue(Map<String, Integer> headerMap, String[] row, String... columnNames) {
        for (String name : columnNames) {
            Integer idx = headerMap.get(name);
            if (idx != null && idx < row.length) {
                return row[idx].trim();
            }
        }
        return null;
    }

    private String getCellStringValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        return getCellStringValue(cell);
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    public byte[] exportToExcel(AgreementStatus agreementStatus, ServiceCycle serviceCycle, String searchQuery) throws IOException {
        UUID orgId = TenantContext.getCurrentTenant();
        Page<Equipment> page = equipmentRepository.findByOrganizationIdWithFilters(
                orgId, agreementStatus, serviceCycle, null, null, searchQuery, PageRequest.of(0, 10000)
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Equipment");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Serial Number", "Asset ID", "Agreement Status", "Service Cycle", "Last Service", "Next Service", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Equipment equipment : page.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(equipment.getSerialNumber());
                row.createCell(1).setCellValue(equipment.getCustAssetId() != null ? equipment.getCustAssetId() : "");
                row.createCell(2).setCellValue(equipment.getAgreementStatus().name());
                row.createCell(3).setCellValue(equipment.getServiceCycle().name());
                row.createCell(4).setCellValue(equipment.getLastService() != null ? equipment.getLastService().toString() : "");
                row.createCell(5).setCellValue(equipment.getNextService() != null ? equipment.getNextService().toString() : "");
                row.createCell(6).setCellValue(stoplightService.calculateStatus(equipment).name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportToPdf(AgreementStatus agreementStatus, ServiceCycle serviceCycle, String searchQuery) throws IOException {
        UUID orgId = TenantContext.getCurrentTenant();
        Page<Equipment> page = equipmentRepository.findByOrganizationIdWithFilters(
                orgId, agreementStatus, serviceCycle, null, null, searchQuery, PageRequest.of(0, 10000)
        );

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

            document.add(new com.itextpdf.text.Paragraph("Equipment Report", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD)));
            document.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);

            String[] headers = {"Serial Number", "Asset ID", "Agreement", "Cycle", "Next Service", "Status"};
            for (String header : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (Equipment equipment : page.getContent()) {
                table.addCell(new com.itextpdf.text.Phrase(equipment.getSerialNumber(), cellFont));
                table.addCell(new com.itextpdf.text.Phrase(equipment.getCustAssetId() != null ? equipment.getCustAssetId() : "", cellFont));
                table.addCell(new com.itextpdf.text.Phrase(equipment.getAgreementStatus().name(), cellFont));
                table.addCell(new com.itextpdf.text.Phrase(equipment.getServiceCycle().name(), cellFont));
                table.addCell(new com.itextpdf.text.Phrase(equipment.getNextService() != null ? equipment.getNextService().toString() : "", cellFont));
                table.addCell(new com.itextpdf.text.Phrase(stoplightService.calculateStatus(equipment).name(), cellFont));
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (com.itextpdf.text.DocumentException e) {
            throw new IOException("Failed to generate PDF", e);
        }
    }
}
