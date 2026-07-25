package com.locuspark.api.dto.response.report;

import com.locuspark.api.enums.VehicleType;

import java.math.BigDecimal;

public record VehicleTypeSummaryResponse(VehicleType type, long ticketCount, BigDecimal revenue, double sharePercent) {}
