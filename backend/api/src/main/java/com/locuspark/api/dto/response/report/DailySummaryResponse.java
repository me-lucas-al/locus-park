package com.locuspark.api.dto.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryResponse(LocalDate date, long entryCount, long exitCount, BigDecimal revenue, BigDecimal discount) {}
