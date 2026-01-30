package com.parctrack.infrastructure.web;

import com.parctrack.application.dto.site.*;
import com.parctrack.application.site.SiteService;
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
@RequestMapping("/api/sites")
@Tag(name = "Sites", description = "Site management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get site by ID")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SiteDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(siteService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List sites with pagination, optionally filtered by customer")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<SiteDto>> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(siteService.list(customerId, page, size, sortBy, sortDirection));
    }

    @GetMapping("/all")
    @Operation(summary = "List all sites (for dropdowns), optionally filtered by customer")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SiteDto>> listAll(@RequestParam(required = false) UUID customerId) {
        return ResponseEntity.ok(siteService.listAll(customerId));
    }

    @PostMapping
    @Operation(summary = "Create new site")
    public ResponseEntity<SiteDto> create(@Valid @RequestBody CreateSiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update site")
    public ResponseEntity<SiteDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateSiteRequest request) {
        return ResponseEntity.ok(siteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete site (orphans equipment)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        siteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
