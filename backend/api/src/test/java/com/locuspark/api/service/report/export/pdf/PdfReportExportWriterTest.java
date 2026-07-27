package com.locuspark.api.service.report.export.pdf;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.service.report.export.ReportLogo;
import com.locuspark.api.service.report.export.ReportResponseFixture;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportDocumentAssembler;
import com.locuspark.api.service.report.export.document.ReportKpiFactory;
import com.locuspark.api.service.report.export.document.section.ClientSection;
import com.locuspark.api.service.report.export.document.section.DailySection;
import com.locuspark.api.service.report.export.document.section.HourlySection;
import com.locuspark.api.service.report.export.document.section.PartnershipSection;
import com.locuspark.api.service.report.export.document.section.PaymentMethodSection;
import com.locuspark.api.service.report.export.document.section.TicketDetailSection;
import com.locuspark.api.service.report.export.document.section.VehicleTypeSection;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfReportExportWriterTest {

    private PdfReportExportWriter writer;
    private ReportDocumentAssembler assembler;

    @BeforeEach
    void setUp() {
        ReportLogo logo = mock(ReportLogo.class);
        when(logo.bytes()).thenReturn(new byte[0]);
        List<com.locuspark.api.service.report.export.document.ReportSectionFactory> sections = List.of(
                new PaymentMethodSection(), new VehicleTypeSection(), new DailySection(), new HourlySection(),
                new PartnershipSection(), new ClientSection(), new TicketDetailSection());
        assembler = new ReportDocumentAssembler(sections, new ReportKpiFactory(), logo);
        writer = new PdfReportExportWriter(new PdfDocumentFactory(), new PdfHeaderRenderer(),
                new PdfKpiGridRenderer(), new PdfDailyRevenueChartRenderer(), new PdfTableRenderer());
    }

    @Test
    void deveGerarPdfValidoComAcentosPreservados() throws IOException {
        ReportResponse report = ReportResponseFixture.full();
        ReportDocument document = assembler.assemble(report);

        byte[] pdf = writer.write(document);

        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        try (PDDocument pdDocument = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(pdDocument);
            assertThat(text).contains("Convênio São Paulo");
            assertThat(text).contains("Caminhão");
            assertThat(text).contains("Página 1 de");
        }
    }

    @Test
    void deveGerarPdfValidoParaPeriodoVazio() throws IOException {
        ReportDocument document = assembler.assemble(ReportResponseFixture.empty());

        byte[] pdf = writer.write(document);

        assertThat(pdf.length).isGreaterThan(0);
        try (PDDocument pdDocument = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(pdDocument);
            assertThat(text).contains("Nenhum registro no período.");
        }
    }
}
