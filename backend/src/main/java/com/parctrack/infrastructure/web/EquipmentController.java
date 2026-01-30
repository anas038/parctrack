package com.parctrack.infrastructure.web;

import com.parctrack.application.dto.equipment.*;
import com.parctrack.application.equipment.EquipmentService;
import com.parctrack.domain.equipment.AgreementStatus;
import com.parctrack.domain.equipment.LifecycleStatus;
import com.parctrack.domain.equipment.ServiceCycle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment")
@Tag(name = "Equipment", description = "Equipment management endpoints")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup equipment by serial number, asset ID, or QR code")
    public ResponseEntity<EquipmentDto> lookup(@RequestParam String q) {
        return ResponseEntity.ok(equipmentService.lookup(q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by ID")
    public ResponseEntity<EquipmentDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List equipment with filters and pagination")
    public ResponseEntity<Page<EquipmentDto>> list(
            @RequestParam(required = false) AgreementStatus agreementStatus,
            @RequestParam(required = false) ServiceCycle serviceCycle,
            @RequestParam(required = false) LocalDate nextServiceFrom,
            @RequestParam(required = false) LocalDate nextServiceTo,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID siteId,
            @RequestParam(required = false) UUID equipmentTypeId,
            @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "serialNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        EquipmentFilterRequest filter = new EquipmentFilterRequest(
                agreementStatus, serviceCycle, nextServiceFrom, nextServiceTo,
                searchQuery, customerId, siteId, equipmentTypeId, lifecycleStatus,
                page, size, sortBy, sortDirection
        );

        return ResponseEntity.ok(equipmentService.list(filter));
    }

    @GetMapping("/orphaned")
    @Operation(summary = "List orphaned equipment (no site assigned)")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<EquipmentDto>> listOrphaned(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(equipmentService.findOrphanedEquipment(page, size));
    }

    @GetMapping("/orphaned/count")
    @Operation(summary = "Count orphaned equipment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Long> countOrphaned() {
        return ResponseEntity.ok(equipmentService.countOrphanedEquipment());
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get equipment service history")
    public ResponseEntity<EquipmentHistoryDto> getHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(equipmentService.getHistory(id));
    }

    @PostMapping
    @Operation(summary = "Create new equipment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<EquipmentDto> create(@Valid @RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<EquipmentDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateEquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.update(id, request));
    }

    @PostMapping("/{id}/service")
    @Operation(summary = "Mark equipment as serviced")
    public ResponseEntity<ServiceRecordDto> markServiced(
            @PathVariable UUID id,
            @RequestBody(required = false) MarkServicedRequest request) {
        return ResponseEntity.ok(equipmentService.markServiced(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete equipment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk/delete")
    @Operation(summary = "Bulk delete equipment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BulkOperationResult> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        return ResponseEntity.ok(equipmentService.bulkDelete(request));
    }

    @PostMapping("/bulk/update-status")
    @Operation(summary = "Bulk update agreement status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BulkOperationResult> bulkUpdateStatus(@Valid @RequestBody BulkUpdateStatusRequest request) {
        return ResponseEntity.ok(equipmentService.bulkUpdateStatus(request));
    }

    @PostMapping("/bulk/update-cycle")
    @Operation(summary = "Bulk update service cycle")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BulkOperationResult> bulkUpdateCycle(@Valid @RequestBody BulkUpdateCycleRequest request) {
        return ResponseEntity.ok(equipmentService.bulkUpdateCycle(request));
    }
}
