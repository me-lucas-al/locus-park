package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.ClientSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de ClientSummaryCalculator")
class ClientSummaryCalculatorTest {

    private final ClientSummaryCalculator calculator = new ClientSummaryCalculator();

    @Test
    @DisplayName("Deve excluir tickets avulsos sem cliente vinculado")
    void excludesTicketsWithoutClient() {
        TicketRecord avulso = TicketRecordFixture.legacy(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0), java.math.BigDecimal.TEN);
        TicketWindow window = TicketWindow.of(List.of(avulso), List.of(), 0);

        List<ClientSummaryResponse> result = calculator.calculate(window);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve agrupar por cliente e listar formas de pagamento distintas ordenadas por ordinal")
    void groupsByClientAndListsDistinctPaymentMethods() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 0);

        List<ClientSummaryResponse> result = calculator.calculate(window);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ticketCount()).isEqualTo(1);
        assertThat(result.get(0).totalSpent()).isEqualByComparingTo(java.math.BigDecimal.valueOf(80));
        assertThat(result.get(0).paymentMethodsUsed()).containsExactly(com.locuspark.api.enums.PaymentMethod.PIX);
    }
}
