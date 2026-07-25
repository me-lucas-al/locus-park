package com.locuspark.api.service.report;

import com.locuspark.api.enums.DiscountType;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.types.Cpf;
import com.locuspark.api.types.Plate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TicketRecordFixture {

    private TicketRecordFixture() {
    }

    public static TicketRecord paid(LocalDateTime enteredAt, LocalDateTime exitedAt) {
        return new TicketRecord(UUID.randomUUID(), TicketStatus.PAID, enteredAt, exitedAt,
                BigDecimal.valueOf(80), BigDecimal.valueOf(100), BigDecimal.valueOf(20), PaymentMethod.PIX,
                new Plate("ABC1234"), "Gol", "Prata", VehicleType.CAR,
                UUID.randomUUID(), "Maria Silva", new Cpf("12345678909"),
                null, null, null, null);
    }

    public static TicketRecord active(LocalDateTime enteredAt) {
        return new TicketRecord(UUID.randomUUID(), TicketStatus.ACTIVE, enteredAt, null,
                null, null, null, null,
                new Plate("ABC1234"), "Gol", "Prata", VehicleType.CAR,
                null, null, null,
                null, null, null, null);
    }

    public static TicketRecord legacy(LocalDateTime enteredAt, LocalDateTime exitedAt, BigDecimal totalAmount) {
        return new TicketRecord(UUID.randomUUID(), TicketStatus.PAID, enteredAt, exitedAt,
                totalAmount, null, null, PaymentMethod.DINHEIRO,
                new Plate("XYZ9876"), "Civic", "Preto", VehicleType.CAR,
                null, null, null,
                null, null, null, null);
    }

    public static TicketRecord withPartnership(LocalDateTime enteredAt, LocalDateTime exitedAt) {
        return new TicketRecord(UUID.randomUUID(), TicketStatus.PAID, enteredAt, exitedAt,
                BigDecimal.valueOf(72), BigDecimal.valueOf(90), BigDecimal.valueOf(18), PaymentMethod.CARD_CREDIT,
                new Plate("DEF5678"), "Onix", "Branco", VehicleType.CAR,
                UUID.randomUUID(), "João Souza", new Cpf("98765432100"),
                UUID.randomUUID(), "Academia VIP", DiscountType.PERCENTAGE, BigDecimal.valueOf(20));
    }
}
