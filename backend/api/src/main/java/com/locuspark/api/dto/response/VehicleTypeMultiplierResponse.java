package com.locuspark.api.dto.response;

import com.locuspark.api.enums.VehicleType;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleTypeMultiplierResponse(
        UUID id,
        VehicleType vehicleType,
        BigDecimal multiplier,
        String label
) {}
