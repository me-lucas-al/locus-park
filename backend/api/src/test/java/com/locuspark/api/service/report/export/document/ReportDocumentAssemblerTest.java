package com.locuspark.api.service.report.export.document;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.service.report.export.ReportLogo;
import com.locuspark.api.service.report.export.ReportResponseFixture;
import com.locuspark.api.service.report.export.document.section.ClientSection;
import com.locuspark.api.service.report.export.document.section.DailySection;
import com.locuspark.api.service.report.export.document.section.HourlySection;
import com.locuspark.api.service.report.export.document.section.PartnershipSection;
import com.locuspark.api.service.report.export.document.section.PaymentMethodSection;
import com.locuspark.api.service.report.export.document.section.TicketDetailSection;
import com.locuspark.api.service.report.export.document.section.VehicleTypeSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportDocumentAssemblerTest {

    private final ReportLogo logo = mock(ReportLogo.class);
    private final List<ReportSectionFactory> sections = List.of(
            new PaymentMethodSection(), new VehicleTypeSection(), new DailySection(), new HourlySection(),
            new PartnershipSection(), new ClientSection(), new TicketDetailSection());
    private final ReportDocumentAssembler assembler = new ReportDocumentAssembler(sections, new ReportKpiFactory(), logo);

    @Test
    void deveMontarDocumentoCompletoComSeteTabelasEQuatorzeKpis() {
        when(logo.bytes()).thenReturn(new byte[]{1, 2, 3});
        ReportResponse report = ReportResponseFixture.full();

        ReportDocument document = assembler.assemble(report);

        assertThat(document.tables()).hasSize(7);
        assertThat(document.kpis()).hasSize(14);
        assertThat(document.header().companyName()).isEqualTo("Estacionamento Convênio Ltda");
        assertThat(document.header().ticketCount()).isEqualTo(3);
        assertThat(document.logo()).containsExactly(1, 2, 3);
    }

    @Test
    void deveMontarDocumentoParaPeriodoVazioSemQuebrar() {
        when(logo.bytes()).thenReturn(new byte[0]);
        ReportResponse report = ReportResponseFixture.empty();

        ReportDocument document = assembler.assemble(report);

        assertThat(document.tables()).hasSize(7);
        assertThat(document.header().ticketCount()).isZero();
    }
}
