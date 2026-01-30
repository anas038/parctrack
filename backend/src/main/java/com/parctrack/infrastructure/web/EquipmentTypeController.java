package com.parctrack.infrastructure.web;

import com.parctrack.application.dto.equipment.*;
import com.parctrack.application.equipment.EquipmentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment-types")
@Tag(name = "Equipment Types", description = "Equipment type management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    public EquipmentTypeController(EquipmentTypeService equipmentTypeService) {
        this.equipmentTypeService = equipmentTypeService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment type by ID")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<EquipmentTypeDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(equipmentTypeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List equipment types with pagination")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<EquipmentTypeDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(equipmentTypeService.list(page, size, sortBy, sortDirection));
    }

    @GetMapping("/all")
    @Operation(summary = "List all equipment types ordered by display order")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<EquipmentTypeDto>> listAll() {
        return ResponseEntity.ok(equipmentTypeService.listAll());
    }

    @GetMapping("/active")
    @Operation(summary = "List active equipment types (for dropdowns)")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<EquipmentTypeDto>> listActive() {
        return ResponseEntity.ok(equipmentTypeService.listActive());
    }

    @PostMapping
    @Operation(summary = "Create new equipment type")
    public ResponseEntity<EquipmentTypeDto> create(@Valid @RequestBody CreateEquipmentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentTypeService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment type")
    public ResponseEntity<EquipmentTypeDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateEquipmentTypeRequest request) {
        return ResponseEntity.ok(equipmentTypeService.update(id, request));
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder equipment types")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderEquipmentTypesRequest request) {
        equipmentTypeService.reorder(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete equipment type")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        equipmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
