package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.RevenueSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de RevenueSummaryCalculator")
class RevenueSummaryCalculatorTest {

    private final RevenueSummaryCalculator calculator = new RevenueSummaryCalculator();

    @Test
    @DisplayName("Deve somar bruto, desconto e liquido apenas dos tickets pagos")
    void sumsGrossDiscountAndNetFromPaidTicketsOnly() {
        TicketRecord ticketA = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketRecord ticketB = TicketRecordFixture.withPartnership(LocalDateTime.of(2026, 1, 2, 8, 0), LocalDateTime.of(2026, 1, 2, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticketA, ticketB), List.of(), 0);

        RevenueSummaryResponse summary = calculator.calculate(window);

        assertThat(summary.grossRevenue()).isEqualByComparingTo(BigDecimal.valueOf(190));
        assertThat(summary.discountGranted()).isEqualByComparingTo(BigDecimal.valueOf(38));
        assertThat(summary.netRevenue()).isEqualByComparingTo(BigDecimal.valueOf(152));
        assertThat(summary.paidTicketCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve contar saidas gratuitas quando o liquido pago for zero")
    void countsFreeExitsWhenNetIsZero() {
        TicketRecord freeExit = new TicketRecord(java.util.UUID.randomUUID(), com.locuspark.api.enums.TicketStatus.PAID,
                LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 8, 5),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, com.locuspark.api.enums.PaymentMethod.PIX,
                new com.locuspark.api.types.Plate("FRE0001"), "Gol", "Prata", com.locuspark.api.enums.VehicleType.CAR,
                null, null, null, null, null, null, null);
        TicketWindow window = TicketWindow.of(List.of(freeExit), List.of(), 0);

        RevenueSummaryResponse summary = calculator.calculate(window);

        assertThat(summary.freeExitCount()).isEqualTo(1);
        assertThat(summary.lowestTicketValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve retornar zeros quando nao ha tickets pagos no periodo")
    void returnsZeroWhenNoTicketsPaid() {
        TicketWindow window = TicketWindow.of(List.of(), List.of(), 0);

        RevenueSummaryResponse summary = calculator.calculate(window);

        assertThat(summary.averageTicketValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.paidTicketCount()).isZero();
    }
}
