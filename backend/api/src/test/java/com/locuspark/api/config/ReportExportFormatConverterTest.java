package com.locuspark.api.config;

import com.locuspark.api.enums.ReportExportFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportExportFormatConverterTest {

    private final ReportExportFormatConverter converter = new ReportExportFormatConverter();

    @Test
    void deveConverterValorEmMinusculas() {
        assertThat(converter.convert("pdf")).isEqualTo(ReportExportFormat.PDF);
        assertThat(converter.convert("xlsx")).isEqualTo(ReportExportFormat.XLSX);
        assertThat(converter.convert("csv")).isEqualTo(ReportExportFormat.CSV);
    }

    @Test
    void deveConverterValorEmMaiusculas() {
        assertThat(converter.convert("PDF")).isEqualTo(ReportExportFormat.PDF);
    }

    @Test
    void deveLancarExcecaoParaFormatoInvalido() {
        assertThatThrownBy(() -> converter.convert("docx")).isInstanceOf(IllegalArgumentException.class);
    }
}
