package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.RecordRequest;
import com.finance.dashboard.dto.response.RecordResponse;
import com.finance.dashboard.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.finance.dashboard.dto.response.PageResponse;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<RecordResponse> createRecord(@Valid @RequestBody RecordRequest request, Principal principal) {
        return new ResponseEntity<>(recordService.createRecord(request, principal.getName()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<RecordResponse>> getAllRecords(
            Principal principal,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.ok(recordService.getAllRecordsForUser(principal.getName(), pageNo, pageSize));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<RecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequest request,
            Principal principal) {
        return ResponseEntity.ok(recordService.updateRecord(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id, Principal principal) {
        recordService.deleteRecord(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
