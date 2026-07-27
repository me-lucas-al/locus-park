package com.locuspark.api.service.report;

import com.locuspark.api.enums.DiscountType;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.types.Cpf;
import com.locuspark.api.types.Plate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.OptionalLong;
import java.util.UUID;

public record TicketRecord(
        UUID ticketId,
        TicketStatus status,
        Instant enteredAt,
        Instant exitedAt,
        BigDecimal totalAmount,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        PaymentMethod paymentMethod,
        Plate plate,
        String model,
        String color,
        VehicleType vehicleType,
        UUID clientId,
        String clientName,
        Cpf clientCpf,
        UUID partnershipId,
        String partnershipName,
        DiscountType partnershipDiscountType,
        BigDecimal partnershipValue
) {

    public static final ZoneId PATIO_ZONE = ZoneId.of("America/Sao_Paulo");

    public LocalDateTime enteredAtLocal() {
        return enteredAt.atZone(PATIO_ZONE).toLocalDateTime();
    }

    public LocalDateTime exitedAtLocal() {
        return exitedAt == null ? null : exitedAt.atZone(PATIO_ZONE).toLocalDateTime();
    }

    public BigDecimal net() {
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public BigDecimal gross() {
        return grossAmount != null ? grossAmount : net();
    }

    public BigDecimal discount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public OptionalLong stayMinutes() {
        return exitedAt == null ? OptionalLong.empty() : OptionalLong.of(Duration.between(enteredAt, exitedAt).toMinutes());
    }
}
