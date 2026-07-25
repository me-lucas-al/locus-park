package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.PartnershipSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de PartnershipSummaryCalculator")
class PartnershipSummaryCalculatorTest {

    private final PartnershipSummaryCalculator calculator = new PartnershipSummaryCalculator();

    @Test
    @DisplayName("usageCount deve incluir tickets ativos, discountGranted nao")
    void usageCountIncludesActiveTicketsButDiscountDoesNot() {
        TicketRecord paidWithPartnership = TicketRecordFixture.withPartnership(
                LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(paidWithPartnership), List.of(paidWithPartnership), 0);

        List<PartnershipSummaryResponse> result = calculator.calculate(window);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).usageCount()).isEqualTo(1);
        assertThat(result.get(0).discountGranted()).isEqualByComparingTo(java.math.BigDecimal.valueOf(18));
    }

    @Test
    @DisplayName("Deve ignorar tickets sem convenio vinculado")
    void ignoresTicketsWithoutPartnership() {
        TicketRecord plain = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(plain), List.of(), 0);

        List<PartnershipSummaryResponse> result = calculator.calculate(window);

        assertThat(result).isEmpty();
    }
}
