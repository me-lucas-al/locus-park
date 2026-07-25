package com.locuspark.api.dto.response.report;

import java.time.LocalDateTime;

public record OccupancySummaryResponse(
        int totalSpots,
        long entryCount,
        long exitCount,
        long activeCount,
        long peakConcurrentVehicles,
        LocalDateTime peakAt,
        double peakOccupancyRate,
        double averageOccupancyRate,
        double turnoverPerSpot
) {}
