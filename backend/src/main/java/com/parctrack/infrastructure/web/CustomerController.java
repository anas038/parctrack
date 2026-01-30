package com.parctrack.infrastructure.web;

import com.parctrack.application.customer.CustomerService;
import com.parctrack.application.dto.customer.*;
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
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CustomerDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List customers with pagination")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<CustomerDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(customerService.list(page, size, sortBy, sortDirection));
    }

    @GetMapping("/all")
    @Operation(summary = "List all customers (for dropdowns)")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CustomerDto>> listAll() {
        return ResponseEntity.ok(customerService.listAll());
    }

    @PostMapping
    @Operation(summary = "Create new customer")
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<CustomerDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete customer (cascades to sites, orphans equipment)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
