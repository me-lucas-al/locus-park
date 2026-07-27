package com.locuspark.api.config;

import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.service.report.export.ReportExportWriter;
import com.locuspark.api.service.report.export.csv.CsvReportExportWriter;
import com.locuspark.api.service.report.export.csv.CsvSectionWriter;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.pdf.*;
import com.locuspark.api.service.report.export.xlsx.XlsxReportExportWriter;
import com.locuspark.api.service.report.export.xlsx.XlsxSheetWriter;
import com.locuspark.api.service.report.export.xlsx.XlsxSummarySheetWriter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportExportWarmupTest {

    @Test
    void deveAquecerTodosOsWritersComDocumentoVazio() {
        ReportExportWriter writer = mock(ReportExportWriter.class);
        when(writer.format()).thenReturn(ReportExportFormat.PDF);
        ReportExportWarmup warmup = new ReportExportWarmup(List.of(writer));

        warmup.beforeCheckpoint(null);

        verify(writer).write(ReportDocument.EMPTY);
    }

    @Test
    void falhaEmUmWriterNaoDeveInterromperOAquecimentoDosDemais() {
        ReportExportWriter broken = mock(ReportExportWriter.class);
        when(broken.format()).thenReturn(ReportExportFormat.PDF);
        when(broken.write(any())).thenThrow(new IllegalStateException("falhou"));

        ReportExportWriter healthy = mock(ReportExportWriter.class);
        when(healthy.format()).thenReturn(ReportExportFormat.CSV);

        ReportExportWarmup warmup = new ReportExportWarmup(List.of(broken, healthy));

        assertThatCode(() -> warmup.beforeCheckpoint(null)).doesNotThrowAnyException();
        verify(healthy).write(ReportDocument.EMPTY);
    }

    @Test
    void osTresWritersReaisDevemRenderizarReportDocumentEmptySemQuebrar() {
        List<ReportExportWriter> realWriters = List.of(
                new PdfReportExportWriter(new PdfDocumentFactory(), new PdfHeaderRenderer(),
                        new PdfKpiGridRenderer(), new PdfDailyRevenueChartRenderer(), new PdfTableRenderer()),
                new XlsxReportExportWriter(new XlsxSummarySheetWriter(), new XlsxSheetWriter()),
                new CsvReportExportWriter(new CsvSectionWriter()));

        for (ReportExportWriter writer : realWriters) {
            assertThatCode(() -> writer.write(ReportDocument.EMPTY)).doesNotThrowAnyException();
        }
    }
}
