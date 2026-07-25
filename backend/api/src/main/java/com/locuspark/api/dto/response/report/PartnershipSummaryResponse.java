package com.locuspark.api.dto.response.report;

import java.math.BigDecimal;
import java.util.UUID;

public record PartnershipSummaryResponse(UUID partnershipId, String name, long usageCount, BigDecimal discountGranted) {}
