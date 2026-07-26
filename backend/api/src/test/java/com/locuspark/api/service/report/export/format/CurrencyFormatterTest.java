package com.locuspark.api.service.report.export.format;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyFormatterTest {

    private static final char NON_BREAKING_SPACE = (char) 0x00A0;

    @Test
    void deveFormatarComPrefixoRsEDecimalPtBr() {
        assertThat(CurrencyFormatter.format(BigDecimal.valueOf(1500.5))).isEqualTo("R$ 1.500,50");
    }

    @Test
    void deveFormatarSemPrefixoParaCsv() {
        assertThat(CurrencyFormatter.formatBare(BigDecimal.valueOf(1500.5))).isEqualTo("1.500,50");
    }

    @Test
    void naoDeveConterEspacoNaoQuebravel() {
        String formatted = CurrencyFormatter.format(BigDecimal.valueOf(10));
        assertThat(formatted.indexOf(NON_BREAKING_SPACE)).isEqualTo(-1);
    }

    @Test
    void deveTratarValorNuloComoZero() {
        assertThat(CurrencyFormatter.format(null)).isEqualTo("R$ 0,00");
    }
}
