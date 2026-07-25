package com.locuspark.api.dto.response.report;

import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketRowResponse(
        UUID ticketId,
        TicketStatus status,
        String plate,
        String model,
        String color,
        VehicleType vehicleType,
        String clientName,
        String clientCpf,
        LocalDateTime enteredAt,
        LocalDateTime exitedAt,
        Long stayMinutes,
        String partnershipName,
        PaymentMethod paymentMethod,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {}
