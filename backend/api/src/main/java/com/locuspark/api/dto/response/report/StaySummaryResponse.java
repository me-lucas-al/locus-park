package com.locuspark.api.dto.response.report;

public record StaySummaryResponse(
        double averageMinutes,
        long minimumMinutes,
        long maximumMinutes,
        long totalMinutes,
        long openStayCount
) {}
