package com.locuspark.api.service.report.export.csv;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.service.report.export.ReportLogo;
import com.locuspark.api.service.report.export.ReportResponseFixture;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportDocumentAssembler;
import com.locuspark.api.service.report.export.document.ReportKpiFactory;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.section.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvReportExportWriterTest {

    private final CsvReportExportWriter writer = new CsvReportExportWriter(new CsvSectionWriter());

    private ReportDocument assemble(ReportResponse report) {
        ReportLogo logo = mock(ReportLogo.class);
        when(logo.bytes()).thenReturn(new byte[0]);
        List<ReportSectionFactory> sections = List.of(
                new PaymentMethodSection(), new VehicleTypeSection(), new DailySection(), new HourlySection(),
                new PartnershipSection(), new ClientSection(), new TicketDetailSection());
        return new ReportDocumentAssembler(sections, new ReportKpiFactory(), logo).assemble(report);
    }

    @Test
    void deveComecarComBomUtf8() {
        byte[] csv = writer.write(assemble(ReportResponseFixture.full()));

        assertThat(csv[0]).isEqualTo((byte) 0xEF);
        assertThat(csv[1]).isEqualTo((byte) 0xBB);
        assertThat(csv[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    void deveUsarPontoEVirgulaEQuebraDeLinhaExcel() {
        String content = new String(writer.write(assemble(ReportResponseFixture.full())), StandardCharsets.UTF_8);

        assertThat(content).contains("\r\n");
        assertThat(content).contains("FORMAS DE PAGAMENTO");
        assertThat(content).contains("Forma de Pagamento;Quantidade;Receita (R$);Participação");
    }

    @Test
    void deveFormatarMoedaSemPrefixoRsEComDecimalVirgula() {
        String content = new String(writer.write(assemble(ReportResponseFixture.full())), StandardCharsets.UTF_8);

        assertThat(content).contains("Dinheiro;100;5.000,00;33,3%");
    }

    @Test
    void deveEnvolverClienteComPontoEVirgulaEmAspas() {
        String content = new String(writer.write(assemble(ReportResponseFixture.full())), StandardCharsets.UTF_8);

        assertThat(content).contains("\"Silva; Souza & Cia\"");
    }

    @Test
    void deveGerarArquivoValidoParaPeriodoVazio() {
        byte[] csv = writer.write(assemble(ReportResponseFixture.empty()));

        String content = new String(csv, StandardCharsets.UTF_8);
        assertThat(content).contains("Nenhum registro no período.");
        assertThat(csv.length).isGreaterThan(0);
    }
}
