package com.locuspark.api.service.report.export.format;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DurationFormatterTest {

    @Test
    void deveFormatarHorasEMinutos() {
        assertThat(DurationFormatter.format(150L)).isEqualTo("2h 30min");
    }

    @Test
    void deveFormatarApenasMinutosQuandoMenorQueUmaHora() {
        assertThat(DurationFormatter.format(45L)).isEqualTo("45min");
    }

    @Test
    void deveIndicarEstadiaEmAbertoParaValorNulo() {
        assertThat(DurationFormatter.format(null)).isEqualTo("Em aberto");
    }
}
