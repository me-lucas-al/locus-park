package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.PaymentMethodSummaryResponse;
import com.locuspark.api.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de PaymentMethodSummaryCalculator")
class PaymentMethodSummaryCalculatorTest {

    private final PaymentMethodSummaryCalculator calculator = new PaymentMethodSummaryCalculator(new SharePercentCalculator());

    @Test
    @DisplayName("Deve emitir todos os valores do enum, inclusive os zerados")
    void emitsAllEnumValuesEvenWhenZeroed() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 0);

        List<PaymentMethodSummaryResponse> result = calculator.calculate(window);

        assertThat(result).hasSize(PaymentMethod.values().length);
        assertThat(result).anySatisfy(summary -> {
            assertThat(summary.method()).isEqualTo(PaymentMethod.DINHEIRO);
            assertThat(summary.ticketCount()).isZero();
        });
    }

    @Test
    @DisplayName("Deve calcular a participacao percentual sobre a receita total")
    void computesSharePercentOverTotalRevenue() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 0);

        List<PaymentMethodSummaryResponse> result = calculator.calculate(window);

        PaymentMethodSummaryResponse pix = result.stream().filter(r -> r.method() == PaymentMethod.PIX).findFirst().orElseThrow();
        assertThat(pix.sharePercent()).isEqualTo(100.0);
    }
}
