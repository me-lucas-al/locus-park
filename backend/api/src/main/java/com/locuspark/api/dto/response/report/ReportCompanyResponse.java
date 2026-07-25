package com.locuspark.api.dto.response.report;

import java.util.UUID;

public record ReportCompanyResponse(UUID id, String name, String cnpj, Integer totalSpots) {}
