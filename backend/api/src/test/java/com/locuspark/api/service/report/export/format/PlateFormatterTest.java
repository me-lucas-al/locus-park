package com.locuspark.api.service.report.export.format;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlateFormatterTest {

    @Test
    void deveMascararPlacaTradicionalComHifen() {
        assertThat(PlateFormatter.format("ABC1234")).isEqualTo("ABC-1234");
    }

    @Test
    void naoDeveInserirHifenEmPlacaMercosul() {
        assertThat(PlateFormatter.format("ABC1D23")).isEqualTo("ABC1D23");
    }

    @Test
    void deveDevolverValorCruQuandoPlacaLegadaEstaMalformada() {
        assertThat(PlateFormatter.format("XX")).isEqualTo("XX");
    }
}
