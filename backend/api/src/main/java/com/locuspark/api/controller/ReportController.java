package com.locuspark.api.controller;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.service.ReportService;
import com.locuspark.api.service.report.ReportDetailLimit;
import com.locuspark.api.service.report.export.ReportExportFile;
import com.locuspark.api.service.report.export.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ReportExportService reportExportService;

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

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestAttribute(name = "companyId", required = false) UUID companyId,
            @RequestParam ReportExportFormat format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireCompany(companyId);
        ReportFilter filter = new ReportFilter(from, to);
        ReportResponse report = reportService.getCompanyReport(companyId, filter, ReportDetailLimit.EXPORT);
        ReportExportFile file = reportExportService.export(report, filter, format);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .contentLength(file.content().length)
                .body(file.content());
    }

    private void requireCompany(UUID companyId) {
        if (companyId == null) {
            throw new BusinessException("Usuário não vinculado a uma empresa.");
        }
    }
}
