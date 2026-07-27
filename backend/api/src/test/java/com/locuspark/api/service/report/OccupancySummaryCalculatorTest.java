package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.OccupancySummaryResponse;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.types.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de OccupancySummaryCalculator")
class OccupancySummaryCalculatorTest {

    private static final ZoneId PATIO_ZONE = ZoneId.of("America/Sao_Paulo");

    private final OccupancySummaryCalculator calculator = new OccupancySummaryCalculator();
    private final LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
    private final LocalDateTime to = LocalDateTime.of(2026, 1, 2, 0, 0);

    private TicketRecord ticket(LocalDateTime enteredAt, LocalDateTime exitedAt) {
        return new TicketRecord(UUID.randomUUID(), exitedAt != null ? TicketStatus.PAID : TicketStatus.ACTIVE,
                enteredAt.atZone(PATIO_ZONE).toInstant(), exitedAt != null ? exitedAt.atZone(PATIO_ZONE).toInstant() : null,
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, PaymentMethod.PIX,
                new Plate("ABC1234"), "Gol", "Prata", VehicleType.CAR, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Uma saida e uma entrada no mesmo instante nao devem inflar o pico (-1 antes de +1)")
    void simultaneousExitAndEntryDoNotInflatePeak() {
        LocalDateTime tieInstant = LocalDateTime.of(2026, 1, 1, 12, 0);
        TicketRecord alreadyPresent = ticket(LocalDateTime.of(2025, 12, 31, 10, 0), tieInstant);
        TicketRecord enteringNow = ticket(tieInstant, null);
        TicketWindow window = TicketWindow.of(List.of(alreadyPresent), List.of(enteringNow), 1);

        OccupancySummaryResponse summary = calculator.calculate(window, 10, from, to);

        assertThat(summary.peakConcurrentVehicles()).isEqualTo(1);
    }

    @Test
    @DisplayName("totalSpots nulo deve resultar em taxas zeradas")
    void nullTotalSpotsResultsInZeroRates() {
        TicketWindow window = TicketWindow.of(List.of(), List.of(), 0);

        OccupancySummaryResponse summary = calculator.calculate(window, null, from, to);

        assertThat(summary.peakOccupancyRate()).isZero();
        assertThat(summary.averageOccupancyRate()).isZero();
        assertThat(summary.turnoverPerSpot()).isZero();
    }

    @Test
    @DisplayName("Deve contar entradas e saidas a partir das listas correspondentes")
    void countsEntriesAndExitsFromRespectiveLists() {
        TicketRecord exited = ticket(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketRecord stillActive = ticket(LocalDateTime.of(2026, 1, 1, 10, 0), null);
        TicketWindow window = TicketWindow.of(List.of(exited), List.of(exited, stillActive), 0);

        OccupancySummaryResponse summary = calculator.calculate(window, 5, from, to);

        assertThat(summary.entryCount()).isEqualTo(2);
        assertThat(summary.exitCount()).isEqualTo(1);
        assertThat(summary.activeCount()).isEqualTo(1);
    }
}
