package com.locuspark.api.service.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de TicketRecord")
class TicketRecordTest {

    @Test
    @DisplayName("Linha legada sem gross/discount deve usar totalAmount como fallback de gross e zero como desconto")
    void legacyRowFallsBackToTotalAmount() {
        TicketRecord record = TicketRecordFixture.legacy(
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0),
                BigDecimal.valueOf(30));

        assertThat(record.net()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(record.gross()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(record.discount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Ticket pago com bruto e desconto próprios não deve cair no fallback")
    void paidRowUsesOwnGrossAndDiscount() {
        TicketRecord record = TicketRecordFixture.paid(
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0));

        assertThat(record.gross()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(record.discount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(record.net()).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    @DisplayName("Ticket ativo sem saída deve retornar stayMinutes vazio")
    void activeTicketHasEmptyStayMinutes() {
        TicketRecord record = TicketRecordFixture.active(LocalDateTime.of(2026, 1, 1, 8, 0));

        assertThat(record.stayMinutes()).isEmpty();
        assertThat(record.net()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Ticket com entrada e saída deve calcular stayMinutes corretamente")
    void computesStayMinutesWhenExited() {
        TicketRecord record = TicketRecordFixture.paid(
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 9, 30));

        assertThat(record.stayMinutes()).hasValue(90);
    }
}
