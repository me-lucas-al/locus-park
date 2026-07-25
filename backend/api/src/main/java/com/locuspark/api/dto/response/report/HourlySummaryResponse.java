package com.locuspark.api.dto.response.report;

import java.math.BigDecimal;

public record HourlySummaryResponse(int hour, long entryCount, long exitCount, BigDecimal revenue) {}
