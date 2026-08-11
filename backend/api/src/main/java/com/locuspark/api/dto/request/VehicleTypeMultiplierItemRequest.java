package com.locuspark.api.dto.request;

import com.locuspark.api.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VehicleTypeMultiplierItemRequest(
        @NotNull(message = "O tipo do veículo é obrigatório")
        VehicleType vehicleType,

        @NotNull(message = "O multiplicador é obrigatório")
        @Positive(message = "O multiplicador deve ser positivo")
        BigDecimal multiplier
) {}
