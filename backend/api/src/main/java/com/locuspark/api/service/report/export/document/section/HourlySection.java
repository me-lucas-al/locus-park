package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.HourlySummaryResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(40)
public class HourlySection implements ReportSectionFactory {

    @Override
    public ReportTable<HourlySummaryResponse> build(ReportResponse report) {
        List<ReportColumn<HourlySummaryResponse>> columns = List.of(
                ReportColumn.of("Hora", ReportCellType.TEXT, r -> String.format("%02dh", r.hour())),
                ReportColumn.of("Entradas (entrada)", ReportCellType.INTEGER, HourlySummaryResponse::entryCount),
                ReportColumn.of("Saídas (saída)", ReportCellType.INTEGER, HourlySummaryResponse::exitCount),
                ReportColumn.of("Faturamento (saída)", ReportCellType.CURRENCY, HourlySummaryResponse::revenue));
        return new ReportTable<>("Movimentação por Hora", columns, report.hourlySummaries());
    }
}
