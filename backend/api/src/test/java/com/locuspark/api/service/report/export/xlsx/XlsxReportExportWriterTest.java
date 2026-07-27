package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.service.report.export.ReportLogo;
import com.locuspark.api.service.report.export.ReportResponseFixture;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportDocumentAssembler;
import com.locuspark.api.service.report.export.document.ReportKpiFactory;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.section.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XlsxReportExportWriterTest {

    private final XlsxReportExportWriter writer = new XlsxReportExportWriter(new XlsxSummarySheetWriter(), new XlsxSheetWriter());

    private ReportDocument assemble(ReportResponse report) {
        return assemble(report, new byte[0]);
    }

    private ReportDocument assemble(ReportResponse report, byte[] logoBytes) {
        ReportLogo logo = mock(ReportLogo.class);
        when(logo.bytes()).thenReturn(logoBytes);
        List<ReportSectionFactory> sections = List.of(
                new PaymentMethodSection(), new VehicleTypeSection(), new DailySection(), new HourlySection(),
                new PartnershipSection(), new ClientSection(), new TicketDetailSection());
        return new ReportDocumentAssembler(sections, new ReportKpiFactory(), logo).assemble(report);
    }

    @Test
    void deveGerarOitoAbasComCelulasTipadas() throws IOException {
        byte[] logoBytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(
                "src/main/resources/reports/locus-park-logo.png"));
        byte[] xlsx = writer.write(assemble(ReportResponseFixture.full(), logoBytes));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(8);
            assertThat(workbook.getSheetName(0)).isEqualTo("Resumo");
            assertThat(workbook.getNumCellStyles()).isLessThan(30);

            Sheet tickets = workbook.getSheet("Tickets");
            Row firstDataRow = tickets.getRow(1);
            Cell grossCell = firstDataRow.getCell(13);
            assertThat(grossCell.getCellType()).isEqualTo(CellType.NUMERIC);

            Sheet paymentMethods = workbook.getSheet("Formas de Pagamento");
            Cell percentCell = paymentMethods.getRow(1).getCell(3);
            assertThat(percentCell.getNumericCellValue()).isCloseTo(0.333, org.assertj.core.data.Offset.offset(0.001));

            assertThat(paymentMethods.getPaneInformation()).isNotNull();
            assertThat(paymentMethods.getColumnWidth(0)).isGreaterThan(0);
        }
    }

    @Test
    void deveGerarArquivoValidoParaPeriodoVazioSemAutofiltro() throws IOException {
        byte[] xlsx = writer.write(assemble(ReportResponseFixture.empty()));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet clients = workbook.getSheet("Clientes");
            assertThat(clients.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Nenhum registro no período.");
        }
        assertThat(xlsx.length).isGreaterThan(0);
    }

    @Test
    void naoDeveDeixarArquivosTemporariosAposEscrita() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        long before = countSxssfTempFiles(tmpDir);

        writer.write(assemble(ReportResponseFixture.full()));

        long after = countSxssfTempFiles(tmpDir);
        assertThat(after).isEqualTo(before);
    }

    private long countSxssfTempFiles(File dir) {
        File[] files = dir.listFiles((d, name) -> name.startsWith("poi-sxssf-sheet") && name.endsWith(".xml"));
        return files != null ? files.length : 0;
    }
}
