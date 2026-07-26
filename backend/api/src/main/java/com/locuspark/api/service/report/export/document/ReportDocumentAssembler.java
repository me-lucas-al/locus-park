package com.locuspark.api.service.report.export.document;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.ReportCompanyResponse;
import com.locuspark.api.dto.response.report.ReportPeriodResponse;
import com.locuspark.api.service.report.export.ReportLogo;
import com.locuspark.api.service.report.export.format.ReportLocale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportDocumentAssembler {

    private final List<ReportSectionFactory> sectionFactories;
    private final ReportKpiFactory kpiFactory;
    private final ReportLogo reportLogo;

    public ReportDocument assemble(ReportResponse report) {
        List<ReportKpi> kpis = kpiFactory.buildAll(report);
        List<ReportTable<?>> tables = sectionFactories.stream().<ReportTable<?>>map(factory -> factory.build(report)).toList();
        return new ReportDocument(buildHeader(report), kpis, tables, reportLogo.bytes());
    }

    private ReportDocumentHeader buildHeader(ReportResponse report) {
        ReportCompanyResponse company = report.company();
        ReportPeriodResponse period = report.period();
        return new ReportDocumentHeader(
                company.name(),
                company.cnpj(),
                company.totalSpots() != null ? company.totalSpots() : 0,
                period.from(),
                period.to(),
                period.days(),
                LocalDateTime.now(ReportLocale.TIME_ZONE),
                report.ticketCount(),
                report.ticketsTruncated());
    }
}
