package com.locuspark.api.controller;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.service.ReportService;
import com.locuspark.api.service.report.ReportDetailLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponse> getReport(
            @RequestAttribute(name = "companyId", required = false) UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireCompany(companyId);
        ReportResponse response = reportService.getCompanyReport(
                companyId, new ReportFilter(from, to), ReportDetailLimit.JSON);
        return ResponseEntity.ok(response);
    }

    private void requireCompany(UUID companyId) {
        if (companyId == null) {
            throw new BusinessException("Usuário não vinculado a uma empresa.");
        }
    }
}
