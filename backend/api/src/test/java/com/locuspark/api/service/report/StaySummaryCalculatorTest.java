package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.StaySummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de StaySummaryCalculator")
class StaySummaryCalculatorTest {

    private final StaySummaryCalculator calculator = new StaySummaryCalculator();

    @Test
    @DisplayName("Deve ignorar tickets ativos na media, mas contabiliza-los como estadias abertas")
    void ignoresActiveTicketsInAverageButCountsAsOpenStays() {
        TicketRecord paid = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 30));
        TicketRecord active = TicketRecordFixture.active(LocalDateTime.of(2026, 1, 1, 10, 0));
        TicketWindow window = TicketWindow.of(List.of(paid), List.of(paid, active), 0);

        StaySummaryResponse summary = calculator.calculate(window);

        assertThat(summary.averageMinutes()).isEqualTo(90.0);
        assertThat(summary.openStayCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar zeros quando a janela nao tem tickets")
    void returnsZeroForEmptyWindow() {
        TicketWindow window = TicketWindow.of(List.of(), List.of(), 0);

        StaySummaryResponse summary = calculator.calculate(window);

        assertThat(summary.averageMinutes()).isZero();
        assertThat(summary.minimumMinutes()).isZero();
        assertThat(summary.maximumMinutes()).isZero();
    }

    @Test
    @DisplayName("Deve calcular minimo e maximo entre estadias concluidas")
    void computesMinAndMaxAmongCompletedStays() {
        TicketRecord shortStay = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 8, 30));
        TicketRecord longStay = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 12, 0));
        TicketWindow window = TicketWindow.of(List.of(shortStay, longStay), List.of(), 0);

        StaySummaryResponse summary = calculator.calculate(window);

        assertThat(summary.minimumMinutes()).isEqualTo(30);
        assertThat(summary.maximumMinutes()).isEqualTo(240);
    }
}
