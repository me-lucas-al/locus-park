package com.locuspark.api.service.report.export.format;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReportDateFormatterTest {

    @Test
    void deveFormatarDataNoPadraoBrasileiro() {
        assertThat(ReportDateFormatter.formatDate(LocalDate.of(2026, 7, 25))).isEqualTo("25/07/2026");
    }

    @Test
    void deveFormatarDataHoraComMinutos() {
        LocalDateTime value = LocalDateTime.of(2026, 7, 25, 14, 32);
        assertThat(ReportDateFormatter.formatDateTime(value)).isEqualTo("25/07/2026 14:32");
    }

    @Test
    void deveRetornarTracoParaValoresNulos() {
        assertThat(ReportDateFormatter.formatDate(null)).isEqualTo("—");
        assertThat(ReportDateFormatter.formatDateTime(null)).isEqualTo("—");
    }
}
