package com.locuspark.api.service.report.export;

import com.locuspark.api.enums.ReportExportFormat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFileNameFactoryTest {

    private final ReportFileNameFactory factory = new ReportFileNameFactory();

    @Test
    void deveGerarNomeDeArquivoAsciiComExtensaoDoFormato() {
        String fileName = factory.fileName(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), ReportExportFormat.PDF);

        assertThat(fileName).isEqualTo("relatorio-locus-park-2026-07-01-a-2026-07-31.pdf");
        assertThat(fileName).matches("^[\\x00-\\x7F]+$");
    }

    @Test
    void deveTrocarExtensaoConformeOFormato() {
        assertThat(factory.fileName(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), ReportExportFormat.XLSX))
                .endsWith(".xlsx");
        assertThat(factory.fileName(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), ReportExportFormat.CSV))
                .endsWith(".csv");
    }
}
