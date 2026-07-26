package com.locuspark.api.service.report.export;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.*;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ReportResponseFixture {

    private static final LocalDate FROM = LocalDate.of(2026, 7, 1);
    private static final LocalDate TO = LocalDate.of(2026, 7, 31);

    private ReportResponseFixture() {
    }

    public static ReportResponse full() {
        RevenueSummaryResponse revenue = new RevenueSummaryResponse(
                BigDecimal.valueOf(15000), BigDecimal.valueOf(1500), BigDecimal.valueOf(13500),
                BigDecimal.valueOf(45.5), BigDecimal.valueOf(200), BigDecimal.ZERO, 300, 5);
        StaySummaryResponse stay = new StaySummaryResponse(125.5, 10, 480, 37650, 2);
        OccupancySummaryResponse occupancy = new OccupancySummaryResponse(
                50, 320, 300, 20, 45, LocalDateTime.of(2026, 7, 15, 18, 30), 0.9, 0.42, 6.0);

        List<PaymentMethodSummaryResponse> paymentMethods = List.of(
                new PaymentMethodSummaryResponse(PaymentMethod.DINHEIRO, 100, BigDecimal.valueOf(5000), 33.3),
                new PaymentMethodSummaryResponse(PaymentMethod.PIX, 200, BigDecimal.valueOf(10000), 66.7));

        List<VehicleTypeSummaryResponse> vehicleTypes = List.of(
                new VehicleTypeSummaryResponse(VehicleType.CAR, 280, BigDecimal.valueOf(14000), 93.3),
                new VehicleTypeSummaryResponse(VehicleType.TRUCK, 20, BigDecimal.valueOf(1000), 6.7));

        List<DailySummaryResponse> daily = List.of(
                new DailySummaryResponse(FROM, 10, 8, BigDecimal.valueOf(450), BigDecimal.valueOf(50)),
                new DailySummaryResponse(FROM.plusDays(1), 12, 11, BigDecimal.valueOf(500), BigDecimal.valueOf(40)));

        List<HourlySummaryResponse> hourly = List.of(
                new HourlySummaryResponse(8, 40, 5, BigDecimal.valueOf(800)),
                new HourlySummaryResponse(18, 10, 45, BigDecimal.valueOf(1200)));

        List<PartnershipSummaryResponse> partnerships = List.of(
                new PartnershipSummaryResponse(UUID.randomUUID(), "Convênio São Paulo", 40, BigDecimal.valueOf(800)));

        List<ClientSummaryResponse> clients = List.of(new ClientSummaryResponse(
                UUID.randomUUID(), "Silva; Souza & Cia", "12345678909", 12,
                BigDecimal.valueOf(600), 95.0, List.of(PaymentMethod.PIX, PaymentMethod.DINHEIRO)));

        List<TicketRowResponse> tickets = List.of(
                new TicketRowResponse(UUID.randomUUID(), TicketStatus.PAID, "ABC1D23", "Onix", "Branco",
                        VehicleType.CAR, "João Convênio", "98765432100",
                        LocalDateTime.of(2026, 7, 10, 8, 0), LocalDateTime.of(2026, 7, 10, 10, 30),
                        150L, "Convênio São Paulo", PaymentMethod.PIX,
                        BigDecimal.valueOf(30), BigDecimal.valueOf(6), BigDecimal.valueOf(24)),
                new TicketRowResponse(UUID.randomUUID(), TicketStatus.PAID, "XYZ9876", "Civic", "Preto",
                        VehicleType.CAR, null, null,
                        LocalDateTime.of(2026, 7, 11, 9, 0), LocalDateTime.of(2026, 7, 11, 9, 45),
                        45L, null, PaymentMethod.DINHEIRO,
                        null, null, BigDecimal.valueOf(20)),
                new TicketRowResponse(UUID.randomUUID(), TicketStatus.ACTIVE, "DEF5678", "Gol", "Prata",
                        VehicleType.CAR, "Maria Estacionada", "11122233344",
                        LocalDateTime.of(2026, 7, 20, 14, 0), null,
                        null, null, null, null, null, null));

        return build(revenue, stay, occupancy, paymentMethods, vehicleTypes, daily, hourly, partnerships, clients, tickets);
    }

    public static ReportResponse empty() {
        RevenueSummaryResponse revenue = new RevenueSummaryResponse(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        StaySummaryResponse stay = new StaySummaryResponse(0, 0, 0, 0, 0);
        OccupancySummaryResponse occupancy = new OccupancySummaryResponse(50, 0, 0, 0, 0, FROM.atStartOfDay(), 0, 0, 0);

        return build(revenue, stay, occupancy,
                ReportZeroedSectionsFixture.paymentMethods(), ReportZeroedSectionsFixture.vehicleTypes(),
                ReportZeroedSectionsFixture.daily(FROM, TO), ReportZeroedSectionsFixture.hourly(),
                List.of(), List.of(), List.of());
    }

    private static ReportResponse build(RevenueSummaryResponse revenue, StaySummaryResponse stay, OccupancySummaryResponse occupancy,
            List<PaymentMethodSummaryResponse> paymentMethods, List<VehicleTypeSummaryResponse> vehicleTypes,
            List<DailySummaryResponse> daily, List<HourlySummaryResponse> hourly,
            List<PartnershipSummaryResponse> partnerships, List<ClientSummaryResponse> clients, List<TicketRowResponse> tickets) {
        ReportCompanyResponse company = new ReportCompanyResponse(UUID.randomUUID(), "Estacionamento Convênio Ltda", "12345678000195", 120);
        ReportPeriodResponse period = new ReportPeriodResponse(FROM, TO, 31);
        ReportSummaryResponse summary = new ReportSummaryResponse(revenue, stay, occupancy);
        return new ReportResponse(period, company, summary, paymentMethods, vehicleTypes, daily, hourly,
                partnerships, clients, tickets, tickets.size(), false, revenue.netRevenue(), revenue.paidTicketCount(), stay.averageMinutes());
    }
}
