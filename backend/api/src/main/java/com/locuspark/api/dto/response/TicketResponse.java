package com.locuspark.api.dto.response;

import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID companyId,
        VehicleResponse vehicle,
        UUID partnershipId,
        LocalDateTime enteredAt,
        LocalDateTime exitedAt,
        TicketStatus status,
        BigDecimal totalAmount,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        PaymentMethod paymentMethod
) {}