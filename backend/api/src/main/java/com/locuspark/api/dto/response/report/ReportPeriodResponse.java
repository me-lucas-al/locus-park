package com.locuspark.api.dto.response.report;

import java.time.LocalDate;

public record ReportPeriodResponse(LocalDate from, LocalDate to, long days) {}
