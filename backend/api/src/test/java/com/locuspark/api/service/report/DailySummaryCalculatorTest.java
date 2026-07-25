package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.DailySummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de DailySummaryCalculator")
class DailySummaryCalculatorTest {

    private final DailySummaryCalculator calculator = new DailySummaryCalculator();

    @Test
    @DisplayName("Deve emitir todo dia do periodo, inclusive os sem movimento")
    void emitsEveryDayInPeriodEvenWithoutMovement() {
        TicketWindow window = TicketWindow.of(List.of(), List.of(), 0);

        List<DailySummaryResponse> result = calculator.calculate(window, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));

        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(day -> assertThat(day.entryCount()).isZero());
    }

    @Test
    @DisplayName("Deve contabilizar entrada pela data de entrada e receita pela data de saida")
    void countsEntryByEnteredDateAndRevenueByExitedDate() {
        TicketRecord crossesMidnight = TicketRecordFixture.paid(
                LocalDateTime.of(2026, 1, 1, 23, 0), LocalDateTime.of(2026, 1, 2, 1, 0));
        TicketWindow window = TicketWindow.of(List.of(crossesMidnight), List.of(crossesMidnight), 0);

        List<DailySummaryResponse> result = calculator.calculate(window, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));

        DailySummaryResponse day1 = result.get(0);
        DailySummaryResponse day2 = result.get(1);
        assertThat(day1.entryCount()).isEqualTo(1);
        assertThat(day1.revenue()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(day2.entryCount()).isZero();
        assertThat(day2.revenue()).isEqualByComparingTo(java.math.BigDecimal.valueOf(80));
    }
}
