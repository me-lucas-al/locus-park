package com.locuspark.api.controller;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.service.ReportService;
import com.locuspark.api.service.report.ReportDetailLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponse> getReport(@RequestAttribute("companyId") UUID companyId) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(29);
        ReportResponse response = reportService.getCompanyReport(companyId, from, to, ReportDetailLimit.JSON);
        return ResponseEntity.ok(response);
    }
}
