package com.locuspark.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VehicleTypePricingBatchRequest(
        @NotEmpty(message = "A lista de multiplicadores não pode ser vazia")
        @Valid
        List<VehicleTypeMultiplierItemRequest> multipliers
) {}
