package com.locuspark.api.service.report.export.format;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentFormatterTest {

    @Test
    void deveMascararCpf() {
        assertThat(DocumentFormatter.formatCpf("12345678909")).isEqualTo("123.456.789-09");
    }

    @Test
    void deveMascararCnpj() {
        assertThat(DocumentFormatter.formatCnpj("12345678000195")).isEqualTo("12.345.678/0001-95");
    }

    @Test
    void deveDevolverValorCruQuandoDocumentoEstaMalformado() {
        assertThat(DocumentFormatter.formatCpf("123")).isEqualTo("123");
        assertThat(DocumentFormatter.formatCnpj("abc")).isEqualTo("abc");
    }
}
