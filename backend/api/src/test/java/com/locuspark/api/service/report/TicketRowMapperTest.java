package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.TicketRowResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de TicketRowMapper")
class TicketRowMapperTest {

    private final TicketRowMapper mapper = new TicketRowMapper();

    @Test
    @DisplayName("Deve mapear plate e cpf para string crua, sem mascara")
    void mapsPlateAndCpfAsRawStrings() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));

        List<TicketRowResponse> rows = mapper.map(List.of(ticket), ReportDetailLimit.JSON);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).plate()).isEqualTo("ABC1234");
        assertThat(rows.get(0).clientCpf()).isEqualTo("12345678909");
        assertThat(rows.get(0).stayMinutes()).isEqualTo(60L);
    }

    @Test
    @DisplayName("Ticket ativo sem saida deve mapear stayMinutes como nulo")
    void activeTicketMapsStayMinutesAsNull() {
        TicketRecord active = TicketRecordFixture.active(LocalDateTime.of(2026, 1, 1, 8, 0));

        List<TicketRowResponse> rows = mapper.map(List.of(active), ReportDetailLimit.JSON);

        assertThat(rows.get(0).stayMinutes()).isNull();
        assertThat(rows.get(0).exitedAt()).isNull();
    }

    @Test
    @DisplayName("Deve truncar a lista no limite configurado")
    void truncatesListAtConfiguredLimit() {
        TicketRecord a = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketRecord b = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 2, 8, 0), LocalDateTime.of(2026, 1, 2, 9, 0));

        List<TicketRowResponse> rows = mapper.map(List.of(a, b), new ReportDetailLimit(1));

        assertThat(rows).hasSize(1);
    }
}
