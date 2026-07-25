package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.HourlySummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de HourlySummaryCalculator")
class HourlySummaryCalculatorTest {

    private final HourlySummaryCalculator calculator = new HourlySummaryCalculator();

    @Test
    @DisplayName("Deve sempre retornar 24 linhas, uma por hora do dia")
    void alwaysReturnsTwentyFourRows() {
        TicketWindow window = TicketWindow.of(List.of(), List.of(), 0);

        List<HourlySummaryResponse> result = calculator.calculate(window);

        assertThat(result).hasSize(24);
    }

    @Test
    @DisplayName("Deve contabilizar entrada e saida na hora correta")
    void countsEntryAndExitAtTheCorrectHour() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 15), LocalDateTime.of(2026, 1, 1, 14, 45));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(ticket), 0);

        List<HourlySummaryResponse> result = calculator.calculate(window);

        assertThat(result.get(8).entryCount()).isEqualTo(1);
        assertThat(result.get(14).exitCount()).isEqualTo(1);
        assertThat(result.get(14).revenue()).isEqualByComparingTo(java.math.BigDecimal.valueOf(80));
    }
}
