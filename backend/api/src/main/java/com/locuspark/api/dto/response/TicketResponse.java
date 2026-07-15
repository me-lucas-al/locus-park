package com.locuspark.api.dto.response;

import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID companyId,
        VehicleResponse vehicle,
        UUID partnershipId,
        Instant enteredAt,
        Instant exitedAt,
        TicketStatus status,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod
) {}