package com.locuspark.api.dto.response.report;

public record ReportSummaryResponse(
        RevenueSummaryResponse revenue,
        StaySummaryResponse stay,
        OccupancySummaryResponse occupancy
) {}
