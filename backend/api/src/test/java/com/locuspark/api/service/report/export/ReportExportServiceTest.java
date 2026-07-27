package com.locuspark.api.service.report.export;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.service.report.export.csv.CsvReportExportWriter;
import com.locuspark.api.service.report.export.csv.CsvSectionWriter;
import com.locuspark.api.service.report.export.document.ReportDocumentAssembler;
import com.locuspark.api.service.report.export.document.ReportKpiFactory;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.section.*;
import com.locuspark.api.service.report.export.pdf.*;
import com.locuspark.api.service.report.export.xlsx.XlsxReportExportWriter;
import com.locuspark.api.service.report.export.xlsx.XlsxSheetWriter;
import com.locuspark.api.service.report.export.xlsx.XlsxSummarySheetWriter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportExportServiceTest {

    private final ReportLogo logo = mock(ReportLogo.class);
    private final ReportFileNameFactory fileNameFactory = new ReportFileNameFactory();

    private ReportExportService buildService() {
        when(logo.bytes()).thenReturn(new byte[0]);
        List<ReportSectionFactory> sections = List.of(
                new PaymentMethodSection(), new VehicleTypeSection(), new DailySection(), new HourlySection(),
                new PartnershipSection(), new ClientSection(), new TicketDetailSection());
        ReportDocumentAssembler assembler = new ReportDocumentAssembler(sections, new ReportKpiFactory(), logo);

        List<ReportExportWriter> writers = List.of(
                new PdfReportExportWriter(new PdfDocumentFactory(), new PdfHeaderRenderer(),
                        new PdfKpiGridRenderer(), new PdfDailyRevenueChartRenderer(), new PdfTableRenderer()),
                new XlsxReportExportWriter(new XlsxSummarySheetWriter(), new XlsxSheetWriter()),
                new CsvReportExportWriter(new CsvSectionWriter()));
        return new ReportExportService(writers, assembler, fileNameFactory);
    }

    @Test
    void deveRecusarExportacaoQuandoRelatorioEstaTruncado() {
        ReportExportService service = buildService();
        ReportResponse truncated = withTruncatedFlag(ReportResponseFixture.full());
        ReportFilter filter = new ReportFilter(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThatThrownBy(() -> service.export(truncated, filter, ReportExportFormat.PDF))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("acima do limite de exportação");
    }

    @Test
    void deveGerarArquivoValidoEmTodosOsFormatosParaRelatorioVazio() {
        ReportExportService service = buildService();
        ReportResponse empty = ReportResponseFixture.empty();
        ReportFilter filter = new ReportFilter(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        for (ReportExportFormat format : ReportExportFormat.values()) {
            ReportExportFile file = service.export(empty, filter, format);
            assertThat(file.content().length).isGreaterThan(0);
            assertThat(file.format()).isEqualTo(format);
            assertThat(file.fileName()).endsWith("." + format.fileExtension());
        }
    }

    private ReportResponse withTruncatedFlag(ReportResponse report) {
        return new ReportResponse(report.period(), report.company(), report.summary(),
                report.paymentMethodSummaries(), report.vehicleTypeSummaries(), report.dailySummaries(),
                report.hourlySummaries(), report.partnershipSummaries(), report.clientSummaries(),
                report.tickets(), 25_000, true, report.totalRevenue(), report.totalServices(), report.averageStayMinutes());
    }
}
