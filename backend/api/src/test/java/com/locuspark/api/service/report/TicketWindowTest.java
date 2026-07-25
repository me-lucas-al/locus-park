package com.locuspark.api.service.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de TicketWindow")
class TicketWindowTest {

    @Test
    @DisplayName("of() deve deduplicar um ticket presente em paid e entered")
    void deduplicatesTicketPresentInBothLists() {
        TicketRecord ticket = TicketRecordFixture.paid(
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0));

        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(ticket), 0);

        assertThat(window.all()).hasSize(1);
    }

    @Test
    @DisplayName("of() deve ordenar all() por data de entrada decrescente")
    void ordersAllByEnteredAtDescending() {
        TicketRecord older = TicketRecordFixture.active(LocalDateTime.of(2026, 1, 1, 8, 0));
        TicketRecord newer = TicketRecordFixture.active(LocalDateTime.of(2026, 1, 2, 8, 0));

        TicketWindow window = TicketWindow.of(List.of(), List.of(older, newer), 0);

        assertThat(window.all()).containsExactly(newer, older);
    }

    @Test
    @DisplayName("of() deve preservar presentAtStart e as listas originais paid/entered")
    void preservesPresentAtStartAndOriginalLists() {
        TicketRecord ticket = TicketRecordFixture.paid(
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0));

        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 7);

        assertThat(window.presentAtStart()).isEqualTo(7);
        assertThat(window.paid()).containsExactly(ticket);
        assertThat(window.entered()).isEmpty();
    }
}
